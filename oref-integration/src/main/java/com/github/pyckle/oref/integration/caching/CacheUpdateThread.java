package com.github.pyckle.oref.integration.caching;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class to handle updates to Pekudei Oref APIs. As we want to retry sooner upon a failure to access an API (to
 * avoid missing alerts during a temporary network blip for example), the Timer/ScheduledThreadPoolExecutors don't
 * work well.
 * <br>
 * This class uses a PriorityQueue to remove the next API call to make and inserts the
 */
public class CacheUpdateThread extends Thread {
    private final Runnable callbackOnUpdate;
    private final List<CachedApiCall<?>> apiCallList;
    private final CountDownLatch latch;
    private final PriorityQueue<ApiUpdateTask> priorityQueue = new PriorityQueue<>();

    public CacheUpdateThread(Runnable callbackOnUpdate, List<CachedApiCall<?>> apiCallList) {
        this.callbackOnUpdate = callbackOnUpdate;
        this.apiCallList = apiCallList;
        for (var api : apiCallList) {
            priorityQueue.offer(new ApiUpdateTask(api));
        }
        this.latch = new CountDownLatch(apiCallList.size());
        this.setName("OrefCacheUpdatingThread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        this.runUpdateLoop();
    }

    public void waitForCacheInitialization() throws InterruptedException {
        latch.await();
    }

    private void runUpdateLoop() {
        while (true) {
            var currTask = priorityQueue.poll();
            long now = System.currentTimeMillis();
            try {
                if (now < currTask.nextTimeToCall) {
                    Thread.sleep(currTask.nextTimeToCall - now);
                }
                boolean wasInitialized = currTask.toRefresh.isInitialized();
                UpdateResult successfulUpdate = currTask.toRefresh.update();
                if (successfulUpdate.success()) {
                    if (!wasInitialized && latch.getCount() > 0) {
                        latch.countDown();
                    }
                    // update timestamp
                    long nextTimeToCall = computeNextTimeToCall(currTask);
                    currTask.setNextTimeToCall(nextTimeToCall);
                    priorityQueue.add(currTask);
                    completedUpdateCallback();
                } else {
                    currTask.setNextTimeToCall(
                            System.currentTimeMillis() + currTask.toRefresh.getWaitOnFailure().toMillis());
                    priorityQueue.add(currTask);
                }

                // run all chain updates, but don't schedule.
                while (successfulUpdate.success() && successfulUpdate.hasNextCallToTrigger()) {
                    successfulUpdate = successfulUpdate.nextCallToTrigger().update();
                    if (successfulUpdate.success())
                        completedUpdateCallback();
                }
            } catch (InterruptedException ex) {
                // exit - we're done!
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void completedUpdateCallback() {
        callbackOnUpdate.run();
    }

    private static long computeNextTimeToCall(ApiUpdateTask currTask) {
        long minWait = currTask.toRefresh.getCacheDuration().toMillis();
        CachedApiResult<?> cachedValue = currTask.toRefresh.getCachedValue();
        long now = cachedValue.localTimestamp().toEpochMilli();
        long minNextTimeToCall = now + minWait;
        long maxNextTimeToCall = now + minWait * 2;
        if (cachedValue.maxAge() >= 1) {
            Instant generatedTimestamp =
                    Objects.requireNonNullElse(cachedValue.serverTimestamp(), cachedValue.localTimestamp());

            // add some randomness to multiple clients shooting the request into the server at the exact same time
            int randomness = ThreadLocalRandom.current().nextInt(1_000) - 100;
            long computedNextTimeToCall =
                    generatedTimestamp.plusSeconds(cachedValue.maxAge()).toEpochMilli() + randomness;

            // be sure to refresh not less than something unreasonably past the requested update interval
            if (computedNextTimeToCall > maxNextTimeToCall) {
                return maxNextTimeToCall;
            }

            // ensure that we don't call more than the duration
            if (computedNextTimeToCall > minNextTimeToCall) {
                return computedNextTimeToCall;
            }
        }

        return minNextTimeToCall;
    }

    static class ApiUpdateTask implements Comparable<ApiUpdateTask> {
        private final CachedApiCall<?> toRefresh;
        private long nextTimeToCall;

        public ApiUpdateTask(CachedApiCall<?> toRefresh) {
            this.toRefresh = toRefresh;
            this.nextTimeToCall = Instant.EPOCH.toEpochMilli();
        }

        public void setNextTimeToCall(long nextTimeToCall) {
            this.nextTimeToCall = nextTimeToCall;
        }

        @Override
        public int compareTo(ApiUpdateTask o) {
            return Long.compare(nextTimeToCall, o.nextTimeToCall);
        }
    }
}
