package lia;

import java.io.StringReader;
import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HighlightTest {
  private static final List<String> FRAGMENTS = List.of(
      "Tapestry in <B>Action</B>",
      "Ant in <B>Action</B>",
      "Lucene in <B>Action</B>, Second Edition",
      "JUnit in <B>Action</B>, Second Edition");

  @Test
  void testHighlighting() throws Exception {
    String text = "The quick brown fox jumps over the lazy dog";

    TermQuery query = new TermQuery(new Term("field", "fox"));
    TokenStream tokenStream = new SimpleAnalyzer().tokenStream("field", new StringReader(text));

    QueryScorer scorer = new QueryScorer(query, "field");
    Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
    Highlighter highlighter = new Highlighter(scorer);
    highlighter.setTextFragmenter(fragmenter);

    assertThat(highlighter.getBestFragment(tokenStream, text)).isEqualTo(
        "The quick brown <B>fox</B> jumps over the lazy dog");
  }

  @Test
  void testHits() throws Exception {
    try (Directory directory = TestUtil.getBookIndexDirectory();
         IndexReader indexReader = DirectoryReader.open(directory)) {

      IndexSearcher searcher = new IndexSearcher(indexReader);
      TermQuery query = new TermQuery(new Term("title", "action"));
      TopDocs hits = searcher.search(query, 10);

      QueryScorer scorer = new QueryScorer(query, "title");
      Highlighter highlighter = new Highlighter(scorer);
      highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));

      Analyzer analyzer = new SimpleAnalyzer();
      for (ScoreDoc sd : hits.scoreDocs) {
        Document doc = searcher.storedFields().document(sd.doc);
        String title = doc.get("title");

        TokenStream stream =
            TokenSources.getTokenStream("title", indexReader.termVectors().get(sd.doc), title, analyzer,
                highlighter.getMaxDocCharsToAnalyze() - 1);

        String fragment = highlighter.getBestFragment(stream, title);
        assertThat(FRAGMENTS).contains(fragment);
      }
    }
  }
}
