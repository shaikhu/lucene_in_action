package lia.synonym;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface SynonymEngine {
  List<String> getSynonyms(String text) throws IOException;
}
