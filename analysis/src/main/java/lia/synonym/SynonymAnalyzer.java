package lia.synonym;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

public class SynonymAnalyzer extends Analyzer {
  private final SynonymEngine engine;

  public SynonymAnalyzer(SynonymEngine engine) {
    this.engine = engine;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer src = new LetterTokenizer();
    TokenStream result = new LowerCaseFilter(src);
    result = new SynonymFilter(result, engine);
    result = new StopFilter(result, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
    return new TokenStreamComponents(src, result);
  }
}
