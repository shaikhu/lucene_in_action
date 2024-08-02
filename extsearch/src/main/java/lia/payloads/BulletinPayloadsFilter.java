package lia.payloads;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

public final class BulletinPayloadsFilter extends TokenFilter {
  private final CharTermAttribute term;

  private final PayloadAttribute payload;

  private final BytesRef boostPayload;

  private boolean isBulletin;

  public BulletinPayloadsFilter(TokenStream input, float warningBoost) {
    super(input);
    payload = addAttribute(PayloadAttribute.class);
    term = addAttribute(CharTermAttribute.class);
    boostPayload = new BytesRef(PayloadHelper.encodeFloat(warningBoost));
  }

  public void setIsBulletin(boolean isBulletin) {
    this.isBulletin = isBulletin;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (isBulletin && term.toString().equals("warning")) {
        payload.setPayload(boostPayload);
      } else {
        payload.setPayload(null);
      }
      return true;
    } else {
      return false;
    }
  }
}
