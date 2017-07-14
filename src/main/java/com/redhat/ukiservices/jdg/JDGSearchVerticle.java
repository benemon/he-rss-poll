package com.redhat.ukiservices.jdg;

import java.util.List;

import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Expression;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.model.HEElementModel;

import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class JDGSearchVerticle extends AbstractJDGVerticle {

	private static final Logger log = LoggerFactory.getLogger("JDGSearchVerticle");

	@Override
	public void start() throws Exception {
		super.start();

		MessageConsumer<JsonObject> ebConsumer = vertx.eventBus()
				.consumer(CommonConstants.VERTX_EVENT_BUS_HE_RSS_JDG_SEARCH);

		ebConsumer.handler(message -> {

			this.searchCache(message);

		});

	}

	private void searchCache(Message<JsonObject> message) {

		JsonObject payload = message.body();
		
		String term = payload.getString("term");

		QueryFactory qf = Search.getQueryFactory(remoteCache);
		Query query = qf.from(HEElementModel.class).select(Expression.property(term), Expression.count(term))
				.groupBy(term).build();

		List<Object[]> results = query.list();

		JsonArray array = new JsonArray();

		for (Object[] result : results) {
			JsonObject object = new JsonObject();
			object.put("road", result[0]);
			object.put("count", result[1]);
			array.add(object);
		}
		
		message.reply(array);

	}
}
