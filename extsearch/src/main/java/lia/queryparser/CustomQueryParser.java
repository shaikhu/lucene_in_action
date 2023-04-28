package lia.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

public class CustomQueryParser extends QueryParser
{
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
    Query orig = super.getFieldQuery(field, queryText, slop);

    if (!(orig instanceof PhraseQuery)) {
      return orig;
    }

    PhraseQuery pq = (PhraseQuery) orig;
    Term[] terms = pq.getTerms();
    SpanTermQuery[] clauses = new SpanTermQuery[terms.length];
    for (int i = 0; i < terms.length; i++) {
      clauses[i] = new SpanTermQuery(terms[i]);
    }

    return new SpanNearQuery(clauses, slop, true);
  }
}
