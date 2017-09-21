package com.myproject.wordscounter;

import org.apache.ignite.cache.affinity.AffinityUuid;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;

public class CacheConfig {

    // Configure streaming cache
    public static CacheConfiguration<AffinityUuid, String> wordsCache() {
        CacheConfiguration<AffinityUuid, String> cfg = new CacheConfiguration<>("words");

        // Index individual words streamed into cache (important for SQL queries)
        cfg.setIndexedTypes(AffinityUuid.class, String.class);

        // Sliding window of 1 seconds (holds streaming data for 1 second)
        cfg.setExpiryPolicyFactory(FactoryBuilder.factoryOf(new CreatedExpiryPolicy(new Duration(SECONDS, 1))));

        return cfg;
    }
}
