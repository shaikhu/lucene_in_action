package lia;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class OpenFileLimitCheck {
  public static void main(String... args) throws IOException {
    List<RandomAccessFile> files = new ArrayList<>();
    try {
      while(true) {
        files.add(new RandomAccessFile("tmp" + files.size(), "rw"));
      }
    } catch (IOException e) {
      System.err.println("ERROR: Failed to open file after " + files.size() + " successful opens: " + e.getMessage());
      e.printStackTrace(System.err);
    } finally {
      int i = 0;
      for (RandomAccessFile file : files) {
        file.close();
        new File("tmp" + i++).delete();
      }
    }
  }
}
