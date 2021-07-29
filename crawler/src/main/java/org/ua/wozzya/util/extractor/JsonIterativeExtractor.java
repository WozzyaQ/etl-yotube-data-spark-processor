package org.ua.wozzya.util.extractor;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonIterativeExtractor implements IterativeExtractor<JSONObject> {
    private final IterativeExtractor<Path> pathIterativeExtractor;

    public JsonIterativeExtractor(IterativeExtractor<Path> pathIterativeExtractor) {
        this.pathIterativeExtractor = Objects.requireNonNull(pathIterativeExtractor,
                "require not null path extractor");
    }


    @Override
    public Iterator<JSONObject> iterator() {
        return new JsonIterator(pathIterativeExtractor.iterator());
    }

    public static class JsonIterator implements Iterator<JSONObject> {
        private final Iterator<Path> pathIterator;
        private final Deque<String> jsonStringList = new LinkedList<>();

        public JsonIterator(Iterator<Path> iterator) {
            this.pathIterator = iterator;
        }

        @Override
        public boolean hasNext() {
            if (jsonStringList.size() == 0 && pathIterator.hasNext()) {
                Path nextPath = pathIterator.next();
                boolean foundEmptyFile = !tryPopulate(nextPath);

                // TODO maybe add flag to ignore empty files?
                if (foundEmptyFile) {
                    throw new RuntimeException("empty file found " + nextPath);
                }
            }

            return jsonStringList.size() != 0;
        }

        @Override
        public JSONObject next() {
            if (jsonStringList.size() == 0) {
                throw new NoSuchElementException();
            }

            return toJson(jsonStringList.pollFirst());
        }

        private JSONObject toJson(String first) {
            return new JSONObject(first);
        }

        private boolean tryPopulate(Path path) {
            boolean populated = false;
            try {
                populated = jsonStringList.addAll(Files.readAllLines(path,
                        Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return populated;
        }

    }
}
