package org.ua.wozzya.util.extractor;

import org.ua.wozzya.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileNameIterativeExtractor implements IterativeExtractor<Path> {

    private final Path baseDirectoryPath;

    public FileNameIterativeExtractor(String baseDirectory) {
        this.baseDirectoryPath = Paths.get(baseDirectory);
        FileUtils.requireExistence(baseDirectoryPath, "directory is not exists");
    }

    @Override
    public Iterator<Path> iterator() {
        return new FileNameIterator(baseDirectoryPath);
    }

    public static class FileNameIterator implements Iterator<Path> {
        private final Path baseDirectoryPath;
        private final List<Path> filePaths = new ArrayList<>();
        private int cursor = 0;
        private int lastRet = -1;

        public FileNameIterator(Path baseDirectoryPath) {
            this.baseDirectoryPath = baseDirectoryPath;
        }

        private void init() {
            try {
                filePaths.addAll(FileUtils.collectFilesFromDirectory(baseDirectoryPath));
            } catch (IOException e) {
                //ignore
            }
        }

        @Override
        public boolean hasNext() {
            if (lastRet < 0) {
                init();
            }
            return cursor != filePaths.size();
        }

        @Override
        public Path next() {
            int i = cursor;
            if (i >= filePaths.size()) {
                throw new NoSuchElementException();
            }
            cursor++;
            return filePaths.get(lastRet = i);
        }
    }
}
