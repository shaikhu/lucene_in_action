package lia;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LockTest
{
  private Directory dir;

  @BeforeEach
  void setup() throws IOException {
    String indexDir = System.getProperty("java.io.tmpdir", "tmp") + System.getProperty("file.separator") + "index";
    dir = FSDirectory.open(Paths.get(indexDir));
  }

  @AfterEach
  void tearDown() throws Exception {
    dir.close();
  }

  @Test
  void testWriteLock() throws IOException {
    try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new WhitespaceAnalyzer()))) {

      assertThatExceptionOfType(LockObtainFailedException.class)
          .isThrownBy(() -> new IndexWriter(dir, new IndexWriterConfig(new WhitespaceAnalyzer())));
    }
  }
}
