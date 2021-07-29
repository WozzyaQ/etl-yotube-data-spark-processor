package org.ua.wozzya.crawler.factory;

import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.storage.Storage;
import org.ua.wozzya.crawler.factory.task.GatherChannelVideosTask;
import org.ua.wozzya.crawler.factory.task.Task;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;

public class VideoGathererTaskFactory
        extends AbstractJsonPayloadedStoringTaskFactory {

    public VideoGathererTaskFactory(Storage storage) {
        super(storage);
    }

    public VideoGathererTaskFactory() {
        super();
    }

    @Override
    protected Task<VoidResponse> prepareTask(JsonPayload payload) {
        return new GatherChannelVideosTask(payload, storage);
    }
}
