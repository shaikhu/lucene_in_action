package lia.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateTestIndex
{
  private static List<Path> findFiles(Path dir) throws IOException {
    return Files.walk(dir).filter(path -> path.getFileName().toString().endsWith("properties")).toList();

  }

  private static Document getDocument(String rootDir, Path path) throws IOException {
    Properties props = new Properties();
    props.load(Files.newInputStream(path));

    Document doc = new Document();
    String category = path.getParent().toString().substring(rootDir.length());
    String isbn = props.getProperty("isbn");
    String title = props.getProperty("title");
    String author = props.getProperty("author");
    String url = props.getProperty("url");
    String subject = props.getProperty("subject");
    String pubmonth = props.getProperty("pubmonth");

    System.out.println(title + "\n" + author + "\n" + subject + "\n" + pubmonth + "\n" + category + "\n---------");

    doc.add(new StringField("isbn", isbn, Store.YES));
    doc.add(new StringField("category", category, Store.YES));
    doc.add(new TextField("title", title, Store.YES));
    doc.add(new StringField("title2", title.toLowerCase(), Store.YES));

    String[] authors = author.split(",");
    for (String a : authors) {
      doc.add(new StringField("author", a, Store.YES));
    }

    doc.add(new StringField("url", url, Store.YES));
    doc.add(new TextField("subject", subject, Store.YES));

    doc.add(new LongField("pubmonth", Long.parseLong(pubmonth)));

    Date date;
    try {
      date = DateTools.stringToDate(pubmonth);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    doc.add(new LongField("pubmonthAsDay", date.getTime()/(1000*3600*24)));

    for (String text : new String[] {title, subject, author , category}) {
      doc.add(new TextField("contents", text, Store.NO));
    }

    return doc;
  }

  public static void main(String[] args) throws IOException {
    String dataDir = args[0];
    String indexDir = args[1];

    List<Path> results = findFiles(Paths.get(dataDir));
    System.out.println(results.size() + " books to index");

    Directory dir = FSDirectory.open(Paths.get(indexDir));
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
    for (Path p : results) {
      Document doc = getDocument(dataDir, p);
      writer.addDocument(doc);
    }
    writer.close();
    dir.close();
  }
}
