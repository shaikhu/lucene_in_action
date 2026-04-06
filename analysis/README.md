# analysis — Chapter 4: Lucene's Analysis Process

This module covers how raw text is transformed into tokens before indexing and at query time. 

It walks through building custom analyzers from scratch, assembling `Tokenizer` and `TokenFilter` chains, and provides examples such as stop-word filtering, Porter stemming, synonym injection, and phonetic encoding via Metaphone. 

**Note**

The i18n section demonstrating `CJKAnalyzer`'s bigram tokenisation strategy for Chinese text, requires a compatible font to be installed on the Operating System, and configured in the IDE.

- `sudo pacman -S noto-fonts-cjk`
- Set the 'Fallback font' in IntelliJ settings to the `noto-fonts-cjk`
