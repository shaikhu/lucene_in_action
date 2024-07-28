package lia.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Properties;

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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class CreateTestIndex
{
  private static List<Path> listFiles(Path directory) throws IOException {
    try (var fileStream = Files.walk(directory)) {
      return fileStream
          .filter(path -> path.getFileName().toString().endsWith("properties"))
          .toList();
    }
  }

  private static Document createDocument(String rootDirectory, Path path) throws IOException {
    var props = new Properties();
    props.load(Files.newInputStream(path));

    var document = new Document();
    var category = path.getParent().toString().substring(rootDirectory.length()).replaceAll("\\\\", "/");
    var isbn = props.getProperty("isbn");
    var title = props.getProperty("title");
    var authors = props.getProperty("author");
    var url = props.getProperty("url");
    var subject = props.getProperty("subject");
    var pubmonth = props.getProperty("pubmonth");

    System.out.println(title + "\n" + authors + "\n" + subject + "\n" + pubmonth + "\n" + category + "\n---------");

    document.add(new StringField("isbn", isbn, Store.YES));
    document.add(new SortedDocValuesField("category", new BytesRef(category)));
    document.add(new StringField("category", category, Store.YES));

    document.add(new Field("title", title, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, true)));
    document.add(new Field("title2", title.toLowerCase(), createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, false)));

    for (var author : authors.split(",")) {
      document.add(new Field("author", author, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, false)));
    }

    document.add(new StringField("url", url, Store.YES));
    document.add(new Field("subject", subject, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, true, true)));
    document.add(new LongField("pubmonth", Long.parseLong(pubmonth), Store.NO));

    try {
      var date = DateTools.stringToDate(pubmonth);
      document.add(new LongField("pubmonthAsDay", date.getTime()/(1000*3600*24), Store.NO));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    for (String text : List.of(title, subject, authors , category)) {
      document.add(new Field("contents", text, createFieldType(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS, false, true)));
    }

    return document;
  }

  private static FieldType createFieldType(IndexOptions indexOptions, boolean stored, boolean tokenized) {
    var fieldType = new FieldType();
    fieldType.setIndexOptions(indexOptions);
    fieldType.setStored(stored);
    fieldType.setTokenized(tokenized);
    fieldType.setStoreTermVectors(true);
    fieldType.setStoreTermVectorOffsets(true);
    fieldType.setStoreTermVectorPositions(true);
    return fieldType;
  }

  public static void main(String... args) throws IOException {
    var dataDirectory = args[0];
    var indexDirectory = args[1];

    var files = listFiles(Paths.get(dataDirectory));
    System.out.println(files.size() + " books to index");

    var indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
    indexWriterConfig.setOpenMode(OpenMode.CREATE);

    try (var directory = FSDirectory.open(Paths.get(indexDirectory));
         var indexWriter = new IndexWriter(directory, indexWriterConfig)) {
      for (var file : files) {
        indexWriter.addDocument(createDocument(dataDirectory, file));
      }
    }
  }
}
