package lia.synonym;

import java.io.IOException;
import java.util.List;

public interface SynonymEngine {
  List<String> getSynonyms(String s) throws IOException;
}
