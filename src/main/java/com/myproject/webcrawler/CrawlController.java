package com.myproject.webcrawler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CrawlController {
    private static final int THREAD_COUNT = 10000;
    private static final long SLEEP_TIME = 1000;
    // A Set of URLs that were already visited
    private Set<URL> mainURLSet = new HashSet<>();
    // List of Futures for WikiCrawler tasks
    private List<Future<WikiCrawler>> futureCrawlers = new ArrayList<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    private static int max_nesting_level;
    private static URL rootURL;

    public CrawlController() {
        max_nesting_level = 5;
        try {
            rootURL = new URL("https://en.wikipedia.org/wiki/Text_mining");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    /* Crawl through Wikipedia web pages starting at the "https://en.wikipedia.org/wiki/Text_mining" article,
       go through all the links from the text to other articles in English Wikipedia up to the fifth level
       of nesting inclusive and save the articles as .txt files. The application runs in parallel,
       pumping out the pages independently in several threads.
    */
    public void startCrawling() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        processNewURL(rootURL, 0);

        try {
            while (checkWikiCrawlers()) ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopWatch.stop();

        System.out.println("Found " + mainURLSet.size() + " urls");
        System.out.println("in " + stopWatch.getTime() / 1000 + " seconds");
    }

    // Check status of the thread pool
    private boolean checkWikiCrawlers() throws InterruptedException {
        Thread.sleep(SLEEP_TIME);
        // Create a set of WikiCrawler tasks
        Set<WikiCrawler> pageCrawlerSet = new HashSet<>();
        Iterator<Future<WikiCrawler>> iterator = futureCrawlers.iterator();

        // Iterate through the Futures of WikiCrawler tasks
        while (iterator.hasNext()) {
            Future<WikiCrawler> futureCrawler = iterator.next();
            // If the task is completed, remove it
            if (futureCrawler.isDone()) {
                iterator.remove();
                try {
                    // Add the completed task to the Set
                    pageCrawlerSet.add(futureCrawler.get());
                } catch (InterruptedException i){ // if the page is loading too slow just skip it
                } catch (ExecutionException e) {
                }
            }
        }
        // Add URLs to process if present
        for (WikiCrawler pageCrawler : pageCrawlerSet) {
            addNewURLs(pageCrawler);
        }
        // Return 'true' if there are tasks for future processing
        return (futureCrawlers.size() > 0);
    }

    // Get URLs from the page, process each new URL and increase a nesting level
    private void addNewURLs(WikiCrawler pageCrawler) {
        for (URL url : pageCrawler.getPageURLs()) {
            if (url.toString().contains("#")) {
                try {
                    url = new URL(StringUtils.substringBefore(url.toString(), "#"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            processNewURL(url, pageCrawler.getNesting_level() + 1);
        }
    }
    // Add URL to the URL Set if it passes validation
    private void processNewURL(URL url, int nesting_level) {
        if (shouldVisit(url, nesting_level)) {
            mainURLSet.add(url);

            // Create new WikiCrawler object for processing given URL and a nesting_level.
            // Then pass it as a task to the execution queue.
            WikiCrawler pageCrawler = new WikiCrawler(url, nesting_level);
            // Returns a Future representing pending completion of the task
            Future<WikiCrawler> futureCrawler = executorService.submit(pageCrawler);
            // Put the Future object into a list for further monitoring
            futureCrawlers.add(futureCrawler);
        }
    }
    // Validate URL and a nesting level: return 'true' if it is not yet in a URLSet and if the nesting level
    // is not greater than the max_nesting_level
    private boolean shouldVisit(URL url, int nesting_level) {
        if (mainURLSet.contains(url)) {
            return false;
        }
        else if (nesting_level > max_nesting_level) {
            return false;
        }
        return true;
    }

}
