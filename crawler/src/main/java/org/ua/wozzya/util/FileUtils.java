package org.ua.wozzya.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    /**
     * Creates or overrides file with given list content.
     * At least 1 last argument is required and will be considered
     * as the name of file.
     * <p>
     * If more than 1 string where given in {@code subPaths}, last string
     * is considered as file name, others will be considered as relative subdirectories
     * to {@code rootPath} and will be automatically created if not present.
     *
     * @param lst      list to be written in file
     * @param rootPath root path
     * @param subPaths subdirectories trailing with file name
     * @param <T>      type of list content
     * @throws RuntimeException if only {@link List<T>} and {@link Path} arguments were given
     */
    public static <T> void writeListToFileOverridely(List<T> lst, Path rootPath, String... subPaths) {
        if (subPaths.length == 0) {
            throw new RuntimeException("at least file name should be provided");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(rootPath);

        String fileName = subPaths[subPaths.length - 1];

        for (int i = 0; i < subPaths.length - 1; ++i) {
            sb.append(File.separator)
                    .append(subPaths[i]);
        }
        Path pathToDirectory = Paths.get(sb.toString());
        writeListToFileOverridely(lst, pathToDirectory, fileName);
    }

    /**
     * Creates or overrides file with given list content.
     *
     * @param lst       list to be written in file
     * @param pathToDir root directory where file will be stored
     * @param fileName  name of file
     * @param <T>       type of list content
     */
    public static <T> void writeListToFileOverridely(List<T> lst, Path pathToDir, String fileName) {
        createDirectoriesIfNotExists(pathToDir);
        File file = overrideOrCreateNewFile(pathToDir, fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            lst.forEach(writer::println);
        } catch (IOException e) {
            //ignore
        }
    }

    public static File overrideOrCreateNewFile(Path directory, String fileName) {
        File file = new File(directory + File.separator + fileName);
        if (file.exists()) {
            boolean success = file.delete();
            if (!success) {
                throw new RuntimeException("an error occur when recreating " + file.getPath());
            }
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            // ignore
        }

        return file;
    }

    public static void createDirectoriesIfNotExists(Path storeDirectoryPath) {
        if (!Files.exists(storeDirectoryPath)) {
            try {
                Files.createDirectories(storeDirectoryPath);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static Path createChildDirectory(Path basePath, String childDirectoryName) {
        Path newDirectoryPath = Paths.get(basePath.toString() + File.separator + childDirectoryName);
        createDirectoriesIfNotExists(newDirectoryPath);

        return newDirectoryPath;
    }

    public static Path requireExistence(Path path) {
        if (!Files.exists(path)) {
            throw new RuntimeException();
        }
        return path;

    }

    public static Path requireExistence(Path path, String msg) {
        if (!Files.exists(path)) {
            throw new RuntimeException(msg);
        }
        return path;
    }

    public static List<Path> collectFilesFromDirectory(Path baseDirectory) throws IOException {
        List<Path> res;
        try (Stream<Path> walk = Files.walk(baseDirectory)) {
            res = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return res;
    }


    public static Path requireNonRegularFile(Path path) {
        if (Files.isRegularFile(path)) {
            throw new RuntimeException();
        }
        return path;
    }

    public static Path requireNonRegularFile(Path path, String msg) {
        if (Files.isRegularFile(path)) {
            throw new RuntimeException(msg);
        }
        return path;
    }
}

