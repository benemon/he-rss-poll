package com.redhat.ukiservices.gateway;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.utils.RssUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;

public class PollingVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger("PollingVerticle");

	private boolean online;
	private WebClient client;

	private Long pollPeriod;

	private String heRssUrl;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);

		heRssUrl = System.getenv(CommonConstants.HE_RSS_URL_ENV) != null ? System.getenv(CommonConstants.HE_RSS_URL_ENV)
				: CommonConstants.HE_RSS_URL_DEFAULT;

		pollPeriod = Long.parseLong(System.getenv(CommonConstants.POLL_PERIOD_ENV) != null
				? System.getenv(CommonConstants.POLL_PERIOD_ENV) : CommonConstants.POLL_PERIOD_DEFAULT);
	}

	@Override
	public void start() throws Exception {
		super.start();

		vertx.setPeriodic(pollPeriod, timerId -> fetchRss());
	}

	private Future<Void> fetchRss() {
		Future<Void> future = Future.future();

		log.info("Start RSS Fetch...");

		createDocument(future);

		return future;
	}

	private void createDocument(Future<Void> future) {
		final SAXBuilder sax = new SAXBuilder();
		try {
			Document doc = sax.build(new URL(heRssUrl));
			
			List<JsonObject> entries = RssUtils.toJson(doc);

			vertx.eventBus().publish((CommonConstants.VERTX_EVENT_BUS_HE_RSS_PROCESS), new JsonArray(entries));

			future.complete();

		} catch (JDOMException | IOException e) {
			future.fail("Exception caught when building SAX Document");
		}
	}

	/**
	 * 
	 * private void readFeeds(List<JsonObject> feeds) {
	 * CompositeFuture.all(feeds.stream().map(this::readFeed).collect(Collectors.toList())).setHandler(
	 * fetchResult -> timerId = vertx.setTimer(, timerIdentifier ->
	 * fetchFeeds())); }
	 * 
	 * private Future<Void> readFeed(JsonObject jsonFeed) { Future<Void> future
	 * = Future.future(); String feedUrl = jsonFeed.getString("url"); String
	 * feedId = jsonFeed.getString("hash"); URL url; try { url = new
	 * URL(feedUrl); } catch (MalformedURLException mfe) { log.warn("Invalid url
	 * : " + feedUrl, mfe); return Future.failedFuture(mfe); }
	 * 
	 * redis.getMaxDate(feedId, maxDate -> getXML(url, response -> { int status
	 * = response.statusCode(); if (status < 200 || status >= 300) { if (future
	 * != null) { future.fail(new RuntimeException( "Could not read feed " +
	 * feedUrl + ". Response status code : " + status)); } return; }
	 * response.bodyHandler(buffer -> this.parseXmlFeed(buffer, maxDate, url,
	 * feedId, future)); })); return future; }
	 * 
	 * private void parseXmlFeed(Buffer buffer, Date maxDate, URL url, String
	 * feedId, Future<Void> future) { String xmlFeed = buffer.toString("UTF-8");
	 * StringReader xmlReader = new StringReader(xmlFeed); SyndFeedInput
	 * feedInput = new SyndFeedInput(); try { SyndFeed feed =
	 * feedInput.build(xmlReader); JsonObject feedJson = FeedUtils.toJson(feed);
	 * log.info(feedJson); List<JsonObject> jsonEntries =
	 * FeedUtils.toJson(feed.getEntries(), maxDate); log.info("Insert " +
	 * jsonEntries.size() + " entries into Redis"); if (jsonEntries.isEmpty()) {
	 * future.complete(); return; } vertx.eventBus().publish(feedId, new
	 * JsonArray(jsonEntries)); redis.insertEntries(feedId, jsonEntries, handler
	 * -> { if (handler.failed()) { log.error("Insert failed", handler.cause());
	 * future.fail(handler.cause()); } else { future.complete(); } }); } catch
	 * (FeedException fe) { log.error("Exception while reading feed : " +
	 * url.toString(), fe); future.fail(fe); } }
	 * 
	 * private void getXML(URL url, Handler<HttpClientResponse> responseHandler)
	 * { client(url).get(url.getPath(),
	 * responseHandler).putHeader(HttpHeaders.ACCEPT, "application/xml").end();
	 * }
	 * 
	 * private HttpClient client(URL url) { HttpClient client =
	 * clients.get(url.getHost()); if (client == null) { client =
	 * createClient(url); clients.put(url.getHost(), client); } return client; }
	 * 
	 * private HttpClient createClient(URL url) { final HttpClientOptions
	 * options = new HttpClientOptions(); options.setDefaultHost(url.getHost());
	 * return vertx.createHttpClient(options); }
	 */

}
