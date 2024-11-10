package lia;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LockTest {
  @TempDir
  private Path index;

  private Directory directory;

  @BeforeEach
  void setup() throws IOException {
    directory = new MMapDirectory(index);
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testWriteLock() throws IOException {
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()))) {

      assertThatExceptionOfType(LockObtainFailedException.class)
          .isThrownBy(() -> new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer())));
    }
  }
}
