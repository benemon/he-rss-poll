package com.redhat.ukiservices.poll;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.utils.RssUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class PollingVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger("PollingVerticle");

	private Long timerId;
	private Long pollPeriod;

	private String heRssUrls;

	private HttpClient httpClient;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);

		heRssUrls = System.getenv(CommonConstants.HE_RSS_URL_LIST_ENV) != null
				? System.getenv(CommonConstants.HE_RSS_URL_LIST_ENV) : CommonConstants.HE_RSS_URL_DEFAULT;

		pollPeriod = Long.parseLong(System.getenv(CommonConstants.POLL_PERIOD_ENV) != null
				? System.getenv(CommonConstants.POLL_PERIOD_ENV) : CommonConstants.POLL_PERIOD_DEFAULT);

		httpClient = vertx.createHttpClient();

	}

	@Override
	public void start(Future<Void> future) throws Exception {
		super.start();

		parseRssTargets();

		future.complete();

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

	private void parseRssTargets() {
		List<String> urls = Arrays.asList(heRssUrls.split(","));

		readRssFeedDetails(urls);

	}

	private void readRssFeedDetails(List<String> urls) {

		CompositeFuture.all(urls.stream().map(this::readFeed).collect(Collectors.toList()))
				.setHandler(fetchResult -> timerId = vertx.setTimer(pollPeriod, timerIdentifier -> parseRssTargets()));

	}

	private Future<Void> readFeed(String feedUrl) {
		Future<Void> future = Future.future();
		long start = System.currentTimeMillis();

		log.info("Reading RSS Feed: " + feedUrl);

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
			vertx.eventBus().publish((CommonConstants.VERTX_EVENT_BUS_HE_RSS_PROCESS), new JsonArray(entries));
			future.complete();

		} catch (JDOMException | IOException e) {
			future.fail(new RuntimeException("Exception caught when building SAX Document", e));
		}
	}

}
