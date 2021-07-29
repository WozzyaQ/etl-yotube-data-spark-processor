package org.ua.wozzya.crawler.factory.task;

import org.ua.wozzya.crawler.factory.response.Response;

import java.util.concurrent.Callable;

public interface Task<T extends Response>
        extends Callable<T>, Interruptible {
}
