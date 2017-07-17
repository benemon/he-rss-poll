package com.redhat.ukiservices.bootstrap;

import com.redhat.ukiservices.jdg.JDGEventVerticle;
import com.redhat.ukiservices.jdg.JDGPutVerticle;
import com.redhat.ukiservices.jdg.JDGSearchVerticle;
import com.redhat.ukiservices.poll.PollingVerticle;
import com.redhat.ukiservices.service.UserVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class BootstrapVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(BootstrapVerticle.class);

	@Override
	public void start() throws Exception {

		super.start();

		vertx.deployVerticle(UserVerticle.class.getName(), res -> {
			if (res.failed()) {
				log.error("Initialisation failed", res.cause());
			}

		});

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

		/**
		 * Set Event Listener thread as a worker because we don't want it
		 * gumming up the event loop as the cache size increases and more events
		 * get triggered.
		 */
		vertx.deployVerticle(JDGEventVerticle.class.getName(), new DeploymentOptions().setWorker(true), res -> {
			if (res.failed()) {
				log.error("Initialisation failed", res.cause());
			}

		});

		vertx.deployVerticle(PollingVerticle.class.getName(), res -> {
			if (res.failed()) {
				log.error("Initialisation failed", res.cause());
			}

		});
	}

}
