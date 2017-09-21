package com.myproject.wordscounter;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.affinity.AffinityUuid;

import java.io.*;
import java.util.*;

public class StreamWordsToCache {
    public static void main(String[] args) throws Exception {
        // Mark the cluster member as a Client
        Ignition.setClientMode(true);

        // Start a 'Client' Node
        try (Ignite ignite = Ignition.start("config/example-ignite.xml")) {
            // Checks if Server nodes not found
            if (!ExamplesUtils.hasServerNodes(ignite))
                return;

            // If cache doesn't exist, create it within the cluster, otherwise use the existing one
            IgniteCache<AffinityUuid, String> theCache = ignite.getOrCreateCache(CacheConfig.wordsCache());

            // Create Streamers for the cache
            try (IgniteDataStreamer<AffinityUuid, String> theStreamer = ignite.dataStreamer(theCache.getName())) {

                //Stream words from Wikipedia articles
                while (true) {
                    File directory = new File("src/main/resources/wiki_text_pages/");
                    if (directory.listFiles() != null) {
                        List<File> filesInDir = new ArrayList<>(Arrays.asList(directory.listFiles()));
                        for (File file : filesInDir) {
                            System.out.println("Start reading file : " + file.getName());
                            // Read files line by line
                            try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file))) {
                                for (String line = lineNumberReader.readLine(); line != null; line = lineNumberReader.readLine()) {
                                    Set<String> uniqueStringSet = uniqueWords(line);
                                    for (String word : uniqueStringSet) {
                                        if (!word.isEmpty() && word.matches("(?!(?:that|with|from))\\b(?<!\\b[-.])[^\\d\\W]{4,}+\\b(?![-.]\\b)"))
                                            // Stream words into Ignite
                                            // Unique key (AffinityUuid) is created for each word
                                            // AffinityUuid ensures that identical words are processed on the same cluster node
                                            // in order to process them faster
                                            theStreamer.addData(new AffinityUuid(word), word);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // The method removes duplicates from a string
    private static Set<String> uniqueWords(String line) {
        String[] words = line.split(" ");
        Set<String> wordsHashSet = new HashSet<>();
        for (String word : words) {
            // Check for duplicates
            if (wordsHashSet.contains(word.toLowerCase())) continue;
            wordsHashSet.add(word.toLowerCase());
        }
        return wordsHashSet;
    }

}
