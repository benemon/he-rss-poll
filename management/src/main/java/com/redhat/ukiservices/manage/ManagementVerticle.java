package com.redhat.ukiservices.manage;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.poll.PollingVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;

public class ManagementVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(ManagementVerticle.class);

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
    }

    @Override
    public void start() throws Exception {

        super.start();

        MessageConsumer<JsonObject> ebConsumer = vertx.eventBus()
                .consumer(CommonConstants.VERTX_EVENT_BUS_MGMT_ACTION);

        ebConsumer.handler(this::processEntries);

    }

    private void processEntries(Message<JsonObject> message) {
        JsonObject payload = message.body();

        if (payload.getBoolean("active")) {
            this.activate(payload);
        } else {
            this.deactivate(payload);
        }
    }

    private void activate(JsonObject endpoint) {
        SharedData sd = vertx.sharedData();

        sd.<String, String>getClusterWideMap("active-verticle.config", sRes -> {

            if (sRes.succeeded()) {

                AsyncMap<String, String> activeVerticles = sRes.result();


                JsonObject config = new JsonObject();
                config.put(CommonConstants.HE_RSS_URL, endpoint.getString("url"));
                config.put(CommonConstants.HE_RSS_URL_POLL_PERIOD, endpoint.getLong("pollingPeriod"));

                vertx.deployVerticle(PollingVerticle.class.getName(), new DeploymentOptions().setConfig(config), res -> {
                    if (res.failed()) {
                        log.error("Initialisation failed", res.cause());
                    } else {

                        String label = endpoint.getString("label");

                        activeVerticles.putIfAbsent(label, res.result(), putRes -> {

                            if (putRes.succeeded()) {
                                log.info(String.format("Initialisation succeeded for %s", res.result()));
                            } else {
                                log.info(String.format("Initialisation failed for %s", res.cause()));
                            }

                        });
                    }
                });
            }
        });

    }

    private void deactivate(JsonObject endpoint) {

        SharedData sd = vertx.sharedData();

        sd.<String, String>getClusterWideMap("active-verticle.config", sRes -> {

            if (sRes.succeeded()) {
                AsyncMap<String, String> activeVerticles = sRes.result();

                String label = endpoint.getString("label");

                activeVerticles.get(label, getRes -> {

                    activeVerticles.remove(label, remRes -> {
                        if (remRes.succeeded()) {
                            String deploymentId = remRes.result();

                            vertx.undeploy(deploymentId, res -> {
                                if (res.failed()) {
                                    log.error("Teardown failed", res.cause());
                                } else {
                                    log.info(String.format("Teardown succeeded for %s", deploymentId));
                                }
                            });
                        }
                    });
                });

            }
        });


    }

}
