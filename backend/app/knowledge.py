from __future__ import annotations

import json
import re
import unicodedata
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from backend.app.schemas import Citation


@dataclass(frozen=True)
class KnowledgeCard:
    id: str
    title: str
    publisher: str
    url: str
    published: str
    accessed: str
    tags: tuple[str, ...]
    summary_en: str
    summary_zh: str
    safe_actions: tuple[str, ...]

    def text(self, locale: str) -> str:
        return self.summary_zh if locale == "zh-CN" else self.summary_en

    def citation(self) -> Citation:
        return Citation(id=self.id, title=self.title, publisher=self.publisher, url=self.url)


class KnowledgeRepository:
    """Small, deterministic lexical retriever for the curated knowledge cards.

    This deliberately stays dependency-free.  English uses normalized word
    features, while Chinese uses contiguous runs plus character bigrams so a
    query such as ``银行诈骗`` can match a card tagged ``银行`` even when the
    card does not contain exactly the same sentence.
    """

    _CJK_RUN = re.compile(r"[\u3400-\u4dbf\u4e00-\u9fff\uF900-\uFAFF]+")
    _LATIN_WORD = re.compile(r"[a-z0-9]+")
    # Keep this deliberately conservative.  These are grammatical/function
    # words that otherwise make generic questions match words in summaries;
    # scam-domain terms (for example ``not``, ``urgent`` and ``bank``) stay
    # searchable because they carry safety meaning.
    _STOPWORDS = frozenset(
        {
            "a",
            "an",
            "am",
            "and",
            "are",
            "as",
            "at",
            "be",
            "been",
            "being",
            "before",
            "by",
            "can",
            "could",
            "did",
            "do",
            "does",
            "doing",
            "during",
            "for",
            "from",
            "had",
            "has",
            "have",
            "having",
            "he",
            "her",
            "hers",
            "him",
            "his",
            "how",
            "i",
            "if",
            "in",
            "into",
            "is",
            "it",
            "its",
            "may",
            "me",
            "might",
            "my",
            "of",
            "on",
            "or",
            "our",
            "please",
            "she",
            "should",
            "that",
            "the",
            "their",
            "them",
            "then",
            "these",
            "they",
            "this",
            "those",
            "through",
            "to",
            "was",
            "we",
            "were",
            "what",
            "when",
            "where",
            "which",
            "who",
            "whom",
            "why",
            "will",
            "with",
            "would",
            "you",
            "your",
        }
    )

    def __init__(self, path: Path | None = None) -> None:
        default_path = Path(__file__).resolve().parents[2] / "knowledge" / "cards.json"
        self.path = path or default_path
        raw: list[dict[str, Any]] = json.loads(self.path.read_text(encoding="utf-8"))
        self.cards = tuple(
            KnowledgeCard(
                id=item["id"],
                title=item["title"],
                publisher=item["publisher"],
                url=item["url"],
                published=item["published"],
                accessed=item["accessed"],
                tags=tuple(item["tags"]),
                summary_en=item["summary_en"],
                summary_zh=item["summary_zh"],
                safe_actions=tuple(item["safe_actions"]),
            )
            for item in raw
        )
        self.by_id = {card.id: card for card in self.cards}

    @staticmethod
    def _normalise(value: str) -> str:
        """Normalize user text and card text before extracting features."""

        # NFKC handles full-width Latin/numbers commonly pasted from SMS.  A
        # lower-case pass makes English matching case-insensitive; CJK is
        # unchanged by it.
        return unicodedata.normalize("NFKC", value).lower()

    @classmethod
    def _tokens(cls, value: str) -> set[str]:
        """Return English words and Chinese runs/bigrams used for retrieval.

        Single Chinese characters are intentionally ignored: they create too
        many accidental matches.  Keeping the full run in addition to bigrams
        lets both a short tag (``银行``) and a longer phrase match naturally.
        """

        value = cls._normalise(value)
        tokens = {
            token
            for token in cls._LATIN_WORD.findall(value)
            if len(token) >= 2 and token not in cls._STOPWORDS
        }
        for run in cls._CJK_RUN.findall(value):
            if len(run) < 2:
                continue
            tokens.add(run)
            tokens.update(run[index : index + 2] for index in range(len(run) - 1))
        return tokens

    @classmethod
    def _field_tokens(cls, card: KnowledgeCard) -> dict[str, set[str]]:
        """Build weighted searchable fields, retaining English and Chinese."""

        return {
            "title": cls._tokens(card.title),
            "tags": cls._tokens(" ".join(card.tags)),
            # Prefer the requested language without excluding cross-language
            # cards (tags and the alternate summary remain useful evidence).
            "summary_en": cls._tokens(card.summary_en),
            "summary_zh": cls._tokens(card.summary_zh),
        }

    def search(self, query: str, locale: str, limit: int = 4) -> list[dict[str, Any]]:
        query_tokens = self._tokens(query)
        if not query_tokens or limit <= 0:
            return []

        # A title/tag hit is stronger evidence than a body-only hit.  The
        # requested-language summary gets a small preference over the other
        # language.  Scores are intentionally simple and inspectable.
        body_field = "summary_zh" if locale == "zh-CN" else "summary_en"
        field_weights = {"title": 5, "tags": 4, body_field: 2}
        field_weights["summary_zh" if body_field == "summary_en" else "summary_en"] = 1
        scored: list[tuple[int, int, str, KnowledgeCard, list[str]]] = []
        for card in self.cards:
            fields = self._field_tokens(card)
            matched_terms = sorted(
                token for token in query_tokens if any(token in values for values in fields.values())
            )
            score = sum(
                max((weight for field, weight in field_weights.items() if token in fields[field]), default=0)
                for token in matched_terms
            )
            if score:
                scored.append((score, len(matched_terms), card.id, card, matched_terms))

        # A stable ID tie-breaker makes results reproducible across processes,
        # and, importantly, no-match queries remain empty rather than adding
        # unrelated cards as citations.
        scored.sort(key=lambda pair: (-pair[0], -pair[1], pair[2]))
        return [
            {
                "id": card.id,
                "title": card.title,
                "publisher": card.publisher,
                "url": card.url,
                "summary": card.text(locale),
                "safe_actions": list(card.safe_actions),
                "match_score": score,
                "matched_terms": matched_terms,
            }
            for score, _, _, card, matched_terms in scored[:limit]
        ]

    def citations(self, ids: list[str]) -> list[Citation]:
        seen: set[str] = set()
        result: list[Citation] = []
        for card_id in ids:
            if card_id in seen or card_id not in self.by_id:
                continue
            seen.add(card_id)
            result.append(self.by_id[card_id].citation())
        return result
