package lia.stopanalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

public class StopAnalyzerFlawed extends Analyzer {
  private final CharArraySet stopWords;

  public StopAnalyzerFlawed() {
    stopWords = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer src = new LetterTokenizer();
    TokenStream result = new StopFilter(src, stopWords);
    result = new LowerCaseFilter(result);
    return new TokenStreamComponents(src, result);
  }
}
