package lia;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class LockTest
{
  private Directory dir;

  @BeforeEach
  void setup() throws IOException {
    String indexDir = System.getProperty("java.io.tmpdir", "tmp") + System.getProperty("file.separator") + "index";
    dir = FSDirectory.open(Paths.get(indexDir));
  }

  @Test
  void testWriteLock() throws IOException {
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new WhitespaceAnalyzer()));

    assertThrows(LockObtainFailedException.class,
        () -> new IndexWriter(dir, new IndexWriterConfig(new WhitespaceAnalyzer())));
    writer.close();
  }
}
