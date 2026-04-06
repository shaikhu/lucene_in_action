# common — Shared Test Infrastructure

This module provides the shared utilities that all other modules depend on. 

`CreateTestIndex` builds the book index from the `data/` directory at the start of every test run — all test tasks declare a dependency on `:common:createTestIndex` to ensure the index exists before any test executes. 

`TestUtil` provides helper methods used across tests e.g. opening the shared index directory, counting hits, checking for a title in results, fetching a `List<Document>` from `TopDocs`.