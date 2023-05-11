package lia.collector;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;

public class BookLinkCollector extends SimpleCollector {
  private final Map<String, String> documents = new HashMap<>();

  private final StoredFields fields;

  public BookLinkCollector(StoredFields fields) {
    this.fields = fields;
  }

  @Override
  public void collect(int doc) throws IOException {
    Document document = fields.document(doc);
    String url = document.get("url");
    String title = document.get("title2");
    documents.put(url, title);
  }

  @Override
  public ScoreMode scoreMode() {
    return ScoreMode.TOP_DOCS;
  }

  public Map<String, String> getLinks() {
    return Collections.unmodifiableMap(documents);
  }
}
