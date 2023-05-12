package lia.payloads;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class BulletinPayloadsAnalyzer extends Analyzer {
  private boolean bulletin;

  private float boost;

  BulletinPayloadsAnalyzer(float boost) {
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
