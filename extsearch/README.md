# extsearch — Chapter 6: Extending Search

This module covers extension points Lucene exposes for customising search behaviour.

Payloads (`BulletinField`, `BulletinPayloadsFilter`) embed per-token metadata (boost weights) enabling scoring decisions that go beyond term frequency. 

`BookLinkCollector` demonstrates how to bypass `TopDocs` entirely and accumulate arbitrary result structures during a search traversal.
