package lia.payloads;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.similarities.ClassicSimilarity;

public class BoostingSimilarity extends ClassicSimilarity {
  // how to make sure this is called?
  public float scorePayload(int docID, String fieldName, int start, int end, byte[] payload, int offset, int length) {
    if (payload != null) {
      return PayloadHelper.decodeFloat(payload, offset);
    } else {
      return 1.0F;
    }
  }
}
