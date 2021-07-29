package org.ua.wozzya.crawler.factory.storage;

import com.amazonaws.services.s3.AmazonS3;

import java.util.List;

public class S3Storage implements Storage {

    private final AmazonS3 s3Client;
    private final String bucketName;
    private final String objectSeparator;
    private final String prefix;

    public S3Storage(AmazonS3 s3client, String bucketName, String prefix, String objectSeparator) {
        this.s3Client = s3client;
        this.bucketName = bucketName;
        this.objectSeparator = objectSeparator;
        this.prefix = prefix;
    }

    public S3Storage(AmazonS3 s3Client, String bucketName, String prefix) {
        this(s3Client, bucketName, prefix, "/");
    }

    @Override
    public void storeList(List<?> obj, String fileName) {
        StringBuilder sb = new StringBuilder();
        obj.forEach(x -> {
            sb.append(x.toString());
            sb.append(System.lineSeparator());
        });


        s3Client.putObject(bucketName, prefix + objectSeparator + fileName, sb.toString());
    }

    @Override
    public void storeList(List<?> obj, String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            sb.append(path)
                    .append(objectSeparator);
        }
        if (paths.length != 0) {
            sb.delete(sb.lastIndexOf(objectSeparator), sb.length());
        }

        storeList(obj, sb.toString());
    }

    public void success() {
        s3Client.putObject(bucketName, prefix + objectSeparator + "_SUCCESS", "");
    }
}
