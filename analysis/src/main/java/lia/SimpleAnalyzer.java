package lia;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class SimpleAnalyzer extends Analyzer {
  @Override
  protected TokenStreamComponents createComponents(final String fieldName) {
    Tokenizer tokenizer = new StandardTokenizer();
    TokenStream result = new LowerCaseFilter(tokenizer);
    return new TokenStreamComponents(tokenizer, result);
  }

  public static void main(String... args) throws IOException {
    AnalyzerUtils.displayTokensWithFullDetails(new SimpleAnalyzer(), "The quick brown fox...");
  }
}
