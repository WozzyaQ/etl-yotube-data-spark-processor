package org.ua.wozzya.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class S3Utils {

    public static List<String> listObjects(AmazonS3 client, String bucketName, String prefix) {
        List<String> objectsKeys = new LinkedList<>();

        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix)
                .withMaxKeys(20);
        ListObjectsV2Result result;

        do {
            result = client.listObjectsV2(req);

            List<String> keys = result.getObjectSummaries()
                    .stream()
                    .map(S3ObjectSummary::getKey)
                    .collect(Collectors.toList());

            objectsKeys.addAll(keys);


            String nextToken = result.getNextContinuationToken();
            req.setContinuationToken(nextToken);
        } while (result.isTruncated());

        return objectsKeys;
    }

    public static List<String> getObjectLines(AmazonS3 client, String bucketName, String key) {
        GetObjectRequest req = new GetObjectRequest(bucketName, key);
        S3Object s3Object = client.getObject(req);

        List<String> lines;

        try (InputStream is = s3Object.getObjectContent();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            lines = reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
        return lines;
    }

}
