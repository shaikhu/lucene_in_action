package lia.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

public class NumericDateRangeQueryParser extends QueryParser {
  public NumericDateRangeQueryParser(String field, Analyzer analyzer) {
    super(field, analyzer);
  }

  @Override
  public Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive)
      throws ParseException {

    TermRangeQuery query = (TermRangeQuery) super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
    if ("pubmonth".equals(field)) {
      long lowerValue = query.includesLower()
          ? Long.parseLong(query.getLowerTerm().utf8ToString())
          : Math.addExact(Long.parseLong(query.getLowerTerm().utf8ToString()), 1);

      long upperValue = query.includesUpper()
          ? Long.parseLong(query.getUpperTerm().utf8ToString())
          : Math.addExact(Long.parseLong(query.getUpperTerm().utf8ToString()), -1);

      return LongPoint.newRangeQuery(field, lowerValue, upperValue);
    }
    else {
      return query;
    }
  }
}
