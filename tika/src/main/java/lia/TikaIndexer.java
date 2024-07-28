package lia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

  private static final Set<String> TEXTUAL_METADATA_FIELDS = Set.of(
    DublinCore.TITLE.getName(),
    DublinCore.CREATOR.getName(),
    Metadata.COMMENT,
    DublinCore.DESCRIPTION.getName(),
    DublinCore.SUBJECT.getName()
  );

  public TikaIndexer(String indexDirectory, String dataDirectory) {
    super(indexDirectory, dataDirectory);
  }

  @Override
  protected Document createDocument(Path path) throws Exception {
    var metadata = new Metadata();
    metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, path.getFileName().toString());

    var content = getContent(metadata, path);

    var document = new Document();
    document.add(new TextField("contents", content.toString(), Store.NO));
    System.out.println("  all text: " + content);

    Arrays.stream(metadata.names()).forEach(key -> {
      var value = metadata.get(key);
      System.out.println(" " + key + ": " + value);
      document.add(new StringField(key, value, Store.YES));

      if (TEXTUAL_METADATA_FIELDS.contains(key)) {
        document.add(new TextField("contents", value, Store.NO));
      }
    });

    System.out.println();
    document.add(new StringField("filename", path.getFileName().toString(), Store.YES));
    return document;
  }

  private ContentHandler getContent(Metadata metadata, Path path) throws Exception {
    var content = new BodyContentHandler();
    var inputStream = Files.newInputStream(path);
    var parser = new AutoDetectParser();
    var parseContext = new ParseContext();
    parseContext.set(Parser.class, parser);

    try {
      parser.parse(inputStream, content, metadata, new ParseContext());
      return content;
    } finally {
      inputStream.close();
    }
  }

  private static List<String> getMediaTypes(TikaConfig tikaConfig) {
    return tikaConfig.getParser().getSupportedTypes(new ParseContext())
            .stream()
            .map(MediaType::getType)
            .distinct()
            .sorted()
            .toList();
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + TikaIndexer.class.getName() + " <index dir> <data dir>");
    }

    var tikaConfig = TikaConfig.getDefaultConfig();
    var mediaTypes = getMediaTypes(tikaConfig);
    System.out.println("Mime type parsers:");
    mediaTypes.forEach(parser -> System.out.println("  " + parser));
    System.out.println();

    new TikaIndexer(args[0], args[1]).index(ACCEPT_ALL_FILTER);
  }
}
