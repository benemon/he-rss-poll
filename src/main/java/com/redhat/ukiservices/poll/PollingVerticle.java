package com.redhat.ukiservices.poll;

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

public class PollingVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger("PollingVerticle");

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

}
