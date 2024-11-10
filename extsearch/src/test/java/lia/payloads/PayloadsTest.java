package lia.payloads;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayloadsTest {
  private Directory directory;

  private BulletinPayloadsAnalyzer analyzer;

  @BeforeEach
  void setup() throws IOException {
    directory = new ByteBuffersDirectory();
    analyzer = new BulletinPayloadsAnalyzer(5.0F);
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testPayloadTermQuery() throws Exception {
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer))){
      indexDocument(indexWriter, "Hurricane warning", "Bulletin: A hurricane warning was issued at 6 AM for the outer great banks");
      indexDocument(indexWriter, "Warning label maker", "The warning label maker is a delightful toy for your precocious seven year old's warning needs");
      indexDocument(indexWriter, "Tornado warning", "Bulletin: There is a tornado warning for Worcester county until 6 PM today");
    }

    var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    var warningQuery = new SpanTermQuery(new Term("contents", "warning"));
    var topDocs = indexSearcher.search(warningQuery, 10);
    assertThat(indexSearcher.storedFields().document(topDocs.scoreDocs[0].doc).get("title")).isEqualTo("Warning label maker");

    var payloadQuery = new PayloadScoreQuery(warningQuery, new MaxPayloadFunction(), PayloadDecoder.FLOAT_DECODER);
    topDocs = indexSearcher.search(payloadQuery, 10);
    assertThat(indexSearcher.storedFields().document(topDocs.scoreDocs[2].doc).get("title")).isEqualTo("Warning label maker");
  }

  private void indexDocument(IndexWriter indexWriter, String title, String contents) throws IOException {
    var document = new Document();
    document.add(new StringField("title", title, Store.YES));
    document.add(new TextField("contents", contents, Store.NO));
    analyzer.setBulletin(contents.startsWith("Bulletin:"));
    indexWriter.addDocument(document);
  }
}
