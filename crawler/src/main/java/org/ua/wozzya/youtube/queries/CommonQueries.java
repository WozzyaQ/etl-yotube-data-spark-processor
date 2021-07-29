package org.ua.wozzya.youtube.queries;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import org.ua.wozzya.youtube.YouTubeEngine;

import java.io.IOException;
import java.util.List;

public class CommonQueries {
    private CommonQueries() {
    }

    public static final String CONTENT_DETAILS = "contentDetails";

    public static YouTube.Channels.List channelListWithChannelId(String channelId) {
        return YouTubeEngine.channelsList(CONTENT_DETAILS).setId(channelId).setMaxResults(1L);
    }

    public static String channelUploadsId(String channelId) throws QueryExecutionException {
        ChannelListResponse r;
        try {
            r = channelListWithChannelId(channelId).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new QueryExecutionException(e);
        }

        List<Channel> channel = r.getItems();
        if (channel.size() != 1) {
            throw new QueryAbnormalResultException("channel not found");
        }

        Channel ch = channel.get(0);
        return ch.getContentDetails().getRelatedPlaylists().getUploads();
    }

    public static YouTube.PlaylistItems.List playlistItemWithPlaylistId(String playlistId) {
        return YouTubeEngine.playlistItemList(CONTENT_DETAILS).setPlaylistId(playlistId);
    }
}
