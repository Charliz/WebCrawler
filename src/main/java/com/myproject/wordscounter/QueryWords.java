package com.myproject.wordscounter;


import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.affinity.AffinityUuid;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.*;

public class QueryWords {

    public static void main(String[] args) throws Exception {
        // Mark this cluster member as client.
        Ignition.setClientMode(true);

        // Start a Client Node
        try (Ignite ignite = Ignition.start("config/example-ignite.xml")) {
            // Checks if Server nodes not found
            if (!ExamplesUtils.hasServerNodes(ignite))
                return;
            CacheConfiguration<AffinityUuid, String> cacheConfig = CacheConfig.wordsCache();

            // The cache is configured with sliding window holding 1 second of the streaming data.
            try (IgniteCache<AffinityUuid, String> theCache = ignite.getOrCreateCache(cacheConfig)) {
                // Type String becomes a table so we use it as a table name
                // Ignite provides its own field name -> _val
                SqlFieldsQuery getAllWords = new SqlFieldsQuery("select _val, count(_val) as cnt from String " +
                        "group by _val " +
                        "order by cnt desc ",
                        true
                );

                // Select top 10 words.
                SqlFieldsQuery getTop10Words = new SqlFieldsQuery("select _val, count(_val) as cnt from String " +
                        "group by _val " +
                        "order by cnt desc " +
                        "limit 10",
                        true
                );

                while (true) {
                    // Execute SQL queries.
                    List<List<?>> top10Words = theCache.query(getTop10Words).getAll();
                    List<List<?>> allWords = theCache.query(getAllWords).getAll();

                    System.out.println("TOP 10 Words! ->");
                    ExamplesUtils.printQueryResults(top10Words);
                    System.out.println();
//                    System.out.println("All Words! ->");
//                    ExamplesUtils.printQueryResults(allWords);

                    // Iterating through all sub-lists of words and counting the Frequency of occurrence for each word
                    for (List<?> wordList : allWords) {
                        // Get a word name
                        String word = String.valueOf(wordList.get(0));
                        // Get count for this word
                        Object wordCount = wordList.get(1);
                        int count = Integer.parseInt(wordCount.toString());

                        // Number of all words
                        int totalWordsNumber = allWords.size();

                        // Count the Frequency of occurrence for each word
                        double frequency = (double) count / totalWordsNumber;
                        System.out.println("Word: " + word + "; Frequency: " + frequency * 100 + "%");
                    }

                    // Execute queries every 5 seconds
                    Thread.sleep(5000);
                }
            } finally {
                // Distributed cache could be removed from cluster only by #destroyCache() call.
                ignite.destroyCache(cacheConfig.getName());
            }
        }
    }
}
