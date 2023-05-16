package lia.positional;

import java.io.IOException;

import lia.common.TestUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionalPorterStopAnalyzerTest {
  private PositionalPorterStopAnalyzer porterAnalyzer;

  private Directory directory;

  private IndexSearcher searcher;

  private QueryParser parser;

  @BeforeEach
  void setup() throws IOException {
    porterAnalyzer = new PositionalPorterStopAnalyzer();

    directory = new ByteBuffersDirectory();

    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(porterAnalyzer));

    Document doc = new Document();
    doc.add(new TextField("contents", "The quick brown fox jumps over the lazy dog", Store.YES));
    writer.addDocument(doc);
    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
    parser = new QueryParser("contents", porterAnalyzer);
  }

  @AfterEach
  void tearDown() throws IOException {
    directory.close();
  }

  @Test
  void testWithSlop() throws Exception {
    parser.setPhraseSlop(1);
    Query query = parser.parse("\"over the lazy\"");
    assertThat(TestUtil.hitCount(searcher, query)).isOne();
  }

  @Test
  void testStems() throws Exception {
    Query query = new QueryParser("contents", porterAnalyzer).parse("laziness");
    assertThat(TestUtil.hitCount(searcher, query)).isOne();

    query = parser.parse("\"fox jumped\"");
    assertThat(TestUtil.hitCount(searcher, query)).isOne();
  }
}
