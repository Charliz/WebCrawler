package com.myproject.webcrawler;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

public class WikiCrawler implements Callable<WikiCrawler> {
    private static final int TIMEOUT_MILLIS = 6000;
    // A Set for storing only unique URLs
    private Set<URL> urlSet = new HashSet<>();
    private List<String> articles = new ArrayList<>();
    // Store page links as URLs
    private URL wiki_url;
    private int nesting_level;

    public WikiCrawler(URL wiki_url, int nesting_level) {
        this.wiki_url = wiki_url;
        this.nesting_level = nesting_level;
    }

    @Override
    public WikiCrawler call() throws Exception {
        Document document = Jsoup.parse(wiki_url, TIMEOUT_MILLIS);
        // Select paragraphs from a page
        Elements paragraph = document.select(".mw-content-ltr p, .mw-content-ltr li");
        // Select links from a page and add them to the Set
        Elements linksOnPage = document.select("#bodyContent a[href^=\"/wiki/\"]:not([href*=\":\"])");
        addLinksToSet(linksOnPage);
        System.out.println("Visiting (" + nesting_level + "): " + wiki_url.toString());
        // Save article text from paragraphs into the list of articles
        articles = getTextFromPage(paragraph);
        String articlePath = wiki_url.getPath();
        String articleFileName = articlePath.substring(articlePath.lastIndexOf('/') + 1);
        // Write the article into a .txt file and give it a name of the Wiki article
        writeArticlesToFile("src/main/resources/wiki_text_pages/" + articleFileName + ".txt");
        return this;
    }
    // Add links found on a page to the set
    private void addLinksToSet(Elements linksOnPage) {
        for (Element link : linksOnPage) {
            String href = link.attr("href");
            if (StringUtils.isBlank(href) || href.startsWith("#")) {
                continue;
            }
            try {
                URL theNextUrl = new URL(wiki_url, href);
                urlSet.add(theNextUrl);
            } catch (MalformedURLException e) {

            }
        }
    }
    // Loop through paragraphs on a page and form the complete article out of them
    private List<String> getTextFromPage(Elements paragraph) {
        Element firstParagraph = paragraph.first();
        Element lastParagraph = paragraph.last();
        List<String> theArticle = new ArrayList<>();

        Element par = firstParagraph;
        theArticle.add(par.text());
        for (int i = 1; par != lastParagraph; i++) {
            par = paragraph.get(i);
            theArticle.add(par.text());
        }
        return theArticle;
    }

    private void writeArticlesToFile(String path) throws IOException {
        FileUtils.writeLines(new File(path), this.articles);
    }

    Set<URL> getPageURLs() {
        return urlSet;
    }

    int getNesting_level() {
        return nesting_level;
    }

//    private void printLinksFromSet() {
//        for (URL theUrl : urlSet) {
//            System.out.println("The links: " + theUrl.toString());
//        }
//    }


//  System.out.println("link : " + link.attr("abs:href"));
//  System.out.println("text : " + body.text());

}







