# tools — Chapter 8: Essential Lucene Extensions

This module covers the high-value extensions bundled with Lucene beyond the core search API. 

Spell checking (`CreateSpellCheckerIndex`, `SpellCheckerExample`) builds a secondary n-gram index over a term dictionary and suggests correctly-spelled alternatives for mistyped queries. 

Highlighting (`HighlightIt`, `FastVectorHighlighterSample`, `HighlightTest`) highlights the matched terms in result snippets, with the fast vector highlighter offering better performance for large fields by using stored term vectors. 
