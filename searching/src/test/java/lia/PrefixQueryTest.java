package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrefixQueryTest
{
  @Test
  void testPrefix() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    Term term = new Term("category", "/technology/computers/programming");
    PrefixQuery query = new PrefixQuery(term);

    TopDocs matches = searcher.search(query, 10);
    long programmingAndBelow = matches.totalHits.value;

    matches = searcher.search(new TermQuery(term), 10);
    long justProgramming = matches.totalHits.value;
    assertTrue(programmingAndBelow > justProgramming);
    directory.close();
  }
}
