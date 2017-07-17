package com.redhat.ukiservices.service;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.JDGSearchVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class UserVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(UserVerticle.class);
	
	private boolean online;

	@Override
	public void start() throws Exception {

		Router router = Router.router(vertx);

		HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx).register("server-online",
				fut -> fut.complete(online ? Status.OK() : Status.KO()));

		router.get("/api/health/readiness").handler(rc -> rc.response().end(CommonConstants.OK));
		router.get("/api/health/liveness").handler(healthCheckHandler);
		router.get("/api/get/count/:term").handler(this::getIncidentCountsForType);
		router.get("/*").handler(StaticHandler.create());

		HttpServer server = vertx.createHttpServer().requestHandler(router::accept)
				.listen(config().getInteger("http.port", 8080), ar -> {
					online = ar.succeeded();
				});
	}

	private void getIncidentCountsForType(RoutingContext rc) {
		String term = rc.request().getParam(CommonConstants.JDG_SEARCH_TERM_KEY);
		JsonObject payload = new JsonObject();
		payload.put(CommonConstants.JDG_SEARCH_TERM_KEY, term);
		payload.put(CommonConstants.JDG_SEARCH_ACTION_KEY, CommonConstants.JDG_SEARCH_ACTION_COUNT);

		vertx.eventBus().send(CommonConstants.VERTX_EVENT_BUS_HE_RSS_JDG_SEARCH, payload, ar -> {
			if (ar.succeeded()) {
				JsonArray ja = (JsonArray) ar.result().body();
				rc.response().setStatusCode(200).end(ja.encodePrettily());
			} else {
				rc.response().setStatusCode(500).end();
			}
		});
	}

}
