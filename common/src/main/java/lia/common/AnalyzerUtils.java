package lia.common;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class AnalyzerUtils {
  public static void displayTokens(Analyzer analyzer, String text) throws IOException {
    displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
  }

  public static void displayTokens(TokenStream tokenStream) throws IOException {
    var charTerm = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      System.out.print("[" + charTerm.toString() + "] ");
    }
    tokenStream.end();
    tokenStream.close();
  }

  public static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException {
    var tokenStream = analyzer.tokenStream("contents", new StringReader(text));
    var charTerm = tokenStream.addAttribute(CharTermAttribute.class);
    var positionIncrement = tokenStream.addAttribute(PositionIncrementAttribute.class);
    var offset = tokenStream.addAttribute(OffsetAttribute.class);
    var type = tokenStream.addAttribute(TypeAttribute.class);

    var position = 0;
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      if (positionIncrement.getPositionIncrement() > 0) {
        position += positionIncrement.getPositionIncrement();
        System.out.println();
        System.out.print(position + ": ");
      }

      System.out.print("[" + charTerm.toString() + ":" + offset.startOffset() + "->" + offset.endOffset() + ":" + type.type() + "] ");
    }
    tokenStream.end();
    tokenStream.close();
    System.out.println();
  }

  public static void displayTokensWithPositions(Analyzer analyzer, String text) throws IOException {
    var tokenStream = analyzer.tokenStream("contents", new StringReader(text));
    var charTerm = tokenStream.addAttribute(CharTermAttribute.class);
    var positionIncrement = tokenStream.addAttribute(PositionIncrementAttribute.class);

    var position = 0;
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      if (positionIncrement.getPositionIncrement() > 0) {
        position += positionIncrement.getPositionIncrement();
        System.out.println();
        System.out.print(position + ": ");
      }
      System.out.print("[" + charTerm.toString() + "] ");
    }
    tokenStream.end();
    tokenStream.close();
    System.out.println();
  }
}
