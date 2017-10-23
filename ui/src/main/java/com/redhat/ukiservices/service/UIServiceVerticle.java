package com.redhat.ukiservices.service;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.model.endpoint.HEEndpointModel;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
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

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

    }

    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);
        configureSharedData();

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

    private void configureSharedData() {
        JsonObject config = config();
        final JsonArray urls = config.getJsonArray(CommonConstants.HE_RSS_URL_LIST_ENV);
        SharedData sd = vertx.sharedData();
        sd.<String, Map<String, HEEndpointModel>>getClusterWideMap("ui.config", res -> {
            if (res.succeeded()) {

                AsyncMap<String, Map<String, HEEndpointModel>> asyncMap = res.result();
                Map<String, HEEndpointModel> endpoints = new HashMap<String, HEEndpointModel>();

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

                asyncMap.put("cluster.endpoints.map", endpoints, resPut -> {
                    if (resPut.succeeded()) {
                        log.info("Configuration updated");
                    }
                });

            } else {
                // Something went wrong!
            }
        });

    }

    private void setEndpointStatusById(RoutingContext rc) {

        SharedData sd = vertx.sharedData();
        sd.<String, Map<String, HEEndpointModel>>getClusterWideMap("ui.config", res -> {
            if (res.succeeded()) {

                AsyncMap<String, Map<String, HEEndpointModel>> asyncMap = res.result();
                asyncMap.get("cluster.endpoints.map", resGet -> {

                    if (resGet.succeeded()) {
                        String label = rc.request().getParam("label");
                        Boolean state = Boolean.parseBoolean(rc.request().getParam("state"));
                        final String formattedLabel = label.replaceAll("%20", " ");

                        Map<String, HEEndpointModel> endpoints = resGet.result();
                        HEEndpointModel model = endpoints.get(formattedLabel);
                        model.setActive(state);

                        endpoints.put(model.getLabel(), model);

                        asyncMap.put("cluster.endpoints.map", endpoints, resPut -> {
                            if (resPut.succeeded()) {
                                vertx.eventBus().send((CommonConstants.VERTX_EVENT_BUS_MGMT_ACTION), model.toJson());
                                log.info("Configuration updated");
                            } else {
                                log.warn("Could not update configuration");
                            }

                        });

                    } else {
                        log.info("Failed to set Endpoint Status");
                    }

                });

            } else {

            }
        });

    }

    private void getEndpointStatusById(RoutingContext rc) {

        SharedData sd = vertx.sharedData();
        sd.<String, Map<String, HEEndpointModel>>getClusterWideMap("ui.config", res -> {
            if (res.succeeded()) {
                String label = rc.request().getParam("label");

                final String formattedLabel = label.replaceAll("%20", " ");

                AsyncMap<String, Map<String, HEEndpointModel>> asyncMap = res.result();
                asyncMap.get("cluster.endpoints.map", resGet -> {
                    Map<String, HEEndpointModel> endpoints = resGet.result();
                    HEEndpointModel model = endpoints.get(formattedLabel);
                    rc.response().setStatusCode(200).end(model.toJson().encodePrettily());
                });

            } else {
                log.info("Failed to get Endpoint Status By ID");
            }

        });

    }

    private void getEndpointStatus(RoutingContext rc) {

        SharedData sd = vertx.sharedData();
        sd.<String, Map<String, HEEndpointModel>>getClusterWideMap("ui.config", res -> {
            if (res.succeeded()) {

                AsyncMap<String, Map<String, HEEndpointModel>> asyncMap = res.result();

                asyncMap.get("cluster.endpoints.map", resGet -> {
                    if (resGet.succeeded()) {
                        log.info("Getting Endpoint Status");
                        JsonArray jsonEndpoints = new JsonArray();
                        Map<String, HEEndpointModel> endpoints = resGet.result();

                        for (Map.Entry<String, HEEndpointModel> entry : endpoints.entrySet()) {
                            jsonEndpoints.add(entry.getValue().toJson());
                        }
                        rc.response().setStatusCode(200).end(jsonEndpoints.encodePrettily());

                    } else {
                        log.info("Failed to get Endpoint Status");
                    }

                });

            } else {
                log.info("Failed to get Endpoint Status");
            }
        });

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
