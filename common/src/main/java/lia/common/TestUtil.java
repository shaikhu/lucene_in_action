package lia.common;

import java.io.IOException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

public class TestUtil
{
  private TestUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  public static long hitCount(IndexSearcher searcher, Query query) throws IOException {
    return searcher.search(query, 1).totalHits.value;
  }
}
