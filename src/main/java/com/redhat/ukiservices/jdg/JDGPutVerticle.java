package com.redhat.ukiservices.jdg;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.model.HEElementModel;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class JDGPutVerticle extends AbstractJDGVerticle {

	private static final Logger log = LoggerFactory.getLogger("JDGPutVerticle");

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
	}

	@Override
	public void start() throws Exception {
		super.start();

		MessageConsumer<JsonArray> ebConsumer = vertx.eventBus()
				.consumer(CommonConstants.VERTX_EVENT_BUS_HE_RSS_JDG_PUT);
		ebConsumer.handler(this::processEntries);
	}

	private void processEntries(Message<JsonArray> message) {

		JsonArray entries = message.body();

		for (Object obj : entries.getList()) {
			JsonObject jobj = (JsonObject) obj;
			HEElementModel model = gson.fromJson(jobj.toString(), HEElementModel.class);

			remoteCache.put(model.getGuid(), model);
		}
	}
}
