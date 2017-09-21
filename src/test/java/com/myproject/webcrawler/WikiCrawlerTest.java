package com.myproject.webcrawler;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class WikiCrawlerTest {
    @Test
    public void callCrawler() throws IOException, ExecutionException, InterruptedException {
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        Future<WikiCrawler> wikiCrawlerFuture = executorService.submit(new WikiCrawler());
//
//        while(!wikiCrawlerFuture.isDone()) {
//            System.out.println("Retrieving...");
//            System.out.println( String.format(
//                    "WikiFuture is %s ",
//                    wikiCrawlerFuture.isDone() ? "done" : "not done"
//                    )
//            );
//            Thread.sleep(300);
//        }
//        WikiCrawler wikiCrawler = wikiCrawlerFuture.get();
//
//        System.out.println();
//        System.out.println("This is Wiki Crawler!!!");
//        wikiCrawler.printLinksFromSet();

        CrawlController crawlController = new CrawlController();
            crawlController.startCrawling();
    }

}








