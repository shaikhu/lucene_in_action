package lia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;


public class TikaIndexer extends Indexer {
  private static final Predicate<Path> ACCEPT_ALL_FILTER = path -> true;

  private boolean DEBUG = true;

  static Set<String> textualMetadataFields = new HashSet<>();

  static {
    textualMetadataFields.add(DublinCore.TITLE.getName());
    textualMetadataFields.add(DublinCore.CREATOR.getName());
    textualMetadataFields.add(Metadata.COMMENT);
    textualMetadataFields.add(DublinCore.DESCRIPTION.getName());
    textualMetadataFields.add(DublinCore.SUBJECT.getName());
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + TikaIndexer.class.getName() + " <index dir> <data dir>");
    }

    TikaConfig config = TikaConfig.getDefaultConfig();
    List<String> mediaTypes = config.getParser()
        .getSupportedTypes(new ParseContext()).stream().map(MediaType::getType)
        .distinct()
        .toList();

    List<String> parsers = new ArrayList<>(mediaTypes);
    Collections.sort(parsers);
    Iterator<String> it = parsers.iterator();
    System.out.println("Mime type parsers:");
    while(it.hasNext()) {
      System.out.println("  " + it.next());
    }
    System.out.println();

    String indexDir = args[0];
    String dataDir = args[1];

    long start = new Date().getTime();
    TikaIndexer indexer = new TikaIndexer(indexDir, dataDir);
    indexer.index(ACCEPT_ALL_FILTER);
    long end = new Date().getTime();

    System.out.println("Indexing took " + (end - start) + " milliseconds");
  }

  public TikaIndexer(String indexDir, String dataDir) {
    super(indexDir, dataDir);
  }

  @Override
  protected Document getDocument(Path path) throws Exception {
    Metadata metadata = new Metadata();
    metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, path.getFileName().toString());

    InputStream is = Files.newInputStream(path);
    Parser parser = new AutoDetectParser();
    ContentHandler handler = new BodyContentHandler();
    ParseContext context = new ParseContext();
    context.set(Parser.class, parser);

    try {
      parser.parse(is, handler, metadata, new ParseContext());
    } finally {
      is.close();
    }

    Document doc = new Document();
    doc.add(new TextField("contents", handler.toString(), Store.NO));

    if (DEBUG) {
      System.out.println("  all text: " + handler);
    }
    
    for(String name : metadata.names()) {
      String value = metadata.get(name);
      if (textualMetadataFields.contains(name)) {
        doc.add(new TextField("contents", value, Store.NO));
      }
      doc.add(new StringField(name, value, Store.YES));
      if (DEBUG) {
        System.out.println("  " + name + ": " + value);
      }
    }
    if (DEBUG) {
      System.out.println();
    }
    doc.add(new StringField("filename", path.getFileName().toString(), Store.YES));
    return doc;
  }
}
