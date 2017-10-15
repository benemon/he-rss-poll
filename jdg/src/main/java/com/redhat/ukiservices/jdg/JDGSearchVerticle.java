package com.redhat.ukiservices.jdg;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.model.element.HEElementModel;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Expression;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.SortOrder;

import java.util.List;

public class JDGSearchVerticle extends AbstractJDGVerticle {

    private static final Logger log = LoggerFactory.getLogger(JDGSearchVerticle.class);
    private static final String SEARCH_MSG_FORMAT = "SEARCH operation completed in %d milliseconds";

    private String cacheName;

    @Override
    public void init(Vertx vertx, Context context) {

        super.init(vertx, context);

        cacheName = System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) != null
                ? System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) : CommonConstants.HE_JDG_VERTX_CACHE_DEFAULT;
    }

    @Override
    public void start() throws Exception {
        super.start();

        MessageConsumer<JsonObject> ebConsumer = vertx.eventBus()
                .consumer(CommonConstants.VERTX_EVENT_BUS_HE_RSS_JDG_SEARCH);

        ebConsumer.handler(this::handleSearch);

    }

    private void handleSearch(Message<JsonObject> message) {
        long start = System.currentTimeMillis();

        vertx.executeBlocking(future -> {
            JsonObject payload = message.body();

            String action = payload.getString(CommonConstants.JDG_SEARCH_ACTION_KEY);

            String term = payload.getString(CommonConstants.JDG_SEARCH_TERM_KEY);

            String id = payload.getString(CommonConstants.JDG_SEARCH_ID_KEY);

            QueryFactory qf = Search.getQueryFactory(getCache(cacheName));
            JsonArray resultsArray = new JsonArray();
            if (action.equalsIgnoreCase(CommonConstants.JDG_SEARCH_ACTION_COUNT)) {

                Query query = null;
                if (id == null || id.isEmpty()) {
                    query = qf.from(HEElementModel.class).select(Expression.property(term), Expression.count(term))
                            .groupBy(term).orderBy(Expression.count(term), SortOrder.DESC).maxResults(10).build();

                    List<Object[]> results = query.list();

                    for (Object[] result : results) {
                        JsonObject object = new JsonObject();
                        object.put(term, result[0]);
                        object.put(action, result[1]);
                        resultsArray.add(object);
                    }

                } else {
                    query = qf.from(HEElementModel.class).select(Expression.count(term)).having(term).eq(id).maxResults(1).build();

                    List<Object[]> results = query.list();
                    for (Object[] result : results) {
                        JsonObject object = new JsonObject();
                        object.put(CommonConstants.JDG_SEARCH_ACTION_COUNT, result[0]);
                        resultsArray.add(object);
                    }

                }


            }
            if (action.equalsIgnoreCase(CommonConstants.JDG_SEARCH_ACTION_DETAIL)) {
                Query query = qf.from(HEElementModel.class).select(Expression.property("title"), Expression.property("description")).having(term).eq(id).build();

                List<Object[]> results = query.list();
                for (Object[] result : results) {
                    JsonObject object = new JsonObject();
                    object.put("title", result[0]);
                    object.put("description", result[1]);
                    resultsArray.add(object);
                }
                log.info(resultsArray);


            }

            future.complete(resultsArray);
        }, res -> {
            long stop = System.currentTimeMillis();

            log.info(String.format(SEARCH_MSG_FORMAT, (stop - start)));

            message.reply(res.result());
        });

    }
}
