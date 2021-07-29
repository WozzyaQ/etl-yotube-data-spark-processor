package org.ua.wozzya.crawler.factory;

import org.ua.wozzya.crawler.factory.response.Response;
import org.ua.wozzya.crawler.factory.task.payload.Payload;
import org.ua.wozzya.crawler.factory.task.Task;

public interface TaskFactory<T extends Payload, U extends Response> {
    Task<U> createTask(T payload);
}
