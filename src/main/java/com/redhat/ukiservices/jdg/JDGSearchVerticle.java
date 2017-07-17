package com.redhat.ukiservices.jdg;

import java.util.List;

import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Expression;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.SortOrder;

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

		ebConsumer.handler(this::handleSearch);

	}

	private void handleSearch(Message<JsonObject> message) {

		JsonObject payload = message.body();

		String action = payload.getString(CommonConstants.JDG_SEARCH_ACTION_KEY);

		String term = payload.getString("term");

		JsonArray resultsArray = new JsonArray();

		if (action.equalsIgnoreCase(CommonConstants.JDG_SEARCH_ACTION_COUNT)) {
			QueryFactory qf = Search.getQueryFactory(remoteCache);
			Query query = qf.from(HEElementModel.class).select(Expression.property(term), Expression.count(term))
					.groupBy(term).orderBy(Expression.count(term), SortOrder.DESC).maxResults(10).build();

			List<Object[]> results = query.list();

			for (Object[] result : results) {
				JsonObject object = new JsonObject();
				object.put("road", result[0]);
				object.put("count", result[1]);
				resultsArray.add(object);
			}

		}

		message.reply(resultsArray);

	}
}
