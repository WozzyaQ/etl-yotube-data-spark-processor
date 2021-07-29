package org.ua.wozzya.crawler.factory.task;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import org.json.JSONObject;
import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.storage.Storage;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;
import org.ua.wozzya.util.FileUtils;
import org.ua.wozzya.youtube.YouTubeEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GatherVideoCommentsTask implements Task<VoidResponse> {
    private final JSONObject json;
    private final Storage storage;

    public GatherVideoCommentsTask(JsonPayload payload, Storage storage) {
        this.json = payload.getJson();
        this.storage = storage;
    }

    @Override
    public VoidResponse call() {
        if(json.isEmpty()) {
            return VoidResponse.EMPTY_PAYLOAD_QUIT;
        }

        // TODO consider protecting against JSONException
        String videoId = json.getJSONObject("contentDetails").getString("videoId");
        String channelName = json.getString("channelName");
        String channelId = json.getString("channelId");
        List<CommentThread> comments = new ArrayList<>();


        YouTube.CommentThreads.List q =
                YouTubeEngine.commentThreadsList("snippet")
                        .setVideoId(videoId);


        String nextPageToken = null;
        // TODO make do-while loop general
        do {
            if(isInterrupted()) {
                return VoidResponse.INTERRUPTED;
            }

            if (nextPageToken != null) {
                q.setPageToken(nextPageToken);
            }

            try {
                CommentThreadListResponse response = q.execute();
                List<CommentThread> commentThreads = response.getItems();
                commentThreads.forEach(x -> x.set("channelId", channelId));
                nextPageToken = response.getNextPageToken();
                comments.addAll(commentThreads);

            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        } while (nextPageToken != null);


        storage.storeList(comments, channelName, videoId);
        return VoidResponse.FINISHED;
    }
}
