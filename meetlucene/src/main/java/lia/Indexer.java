package lia;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer
{
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
        }

        String indexDir = args[0];
        String dataDir = args[1];

        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer(indexDir);
        int numIndexed;
        try {
            numIndexed = indexer.index(dataDir, new TextFilesFilter());
        } finally {
            indexer.close();
        }
        long end = System.currentTimeMillis();

        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
    }

    private IndexWriter writer;

    public Indexer(String indexDir) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
    }

    public void close() throws IOException {
        writer.close();
    }

    public int index(String dataDir, FileFilter textFileFilter) throws Exception {
        List<Path> files = Files.list(Paths.get(dataDir)).toList();
        for (Path file : files) {
            if (!Files.isDirectory(file) && !Files.isHidden(file) &&
                Files.exists(file) && Files.isReadable(file) &&
                textFileFilter.accept(file.toFile())) {
                indexFile(file);
            }
        }
        return writer.getDocStats().numDocs;
    }

    private static class TextFilesFilter implements FileFilter {
        @Override
        public boolean accept(File path) {
            return path.getName().toLowerCase().endsWith(".txt");
        }
    }

    private void indexFile(Path path) throws Exception {
        System.out.println("Indexing " + path.getFileName());
        Document doc = getDocument(path);
        writer.addDocument(doc);
    }

    private Document getDocument(Path path) throws Exception {
        Document doc = new Document();
        doc.add(new TextField("contents", Files.newBufferedReader(path)));
        doc.add(new StringField("filename", path.getFileName().toString(), Store.YES));
        doc.add(new StringField("fullpath", path.getParent().toString() + "/" + path.getFileName(), Store.YES));
        return doc;
    }
}