package lia;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;

public class CreateSpellCheckerIndex {
  public static void main(String... args) throws Exception {
    if (args.length != 3) {
      throw new IllegalArgumentException("Usage: java " + CreateSpellCheckerIndex.class.getName() +  " <spell check dir> <index dir> <index field>");
    }

    var spellCheckDir = args[0];
    var indexDir = args[1];
    var indexField = args[2];

    System.out.println("Now build SpellChecker index...");
    try (var directory1 = FSDirectory.open(Paths.get(spellCheckDir));
         var directory2 = FSDirectory.open(Paths.get(indexDir))) {

      var spellChecker = new SpellChecker(directory1);

      var startTime = System.currentTimeMillis();
      try (var directoryReader = DirectoryReader.open(directory2)) {
        spellChecker.indexDictionary(new LuceneDictionary(directoryReader, indexField), new IndexWriterConfig(new StandardAnalyzer()), true);
      }
      var endTime = System.currentTimeMillis();
      System.out.println("  took " + (endTime-startTime) + " milliseconds");
    }
  }
}
