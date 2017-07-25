package com.redhat.ukiservices.service;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.model.endpoint.HEEndpointModel;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
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
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.Map;

public class UIServiceVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(UIServiceVerticle.class);

    private boolean online;

    private Map<String, HEEndpointModel> endpoints;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        endpoints = new HashMap<>();

    }

    @Override
    public void start() throws Exception {

        JsonObject config = config();
        JsonArray urls = config.getJsonArray(CommonConstants.HE_RSS_URL_LIST_ENV);
        urls.iterator().forEachRemaining(o -> {
            String url = (String) o;
            HEEndpointModel model = new HEEndpointModel();
            String label = stripAndFormatSuffix(url);
            model.setLabel(label);
            model.setUrl(url);
            model.setPollingPeriod(30000);
            model.setActive(false);
            endpoints.put(label, model);
        });

        Router router = Router.router(vertx);

        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx).register("server-online",
                fut -> fut.complete(online ? Status.OK() : Status.KO()));

        router.get("/api/health/readiness").handler(rc -> rc.response().end(CommonConstants.OK));
        router.get("/api/health/liveness").handler(healthCheckHandler);
        router.get("/api/get/count/:term").handler(this::getIncidentCountsForType);
        router.get("/api/get/count/:term/:id").handler(this::getIncidentCountsForTypeWithId);
        router.get("/api/get/detail/:term/:id").handler(this::getIncidentDetailsById);
        router.get("/api/config/endpoint/all").handler(this::getEndpointStatus);
        router.get("/api/config/endpoint/:label").handler(this::getEndpointStatusById);
        router.get("/*").handler(StaticHandler.create());

        router.post("/api/config/endpoint/:label/:state").handler(this::setEndpointStatusById);

        HttpServer server = vertx.createHttpServer().requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), ar -> {
                    online = ar.succeeded();
                });
    }

    private void setEndpointStatusById(RoutingContext rc) {
        String label = rc.request().getParam("label");
        Boolean state = Boolean.parseBoolean(rc.request().getParam("state"));

        final String formattedLabel = label.replaceAll("%20", " ");

        HEEndpointModel model = endpoints.get(formattedLabel);
        model.setActive(state);

        vertx.eventBus().publish((CommonConstants.VERTX_EVENT_BUS_MGMT_ACTION), model.toJson());
    }

    private void getEndpointStatusById(RoutingContext rc) {
        String label = rc.request().getParam("label");
        JsonArray jsonEndpoints = new JsonArray();

        final String formattedLabel = label.replaceAll("%20", " ");
        jsonEndpoints.add(endpoints.get(formattedLabel).toJson());

        rc.response().setStatusCode(200).end(jsonEndpoints.encodePrettily());

    }

    private void getEndpointStatus(RoutingContext rc) {
        String label = rc.request().getParam("label");
        JsonArray jsonEndpoints = new JsonArray();

        for (Map.Entry<String, HEEndpointModel> entry : endpoints.entrySet()) {
            jsonEndpoints.add(entry.getValue().toJson());
        }

        rc.response().setStatusCode(200).end(jsonEndpoints.encodePrettily());
    }

    private void configureEndpoints(RoutingContext rc) {
        rc.response().setStatusCode(200).end();
    }

    private void getIncidentCountsForType(RoutingContext rc) {
        String term = rc.request().getParam(CommonConstants.JDG_SEARCH_TERM_KEY);
        JsonObject payload = new JsonObject();
        payload.put(CommonConstants.JDG_SEARCH_TERM_KEY, term);
        payload.put(CommonConstants.JDG_SEARCH_ACTION_KEY, CommonConstants.JDG_SEARCH_ACTION_COUNT);

        this.sendSearchRequest(payload, rc);
    }

    private void getIncidentCountsForTypeWithId(RoutingContext rc) {
        String term = rc.request().getParam(CommonConstants.JDG_SEARCH_TERM_KEY);
        String id = rc.request().getParam(CommonConstants.JDG_SEARCH_ID_KEY);
        JsonObject payload = new JsonObject();
        payload.put(CommonConstants.JDG_SEARCH_TERM_KEY, term);
        payload.put(CommonConstants.JDG_SEARCH_ACTION_KEY, CommonConstants.JDG_SEARCH_ACTION_COUNT);
        payload.put(CommonConstants.JDG_SEARCH_ID_KEY, id);

        this.sendSearchRequest(payload, rc);

    }

    private void getIncidentDetailsById(RoutingContext rc) {
        String term = rc.request().getParam(CommonConstants.JDG_SEARCH_TERM_KEY);
        String id = rc.request().getParam(CommonConstants.JDG_SEARCH_ID_KEY);
        JsonObject payload = new JsonObject();
        payload.put(CommonConstants.JDG_SEARCH_TERM_KEY, term);
        payload.put(CommonConstants.JDG_SEARCH_ACTION_KEY, CommonConstants.JDG_SEARCH_ACTION_DETAIL);
        payload.put(CommonConstants.JDG_SEARCH_ID_KEY, id);

        this.sendSearchRequest(payload, rc);

    }

    private void sendSearchRequest(JsonObject payload, RoutingContext rc) {
        vertx.eventBus().send(CommonConstants.VERTX_EVENT_BUS_HE_RSS_JDG_SEARCH, payload, ar -> {
            if (ar.succeeded()) {
                JsonArray ja = (JsonArray) ar.result().body();
                rc.response().setStatusCode(200).end(ja.encodePrettily());
            } else {
                rc.response().setStatusCode(500).end();
            }
        });
    }

    private String stripAndFormatSuffix(String fullLabel) {
        String label = fullLabel.substring(fullLabel.lastIndexOf("/") + 1, fullLabel.lastIndexOf("."));
        label = label.replace("%20", " ");
        return WordUtils.capitalize(label);
    }
}
