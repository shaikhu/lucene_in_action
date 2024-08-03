package lia;

import java.io.IOException;
import java.util.List;

import lia.common.AnalyzerUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerDemo {
  public static final List<Analyzer> ANALYZERS = List.of(
      new WhitespaceAnalyzer(),
      new SimpleAnalyzer(),
      new StopAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET),
      new StandardAnalyzer()
  );

  private static final List<String> ANALYZER_TEXT_EXAMPLES = List.of(
      "The quick brown fox jumped over the lazy dog",
      "XY&Z Corporation - xyz@example.com");


  private static void analyze() throws IOException {
    for (var text : ANALYZER_TEXT_EXAMPLES) {
      System.out.println("Analyzing \"" + text + "\"");
      for (var analyzer : ANALYZERS) {
        System.out.println(" " + analyzer.getClass().getSimpleName() + ":");
        System.out.print("    ");
        AnalyzerUtils.displayTokens(analyzer, text);
        System.out.println("\n");
      }
    }
  }

  private static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException{
    System.out.println(analyzer.getClass().getSimpleName() + " - displayTokensWithFullDetails");
    AnalyzerUtils.displayTokensWithFullDetails(analyzer, text);
    System.out.println("\n----");
  }

  public static void main(String... args) throws IOException {
    analyze();
    displayTokensWithFullDetails(new SimpleAnalyzer(), "The quick brown fox....");
    displayTokensWithFullDetails(new StandardAnalyzer(), "I'll email you at xyz@example.com");
  }
}