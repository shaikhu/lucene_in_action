package lia;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexingTest
{
  private static final List<String> IDS = Arrays.asList("1", "2");

  private static final List<String> UNINDEXED = Arrays.asList("Netherlands", "Italy");

  private static final List<String> UNSTORED = Arrays.asList("Amsterdam has lots of bridges", "Venice has lots of canals");

  private static final List<String> TEXT = Arrays.asList("Amsterdam", "Venice");

  private Directory directory;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();

    try (IndexWriter writer = getWriter()) {
      for (int i = 0; i < IDS.size(); i++) {
        Document doc = new Document();
        doc.add(new StringField("id", IDS.get(i), Store.YES));
        doc.add(new StringField("country", UNINDEXED.get(i), Store.YES));
        doc.add(new TextField("contents", UNSTORED.get(i), Store.NO));
        doc.add(new StringField("city", TEXT.get(i), Store.YES));
        writer.addDocument(doc);
      }
    }
  }

  private IndexWriter getWriter() throws IOException {
    return new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
  }

  private long getHitCount(String fieldName, String searchString) throws IOException {
    IndexReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    Term t = new Term(fieldName, searchString);
    Query query = new TermQuery(t);
    long hitCount = TestUtil.hitCount(searcher, query);
    reader.close();
    return hitCount;
  }

  @Test
  void testIndexWriter() throws IOException {
    IndexWriter writer = getWriter();
    assertThat(writer.getDocStats().numDocs).isEqualTo(IDS.size());
    writer.close();
  }

  @Test
  void testIndexReader() throws IOException {
    IndexReader reader = DirectoryReader.open(directory);
    assertThat(reader.maxDoc()).isEqualTo(IDS.size());
    assertThat(reader.numDocs()).isEqualTo(IDS.size());
    reader.close();
  }

  @Test
  void testDelete() throws IOException {
    IndexWriter writer = getWriter();
    assertThat(writer.getDocStats().numDocs).isEqualTo(2);
    writer.deleteDocuments(new Term("id", "1"));
    writer.commit();
    assertThat(writer.hasDeletions()).isTrue();

    assertThat(writer.getDocStats().maxDoc).isEqualTo(2);
    assertThat(writer.getDocStats().numDocs).isOne();

    writer.close();
  }

  @Test
  void testUpdate() throws IOException {
    assertThat(getHitCount("city", "Amsterdam")).isOne();

    IndexWriter writer = getWriter();
    Document document = new Document();
    document.add(new StringField("id", "1", Store.YES));
    document.add(new StringField("country", "Netherlands", Store.YES));
    document.add(new TextField("contents", "Den Haag has a lot of museums", Store.NO));
    document.add(new StringField("city", "Den Haag", Store.YES));
    writer.updateDocument(new Term("id", "1"), document);
    writer.close();

    assertThat(getHitCount("city", "Amsterdam")).isZero();
    assertThat(getHitCount("city", "Den Haag")).isOne();
  }

  @Test
  void testMaxFieldLength() throws IOException {
    assertThat(getHitCount("contents", "bridges")).isOne();
    IndexWriter writer =
        new IndexWriter(directory, new IndexWriterConfig(new LimitTokenCountAnalyzer(new WhitespaceAnalyzer(), 1)));

    Document doc = new Document();
    doc.add(new TextField("contents", "these bridges can't be found", Store.NO));
    writer.addDocument(doc);
    writer.close();

    assertThat(getHitCount("contents", "bridges")).isOne();
  }
}
