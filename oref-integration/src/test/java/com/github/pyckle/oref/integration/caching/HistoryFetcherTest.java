package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.integration.config.OrefApiUris;
import com.github.pyckle.oref.integration.config.OrefConfig;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class HistoryFetcherTest {
    @Test
    void testHistory() throws Exception {
        // default config is fine
        OrefConfig orefConfig = new OrefConfig(new Properties());
        var historyApi = HistoryApiFactory.buildCachedHistoryApi(new OrefApiUris(orefConfig));
        historyApi.update();
        var history = historyApi.getCachedValue();
        System.out.println(history.toString());
    }
}
