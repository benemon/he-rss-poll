package com.redhat.ukiservices.bootstrap;

import com.redhat.ukiservices.gateway.PollingVerticle;
import com.redhat.ukiservices.process.ProcessVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class BootstrapVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger("BootstrapVerticle");

	@Override
	public void start() throws Exception {

		super.start();

		vertx.deployVerticle(PollingVerticle.class.getName(), res -> {
			if (res.failed()) {
				log.error("Initialisation failed", res.cause());
			}

		});

		vertx.deployVerticle(ProcessVerticle.class.getName(), new DeploymentOptions().setWorker(true), res -> {
			if (res.failed()) {
				log.error("Initialisation failed", res.cause());
			}

		});
	}

}
