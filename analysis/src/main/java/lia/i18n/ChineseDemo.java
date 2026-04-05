package lia.i18n;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import lia.SimpleAnalyzer;

/**
 * Demonstrates how different analyzers tokenize CJK (Chinese/Japanese/Korean) text.
 *
 * SimpleAnalyzer and StandardAnalyzer treat the whole string as one token.
 * CJKAnalyzer produces overlapping bigrams — e.g. "道德經" → [道德] [德經] — which
 * better supports partial-match search across CJK scripts.
 */
public class ChineseDemo {
  // CJK text for 'Tao Te Ching'
  private static final String TEXT = "道德經";

  private static final List<Analyzer> ANALYZERS = List.of(
      new SimpleAnalyzer(),
      new StandardAnalyzer(),
      new CJKAnalyzer());

  private static void analyze(String text, Analyzer analyzer) throws IOException {
    try (var tokenStream = analyzer.tokenStream("contents", new StringReader(text))) {
      var charTerm = tokenStream.addAttribute(CharTermAttribute.class);
      var sb = new StringBuilder();
      tokenStream.reset();
      while (tokenStream.incrementToken()) {
        sb.append("[").append(charTerm).append("] ");
      }
      tokenStream.end();
      System.out.printf("%-30s : %s%n", analyzer.getClass().getSimpleName(), sb);
    }
  }

  public static void main(String... args) throws IOException {
    System.out.println("Text: " + TEXT);
    System.out.println();
    for (var analyzer : ANALYZERS) {
      analyze(TEXT, analyzer);
    }
  }
}
