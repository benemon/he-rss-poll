package com.redhat.ukiservices.jdg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class JDGPutVerticle extends AbstractJDGVerticle {

    private static final Logger log = LoggerFactory.getLogger(JDGPutVerticle.class);

    private static final String PUT_MSG_FORMAT = "PUT operation completed in %d milliseconds";

    private Gson gson;
    private String cacheName;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        gson = new GsonBuilder().setDateFormat("EEE, d MMM yyyy HH:mm:ss z").create();
        cacheName = System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) != null
                ? System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) : CommonConstants.HE_JDG_VERTX_CACHE_DEFAULT;
    }

    @Override
    public void start() throws Exception {
        super.start();

        MessageConsumer<JsonArray> ebConsumer = vertx.eventBus()
                .consumer(CommonConstants.VERTX_EVENT_BUS_HE_RSS_JDG_PUT);
        ebConsumer.handler(this::processEntries);
    }

    private void processEntries(Message<JsonArray> message) {

        long start = System.currentTimeMillis();
        vertx.executeBlocking(future -> {

            JsonArray entries = message.body();

            for (Object obj : entries.getList()) {
                JsonObject jobj = (JsonObject) obj;
                HEElementModel model = gson.fromJson(jobj.toString(), HEElementModel.class);

                this.getCache(cacheName).put(model.getGuid(), model);
            }

            future.complete();
        }, res -> {
            long stop = System.currentTimeMillis();
            log.info(String.format(PUT_MSG_FORMAT, stop - start));
        });
    }
}
