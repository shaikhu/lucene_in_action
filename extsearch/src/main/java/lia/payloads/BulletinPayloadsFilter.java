package lia.payloads;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

public final class BulletinPayloadsFilter extends TokenFilter {
  private final CharTermAttribute termAtt;

  private final PayloadAttribute payloadAttr;

  private final BytesRef boostPayload;

  private boolean isBulletin;

  public BulletinPayloadsFilter(TokenStream input, float warningBoost) {
    super(input);
    payloadAttr = addAttribute(PayloadAttribute.class);
    termAtt = addAttribute(CharTermAttribute.class);
    boostPayload = new BytesRef(PayloadHelper.encodeFloat(warningBoost));
  }

  public void setIsBulletin(boolean isBulletin) {
    this.isBulletin = isBulletin;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (isBulletin && termAtt.toString().equals("warning")) {
        payloadAttr.setPayload(boostPayload);
      } else {
        payloadAttr.setPayload(null);
      }
      return true;
    } else {
      return false;
    }
  }
}
