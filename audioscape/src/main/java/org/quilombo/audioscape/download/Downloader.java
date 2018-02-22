package org.quilombo.audioscape.download;

import it.tadbir.Jbase;
import it.tadbir.gbid.DownloadHtml;
import it.tadbir.gbid.DownloadImage;
import it.tadbir.net.Google.Googler;
import it.tadbir.net.Google.Parser;
import it.tadbir.net.SignalHtml;
import it.tadbir.net.SignalImage;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static it.tadbir.net.Google.Constants.Search.Image.FILE_TYPE_KEY_JPG;
import static it.tadbir.net.Google.Constants.Search.Image.SIZE_KEY_MEDIUM;

public class Downloader {

    public static String SAVE_PATH = "download/";
    private ExecutorService es;
    private volatile int numHtmlGot;
    private volatile int numImgGot;
    private volatile int numImgCount;
    private boolean finished;

    public Downloader() {
    }

    public void download(final DownloaderConfig config, final ArrayList<Googler> queries) throws Exception {
        numHtmlGot = 0;
        numImgGot = 0;
        numImgCount = 0;
        finished = false;
        Jbase.initTracer();

        //init thread pool
        es = Executors.newScheduledThreadPool(config.maxThreads);

        //make save folder
        File target = new File(SAVE_PATH);
        target.mkdirs();

        //process search queries
        for (int i = 0; i < queries.size(); i++) {

            //set a download thread for search result
            DownloadHtml threadHtml = new DownloadHtml(config.userAgent);
            threadHtml.url = queries.get(i).toString();
            final String filename = queries.get(i).getPhrase();

            threadHtml.signal = new SignalHtml() {
                @Override
                public void Call(HttpEntity entity) {
                    numHtmlGot += 1;
                    Jbase.trace(String.format("Html Done (%d/%d): %s", numHtmlGot, queries.size(), filename));

                    //read image urls from html file
                    List<String> imageUrls = null;
                    try {
                        imageUrls = Parser.parseHtml(EntityUtils.toString(entity));

                        //make save search folder
                        File target = new File(SAVE_PATH + filename);
                        target.mkdirs();

                        int urlCount = Math.min(imageUrls.size(), config.maxFiles);
                        numImgCount += urlCount;

                        //create new thread for each image
                        for (int j = 0; j < urlCount; j++) {
                            final DownloadImage threadImage = new DownloadImage(config.userAgent);
                            Jbase.trace("URL=>" + imageUrls.get(j));
                            threadImage.url = imageUrls.get(j);

                            //set save name for image
                            if (config.dontRename) {
                                threadImage.filename = SAVE_PATH + filename + "/" + fileName(threadImage.url);
                            } else {
                                threadImage.filename = SAVE_PATH + filename + "/" +
                                        filename + "_" + String.valueOf(j) + "." + fileExt(threadImage.url);
                            }

                            //if verbose switch selected show image download complete

                            threadImage.signal = new SignalImage() {
                                @Override
                                public void Call() {
                                    numImgGot += 1;

                                    if (config.verbose)
                                        Jbase.trace(String.format("Image Done (%d/%d): %s", numImgGot, numImgCount, threadImage.filename));

                                    //if all the jobs done
                                    if (numImgGot == numImgCount && numHtmlGot == queries.size()) {
                                        Jbase.trace("Downloader jobe done.");
                                        finished = true;
                                        //System.exit(0);
                                    }
                                }
                            };


                            //submit image download to thread pool
                            es.submit(threadImage);
                        }


                    } catch (Exception e) {
                        Jbase.error("Query process error:", e.toString());
                    }


                    //shutdown the thread
                    if (numHtmlGot == queries.size()) {
                        if (config.verbose)
                            Jbase.trace("Queries, job closed.");

                        if (es != null)
                            es.shutdown();
                    }
                }
            };

            //submit html download to thread pool
            es.submit(threadHtml);
        }
        while (!finished) {
            Thread.sleep(500);
        }
        //es.awaitTermination(999,TimeUnit.SECONDS);
        es.shutdownNow();
    }

    public static String fileName(String s) {
        return s.substring(s.lastIndexOf('/') + 1).split("&")[0].split("\\?")[0].split("#")[0];
    }

    public static String fileExt(String s) {
        try {
            return s.substring(s.lastIndexOf('/') + 1).split("&")[0].split("\\?")[0].split("#")[0].split("_")[0].split("\\.")[1];
        } catch (Exception e) {
            return "jpg";
        }
    }

    public static void main(String[] args) throws Exception {
        Downloader down = new Downloader();
        ArrayList<Googler> queries = new ArrayList<>();
        Googler query = new Googler();
        query.setQuery("testando").setOption().setFileType(FILE_TYPE_KEY_JPG).setSize(SIZE_KEY_MEDIUM, null, 0, 0);
        queries.add(query);
        down.download(new DownloaderConfig(), queries);
        System.out.println("ended");
    }

}
