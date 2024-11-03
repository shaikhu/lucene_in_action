package lia;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
  private static void search(String indexDir, String searchTerm) throws Exception {
    try (var directory = FSDirectory.open(Paths.get(indexDir))) {
      var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
      var queryParser = new QueryParser("contents", new StandardAnalyzer());
      var query = queryParser.parse(searchTerm);

      var startTime = System.currentTimeMillis();
      var topDocs = indexSearcher.search(query, 10);
      var endTime = System.currentTimeMillis();

      System.out.printf("Found %d document(s) (in %d milliseconds) that matched query '%s':%n", topDocs.totalHits.value(), endTime - startTime, searchTerm);
      for (var scoreDoc : topDocs.scoreDocs) {
        var document = indexSearcher.storedFields().document(scoreDoc.doc);
        System.out.println(document.get("fullpath"));
      }
    }
  }

  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + Searcher.class.getName() + " <index dir> <query>");
    }
    search(args[0], args[1]);
  }
}
