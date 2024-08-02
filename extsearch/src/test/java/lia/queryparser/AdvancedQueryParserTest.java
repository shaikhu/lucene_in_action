package lia.queryparser;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AdvancedQueryParserTest {
  private CustomQueryParser customQueryParser;

  @BeforeEach
  void setup() {
    customQueryParser = new CustomQueryParser("field", new WhitespaceAnalyzer());
  }

  @Test
  void testCustomQueryParser_wildCardQuery() {
    assertThatExceptionOfType(ParseException.class)
        .isThrownBy(() -> customQueryParser.parse("a?t"))
        .withMessageContaining("Wildcard not allowed");
  }

  @Test
  void testCustomQueryParse_fuzzyQuery() {
    assertThatExceptionOfType(ParseException.class)
        .isThrownBy(() -> customQueryParser.parse("xunit~"))
        .withMessageContaining("Fuzzy queries not allowed");
  }

  @Test
  void testPhraseQuery() throws Exception {
    var query = customQueryParser.parse("singleTerm");
    assertThat(query).isInstanceOf(TermQuery.class);

    query = customQueryParser.parse("\"a phrase\"");
    assertThat(query).isInstanceOf(SpanNearQuery.class);
  }
}
