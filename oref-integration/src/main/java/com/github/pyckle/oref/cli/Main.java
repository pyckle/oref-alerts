package com.github.pyckle.oref.cli;

import com.github.pyckle.oref.integration.activealerts.ActiveAlert;
import com.github.pyckle.oref.integration.caching.CachedApiResult;
import com.github.pyckle.oref.integration.caching.OrefApiCachingService;
import com.github.pyckle.oref.integration.config.OrefConfig;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * A very simple main class that updates the terminal when alerts are refreshed
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        OrefApiCachingService orefApiCachingService = new OrefApiCachingService(new OrefConfig(new Properties()));
        orefApiCachingService.waitForInitialization();
        System.out.println("Caches initialized");
        System.out.println("History: " + orefApiCachingService.getHistory());

        Instant lastRetrieved = Instant.EPOCH;
        while (true) {
            CachedApiResult<List<ActiveAlert>> alert = orefApiCachingService.getAlert();
            if (!Objects.equals(alert.lastRetrieved(), lastRetrieved)) {
                System.out.println(alert.lastRetrieved() + " " + alert.retrievedValue());
                lastRetrieved = alert.lastRetrieved();
            }
            // todo: implement notification callback rather than sleeping.
            Thread.sleep(100);
        }
    }
}
