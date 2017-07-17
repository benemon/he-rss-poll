package com.redhat.ukiservices.jdg;

import java.util.List;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Expression;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.SortOrder;

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

public class JDGSearchVerticle extends AbstractJDGVerticle {

	private static final Logger log = LoggerFactory.getLogger(JDGSearchVerticle.class);
	private static final String SEARCH_MSG = "Search for %s:%s with %d results completed in %d milliseconds";

	private String cacheName;

	private RemoteCache<String, HEElementModel> cache;

	@Override
	public void init(Vertx vertx, Context context) {

		super.init(vertx, context);

		cacheName = System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) != null
				? System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) : CommonConstants.HE_JDG_VERTX_CACHE_DEFAULT;

		cache = getCache(cacheName);
	}

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

		String term = payload.getString(CommonConstants.JDG_SEARCH_TERM_KEY);

		JsonArray resultsArray = new JsonArray();
		long start = System.currentTimeMillis();
		if (action.equalsIgnoreCase(CommonConstants.JDG_SEARCH_ACTION_COUNT)) {
			QueryFactory qf = Search.getQueryFactory(cache);
			Query query = qf.from(HEElementModel.class).select(Expression.property(term), Expression.count(term))
					.groupBy(term).orderBy(Expression.count(term), SortOrder.DESC).maxResults(10).build();

			List<Object[]> results = query.list();

			for (Object[] result : results) {
				JsonObject object = new JsonObject();
				object.put(term, result[0]);
				object.put(action, result[1]);
				resultsArray.add(object);
			}

		}
		long stop = System.currentTimeMillis();
		log.info(String.format(SEARCH_MSG, term, action, resultsArray.size(), (stop - start)));
		message.reply(resultsArray);

	}
}
