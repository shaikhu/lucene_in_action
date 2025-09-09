package lia;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;

import java.io.InputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class SAXXMLDocument extends DefaultHandler {
  private final StringBuilder elementBuffer = new StringBuilder();

  private final Map<String,String> attributeMap = new HashMap<>();

  private Document document;

  public Document getDocument(InputStream inputStream) throws DocumentHandlerException {
    var parserFactory = SAXParserFactory.newInstance();
    try {
      var parser = parserFactory.newSAXParser();
      parser.parse(inputStream, this);
    } catch (Exception e) {
      throw new DocumentHandlerException("Cannot parse XML document", e);
    }
    return document;
  }

  public void startDocument() {
    document = new Document();
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes){
    elementBuffer.setLength(0);
    attributeMap.clear();
    IntStream.range(0, attributes.getLength()).forEach(i -> attributeMap.put(attributes.getQName(i), attributes.getValue(i)));
  }

  public void characters(char[] text, int start, int length) {
    elementBuffer.append(text, start, length);
  }

  public void endElement(String uri, String localName, String qName) {
    if (qName.equals("contact")) {
      attributeMap.forEach((String key, String value) -> document.add(new StringField(key, value, Store.YES)));
    }
    else {
      document.add(new StringField(qName, elementBuffer.toString(), Store.YES));
    }
  }

  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      throw new IllegalArgumentException("Usage: java " + SAXXMLDocument.class.getName() + " <xml file>");
    }
    var xmlDocument = new SAXXMLDocument();
    try (var fileInputStream = new FileInputStream(args[0])) {
      var document = xmlDocument.getDocument(fileInputStream);
      System.out.println(document);
    }
  }
}
