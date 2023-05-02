package lia.codec;

import java.io.IOException;

import org.apache.commons.codec.language.Metaphone;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public final class MetaphoneReplacementFilter extends TokenFilter {
  private static final String METAPHONE = "metaphone";

  private final Metaphone metaphoner = new Metaphone();

  private final CharTermAttribute termAttr;

  private final TypeAttribute typeAttr;

  public MetaphoneReplacementFilter(TokenStream input) {
    super(input);
    termAttr = addAttribute(CharTermAttribute.class);
    typeAttr = addAttribute(TypeAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      return false;
    }

    String encoded = metaphoner.encode(termAttr.toString());
    termAttr.setEmpty().append(encoded);
    typeAttr.setType(METAPHONE);
    return true;
  }
}
