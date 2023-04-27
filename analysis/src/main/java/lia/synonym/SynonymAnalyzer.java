package lia.synonym;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class SynonymAnalyzer extends Analyzer
{
  private final SynonymEngine engine;

  public SynonymAnalyzer(SynonymEngine engine) {
    this.engine = engine;
  }

  @Override
  protected TokenStreamComponents createComponents(final String fieldName) {
    Tokenizer source = new StandardTokenizer();
    TokenStream result = new SynonymFilter(source, engine);
    result = new StopFilter(result, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
    return new TokenStreamComponents(source, result);
  }
}
