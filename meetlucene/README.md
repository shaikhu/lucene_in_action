# meetlucene — Chapters 1–3: Building a Search Index and Adding Search to Your Application

This module is the  entry point to the project, covering the core indexing and searching lifecycle. 

`Indexer` walks a directory of `.txt` files, wraps each in a `Document` with stored fields, and commits them to an index using `IndexWriter`. 

`Searcher` then opens that index with `DirectoryReader` and `IndexSearcher`, parses a query string via `QueryParser`, and prints the matching filenames.
