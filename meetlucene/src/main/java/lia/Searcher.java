package lia;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
  private static void search(String indexDir, String searchTerm) throws Exception {
    try (Directory directory = FSDirectory.open(Paths.get(indexDir))) {
      IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));

      QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
      Query query = parser.parse(searchTerm);
      long start = System.currentTimeMillis();
      TopDocs hits = searcher.search(query, 10);
      long end = System.currentTimeMillis();

      System.out.println("Found " + hits.totalHits.value + " document(s) (in " + (end - start) + " milliseconds) that matched query '" + searchTerm + "':");
      for (ScoreDoc scoreDoc : hits.scoreDocs) {
        Document document = searcher.storedFields().document(scoreDoc.doc);
        System.out.println(document.get("fullpath"));
      }
    }
  }

  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + Searcher.class.getName() + " <index dir> <query>");
    }

    String indexDir = args[0];
    String query = args[1];

    search(indexDir, query);
  }
}
