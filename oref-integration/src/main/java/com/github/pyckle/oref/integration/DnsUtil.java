package com.github.pyckle.oref.integration;

public class DnsUtil
{
    /**
     * Disable JVM dns caching to ensure we get fresh hosts whenever we resolve oref servers.
     * <br>
     * This MUST be called on startup before any networking is done. See also: <a
     * href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/doc-files/net-properties.html">...</a>
     */
    public static void disableDnsCaching() {
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
    }
}
