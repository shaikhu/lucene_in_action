package lia;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher
{
  public static void main(String[] args) throws IllegalArgumentException, IOException, ParseException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + Searcher.class.getName() + " <index dir> <query>");
    }

    String indexDir = args[0];
    String q = args[1];

    search(indexDir, q);
  }

  private static void search(String indexDir, String q) throws IOException, ParseException
  {
    try (Directory dir = FSDirectory.open(Paths.get(indexDir))) {
      IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));

      QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
      Query query = parser.parse(q);
      long start = System.currentTimeMillis();
      TopDocs hits = is.search(query, 10);
      long end = System.currentTimeMillis();

      System.out.println("Found " + hits.totalHits + " document(s) (in " + (end - start) + " milliseconds) that matched query '" + q + "':");

      for(ScoreDoc scoreDoc : hits.scoreDocs) {
        Document doc= is.storedFields().document(scoreDoc.doc);
        System.out.println(doc.get("fullpath"));
      }
    }
  }
}
