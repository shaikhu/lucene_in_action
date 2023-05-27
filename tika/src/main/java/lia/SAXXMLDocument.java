package lia;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SAXXMLDocument extends DefaultHandler {
  private final StringBuilder elementBuffer = new StringBuilder();

  private final Map<String,String> attributeMap = new HashMap<>();

  private Document doc;

  public Document getDocument(InputStream is) throws DocumentHandlerException {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    try {
      SAXParser parser = spf.newSAXParser();
      parser.parse(is, this);
    } catch (Exception e) {
      throw new DocumentHandlerException("Cannot parse XML document", e);
    }
    return doc;
  }

  public void startDocument() {
    doc = new Document();
  }

  public void startElement(String uri, String localName, String qName, Attributes atts){
    elementBuffer.setLength(0);
    attributeMap.clear();
    int numAtts = atts.getLength();
		if (numAtts > 0) {
      for (int i = 0; i < numAtts; i++) {
        attributeMap.put(atts.getQName(i), atts.getValue(i));
      }
    }
  }

  public void characters(char[] text, int start, int length) {
    elementBuffer.append(text, start, length);
  }

  public void endElement(String uri, String localName, String qName) {
    if (qName.equals("contact")) {
    	for (Entry<String,String> attribute : attributeMap.entrySet()) {
    		String attName = attribute.getKey();
    		String attValue = attribute.getValue();
    		doc.add(new StringField(attName, attValue, Store.YES));
    	}
    }
    else {
      doc.add(new StringField(qName, elementBuffer.toString(), Store.YES));
    }
  }

  public static void main(String... args) throws Exception {
    SAXXMLDocument handler = new SAXXMLDocument();
    Document doc = handler.getDocument(new FileInputStream(args[0]));
    System.out.println(doc);
  }
}
