package org.ua.wozzya.util.extractor;

import com.amazonaws.services.s3.AmazonS3;
import org.json.JSONObject;
import org.ua.wozzya.util.S3Utils;

import java.util.*;

public class S3JsonExtractor implements IterativeExtractor<JSONObject> {

    private final AmazonS3 s3Client;
    private final String bucketName;
    private final String prefix;


    public S3JsonExtractor(AmazonS3 s3Client, String bucketName, String prefix) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.prefix = prefix;
    }


    @Override
    public Iterator<JSONObject> iterator() {
        return new S3JsonIterator();
    }

    private class S3JsonIterator implements Iterator<JSONObject> {
        private final Deque<String> s3ObjectsNames;
        private List<String> currentJsonLines = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public S3JsonIterator() {
            s3ObjectsNames = (Deque<String>) S3Utils.listObjects(s3Client, bucketName, prefix);
        }

        @Override
        public boolean hasNext() {
            return s3ObjectsNames.peek() != null || !currentJsonLines.isEmpty();
        }

        @Override
        public JSONObject next() {
            if (s3ObjectsNames.size() <= 0 && currentJsonLines.isEmpty()) {
                throw new NoSuchElementException();
            }

            if (currentJsonLines.isEmpty()) {
                String s3ObjectKey = s3ObjectsNames.pollFirst();
                currentJsonLines = S3Utils.getObjectLines(s3Client, bucketName, s3ObjectKey);
                if (currentJsonLines.isEmpty()) {
                    System.out.println("warn: got 0 lines when extracting S3's object lines. Returning dummy object");
                    return new JSONObject();
                }
            }

            String currentJsonLine = currentJsonLines.remove(currentJsonLines.size() - 1);

            return new JSONObject(currentJsonLine);
        }
    }
}
