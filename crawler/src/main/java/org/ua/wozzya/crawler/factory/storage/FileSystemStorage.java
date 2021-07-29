package org.ua.wozzya.crawler.factory.storage;

import org.ua.wozzya.util.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileSystemStorage implements Storage {

    private final Path rootPath;

    public FileSystemStorage(String rootDirectory) {
        this.rootPath = Paths.get(rootDirectory);
    }

    @Override
    public void storeList(List<?> obj, String fileName) {
        FileUtils.writeListToFileOverridely(obj, rootPath, fileName);
    }

    @Override
    public void storeList(List<?> obj, String... paths) {
        FileUtils.writeListToFileOverridely(obj, rootPath, paths);
    }
}
