package org.ua.wozzya.util;


import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


class FileUtilsTest {

    @Test
    void shouldThrowExceptionWhenPointingToRegularFile() throws IOException {
        File tempFile = File.createTempFile("pref", "suff");
        assertThrows(RuntimeException.class, () -> {
            FileUtils.requireNonRegularFile(tempFile.toPath());
        });

        tempFile.deleteOnExit();
    }

    @Test
    void shouldReturnPathWhenPointingToDirectory() throws IOException {
        Path base = Files.createTempDirectory("pref");

        Path actualPath = FileUtils.requireNonRegularFile(base);
        assertEquals(base, actualPath);
        Files.deleteIfExists(base);
    }

    @Test
    void shouldThrowExceptionWhenPathNotExists() throws IOException {
        File tempFile = File.createTempFile("pref", "suff");
        assertTrue(tempFile.delete());
        assertThrows(RuntimeException.class,
                () -> FileUtils.requireExistence(tempFile.toPath()));
    }

    @Test
    void shouldCreateDirectories() {
        Path dir1 = Paths.get("directory1");
        Path dir2 = Paths.get("directory2");
        Path path = Paths.get(dir1 + File.separator + dir2);
        FileUtils.createDirectoriesIfNotExists(path);

        boolean res = Files.exists(path);
        assertTrue(res);
        try {
            Files.deleteIfExists(path);
            Files.deleteIfExists(dir1);
        } catch (IOException e) {
            // do nothing
        }
    }

    @Test
    void shouldThrowExceptionWhenNoSubpathsSupplied() {
        assertThrows(RuntimeException.class, () ->
                FileUtils.writeListToFileOverridely(Collections.emptyList(), null));
    }

    @Test
    void shouldWriteListToFile() throws IOException {
        String[] expectedItems = {"item1", "item2", "item3"};
        List<String> listItems = Arrays.stream(expectedItems).collect(Collectors.toList());
        FileUtils.writeListToFileOverridely(listItems, Paths.get("tmp"), "tmp", "temp.txt");

        Path pathToFile = Paths.get("tmp/tmp/temp.txt");

        String[] actualItems = Files.readAllLines(pathToFile).toArray(new String[0]);

        assertArrayEquals(expectedItems, actualItems);

        Files.deleteIfExists(pathToFile);
        Files.deleteIfExists(Paths.get("tmp/tmp"));
        Files.deleteIfExists(Paths.get("tmp"));
    }

    @Test
    void shouldCreateChildDirectory() throws IOException {
        Path basePath = Paths.get("tmp");
        Path childDirectory = Paths.get("child");
        Path pathToChildDirectory = FileUtils.createChildDirectory(basePath, childDirectory.toString());

        assertTrue(Files.exists(pathToChildDirectory));
        Files.deleteIfExists(pathToChildDirectory);
        Files.deleteIfExists(basePath);
    }

    @Test
    void shouldCollectFilesFromBaseDirectory() throws IOException {
        Path rootDirectory = Files.createTempDirectory("temp");
        File childFile1 = File.createTempFile("child1", "child1", rootDirectory.toFile());
        File childFile2 = File.createTempFile("child2", "child2", rootDirectory.toFile());

        Path[] expectedPaths = new Path[]{childFile1.toPath(), childFile2.toPath()};
        Path[] actualPaths = FileUtils.collectFilesFromDirectory(rootDirectory).toArray(new Path[0]);

        assertArrayEquals(expectedPaths, actualPaths);

        childFile1.delete();
        childFile2.delete();
        Files.deleteIfExists(rootDirectory);
    }
}