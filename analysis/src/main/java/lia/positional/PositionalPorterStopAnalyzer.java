package lia.positional;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;

public class PositionalPorterStopAnalyzer extends Analyzer {
  private final CharArraySet stopWords;

  public PositionalPorterStopAnalyzer() {
    this(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
  }

  public PositionalPorterStopAnalyzer(CharArraySet stopWords) {
    this.stopWords = stopWords;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer src = new LetterTokenizer();
    TokenStream result = new LowerCaseFilter(src);
    result = new StopFilter(result, stopWords);
    result = new PorterStemFilter(result);
    return new TokenStreamComponents(src, result);
  }
}
