package lia.synonym;

import java.io.StringReader;
import java.util.List;

import lia.common.TestUtil;
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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SynonymAnalyzerTest {
  private Directory directory;

  private SynonymAnalyzer synonymAnalyzer;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();
    synonymAnalyzer = new SynonymAnalyzer(new TestSynonymEngine());

    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(synonymAnalyzer))) {
      var document = new Document();
      document.add(new TextField("content", "The quick brown fox jumps over the lazy dog", Store.YES));
      indexWriter.addDocument(document);
    }

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testJumps() throws Exception {
    var tokenStream = synonymAnalyzer.tokenStream("contents", new StringReader("jumps"));
    var charTerm = tokenStream.addAttribute(CharTermAttribute.class);
    var positionIncrement = tokenStream.addAttribute(PositionIncrementAttribute.class);

    List<String> expected = List.of("jumps", "hops", "leaps");
    var index = 0;
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      assertThat(charTerm.toString()).isEqualTo(expected.get(index));
      var expectedPosition = index == 0 ? 1 : 0;
      assertThat(positionIncrement.getPositionIncrement()).isEqualTo(expectedPosition);
      index++;
    }
    tokenStream.close();
    assertThat(index).isEqualTo(3);
  }

  @Test
  void testSearchByAPI() throws Exception {
    var termQuery = new TermQuery(new Term("content", "hops"));
    assertThat(TestUtil.hitCount(indexSearcher, termQuery)).isOne();

    var phraseQuery = new PhraseQuery.Builder()
        .add(new Term("content", "fox"))
        .add(new Term("content", "hops"))
        .build();
    assertThat(TestUtil.hitCount(indexSearcher, phraseQuery)).isOne();
  }

  @Test
  void testWithQueryParser() throws Exception {
    var queryParser = new QueryParser("content", synonymAnalyzer);
    var query = queryParser.parse("\"fox jumps\"");
    assertThat(TestUtil.hitCount(indexSearcher, query)).isOne();

    queryParser = new QueryParser("content", new StandardAnalyzer());
    query = queryParser.parse("\"fox jumps\"");
    assertThat(TestUtil.hitCount(indexSearcher, query)).isOne();
  }
}
