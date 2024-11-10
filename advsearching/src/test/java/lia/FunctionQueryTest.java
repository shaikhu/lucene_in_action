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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionQueryTest {
  @TempDir
  private Path index;

  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    directory = new MMapDirectory(index);
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
      indexDocument(indexWriter, 7, "this hat is green");
      indexDocument(indexWriter, 42, "this hat is blue");
    }

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testFieldScoreQuery() throws Throwable {
    var functionQuery = new FunctionQuery(ValueSource.fromDoubleValuesSource(DoubleValuesSource.fromLongField("score")));
    var topDocs = indexSearcher.search(functionQuery, 10);

    assertThat(topDocs.scoreDocs).hasSize(2);
    assertThat(topDocs.scoreDocs[0].doc).isOne();
    assertThat(topDocs.scoreDocs[0].score).isEqualTo(42);
    assertThat(topDocs.scoreDocs[1].doc).isZero();
    assertThat(topDocs.scoreDocs[1].score).isEqualTo(7);
  }


  private void indexDocument(IndexWriter indexWriter, long score, String content) throws Exception {
    var document = new Document();
    document.add(new NumericDocValuesField("score", score));
    document.add(new TextField("content", content, Store.NO));
    indexWriter.addDocument(document);
  }
}
