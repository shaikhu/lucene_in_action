package lia.i18n;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Label;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import lia.SimpleAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class ChineseDemo {
  private static final List<String> TEXT = List.of("道德經");

  private static final List<? extends Analyzer> ANALYSERS = List.of(
      new SimpleAnalyzer(),
      new StandardAnalyzer(),
      new CJKAnalyzer());

  private static void analyze(String string, Analyzer analyzer) throws IOException {
    StringBuilder sb = new StringBuilder();
    TokenStream stream = analyzer.tokenStream("contents", new StringReader(string));
    CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);

    stream.reset();
    while(stream.incrementToken()) {
      sb.append("[");
      sb.append(term.toString());
      sb.append("] ");
    }
    stream.end();
    stream.close();

    String output = sb.toString();

    Frame f = new Frame();
    f.setTitle(analyzer.getClass().getSimpleName() + " : " + string);
    f.setResizable(true);

    Font font = new Font(null, Font.PLAIN, 36);
    int width = getWidth(f.getFontMetrics(font), output);

    f.setSize((width < 250) ? 250 : width + 50, 75);

    // NOTE: if Label doesn't render the Chinese characters
    // properly, try using javax.swing.JLabel instead
    Label label = new Label(output);   //D
    label.setSize(width, 75);
    label.setAlignment(Label.CENTER);
    label.setFont(font);
    f.add(label);
    f.setVisible(true);
  }

  private static int getWidth(FontMetrics metrics, String s) {
    int size = 0;
    int length = s.length();
    for (int i = 0; i < length; i++) {
      size += metrics.charWidth(s.charAt(i));
    }
    return size;
  }

  public static void main(String... args) throws IOException {
    for (String string : TEXT) {
      for (Analyzer analyzer : ANALYSERS) {
        analyze(string, analyzer);
      }
    }
  }
}