package org.ua.wozzya.crawler;

import org.ua.wozzya.crawler.factory.TaskFactory;
import org.ua.wozzya.crawler.factory.response.Response;
import org.ua.wozzya.crawler.factory.task.payload.Payload;
import org.ua.wozzya.util.extractor.IterativeExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class AbstractExecutorCrawler<T, U extends Payload, F extends Response>
        implements Crawler<T, U, F> {

    protected IterativeExtractor<T> extractor;
    protected TaskFactory<U, F> taskFactory;
    protected int threadAmount = Runtime.getRuntime().availableProcessors();

    @Override
    public Crawler<T, U, F> crawlFrom(IterativeExtractor<T> iterativeExtractor) {
        extractor = Objects.requireNonNull(iterativeExtractor);
        return this;
    }


    @Override
    public Crawler<T, U, F> withTaskFactory(TaskFactory<U, F> factory) {
        taskFactory = Objects.requireNonNull(factory);
        return this;
    }

    @Override
    public Crawler<T, U, F> withThreadAmount(int threadAmount) {
        if (threadAmount <= 0) {
            throw new IllegalArgumentException();
        }
        this.threadAmount = threadAmount;
        return this;
    }

    @Override
    public void crawl() {
        setup();
        ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<F>> futures = new ArrayList<>();
        for (T entry : extractor) {
            U payload = constructPayload(entry);
            Future<F> future = ex.submit(
                    taskFactory.createTask(payload)
            );

            futures.add(future);
        }
        ex.shutdown();

        boolean terminated = false;
        try {
            terminated = ex.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (terminated) {
            postprocessFutures(futures);
        } else {
            ex.shutdownNow();
        }
    }

    protected void setup() {
    }

    protected void cancelFutures(List<Future<F>> futures) {
        for (Future<F> future : futures) {
            future.cancel(true);
        }
    }

    protected abstract void postprocessFutures(List<Future<F>> futures);

    protected abstract U constructPayload(T rawData);
}
