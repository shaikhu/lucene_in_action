package lia;

import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SpellCheckerExample {
  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: java lia.tools.SpellCheckerTest SpellCheckerIndexDir wordToRespell");
      System.exit(1);
    }

    String spellCheckDir = args[0];
    String wordToRespell = args[1];

    try (Directory directory = FSDirectory.open(Paths.get(spellCheckDir))) {
      if (!DirectoryReader.indexExists(directory)) {
        System.out.println("ERROR: No spellchecker index at path \"" + spellCheckDir + "\"; please run CreateSpellCheckerIndex first");
        System.exit(1);
      }

      SpellChecker spell = new SpellChecker(directory);
      spell.setStringDistance(new LevenshteinDistance());
      String[] suggestions = spell.suggestSimilar(wordToRespell, 5);
      System.out.println(suggestions.length + " suggestions for '" + wordToRespell + "':");
      for (String suggestion : suggestions) {
        System.out.println(" " + suggestion);
      }
    }
  }
}
