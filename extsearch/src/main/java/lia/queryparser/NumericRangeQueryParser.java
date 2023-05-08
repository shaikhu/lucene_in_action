package lia.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

import static org.apache.lucene.document.DoublePoint.nextDown;
import static org.apache.lucene.document.DoublePoint.nextUp;

public class NumericRangeQueryParser extends QueryParser {
  public NumericRangeQueryParser(String field, Analyzer analyzer) {
    super(field, analyzer);
  }

  @Override
  public Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive)
      throws ParseException {

    TermRangeQuery query = (TermRangeQuery) super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
    if ("price".equals(field)) {
      double lowerValue = query.includesLower()
          ? Double.parseDouble(query.getLowerTerm().utf8ToString())
          : nextUp(Double.parseDouble(query.getLowerTerm().utf8ToString()));


      double upperValue = query.includesUpper()
          ? Double.parseDouble(query.getUpperTerm().utf8ToString())
          : nextDown(Double.parseDouble(query.getUpperTerm().utf8ToString()));

      return DoublePoint.newRangeQuery(field, lowerValue, upperValue);
    }
    else {
      return query;
    }
  }
}
