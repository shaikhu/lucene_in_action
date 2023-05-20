package lia;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpatialQueryTest
{
  private Directory directory;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();

    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
    addLocation(writer, "McCormick & Schmick's Seafood Restaurant", 38.9579000, -77.3572000);
    addLocation(writer, "Jimmy's Old Town Tavern", 38.9690000, -77.3862000);
    addLocation(writer, "Ned Devine's", 38.9510000, -77.4107000);
    addLocation(writer, "Old Brogue Irish Pub", 38.9955000, -77.2884000);
    addLocation(writer, "Alf Laylah Wa Laylah", 38.8956000, -77.4258000);
    addLocation(writer, "Sully's Restaurant & Supper", 38.9003000, -77.4467000);
    addLocation(writer, "TGIFriday", 38.8725000, -77.3829000);
    addLocation(writer, "Potomac Swing Dance Club", 38.9027000, -77.2639000);
    addLocation(writer, "White Tiger Restaurant", 38.9027000, -77.2638000);
    addLocation(writer, "Jammin' Java", 38.9039000, -77.2622000);
    addLocation(writer, "Potomac Swing Dance Club", 38.9027000, -77.2639000);
    addLocation(writer, "WiseAcres Comedy Club", 38.9248000, -77.2344000);
    addLocation(writer, "Glen Echo Spanish Ballroom", 38.9691000, -77.1400000);
    addLocation(writer, "Whitlow's on Wilson", 38.8889000, -77.0926000);
    addLocation(writer, "Iota Club and Cafe", 38.8890000, -77.0923000);
    addLocation(writer, "Hilton Washington Embassy Row", 38.9103000, -77.0451000);
    addLocation(writer, "HorseFeathers, Bar & Grill", 39.01220000000001, -77.3942);
    writer.close();
  }

  private void addLocation(IndexWriter writer, String name, double latitude, double longitude) throws Exception {
    Document document = new Document();
    document.add(new TextField("name", name, Store.YES));
    document.add(new LatLonPoint("location", latitude, longitude));
    writer.addDocument(document);
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testBasicQuery() throws Exception {
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    TopDocs hits = searcher.search(LatLonPoint.newDistanceQuery("location", 38.8725000, -77.3829000, 10000), 10);
    assertThat(hits.scoreDocs)
        .extracting(scoreDoc -> searcher.storedFields().document(scoreDoc.doc).get("name"))
        .containsOnly(
            "McCormick & Schmick's Seafood Restaurant",
            "Ned Devine's",
            "Alf Laylah Wa Laylah", "Sully's Restaurant & Supper",
            "TGIFriday");
  }

  @Test
  void testBooleanQuery() throws Exception {
    Query distanceQuery = LatLonPoint.newDistanceQuery("location", 38.8725000, -77.3829000, 10000);
    Query restaurants = new TermQuery(new Term("name", "Restaurant"));

    Query localRestaurants = new BooleanQuery.Builder()
        .add(distanceQuery, Occur.MUST)
        .add(restaurants, Occur.MUST)
        .build();

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    TopDocs hits = searcher.search(localRestaurants, 10);
    assertThat(hits.scoreDocs)
        .extracting(scoreDoc -> searcher.storedFields().document(scoreDoc.doc).get("name"))
        .containsOnly(
            "Sully's Restaurant & Supper",
            "McCormick & Schmick's Seafood Restaurant");
  }
}
