package com.redhat.ukiservices.poll;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.utils.RssUtils;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PollingVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(PollingVerticle.class);

    private Long timerId;
    private Long pollPeriod;

    private HttpClient httpClient;

    private String heRssUrl;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        httpClient = vertx.createHttpClient();

    }

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start();

        JsonObject config = config();
        heRssUrl = config.getString(CommonConstants.HE_RSS_URL);
        pollPeriod = config.getLong(CommonConstants.HE_RSS_URL_POLL_PERIOD);

        bootstrapRssFeed();

        future.complete();
    }

    public void bootstrapRssFeed() {

        // Do initial read...
        readFeed(heRssUrl);


        //...then set up periodic poll
        vertx.setPeriodic(pollPeriod, handler -> {
            readFeed(heRssUrl);
        });
    }

    @Override
    public void stop(Future<Void> future) {
        if (timerId != null) {
            vertx.cancelTimer(timerId);
        }

        if (httpClient != null) {
            httpClient.close();
        }

        future.complete();
    }

    private Future<Void> readFeed(String feedUrl) {
        Future<Void> future = Future.future();
        long start = System.currentTimeMillis();

        log.info("Verticle " + this.deploymentID() + " - Reading RSS Feed: " + feedUrl);

        URL url;

        try {
            url = new URL(feedUrl);
        } catch (MalformedURLException mfe) {
            log.warn("Invalid url : " + feedUrl, mfe);
            return Future.failedFuture(mfe);
        }

        this.getXml(url, response -> {
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                if (future != null) {
                    future.fail(new RuntimeException(
                            "Could not read feed " + feedUrl + ". Response status code : " + status));
                }
                return;
            }
            response.bodyHandler(buffer -> this.parseXml(buffer, future));
        });

        long stop = System.currentTimeMillis();

        log.info("Read completed in " + (stop - start) + "ms");

        return future;
    }

    private void getXml(URL url, Handler<HttpClientResponse> responseHandler) {

        httpClient.get(url.getHost(), url.getPath(), responseHandler).putHeader(HttpHeaders.ACCEPT, "application/xml")
                .end();
    }

    private void parseXml(Buffer buffer, Future<Void> future) {
        String xmlFeed = buffer.toString("UTF-8");

        final SAXBuilder sax = new SAXBuilder();

        try {

            Document doc = sax.build(new InputSource(new StringReader(xmlFeed)));
            List<JsonObject> entries = RssUtils.toJson(doc);
            vertx.eventBus().publish((CommonConstants.VERTX_EVENT_BUS_HE_RSS_JDG_PUT), new JsonArray(entries));
            future.complete();

        } catch (JDOMException | IOException e) {
            future.fail(new RuntimeException("Exception caught when building SAX Document", e));
        }
    }

}
