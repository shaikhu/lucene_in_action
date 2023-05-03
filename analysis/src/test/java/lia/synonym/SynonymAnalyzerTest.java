package lia.synonym;

import java.io.StringReader;

import lia.common.TestUtil;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SynonymAnalyzerTest {
  private Directory directory;

  private SynonymAnalyzer analyzer;

  private IndexSearcher searcher;


  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();

    analyzer = new SynonymAnalyzer(new TestSynonymEngine());
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));

    Document doc = new Document();
    doc.add(new TextField("content", "The quick brown fox jumps over the lazy dog", Store.YES));
    writer.addDocument(doc);

    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testJumps() throws Exception {
    TokenStream stream = analyzer.tokenStream("contents", new StringReader("jumps"));
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);

    int i = 0;
    String[] expected = new String[]{"jumps", "hops", "leaps"};
    stream.reset();
    while(stream.incrementToken()) {
      assertThat(term.toString()).isEqualTo(expected[i]);
      int expectedPos;
      if (i == 0) {
        expectedPos = 1;
      } else {
        expectedPos = 0;
      }
      assertThat(posIncr.getPositionIncrement()).isEqualTo(expectedPos);
      i++;
    }
    stream.close();
    assertThat(i).isEqualTo(3);
  }

  @Test
  void testSearchByAPI() throws Exception {
    TermQuery tq = new TermQuery(new Term("content", "hops"));
    assertThat(TestUtil.hitCount(searcher, tq)).isOne();

    PhraseQuery pq = new PhraseQuery.Builder()
        .add(new Term("content", "fox"))
        .add(new Term("content", "hops"))
        .build();
    assertThat(TestUtil.hitCount(searcher, pq)).isOne();
  }

  @Test
  void testWithQueryParser() throws Exception {
    Query query = new QueryParser("content", analyzer).parse("\"fox jumps\"");
    assertThat(TestUtil.hitCount(searcher, query)).isOne();

    query = new QueryParser("content", new StandardAnalyzer()).parse("\"fox jumps\"");
    assertThat(TestUtil.hitCount(searcher, query)).isOne();
  }
}
