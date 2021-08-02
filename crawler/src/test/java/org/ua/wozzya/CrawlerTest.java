package org.ua.wozzya;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemContentDetails;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ua.wozzya.crawler.factory.AbstractJsonPayloadedStoringTaskFactory;
import org.ua.wozzya.crawler.factory.VideoGathererTaskFactory;
import org.ua.wozzya.crawler.factory.storage.S3Storage;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;
import org.ua.wozzya.util.DateUtils;
import org.ua.wozzya.util.S3Utils;
import org.ua.wozzya.util.extractor.S3JsonExtractor;
import org.ua.wozzya.youtube.queries.CommonQueries;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CrawlerTest {
    static final String INPUT_PAYLOAD = "{\"channelName\": \"sample\", \"channelId\": \"sample\"}\n";
    static final String expectedData = "{\"contentDetails\":{\"videoId\":\"UvUkpduo6uE\",\"videoPublishedAt\":\"2021-07-22T14:44:46.000Z\"},\"etag\":\"gH9TPgr_w4mIZWqomNucBOWZzQg\",\"id\":\"VVVSUzREdk85WDdxYXFWWVVXMl9kd093LlV2VWtwZHVvNnVF\",\"kind\":\"youtube#playlistItem\",\"channelId\":\"sample\",\"channelName\":\"sample\"}";

    static final PlaylistItemContentDetails CONTENT_DETAILS = new PlaylistItemContentDetails();
    static final String ETAG;
    static final String ID;
    static final String KIND;

    static {
        CONTENT_DETAILS.setVideoId("UvUkpduo6uE");
        CONTENT_DETAILS.setVideoPublishedAt(DateTime.parseRfc3339("2021-07-22T14:44:46.000Z"));
        ETAG = "gH9TPgr_w4mIZWqomNucBOWZzQg";
        ID = "VVVSUzREdk85WDdxYXFWWVVXMl9kd093LlV2VWtwZHVvNnVF";
        KIND = "youtube#playlistItem";
    }

    @Test
    void shouldStoreToS3() {

        S3Mock api = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
        api.start();

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration("http://localhost:8001", "us-east-2");


        AmazonS3 s3 = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();


        String inputBucketName = "input-bucket";
        String inputPrefix = "input/channels";
        String outputBucketName = "store-bucket";
        String outputPrefix = "channel-videos";

        s3.createBucket(inputBucketName);
        s3.createBucket(outputBucketName);
        s3.putObject(inputBucketName, inputPrefix, INPUT_PAYLOAD);


        S3Storage storage = new S3Storage(s3, outputBucketName,
                outputPrefix + "/" + DateUtils.getDateStringNowOfPattern(DateUtils.yyyyMMdd));

        S3JsonExtractor s3Extractor = new S3JsonExtractor(s3, inputBucketName, inputPrefix);
        AbstractJsonPayloadedStoringTaskFactory factory = new VideoGathererTaskFactory(storage);

        try (MockedStatic<CommonQueries> queries = Mockito.mockStatic(CommonQueries.class)) {
            queries.when(() -> CommonQueries.channelUploadsId("sample"))
                    .thenReturn("test-id");


            YouTube.PlaylistItems.List mockedPlaylistItems = Mockito.mock(YouTube.PlaylistItems.List.class);
            PlaylistItemListResponse mockedPlaylistItemsResponse = Mockito.mock(PlaylistItemListResponse.class);
            PlaylistItem playlistItem = new PlaylistItem();

            playlistItem.setFactory(new JacksonFactory());
            playlistItem.setContentDetails(CONTENT_DETAILS);
            playlistItem.setId(ID);
            playlistItem.setEtag(ETAG);
            playlistItem.setKind(KIND);

            Mockito.when(mockedPlaylistItemsResponse.getItems()).thenReturn(Collections.singletonList(playlistItem));
            Mockito.when(mockedPlaylistItems.execute()).thenReturn(mockedPlaylistItemsResponse);


            queries.when(() -> CommonQueries.playlistItemWithPlaylistId("test-id"))
                    .thenReturn(mockedPlaylistItems);


            // call single task manually because mocked static
            // classes are not reachable from other threads (executor in crawler)
            factory.createTask(new JsonPayload(s3Extractor.iterator().next()))
                    .call();

            storage.success();
        } catch (Exception e) {
            e.printStackTrace();
        }


        String objectKey = outputPrefix + "/" + DateUtils.getDateStringNowOfPattern(DateUtils.yyyyMMdd) + "/";

        String actualData = S3Utils.getObjectLines(s3, outputBucketName, objectKey + "sample")
                .stream().collect(Collectors.joining(System.lineSeparator()));

        assertTrue(s3.doesObjectExist(outputBucketName, objectKey + "_SUCCESS"));
        assertEquals(expectedData, actualData);

        api.shutdown();
    }
}
