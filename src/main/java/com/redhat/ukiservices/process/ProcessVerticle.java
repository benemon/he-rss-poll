package com.redhat.ukiservices.process;

import com.redhat.ukiservices.common.CommonConstants;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ProcessVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger("ProcessVerticle");

	@Override
	public void start() throws Exception {
		super.start();

		MessageConsumer<JsonArray> ebConsumer = vertx.eventBus()
				.consumer(CommonConstants.VERTX_EVENT_BUS_HE_RSS_PROCESS);
		ebConsumer.handler(payload -> {
			log.info("message");
			
			processEntries(payload.body());
		});
	}

	private void processEntries(JsonArray entries) {
		log.info(entries);
	}
}
