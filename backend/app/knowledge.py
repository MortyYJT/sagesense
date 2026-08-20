from __future__ import annotations

import json
import re
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
    def _tokens(value: str) -> set[str]:
        latin = re.findall(r"[a-z0-9]{2,}", value.lower())
        chinese = re.findall(r"[\u4e00-\u9fff]{2,}", value)
        return set(latin + chinese)

    def search(self, query: str, locale: str, limit: int = 4) -> list[dict[str, Any]]:
        query_tokens = self._tokens(query)
        scored: list[tuple[int, KnowledgeCard]] = []
        for card in self.cards:
            haystack = " ".join((card.title, *card.tags, card.summary_en, card.summary_zh))
            score = len(query_tokens & self._tokens(haystack))
            if score:
                scored.append((score, card))
        if not scored:
            scored = [(0, card) for card in self.cards[:2]]
        scored.sort(key=lambda pair: (-pair[0], pair[1].id))
        return [
            {
                "id": card.id,
                "title": card.title,
                "publisher": card.publisher,
                "url": card.url,
                "summary": card.text(locale),
                "safe_actions": list(card.safe_actions),
            }
            for _, card in scored[:limit]
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
