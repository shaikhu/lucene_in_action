package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiFieldQueryParserTest
{
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testDefaultOperator() throws Exception {
    Query query = new MultiFieldQueryParser(new String[]{"title", "subject"}, new SimpleAnalyzer()).parse("development");
    TopDocs hits = searcher.search(query, 10);

    assertThat(TestUtil.hitsIncludeTitle(searcher, hits, "Ant in Action")).isTrue();
    assertThat(TestUtil.hitsIncludeTitle(searcher, hits, "Extreme Programming Explained")).isTrue();
  }

  @Test
  void testSpecifiedOperator() throws Exception {
    Query query = MultiFieldQueryParser.parse("lucene", new String[]{"title", "subject"},
        new Occur[] { Occur.MUST, Occur.MUST},
        new SimpleAnalyzer());

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    TopDocs hits = searcher.search(query, 10);

    assertThat(TestUtil.hitsIncludeTitle(searcher, hits, "Lucene in Action, Second Edition")).isTrue();
    assertThat(hits.scoreDocs).hasSize(1);
  }
}