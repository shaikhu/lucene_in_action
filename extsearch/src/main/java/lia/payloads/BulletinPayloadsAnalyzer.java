package lia.payloads;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class BulletinPayloadsAnalyzer extends Analyzer {
  /** No reuse is not recommended due to performance degradation
   *  however is needed to ensure BulletinPayloadsAnalyzer#createComponents
   *  sets bulletin correctly
   */
  private final static ReuseStrategy NO_REUSE = new ReuseStrategy() {
    @Override
    public TokenStreamComponents getReusableComponents(Analyzer analyzer, String fieldName) {
      return null;
    }

    @Override
    public void setReusableComponents(Analyzer analyzer, String fieldName, TokenStreamComponents components) {}
  };

  private final float boost;

  private boolean bulletin;


  BulletinPayloadsAnalyzer(float boost) {
    super(NO_REUSE);
    this.boost = boost;
  }

  public void setBulletin(boolean bulletin) {
    this.bulletin = bulletin;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer src = new StandardTokenizer();
    BulletinPayloadsFilter result = new BulletinPayloadsFilter(src, boost);
    result.setIsBulletin(bulletin);
    return new TokenStreamComponents(src, result);
  }
}
