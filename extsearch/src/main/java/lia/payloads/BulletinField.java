package lia.payloads;

import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * A custom Field that creates a pre-analyzed TokenStream with bulletin payload handling.
 * Builds the TokenStream chain directly with the bulletin state known at indexing time.
 */
public class BulletinField extends Field {
  
  private static final FieldType FIELD_TYPE = new FieldType();
  
  static {
    FIELD_TYPE.setTokenized(true);
    FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    FIELD_TYPE.setStoreTermVectors(false);
    FIELD_TYPE.freeze();
  }

  /**
   * Creates a bulletin field with payload-enhanced TokenStream.
   * 
   * @param name field name
   * @param value text content to index
   * @param isBulletin whether this document is a bulletin (affects payload scoring)
   * @param warningBoost boost value to apply to "warning" terms in bulletins
   */
  public BulletinField(String name, String value, boolean isBulletin, float warningBoost) {
    super(name, createTokenStream(value, isBulletin, warningBoost), FIELD_TYPE);
  }
  
  private static TokenStream createTokenStream(String value, boolean isBulletin, float warningBoost) {
    try {
      // Create the TokenStream chain: StandardTokenizer -> BulletinPayloadsFilter
      StandardTokenizer tokenizer = new StandardTokenizer();
      tokenizer.setReader(new StringReader(value));
      
      BulletinPayloadsFilter payloadFilter = new BulletinPayloadsFilter(tokenizer, warningBoost);
      payloadFilter.setIsBulletin(isBulletin);
      
      return payloadFilter;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create TokenStream for BulletinField", e);
    }
  }
}
