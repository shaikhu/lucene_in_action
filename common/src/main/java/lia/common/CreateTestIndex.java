package lia.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class CreateTestIndex
{
  private static List<Path> findFiles(Path dir) throws IOException {
    try (Stream<Path> fileStream = Files.walk(dir)) {
      return fileStream
          .filter(path -> path.getFileName().toString().endsWith("properties"))
          .toList();
    }
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
    doc.add(new SortedDocValuesField("category", new BytesRef(category)));
    doc.add(new StringField("category", category, Store.YES));

    doc.add(new Field("title", title, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, true)));
    doc.add(new Field("title2", title.toLowerCase(), createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, false)));

    String[] authors = author.split(",");
    for (String a : authors) {
      doc.add(new Field("author", a, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, false)));
    }

    doc.add(new StringField("url", url, Store.YES));
    doc.add(new Field("subject", subject, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, true)));

    doc.add(new LongField("pubmonth", Long.parseLong(pubmonth)));

    Date date;
    try {
      date = DateTools.stringToDate(pubmonth);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    doc.add(new LongField("pubmonthAsDay", date.getTime()/(1000*3600*24)));

    for (String text : new String[] {title, subject, author , category}) {
      doc.add(new Field("contents", text, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, false, true)));
    }

    return doc;
  }

  private static FieldType createFieldType(IndexOptions indexOptions, boolean stored, boolean tokenized) {
    FieldType fieldType = new FieldType();
    fieldType.setIndexOptions(indexOptions);
    fieldType.setStored(stored);
    fieldType.setTokenized(tokenized);
    fieldType.setStoreTermVectors(true);
    fieldType.setStoreTermVectorOffsets(true);
    fieldType.setStoreTermVectorPositions(true);
    return fieldType;
  }

  public static void main(String... args) throws IOException {
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
