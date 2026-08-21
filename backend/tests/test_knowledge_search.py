from backend.app.knowledge import KnowledgeRepository


def test_chinese_bigrams_match_and_explain_the_evidence() -> None:
    results = KnowledgeRepository().search("请核实银行诈骗链接", "zh-CN")

    assert results
    assert all(result["match_score"] > 0 for result in results)
    assert any("银行" in result["matched_terms"] for result in results)
    assert any("链接" in result["matched_terms"] for result in results)


def test_title_hit_outranks_tag_only_hit() -> None:
    results = KnowledgeRepository().search("impersonation", "en-AU")

    assert [result["id"] for result in results[:2]] == [
        "ftc-older-adults-imposters",
        "scamwatch-methods",
    ]
    assert results[0]["match_score"] > results[1]["match_score"]


def test_unrelated_query_returns_no_cards() -> None:
    repository = KnowledgeRepository()

    assert repository.search("moon gardening recipe", "en-AU") == []
    assert repository.search("What is the weather today?", "en-AU") == []
    assert repository.search("Please write an essay about history", "en-AU") == []
    assert repository.search("How do I bake a cake?", "en-AU") == []


def test_order_is_stable_and_limit_is_applied() -> None:
    repository = KnowledgeRepository()
    query = "bank phishing link"

    first = repository.search(query, "en-AU")
    second = repository.search(query, "en-AU")

    assert [item["id"] for item in first] == [item["id"] for item in second]
    assert repository.search(query, "en-AU", limit=2) == first[:2]
    assert repository.search(query, "en-AU", limit=0) == []
