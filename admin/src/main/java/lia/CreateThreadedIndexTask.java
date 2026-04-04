package lia;

import java.io.IOException;

import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.CreateIndexTask;
import org.apache.lucene.index.IndexWriter;

public class CreateThreadedIndexTask extends CreateIndexTask {
  public CreateThreadedIndexTask(PerfRunData runData) {
    super(runData);
  }

  @Override
  public int doLogic() throws IOException {
    PerfRunData runData = getRunData();

    IndexWriter writer = new ThreadedIndexWriter(
        runData.getDirectory(),
        runData.getAnalyzer());

    runData.setIndexWriter(writer);
    return 1;
  }
}
