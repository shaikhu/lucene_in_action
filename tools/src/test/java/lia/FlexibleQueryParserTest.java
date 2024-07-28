package lia;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FlexibleQueryParserTest {
  private StandardQueryParser queryParser;

  @Test
  void testSimple() throws Exception {
    queryParser = new StandardQueryParser(new StandardAnalyzer());
    var query = queryParser.parse("(agile OR extreme) AND methodology", "subject");
    assertThat(query).hasToString("+(subject:agile subject:extreme) +subject:methodology");
  }

  @Test
  void testThrowsExceptionForWildCardQuery() {
    queryParser = new CustomFlexibleQueryParser(new StandardAnalyzer());
    assertThatExceptionOfType(QueryNodeException.class).isThrownBy(() -> queryParser.parse("agil*", "subjexct"));
  }

  @Test
  void testThrowsExceptionForFuzzyQuery() {
    queryParser = new CustomFlexibleQueryParser(new StandardAnalyzer());
    assertThatExceptionOfType(QueryNodeException.class).isThrownBy(() -> queryParser.parse("agil~0.8", "subjexct"));
  }
}
