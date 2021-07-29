package org.ua.wozzya.youtube.queries;

import java.io.IOException;

public class QueryExecutionException extends IOException {
    public QueryExecutionException(Exception e) {
        super(e);
    }
}
