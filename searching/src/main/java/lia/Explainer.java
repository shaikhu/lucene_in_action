package lia;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Explainer
{
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: Explainer <index dir> <query>");
      System.exit(1);
    }

    String indexDir = args[0];
    String queryExpression = args[1];

    Directory directory = FSDirectory.open(Paths.get(indexDir));
    QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
    Query query = parser.parse(queryExpression);

    System.out.println("Query: " + queryExpression);
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    TopDocs topDocs = searcher.search(query, 10);
    for (ScoreDoc match : topDocs.scoreDocs) {
      Explanation explanation = searcher.explain(query, match.doc);
      System.out.println("----------");
      Document doc = searcher.storedFields().document(match.doc);
      System.out.println(doc.get("title"));
      System.out.println(explanation.toString());
    }
    directory.close();
  }
}
