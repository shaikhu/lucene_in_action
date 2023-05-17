package lia;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FlexibleQueryParserTest {
  private StandardQueryParser parser;

  @Test
  void testSimple() throws Exception {
    parser = new StandardQueryParser(new StandardAnalyzer());
    Query query = parser.parse("(agile OR extreme) AND methodology", "subject");
    assertThat(query).hasToString("+(subject:agile subject:extreme) +subject:methodology");
  }

  @Test
  void testThrowsExceptionForWildCardQuery() {
    parser = new CustomFlexibleQueryParser(new StandardAnalyzer());
    assertThatExceptionOfType(QueryNodeException.class).isThrownBy(() -> parser.parse("agil*", "subjexct"));
  }

  @Test
  void testThrowsExceptionForFuzzyQuery() {
    parser = new CustomFlexibleQueryParser(new StandardAnalyzer());
    assertThatExceptionOfType(QueryNodeException.class).isThrownBy(() -> parser.parse("agil~0.8", "subjexct"));
  }
}
