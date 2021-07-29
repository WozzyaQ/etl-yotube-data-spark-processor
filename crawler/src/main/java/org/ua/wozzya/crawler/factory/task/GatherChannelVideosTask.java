package org.ua.wozzya.crawler.factory.task;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import org.json.JSONObject;
import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.storage.Storage;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;
import org.ua.wozzya.util.FileUtils;
import org.ua.wozzya.youtube.queries.CommonQueries;
import org.ua.wozzya.youtube.queries.QueryExecutionException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GatherChannelVideosTask
        implements Task<VoidResponse> {

    private final JSONObject json;
    private final Storage storage;

    public GatherChannelVideosTask(JsonPayload json, Storage storage) {
        this.json = json.getJson();
        this.storage = storage;
    }

    @Override
    public VoidResponse call() {
        if(json.isEmpty()) {
            return VoidResponse.EMPTY_PAYLOAD_QUIT;
        }

        String channelId = json.getString("channelId");
        String channelName = json.getString("channelName");
        // get uploads id related to channel
        String uploads;
        try {
            uploads = CommonQueries.channelUploadsId(channelId);
        } catch (QueryExecutionException e) {
            e.printStackTrace();
            return VoidResponse.ABNORMAL_STOP;
        }

        List<PlaylistItem> videoData = new ArrayList<>();
        YouTube.PlaylistItems.List q = CommonQueries.playlistItemWithPlaylistId(uploads);
        q.setMaxResults(10L);

        String nextPageToken = null;
        do {
            if (isInterrupted()) {
                return VoidResponse.INTERRUPTED;
            }
            try {
                if (nextPageToken != null) {
                    q.setPageToken(nextPageToken);
                }
                PlaylistItemListResponse response = q.execute();

                // TODO consider filtering by date, full scan is not good though


                List<PlaylistItem> result = response.getItems();
                result.forEach(x -> {
                    x.set("channelId", channelId);
                    x.set("channelName", channelName);
                });


                videoData.addAll(result);
                nextPageToken = response.getNextPageToken();
            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        } while (nextPageToken != null);


        storage.storeList(videoData, json.getString("channelName"));
        return VoidResponse.FINISHED;
    }
}
