package org.ua.wozzya.crawler.factory.response;

public class VoidResponse implements Response {
    public static final VoidResponse FINISHED = VoidResponse.finished();
    public static final VoidResponse ABNORMAL_STOP = VoidResponse.abnormal();

    public static final VoidResponse INTERRUPTED = VoidResponse.interrupted();
    public static final VoidResponse EMPTY_PAYLOAD_QUIT = VoidResponse.emptyPayloadQuit();

    private static VoidResponse emptyPayloadQuit() {
        return new VoidResponse(Status.EMPTY_PAYLOAD_QUIT);
    }


    private final Status status;

    private VoidResponse(Status status) {
        this.status = status;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    private static VoidResponse finished() {
        return new VoidResponse(Status.FINISHED);
    }

    private static VoidResponse interrupted() {
        return new VoidResponse(Status.INTERRUPTED);
    }

    private static VoidResponse abnormal() {
        return new VoidResponse(Status.ABNORMAL_STOP);
    }


}
