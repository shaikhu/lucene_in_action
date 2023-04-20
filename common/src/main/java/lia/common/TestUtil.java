package lia.common;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class TestUtil
{
  private TestUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  public static long hitCount(IndexSearcher searcher, Query query) throws IOException {
    return searcher.search(query, 1).totalHits.value;
  }

  public static Directory getBookIndexDirectory() throws IOException {
    return FSDirectory.open(Paths.get("/home/shaikhu/projects/lucene_in_action/testIndex"));
  }
}
