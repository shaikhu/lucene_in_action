package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiFieldQueryParserTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testDefaultOperator() throws Exception {
    var queryParser = new MultiFieldQueryParser(new String[]{"title", "subject"}, new SimpleAnalyzer());
    var query = queryParser.parse("development");
    var topDocs = indexSearcher.search(query, 10);

    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "Ant in Action")).isTrue();
    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "Extreme Programming Explained")).isTrue();
  }

  @Test
  void testSpecifiedOperator() throws Exception {
    var query = MultiFieldQueryParser.parse("lucene", new String[]{"title", "subject"},
            new Occur[]{Occur.MUST, Occur.MUST}, new SimpleAnalyzer());
    var topDocs = indexSearcher.search(query, 10);

    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "Lucene in Action, Second Edition")).isTrue();
    assertThat(topDocs.scoreDocs).hasSize(1);
  }
}