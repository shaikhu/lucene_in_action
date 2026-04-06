# searching — Chapter 3: Adding Search to Your Application

This module covers the full range of built-in query types and the mechanics of relevance scoring. The tests walk through `TermQuery`, `BooleanQuery`, `PhraseQuery`, `PrefixQuery`, `TermRangeQuery`, numeric range queries (`LongPoint`, `DoublePoint`), and `QueryParser`. 

`NearRealTimeTest` demonstrates opening a reader directly from an `IndexWriter` to make newly indexed documents searchable without a full commit. 

`Explainer` shows how to use `IndexSearcher.explain()` to inspect the BM25 score breakdown for a given document.
