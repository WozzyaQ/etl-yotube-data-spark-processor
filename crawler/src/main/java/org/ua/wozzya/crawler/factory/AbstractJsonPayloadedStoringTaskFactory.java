package org.ua.wozzya.crawler.factory;

import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.storage.Storage;
import org.ua.wozzya.crawler.factory.task.Task;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;

public abstract class AbstractJsonPayloadedStoringTaskFactory
        implements TaskFactory<JsonPayload, VoidResponse> {

    protected Storage storage;

    public AbstractJsonPayloadedStoringTaskFactory(Storage storage) {
        this.storage = storage;
    }

    public AbstractJsonPayloadedStoringTaskFactory() {

    }

    @Override
    public Task<VoidResponse> createTask(JsonPayload payload) {
        return prepareTask(payload);
    }

    protected abstract Task<VoidResponse> prepareTask(JsonPayload payload);

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
