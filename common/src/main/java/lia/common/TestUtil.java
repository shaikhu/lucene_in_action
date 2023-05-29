package lia.common;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TestUtil
{
  private TestUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  public static long hitCount(IndexSearcher searcher, Query query) throws IOException {
    return searcher.search(query, 1).totalHits.value;
  }

  public static Directory getBookIndexDirectory() throws IOException {
    return FSDirectory.open(Paths.get("../testIndex"));
  }

  public static boolean hitsIncludeTitle(IndexSearcher searcher,  TopDocs matches, String title) throws IOException {
    for (ScoreDoc docs : matches.scoreDocs) {
      Document doc = searcher.storedFields().document(docs.doc);
      if (title.equals(doc.get("title"))) {
        return true;
      }
    }
    return false;
  }

  public static List<String> getTokens(Analyzer analyzer, String input) throws Exception {
    List<String> tokens = new ArrayList<>();
    TokenStream stream = analyzer.tokenStream("field", new StringReader(input));
    CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
    stream.reset();
    while (stream.incrementToken()) {
      tokens.add(termAttr.toString());
    }
    stream.end();
    stream.close();
    return tokens;
  }
}
