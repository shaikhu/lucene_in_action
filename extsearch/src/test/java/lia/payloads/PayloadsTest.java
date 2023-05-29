package lia.payloads;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayloadsTest {
  private static final Similarity BOOSTING_SIMILARITY = new BoostingSimilarity();

  private Directory directory;

  private BulletinPayloadsAnalyzer analyzer;

  @BeforeEach
  void setup(){
    directory = new ByteBuffersDirectory();
    analyzer = new BulletinPayloadsAnalyzer(5.0F);
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  private void addDoc(IndexWriter writer, String title, String contents) throws IOException {
    Document doc = new Document();
    doc.add(new StringField("title", title, Store.YES));
    doc.add(new TextField("contents", contents, Store.NO));
    analyzer.setBulletin(contents.startsWith("Bulletin:"));
    writer.addDocument(doc);
  }

  @Test
  void testPayloadTermQuery() throws Throwable {
    IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
    writerConfig.setSimilarity(BOOSTING_SIMILARITY);

    IndexWriter writer = new IndexWriter(directory, writerConfig);
    addDoc(writer, "Hurricane warning", "Bulletin: A hurricane warning was issued at 6 AM for the outer great banks");
    addDoc(writer, "Warning label maker", "The warning label maker is a delightful toy for your precocious seven year old's warning needs");
    addDoc(writer, "Tornado warning", "Bulletin: There is a tornado warning for Worcester county until 6 PM today");
    writer.close();

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    searcher.setSimilarity(BOOSTING_SIMILARITY);

    SpanQuery warning = new SpanTermQuery(new Term("contents", "warning"));
    TopDocs hits = searcher.search(warning, 10);

    assertThat(searcher.storedFields().document(hits.scoreDocs[0].doc).get("title")).isEqualTo("Warning label maker");

    PayloadScoreQuery query2 = new PayloadScoreQuery(warning, new AveragePayloadFunction(), PayloadDecoder.FLOAT_DECODER);
    hits = searcher.search(query2, 10);

    // this assertion is failing as the other docs aren't getting boosted using the payloadscorequery :(
    assertThat(searcher.storedFields().document(hits.scoreDocs[2].doc).get("title")).isEqualTo("Warning label maker");
  }
}
