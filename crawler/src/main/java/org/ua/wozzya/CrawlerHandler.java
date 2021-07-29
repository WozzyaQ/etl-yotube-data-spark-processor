package org.ua.wozzya;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.json.JSONObject;
import org.ua.wozzya.crawler.Crawler;
import org.ua.wozzya.crawler.JsonPayloadedVoidResponseCrawler;
import org.ua.wozzya.crawler.factory.AbstractJsonPayloadedStoringTaskFactory;
import org.ua.wozzya.crawler.factory.CommentGathererTaskFactory;
import org.ua.wozzya.crawler.factory.VideoGathererTaskFactory;
import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.storage.S3Storage;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;
import org.ua.wozzya.util.DateUtils;
import org.ua.wozzya.util.extractor.S3JsonExtractor;
import org.ua.wozzya.youtube.YouTubeEngine;

import java.util.Map;
import java.util.Optional;

public class CrawlerHandler {
    public void handleEvent(Map<String, String> params, Context ctx) {
        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.US_EAST_2)
                .build();


        String inputBucketName = getParamOrElseThrow(params, "input_bucket");
        String inputPrefix = getParamOrElseThrow(params, "input_prefix");

        String storeBucketName = getParamOrElseThrow(params, "store_bucket");
        String storePrefix = getParamOrElseThrow(params, "store_prefix");

        String task = getParamOrElseThrow(params, "task");
        String apiKey = getParamOrElseThrow(params, "youtube_api_key");

        YouTubeEngine.init(apiKey);

        S3Storage storage = new S3Storage(s3, storeBucketName,
                storePrefix + "/" + DateUtils.getDateStringNowOfPattern(DateUtils.yyyyMMdd));

        S3JsonExtractor s3Extractor = new S3JsonExtractor(s3, inputBucketName, inputPrefix);
        AbstractJsonPayloadedStoringTaskFactory factory = switchFactory(task);
        factory.setStorage(storage);

        Crawler<JSONObject, JsonPayload, VoidResponse> crawler = new JsonPayloadedVoidResponseCrawler()
                .crawlFrom(s3Extractor)
                .withTaskFactory(factory);

        crawler.crawl();

        storage.success();
    }

    private AbstractJsonPayloadedStoringTaskFactory switchFactory(String task) {
        switch (task) {
            case "gather-videos":
                return new VideoGathererTaskFactory();
            case "gather-comments":
                return new CommentGathererTaskFactory();

            default:
                throw new IllegalArgumentException("no such type of a task: " + task);
        }
    }


    public String getParamOrElseThrow(Map<String, String> params, String paramName) {
        return Optional.ofNullable(params.get(paramName))
                .orElseThrow(() ->
                        new IllegalArgumentException(paramName + " argument not present")
                );
    }
}
