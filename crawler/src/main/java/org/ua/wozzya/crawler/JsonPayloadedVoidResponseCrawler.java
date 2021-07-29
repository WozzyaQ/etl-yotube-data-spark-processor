package org.ua.wozzya.crawler;

import org.json.JSONObject;
import org.ua.wozzya.crawler.factory.response.VoidResponse;
import org.ua.wozzya.crawler.factory.task.payload.JsonPayload;

import java.util.List;
import java.util.concurrent.Future;

public class JsonPayloadedVoidResponseCrawler
        extends AbstractVoidResponceCrawler<JSONObject, JsonPayload> {

    @Override
    protected void postprocessFutures(List<Future<VoidResponse>> futures) {
        // do nothing
    }

    @Override
    protected JsonPayload constructPayload(JSONObject rawData) {
        return new JsonPayload(rawData);
    }
}
