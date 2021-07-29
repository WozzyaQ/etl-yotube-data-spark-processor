package org.ua.wozzya.youtube;

import com.google.api.services.youtube.YouTube;

import java.io.IOException;

public class YouTubeEngine {
    private static YouTube youTube;
    private static String apiKey;

    public static void init(String apiKey) {
        YouTubeEngine.apiKey = apiKey;

        youTube = new YouTube.Builder(
                Auth.HTTP_TRANSPORT,
                Auth.JSON_FACTORY,
                httpRequest -> {
                }
        ).setApplicationName("youtube-video-crawler").build();
    }

    public static YouTube.Search.List searchList(String part) {
        YouTube.Search.List searchList;

        try {
            searchList = youTube.search().list(part);
            searchList.setKey(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QueryCreationException("exception near YouTubeEngine#searchList");
        }

        return searchList;
    }


    public static YouTube.Channels.List channelsList(String part) {
        YouTube.Channels.List channelsList;

        try {
            channelsList = youTube.channels().list(part);
            channelsList.setKey(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QueryCreationException("exception near YouTubeEngine#channelsList");
        }

        return channelsList;
    }

    public static YouTube.PlaylistItems.List playlistItemList(String part) {
        YouTube.PlaylistItems.List playlistItemsList;

        try {
            playlistItemsList = youTube.playlistItems().list(part);
            playlistItemsList.setKey(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QueryCreationException("exception near YouTubeEngine#playlistItemList");
        }

        return playlistItemsList;
    }

    public static YouTube.Videos.List videosList(String part) {
        YouTube.Videos.List videosList;
        try {
            videosList = youTube.videos().list(part);
            videosList.setKey(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QueryCreationException("exception near YouTubeEngine#playlistItemList");
        }

        return videosList;
    }

    public static YouTube.CommentThreads.List commentThreadsList(String part) {
        YouTube.CommentThreads.List commendThreadsList;
        try {
            commendThreadsList = youTube.commentThreads().list(part);
            commendThreadsList.setKey(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
            throw new QueryCreationException("exception near YouTubeEngine#playlistItemList");
        }

        return commendThreadsList;
    }
}
