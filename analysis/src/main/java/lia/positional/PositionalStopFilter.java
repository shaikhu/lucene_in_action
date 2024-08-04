package lia.positional;

import java.io.IOException;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public final class PositionalStopFilter extends TokenFilter {
  private final CharArraySet stopWords;

  private final PositionIncrementAttribute positionIncrement;

  private final CharTermAttribute charTerm;

  public PositionalStopFilter(TokenStream input, CharArraySet stopWords) {
    super(input);
    this.stopWords = stopWords;
    this.positionIncrement = addAttribute(PositionIncrementAttribute.class);
    this.charTerm = addAttribute(CharTermAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    var increment = 0;
    while(input.incrementToken()) {
      if (!stopWords.contains(charTerm.toString())) {
        positionIncrement.setPositionIncrement(positionIncrement.getPositionIncrement() + increment);
        return true;
      }

      increment += positionIncrement.getPositionIncrement();
    }

    return false;
  }
}
