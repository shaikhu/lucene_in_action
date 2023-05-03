package lia.positional;

import java.io.IOException;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public final class PositionalStopFilter extends TokenFilter {
  private final CharArraySet stopWords;

  private final PositionIncrementAttribute posIncrAttr;

  private final CharTermAttribute termAttr;

  public PositionalStopFilter(TokenStream input, CharArraySet stopWords) {
    super(input);
    this.stopWords = stopWords;
    this.posIncrAttr = addAttribute(PositionIncrementAttribute.class);
    this.termAttr = addAttribute(CharTermAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    int increment = 0;
    while(input.incrementToken()) {
      if (!stopWords.contains(termAttr.toString())) {
        posIncrAttr.setPositionIncrement(posIncrAttr.getPositionIncrement() + increment);
        return true;
      }

      increment += posIncrAttr.getPositionIncrement();
    }

    return false;
  }
}
