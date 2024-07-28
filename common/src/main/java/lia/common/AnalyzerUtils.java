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

  public static void displayTokens(TokenStream stream) throws IOException {
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
    stream.reset();
    while(stream.incrementToken()) {
      System.out.print("[" + term.toString() + "] ");
    }
    stream.end();
    stream.close();
  }

  public static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException {
    TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(text));

    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute positionIncrement = tokenStream.addAttribute(PositionIncrementAttribute.class);
    OffsetAttribute offset = tokenStream.addAttribute(OffsetAttribute.class);
    TypeAttribute type = tokenStream.addAttribute(TypeAttribute.class);

    var position = 0;
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      if (positionIncrement.getPositionIncrement() > 0) {
        position += positionIncrement.getPositionIncrement();
        System.out.println();
        System.out.print(position + ": ");
      }

      System.out.print("[" + term.toString() + ":" + offset.startOffset() + "->" + offset.endOffset() + ":" + type.type() + "] ");
    }
    tokenStream.end();
    tokenStream.close();
    System.out.println();
  }

  public static void displayTokensWithPositions(Analyzer analyzer, String text) throws IOException {
    TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(text));
    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute positionIncrement = tokenStream.addAttribute(PositionIncrementAttribute.class);

    var position = 0;
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      if (positionIncrement.getPositionIncrement() > 0) {
        position += positionIncrement.getPositionIncrement();
        System.out.println();
        System.out.print(position + ": ");
      }
      System.out.print("[" + term.toString() + "] ");
    }
    tokenStream.end();
    tokenStream.close();
    System.out.println();
  }
}
