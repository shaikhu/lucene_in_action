package lia.synonym;

import java.util.List;
import java.util.Map;

public class TestSynonymEngine implements SynonymEngine  {
  private static final Map<String, List<String>> SYNONYMS = Map.of(
          "quick", List.of("fast", "speedy"),
          "jumps", List.of("leaps", "hops"),
          "over", List.of("above"),
          "lazy", List.of("apathetic", "sluggish"),
          "dog", List.of("canine", "pooch"));

  @Override
  public List<String> getSynonyms(String s)  {
    return SYNONYMS.get(s);
  }
}
