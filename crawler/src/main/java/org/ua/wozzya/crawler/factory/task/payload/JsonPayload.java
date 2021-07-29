package org.ua.wozzya.crawler.factory.task.payload;

import org.json.JSONObject;

import java.nio.file.Path;

public class JsonPayload implements Payload {
    private final JSONObject json;

    public JsonPayload(JSONObject rawData) {
        this.json = rawData;
    }

    public JSONObject getJson() {
        return json;
    }

}
