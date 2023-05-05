package lia;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FunctionQueryTest {
  private Directory directory;

  private IndexWriter writer;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();
    writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));
    addDocument(7, "this hat is green");
    addDocument(42, "this hat is blue");
    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testFieldScoreQuery() throws Throwable {
    FunctionQuery query =
        new FunctionQuery(ValueSource.fromDoubleValuesSource(DoubleValuesSource.fromLongField("score")));
    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.scoreDocs).hasSize(2);
    assertThat(hits.scoreDocs[0].doc).isOne();
    assertThat(hits.scoreDocs[0].score).isEqualTo(42);
    assertThat(hits.scoreDocs[1].doc).isZero();
    assertThat(hits.scoreDocs[1].score).isEqualTo(7);
  }


  private void addDocument(long score, String content) throws Exception {
    Document doc = new Document();
    doc.add(new NumericDocValuesField("score", score));
    doc.add(new TextField("content", content, Store.NO));
    writer.addDocument(doc);
  }
}
