# WebCrawler

The WebCrawler folder contains the following subfolders:
- 'config' - contains Ignite configuration files.
- 'src/main/java/com/myproject/webcrawler/' - contains a webcrawler project.
- 'src/main/java/com/myproject/wordscounter/' - contains a wordscounter project.

To start WebCrawler: run main in CrawlService.java 

To start streaming and quering words with wordscounter progect:
- start one or more nodes using NodeStartup.java
- start streaming using StreamWordsToCache.java
- start quering using QueryWords.java
