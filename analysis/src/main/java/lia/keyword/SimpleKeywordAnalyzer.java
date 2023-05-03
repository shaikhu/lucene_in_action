package lia.keyword;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharTokenizer;

public class SimpleKeywordAnalyzer extends Analyzer {
  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    return new TokenStreamComponents(new CharTokenizer() {
      @Override
      protected boolean isTokenChar(final int c) {
        return true;
      }
    });
  }
}
