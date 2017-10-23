package com.redhat.ukiservices.bootstrap;

import com.redhat.ukiservices.jdg.JDGPutVerticle;
import com.redhat.ukiservices.jdg.JDGSearchVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class BootstrapVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(BootstrapVerticle.class);

    @Override
    public void start() throws Exception {

        super.start();
        Vertx.clusteredVertx(new VertxOptions(), clusterRes -> {
            if (clusterRes.succeeded()) {
                Vertx vertx = clusterRes.result();

                vertx.deployVerticle(JDGPutVerticle.class.getName(), res -> {
                    if (res.failed()) {
                        log.error("Initialisation failed", res.cause());
                    }

                });

                vertx.deployVerticle(JDGSearchVerticle.class.getName(), res -> {
                    if (res.failed()) {
                        log.error("Initialisation failed", res.cause());
                    }

                });
            }
        });
    }

}
