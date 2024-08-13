package lia.collector;

import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.CollectorManager;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BookLinkCollectorManager implements CollectorManager<BookLinkCollector, Map<String, String>> {
  private final StoredFields storedFields;

  public BookLinkCollectorManager(StoredFields storedFields) {
    this.storedFields = storedFields;
  }

  @Override
  public BookLinkCollector newCollector() throws IOException {
    return new BookLinkCollector(storedFields);
  }

  @Override
  public Map<String, String> reduce(Collection<BookLinkCollector> bookLinkCollectors) throws IOException {
    var documents = new HashMap<String,String>();
    for (var bookLinkCollector : bookLinkCollectors) {
      documents.putAll(bookLinkCollector.getLinks());
    }
    return documents;
  }
}
