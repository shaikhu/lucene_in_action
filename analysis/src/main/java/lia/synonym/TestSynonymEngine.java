package lia.synonym;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSynonymEngine implements SynonymEngine  {
  private static final Map<String, List<String>> SYNONYMS = new HashMap<>();

  static {
    SYNONYMS.put("quick", List.of("fast", "speedy"));
    SYNONYMS.put("jumps", List.of("leaps", "hops"));
    SYNONYMS.put("over", List.of("above"));
    SYNONYMS.put("lazy", List.of("apathetic", "sluggish"));
    SYNONYMS.put("dog", List.of("canine", "pooch"));
  }

  @Override
  public List<String> getSynonyms(String s)  {
    return SYNONYMS.get(s);
  }
}
