package org.ua.wozzya.crawler.factory.task;

public interface Interruptible {
    default boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }
}
