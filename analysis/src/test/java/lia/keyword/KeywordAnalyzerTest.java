package lia.keyword;

import java.util.Map;

import lia.SimpleAnalyzer;
import lia.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordAnalyzerTest {
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();

    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new SimpleAnalyzer()));

    Document doc = new Document();
    doc.add(new StringField("partnum", "Q36", Store.NO));
    doc.add(new TextField("description", "Illidium Space Modulator", Store.YES));
    writer.addDocument(doc);
    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testTermQuery() throws Exception {
    Query query = new TermQuery(new Term("partnum", "Q36"));
    assertThat(TestUtil.hitCount(searcher, query)).isOne();
  }

  @Test
  void testBasicQueryParser() throws Exception {
    Query query = new QueryParser("description", new SimpleAnalyzer()).parse("partnum:Q36 AND SPACE");
    assertThat(query.toString("description")).isEqualTo("+partnum:q36 +space");
    assertThat(TestUtil.hitCount(searcher, query)).isZero();
  }

  @Test
  void testPerFieldAnalyzer() throws Exception {
    Map<String, Analyzer> fieldAnalyzers = Map.of("partnum", new KeywordAnalyzer());
    PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new SimpleAnalyzer(), fieldAnalyzers);
    Query query = new QueryParser("description", analyzer).parse("partnum:Q36 AND SPACE");

    assertThat(query.toString("description")).isEqualTo("+partnum:Q36 +space");
    assertThat(TestUtil.hitCount(searcher, query)).isOne();
  }
}
