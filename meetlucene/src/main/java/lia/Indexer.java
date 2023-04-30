package lia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import static java.util.function.Predicate.not;

public class Indexer {
  private static final Predicate<Path> TEXT_FILE_FILTER =
      path -> path.getFileName().toString().toLowerCase().endsWith(".txt");

  private final String indexDir;

  private final String dataDir;

  public Indexer(String indexDir, String dataDir) {
    this.indexDir = indexDir;
    this.dataDir = dataDir;
  }

  public void index() throws IOException {
    try (Directory directory = FSDirectory.open(Paths.get(indexDir));
         IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
      long start = System.currentTimeMillis();
      try (Stream<Path> files = Files.list(Paths.get(dataDir))) {
        long numIndexed = files
            .filter(not(Files::isDirectory))
            .filter(Files::exists)
            .filter(Files::isReadable)
            .filter(TEXT_FILE_FILTER)
            .mapToInt(path -> indexFile(writer, path))
            .count();

        long end = System.currentTimeMillis();
        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
      }
    }
  }

  private int indexFile(IndexWriter writer, Path path){
    System.out.println("Indexing " + path.toString());
    try {
      Document doc = getDocument(path);
      writer.addDocument(doc);
      return writer.getDocStats().numDocs;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Document getDocument(Path path) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("contents", Files.newBufferedReader(path)));
    doc.add(new StringField("filename", path.getFileName().toString(), Store.YES));
    doc.add(new StringField("fullpath", path.toString(), Store.YES));
    return doc;
  }

  public static void main(String... args) throws IOException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
    }
    new Indexer(args[0], args[1]).index();
  }
}