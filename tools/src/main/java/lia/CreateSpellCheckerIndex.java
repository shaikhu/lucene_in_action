package lia;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateSpellCheckerIndex {
  public static void main(String... args) throws Exception {
    if (args.length != 3) {
      System.out.println("Usage: java lia.SpellCheckerTest SpellCheckerIndexDir IndexDir IndexField");
      System.exit(1);
    }

    String spellCheckDir = args[0];
    String indexDir = args[1];
    String indexField = args[2];

    System.out.println("Now build SpellChecker index...");
    try (Directory directory1 = FSDirectory.open(Paths.get(spellCheckDir));
         Directory directory2 = FSDirectory.open(Paths.get(indexDir))) {

      SpellChecker spell = new SpellChecker(directory1);

      long startTime = System.currentTimeMillis();
      try (DirectoryReader reader = DirectoryReader.open(directory2)) {
        spell.indexDictionary(new LuceneDictionary(reader, indexField), new IndexWriterConfig(new StandardAnalyzer()), true);
      }
      long endTime = System.currentTimeMillis();
      System.out.println("  took " + (endTime-startTime) + " milliseconds");
    }
  }
}
