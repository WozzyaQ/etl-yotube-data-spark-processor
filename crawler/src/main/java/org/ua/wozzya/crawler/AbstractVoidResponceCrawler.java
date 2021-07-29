package org.ua.wozzya.crawler;

import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.task.payload.Payload;
import org.ua.wozzya.util.FileUtils;

import java.nio.file.Path;
import java.util.Objects;

public abstract class AbstractVoidResponceCrawler<T, U extends Payload>
        extends AbstractExecutorCrawler<T, U, VoidResponse> {
}
