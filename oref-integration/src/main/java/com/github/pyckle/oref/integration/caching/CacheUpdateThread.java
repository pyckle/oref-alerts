package com.github.pyckle.oref.integration.caching;

import java.time.Instant;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;

/**
 * A class to handle updates to Pekudei Oref APIs. As we want to retry sooner upon a failure to access an API (to
 * avoid missing alerts during a temporary network blip for example), the Timer/ScheduledThreadPoolExecutors don't
 * work well.
 * <br>
 * This class uses a PriorityQueue to remove the next API call to make and inserts the
 */
public class CacheUpdateThread extends Thread {
    private final List<CachedApiCall<?>> apiCallList;
    private final CountDownLatch latch;
    private final PriorityQueue<ApiUpdateTask> priorityQueue = new PriorityQueue<>();

    public CacheUpdateThread(List<CachedApiCall<?>> apiCallList) {
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
            var nextTask = priorityQueue.poll();
            long now = System.currentTimeMillis();
            try {
                if (now < nextTask.nextTimeToCall) {
                    Thread.sleep(nextTask.nextTimeToCall - now);
                }
                boolean wasInitialized = nextTask.toRefresh.isInitializedYet();
                boolean successfulUpdate = nextTask.toRefresh.update();
                if (successfulUpdate) {
                    if (!wasInitialized) {
                        latch.countDown();
                    }
                    nextTask.setNextTimeToCall(nextTask.toRefresh.getCachedValue().lastRetrieved().toEpochMilli() +
                            nextTask.toRefresh.getCacheDuration().toMillis());
                    priorityQueue.add(nextTask);
                } else {
                    nextTask.setNextTimeToCall(
                            System.currentTimeMillis() + nextTask.toRefresh.getWaitOnFailure().toMillis());
                    priorityQueue.add(nextTask);
                }
            } catch (InterruptedException ex) {
                // exit - we're done!
                Thread.currentThread().interrupt();
                return;
            }
        }
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
