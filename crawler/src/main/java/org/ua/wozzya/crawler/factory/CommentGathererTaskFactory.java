package org.ua.wozzya.crawler.factory;

import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.storage.S3Storage;
import org.ua.wozzya.crawler.factory.storage.Storage;
import org.ua.wozzya.crawler.factory.task.GatherVideoCommentsTask;
import org.ua.wozzya.crawler.factory.task.Task;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;

public class CommentGathererTaskFactory
        extends AbstractJsonPayloadedStoringTaskFactory {


    public CommentGathererTaskFactory(S3Storage s3Storage) {
        super(s3Storage);
    }

    public CommentGathererTaskFactory() {
        super();
    }

    @Override
    protected Task<VoidResponse> prepareTask(JsonPayload payload) {
        return new GatherVideoCommentsTask(payload, storage);
    }
}
