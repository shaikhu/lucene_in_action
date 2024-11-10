package lia.keyword;

import java.nio.file.Path;
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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordAnalyzerTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new SimpleAnalyzer()))) {
      var document = new Document();
      document.add(new StringField("partnum", "Q36", Store.NO));
      document.add(new TextField("description", "Illidium Space Modulator", Store.YES));
      indexWriter.addDocument(document);
    }
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testTermQuery() throws Exception {
    var termQuery = new TermQuery(new Term("partnum", "Q36"));
    assertThat(TestUtil.hitCount(indexSearcher, termQuery)).isOne();
  }

  @Test
  void testBasicQueryParser() throws Exception {
    var queryParser = new QueryParser("description", new SimpleAnalyzer());
    var query = queryParser.parse("partnum:Q36 AND SPACE");
    assertThat(query.toString("description")).isEqualTo("+partnum:q36 +space");
    assertThat(TestUtil.hitCount(indexSearcher, query)).isZero();
  }

  @Test
  void testPerFieldAnalyzer() throws Exception {
    Map<String, Analyzer> fieldAnalyzers = Map.of("partnum", new KeywordAnalyzer());
    var analyzerWrapper = new PerFieldAnalyzerWrapper(new SimpleAnalyzer(), fieldAnalyzers);
    var query = new QueryParser("description", analyzerWrapper).parse("partnum:Q36 AND SPACE");
    assertThat(query.toString("description")).isEqualTo("+partnum:Q36 +space");
    assertThat(TestUtil.hitCount(indexSearcher, query)).isOne();
  }
}
