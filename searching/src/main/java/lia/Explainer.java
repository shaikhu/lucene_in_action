package lia;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Explainer {
  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: Explainer <index dir> <query>");
    }

    var indexDirectory = args[0];
    var searchText = args[1];

    try (Directory directory = FSDirectory.open(Paths.get(indexDirectory))) {
      var queryParser = new QueryParser("contents", new StandardAnalyzer());
      var query = queryParser.parse(searchText);

      System.out.println("Query: " + searchText);
      var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
      var topDocs = indexSearcher.search(query, 10);
      for (var scoreDoc : topDocs.scoreDocs) {
        var explanation = indexSearcher.explain(query, scoreDoc.doc);
        System.out.println("----------");
        var document = indexSearcher.storedFields().document(scoreDoc.doc);
        System.out.println(document.get("title"));
        System.out.println(explanation.toString());
      }
    }
  }
}
