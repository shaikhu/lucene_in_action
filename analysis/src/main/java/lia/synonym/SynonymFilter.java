package lia.synonym;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public final class SynonymFilter extends TokenFilter {
  private final Stack<String> synonymStack;

  private final SynonymEngine engine;

  private final CharTermAttribute termAtt;

  private final PositionIncrementAttribute posIncrAtt;

  private AttributeSource.State current;

  public SynonymFilter(TokenStream input, SynonymEngine engine) {
    super(input);
    synonymStack = new Stack<>();
    this.engine = engine;
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (!synonymStack.isEmpty()) {
      String syn = synonymStack.pop();
      restoreState(current);
      termAtt.setEmpty();
      termAtt.append(syn);
      posIncrAtt.setPositionIncrement(0);
      return true;
    }

    if (!input.incrementToken())
      return false;

    if (addAliasesToStack()) {
      current = captureState();
    }
    return true;
  }

  private boolean addAliasesToStack() throws IOException {
    List<String> synonyms = engine.getSynonyms(termAtt.toString());
    if (synonyms == null) {
      return false;
    }
    for (String synonym : synonyms) {
      synonymStack.push(synonym);
    }
    return true;
  }
}
