package lia.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

import java.util.Arrays;

public class CustomQueryParser extends QueryParser {
  public CustomQueryParser(String field, Analyzer analyzer) {
    super(field, analyzer);
  }

  @Override
  public Query getWildcardQuery(String field, String termStr) throws ParseException {
    throw new ParseException("Wildcard not allowed");
  }

  @Override
  public Query getFuzzyQuery(String field, String term, float minSimilarity) throws ParseException {
    throw new ParseException("Fuzzy queries not allowed");
  }

  /**
   * Replace PhraseQuery with SpanNearQuery to force in-order
   * phrase matching rather than reverse.
   */
  @Override
  public Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
    var originalQuery = super.getFieldQuery(field, queryText, slop);

    if (!(originalQuery instanceof PhraseQuery phraseQuery)) {
      return originalQuery;
    }

    var spanTermQueryClauses = Arrays.stream(phraseQuery.getTerms())
            .map(SpanTermQuery::new)
            .toArray(SpanTermQuery[]::new);

    return new SpanNearQuery(spanTermQueryClauses, slop, true);
  }
}
