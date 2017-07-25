package com.redhat.ukiservices.manage;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.poll.PollingVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;

public class ManagementVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(ManagementVerticle.class);

    private static Map<String, List<String>> activeVerticles;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        activeVerticles = new HashMap<>();
    }

    @Override
    public void start() throws Exception {

        super.start();

        MessageConsumer<JsonObject> ebConsumer = vertx.eventBus()
                .consumer(CommonConstants.VERTX_EVENT_BUS_MGMT_ACTION);

        ebConsumer.handler(handle -> {

            JsonObject payload = handle.body();

            if (payload.getBoolean("active")) {
                JsonObject config = new JsonObject();
                config.put(CommonConstants.HE_RSS_URL, payload.getString("url"));
                config.put(CommonConstants.HE_RSS_URL_POLL_PERIOD, payload.getLong("pollingPeriod"));

                log.info(config);
                vertx.deployVerticle(PollingVerticle.class.getName(), new DeploymentOptions().setConfig(config), res -> {
                    if (res.failed()) {
                        log.error("Initialisation failed", res.cause());
                    } else {

                        String label = payload.getString("label");

                        List<String> deploymentIds = activeVerticles.get(label);

                        if (deploymentIds == null) {
                            deploymentIds = new ArrayList<>();
                        }
                        deploymentIds.add(res.result());
                        activeVerticles.put(label, deploymentIds);
                        log.info(String.format("Initialisation succeeded for %s", res.result()));
                    }
                });

            } else {
                String label = payload.getString("label");

                List<String> deploymentIds = activeVerticles.remove(label);

                for (String deploymentId : deploymentIds) {
                    vertx.undeploy(deploymentId, res -> {
                        if (res.failed()) {
                            log.error("Teardown failed", res.cause());
                        } else {
                            log.info(String.format("Teardown succeeded for %s", deploymentId));
                        }
                    });
                }
            }
        });

    }

}
