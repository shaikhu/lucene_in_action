package lia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import static java.util.function.Predicate.not;

public class Indexer {
  private static final Predicate<Path> TEXT_FILE_FILTER =
      path -> path.getFileName().toString().toLowerCase().endsWith(".txt");

  private final String indexDirectory;

  private final String dataDirectory;

  public Indexer(String indexDirectory, String dataDirectory) {
    this.indexDirectory = indexDirectory;
    this.dataDirectory = dataDirectory;
  }

  public void index(Predicate<Path> fileFilter) throws IOException {
    try (var directory = FSDirectory.open(Paths.get(indexDirectory));
         var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {

      var startTime = System.currentTimeMillis();
      try (var files = Files.list(Paths.get(dataDirectory))) {
        var numFilesIndexed = files
            .filter(not(Files::isDirectory))
            .filter(Files::exists)
            .filter(Files::isReadable)
            .filter(fileFilter)
            .mapToInt(path -> indexFile(indexWriter, path))
            .count();

        var endTime = System.currentTimeMillis();
        System.out.printf("Indexing %d files took %d milliseconds%n", numFilesIndexed, endTime - startTime);
      }
    }
  }

  private int indexFile(IndexWriter writer, Path path){
    System.out.println("Indexing " + path.toString());
    try {
      writer.addDocument(createDocument(path));
      return writer.getDocStats().numDocs;
    } catch (Exception e) {
      throw new RuntimeException("Failed to index file: " + path, e);
    }
  }

  protected Document createDocument(Path path) throws Exception {
    var document = new Document();
    document.add(new TextField("contents", Files.newBufferedReader(path)));
    document.add(new StringField("filename", path.getFileName().toString(), Store.YES));
    document.add(new StringField("fullpath", path.toString(), Store.YES));
    return document;
  }

  public static void main(String... args) throws IOException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
    }
    new Indexer(args[0], args[1]).index(TEXT_FILE_FILTER);
  }
}