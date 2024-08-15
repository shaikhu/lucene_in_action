package lia;

import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;

public class SpellCheckerExample {
  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + SpellCheckerExample.class.getName() + " + <spell checker index dir> <word to respell>");
    }

    var spellCheckDir = args[0];
    var wordToRespell = args[1];

    try (var directory = FSDirectory.open(Paths.get(spellCheckDir))) {
      if (!DirectoryReader.indexExists(directory)) {
        throw new RuntimeException("ERROR: No spellchecker index at path \"" + spellCheckDir + "\"; please run gradle task createSpellCheckerIndex first");
      }

      var spellChecker = new SpellChecker(directory);
      spellChecker.setStringDistance(new LevenshteinDistance());

      var suggestionsArray = spellChecker.suggestSimilar(wordToRespell, 5);
      System.out.println(suggestionsArray.length + " suggestions for '" + wordToRespell + "':");
      Arrays.stream(suggestionsArray).forEach(System.out::println);
    }
  }
}
