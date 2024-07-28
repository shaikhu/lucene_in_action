package lia;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lia.common.AnalyzerUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerDemo {
  private static final List<String> EXAMPLES = List.of(
      "The quick brown fox jumped over the lazy dog",
      "XY&Z Corporation - xyz@example.com");

  public static final List<Analyzer> ANALYZERS = List.of(
      new WhitespaceAnalyzer(),
      new SimpleAnalyzer(),
      new StopAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET),
      new StandardAnalyzer()
  );

  private static void analyze(String text) throws IOException {
    System.out.println("Analyzing \"" + text + "\"");
    for (Analyzer analyzer : ANALYZERS) {
      String name = analyzer.getClass().getSimpleName();
      System.out.println(" " + name + ":");
      System.out.print("    ");
      AnalyzerUtils.displayTokens(analyzer, text);
      System.out.println("\n");
    }

    System.out.println("SimpleAnalyzer - displayTokensWithFullDetails");
    AnalyzerUtils.displayTokensWithFullDetails(new SimpleAnalyzer(), "The quick brown fox....");

    System.out.println("\n----");
    System.out.println("StandardAnalyzer - displayTokensWithFullDetails");
    AnalyzerUtils.displayTokensWithFullDetails(new StandardAnalyzer(), "I'll email you at xyz@example.com");
  }

  public static void main(String... args) throws IOException {
    for (String text : EXAMPLES) {
      analyze(text);
    }
  }
}