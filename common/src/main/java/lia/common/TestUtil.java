package lia.common;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
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

  public static boolean hitsIncludeTitle(IndexSearcher indexSearcher, TopDocs matches, String title) throws IOException {
    for (var scoreDoc : matches.scoreDocs) {
      var document = indexSearcher.storedFields().document(scoreDoc.doc);
      if (title.equals(document.get("title"))) {
        return true;
      }
    }
    return false;
  }

  public static List<String> getTokens(Analyzer analyzer, String input) throws Exception {
    var tokens = new ArrayList<String>();
    TokenStream tokenStream = analyzer.tokenStream("field", new StringReader(input));
    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      tokens.add(term.toString());
    }
    tokenStream.end();
    tokenStream.close();
    return tokens;
  }
}
