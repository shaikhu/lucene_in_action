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

  private IndexSearcher indexSearcher;

  private QueryParser queryParser;

  @BeforeEach
  void setup() throws IOException {
    porterAnalyzer = new PositionalPorterStopAnalyzer();
    directory = new ByteBuffersDirectory();

    try (IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(porterAnalyzer))) {
      var document = new Document();
      document.add(new TextField("contents", "The quick brown fox jumps over the lazy dog", Store.YES));
      indexWriter.addDocument(document);
    }

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    queryParser = new QueryParser("contents", porterAnalyzer);
  }

  @AfterEach
  void tearDown() throws IOException {
    directory.close();
  }

  @Test
  void testWithSlop() throws Exception {
    queryParser.setPhraseSlop(1);
    var query = queryParser.parse("\"over the lazy\"");
    assertThat(TestUtil.hitCount(indexSearcher, query)).isOne();
  }

  @Test
  void testStems() throws Exception {
    var query = new QueryParser("contents", porterAnalyzer).parse("laziness");
    assertThat(TestUtil.hitCount(indexSearcher, query)).isOne();

    query = queryParser.parse("\"fox jumped\"");
    assertThat(TestUtil.hitCount(indexSearcher, query)).isOne();
  }
}
