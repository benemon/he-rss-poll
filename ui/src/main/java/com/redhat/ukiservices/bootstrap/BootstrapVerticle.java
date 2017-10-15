package com.redhat.ukiservices.bootstrap;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.service.UIServiceVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class BootstrapVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(BootstrapVerticle.class);

    @Override
    public void start() throws Exception {

        super.start();

        String heRssUrls = System.getenv(CommonConstants.HE_RSS_URL_LIST_ENV) != null
                ? System.getenv(CommonConstants.HE_RSS_URL_LIST_ENV) : CommonConstants.HE_RSS_URL_DEFAULT;

        List<String> urls = Arrays.asList(heRssUrls.split(","));

        JsonObject config = new JsonObject();
        config.put(CommonConstants.HE_RSS_URL_LIST_ENV, urls);

        Vertx.clusteredVertx(new VertxOptions(), clusterRes -> {
            if (clusterRes.succeeded()) {
                Vertx vertx = clusterRes.result();

                vertx.deployVerticle(UIServiceVerticle.class.getName(), new DeploymentOptions().setConfig(config),
                        res -> {
                            if (res.failed()) {
                                log.error("Initialisation failed", res.cause());
                            }

                        });
            } else {
                // failed!
            }
        });

    }

}
