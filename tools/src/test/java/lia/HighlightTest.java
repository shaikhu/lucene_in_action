package lia;

import java.io.StringReader;
import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HighlightTest {
  private static final List<String> TEXT_FRAGMENTS = List.of(
      "Tapestry in <B>Action</B>",
      "Ant in <B>Action</B>",
      "Lucene in <B>Action</B>, Second Edition",
      "JUnit in <B>Action</B>, Second Edition");

  @Test
  void testHighlighting() throws Exception {
    var text = "The quick brown fox jumps over the lazy dog";
    var query = new TermQuery(new Term("field", "fox"));
    var tokenStream = new SimpleAnalyzer().tokenStream("field", new StringReader(text));

    var queryScorer = new QueryScorer(query, "field");
    var fragmenter = new SimpleSpanFragmenter(queryScorer);
    var  highlighter = new Highlighter(queryScorer);
    highlighter.setTextFragmenter(fragmenter);

    assertThat(highlighter.getBestFragment(tokenStream, text)).isEqualTo("The quick brown <B>fox</B> jumps over the lazy dog");
  }

  @Test
  void testHits() throws Exception {
    try (var directory = TestUtil.getBookIndexDirectory();
         var indexReader = DirectoryReader.open(directory)) {

      var indexSearcher = new IndexSearcher(indexReader);
      var query = new TermQuery(new Term("title", "action"));
      var topDocs = indexSearcher.search(query, 10);

      var queryScorer = new QueryScorer(query, "title");
      var  highlighter = new Highlighter(queryScorer);
      highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer));

      for (var scoreDoc : topDocs.scoreDocs) {
        var document = indexSearcher.storedFields().document(scoreDoc.doc);
        var title = document.get("title");

        var tokenStream = TokenSources.getTokenStream("title",
                indexReader.termVectors().get(scoreDoc.doc), title, new SimpleAnalyzer(), highlighter.getMaxDocCharsToAnalyze() - 1);

        var textFragment = highlighter.getBestFragment(tokenStream, title);
        assertThat(TEXT_FRAGMENTS).contains(textFragment);
      }
    }
  }
}
