package lia;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorPipeline;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;

public class CustomFlexibleQueryParser extends StandardQueryParser {
  public CustomFlexibleQueryParser(Analyzer analyzer) {
    super(analyzer);
    QueryNodeProcessorPipeline processors = (QueryNodeProcessorPipeline) getQueryNodeProcessor();
    processors.add(new NoFuzzyOrWildcardQueryProcessor());
  }

  private static final class NoFuzzyOrWildcardQueryProcessor extends QueryNodeProcessorImpl {
    @Override
    protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {
      if (node instanceof FuzzyQueryNode || node instanceof WildcardQueryNode) {
        throw new QueryNodeException(new MessageImpl("no"));
      }
      return node;
    }

    @Override
    protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
      return node;
    }

    @Override
    protected List<QueryNode> setChildrenOrder(List<QueryNode> children) {
      return children;
    }
  }
}
