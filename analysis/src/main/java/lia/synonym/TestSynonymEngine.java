package lia.synonym;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSynonymEngine implements SynonymEngine
{
  private static final Map<String, List<String>> SYNONYMS = new HashMap<>();

  static {
    SYNONYMS.put("quick", Arrays.asList("fast", "speedy"));
    SYNONYMS.put("jumps", Arrays.asList("leaps", "hops"));
    SYNONYMS.put("over", Arrays.asList("above"));
    SYNONYMS.put("lazy", Arrays.asList("apathetic", "sluggish"));
    SYNONYMS.put("dog", Arrays.asList("canine", "pooch"));
  }

  @Override
  public List<String> getSynonyms(final String s)  {
    return SYNONYMS.get(s);
  }
}
