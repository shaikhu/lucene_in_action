package lia.queryparser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import static org.apache.lucene.document.DoublePoint.nextDown;
import static org.apache.lucene.document.DoublePoint.nextUp;

public class NumericRangeQueryParser extends QueryParser {
  public NumericRangeQueryParser(String field, Analyzer analyzer) {
    super(field, analyzer);
  }

  @Override
  public Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive)
      throws ParseException {
    if (!"price".equals(field)) {
      return super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
    }
    double lower = startInclusive ? Double.parseDouble(part1) : nextUp(Double.parseDouble(part1));
    double upper = endInclusive ? Double.parseDouble(part2) : nextDown(Double.parseDouble(part2));
    return DoublePoint.newRangeQuery(field, lower, upper);
  }
}
