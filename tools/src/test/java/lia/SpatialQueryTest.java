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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpatialQueryTest
{
  record SpatialData(String name, double latitude, double longitude) {}

  private static final List<SpatialData> TEST_DATA = List.of(
      new SpatialData("McCormick & Schmick's Seafood Restaurant", 38.9579000, -77.3572000),
      new SpatialData("Jimmy's Old Town Tavern", 38.9690000, -77.3862000),
      new SpatialData("Ned Devine's", 38.9510000, -77.4107000),
      new SpatialData("Old Brogue Irish Pub", 38.9955000, -77.2884000),
      new SpatialData("Alf Laylah Wa Laylah", 38.8956000, -77.4258000),
      new SpatialData("Sully's Restaurant & Supper", 38.9003000, -77.4467000),
      new SpatialData("TGIFriday", 38.8725000, -77.3829000),
      new SpatialData("Potomac Swing Dance Club", 38.9027000, -77.2639000),
      new SpatialData("White Tiger Restaurant", 38.9027000, -77.2638000),
      new SpatialData("Jammin' Java", 38.9039000, -77.2622000),
      new SpatialData("Potomac Swing Dance Club", 38.9027000, -77.2639000),
      new SpatialData("WiseAcres Comedy Club", 38.9248000, -77.2344000),
      new SpatialData("Glen Echo Spanish Ballroom", 38.9691000, -77.1400000),
      new SpatialData("Whitlow's on Wilson", 38.8889000, -77.0926000),
      new SpatialData("Iota Club and Cafe", 38.8890000, -77.0923000),
      new SpatialData("Hilton Washington Embassy Row", 38.9103000, -77.0451000)
  );

  private Directory directory;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();

    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()))) {
      for (var data : TEST_DATA) {
        var document = new Document();
        document.add(new TextField("name", data.name, Store.YES));
        document.add(new LatLonPoint("location", data.latitude, data.longitude));
        indexWriter.addDocument(document);
      }
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testBasicQuery() throws Exception {
    var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    var topDocs = indexSearcher.search(LatLonPoint.newDistanceQuery("location", 38.8725000, -77.3829000, 10000), 10);
    assertThat(topDocs.scoreDocs)
        .extracting(scoreDoc -> indexSearcher.storedFields().document(scoreDoc.doc).get("name"))
        .containsOnly(
            "McCormick & Schmick's Seafood Restaurant",
            "Ned Devine's",
            "Alf Laylah Wa Laylah", "Sully's Restaurant & Supper",
            "TGIFriday");
  }

  @Test
  void testBooleanQuery() throws Exception {
    var distanceQuery = LatLonPoint.newDistanceQuery("location", 38.8725000, -77.3829000, 10000);
    var restaurantsQuery = new TermQuery(new Term("name", "Restaurant"));

    var localRestaurantsQuery = new BooleanQuery.Builder()
        .add(distanceQuery, Occur.MUST)
        .add(restaurantsQuery, Occur.MUST)
        .build();

    var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    var topDocs = indexSearcher.search(localRestaurantsQuery, 10);
    assertThat(topDocs.scoreDocs)
        .extracting(scoreDoc -> indexSearcher.storedFields().document(scoreDoc.doc).get("name"))
        .containsOnly(
            "Sully's Restaurant & Supper",
            "McCormick & Schmick's Seafood Restaurant");
  }
}
