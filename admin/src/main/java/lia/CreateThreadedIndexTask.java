package lia;

import java.io.IOException;

import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.tasks.CreateIndexTask;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.index.IndexWriter;

public class CreateThreadedIndexTask extends CreateIndexTask {
  public CreateThreadedIndexTask(PerfRunData runData) {
    super(runData);
  }

  @Override
  public int doLogic() throws IOException {
    PerfRunData runData = getRunData();
    Config config = runData.getConfig();

    IndexWriter writer = new ThreadedIndexWriter(
        runData.getDirectory(),
        runData.getAnalyzer(),
        config.get("writer.num.threads", 4),
        config.get("writer.max.thread.queue.size", 20));

    runData.setIndexWriter(writer);
    return 1;
  }
}
