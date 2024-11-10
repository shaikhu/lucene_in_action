package lia;

import lia.common.TestUtil;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityFilterTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()))) {
      var document = new Document();
      document.add(new StringField("owner", "elwood", Store.YES));
      document.add(new TextField("keywords", "elwood's sensitive info", Store.YES));
      indexWriter.addDocument(document);

      document = new Document();
      document.add(new StringField("owner", "jake", Store.YES));
      document.add(new TextField("keywords", "jake's sensitive info", Store.YES));
      indexWriter.addDocument(document);
    }

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testSecurityFilter() throws Exception {
    var keywordQuery = new TermQuery(new Term("keywords", "info"));
    assertThat(TestUtil.hitCount(indexSearcher, keywordQuery)).isEqualTo(2);

    var ownerQuery = new TermQuery(new Term("owner", "jake"));
    assertThat(TestUtil.hitCount(indexSearcher, ownerQuery)).isOne();

    var booleanFilterQuery = new BooleanQuery.Builder()
        .add(keywordQuery, Occur.MUST)
        .add(ownerQuery, Occur.FILTER)
        .build();

    var topDocs = indexSearcher.search(booleanFilterQuery, 10);
    assertThat(topDocs.totalHits.value()).isOne();

    var document = indexSearcher.storedFields().document(topDocs.scoreDocs[0].doc);
    assertThat(document.get("keywords")).isEqualTo("jake's sensitive info");
  }
}
