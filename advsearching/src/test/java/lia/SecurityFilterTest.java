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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityFilterTest {
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));

    Document document = new Document();
    document.add(new StringField("owner", "elwood", Store.YES));
    document.add(new TextField("keywords", "elwood's sensitive info", Store.YES));
    writer.addDocument(document);

    document = new Document();
    document.add(new StringField("owner","jake", Store.YES));
    document.add(new TextField("keywords", "jake's sensitive info", Store.YES));
    writer.addDocument(document);
    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testSecurityFilter() throws Exception {
    TermQuery query = new TermQuery(new Term("keywords", "info"));
    assertThat(TestUtil.hitCount(searcher, query)).isEqualTo(2);

    BooleanQuery jakeFilter = new BooleanQuery.Builder()
        .add(query, Occur.MUST)
        .add(new TermQuery(new Term("owner", "jake")), Occur.FILTER)
        .build();

    TopDocs hits = searcher.search(jakeFilter, 10);
    assertThat(hits.totalHits.value).isOne();

    Document doc = searcher.storedFields().document(hits.scoreDocs[0].doc);
    assertThat(doc.get("keywords")).isEqualTo("jake's sensitive info");
  }
}
