package org.ua.wozzya.crawler.factory.storage;

import java.util.List;

public interface Storage {
    void storeList(List<?> obj, String fileName);
    void storeList(List<?> obj, String... paths);
}
