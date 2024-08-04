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

  private final CharTermAttribute charTerm;

  private final TypeAttribute type;

  public MetaphoneReplacementFilter(TokenStream input) {
    super(input);
    charTerm = addAttribute(CharTermAttribute.class);
    type = addAttribute(TypeAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) {
      return false;
    }

    String encodedCharTerm = metaphoner.encode(charTerm.toString());
    charTerm.setEmpty().append(encodedCharTerm);
    type.setType(METAPHONE);
    return true;
  }
}
