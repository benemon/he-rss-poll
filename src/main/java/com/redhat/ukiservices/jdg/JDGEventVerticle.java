package com.redhat.ukiservices.jdg;

import org.infinispan.client.hotrod.RemoteCache;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.listener.RemoteCacheListener;
import com.redhat.ukiservices.jdg.model.HEElementModel;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class JDGEventVerticle extends AbstractJDGVerticle {

	private static final Logger log = LoggerFactory.getLogger(JDGEventVerticle.class);

	private String cacheName;
	private RemoteCache<String, HEElementModel> cache;
	private RemoteCacheListener listener;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		cacheName = System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) != null
				? System.getenv(CommonConstants.HE_JDG_VERTX_CACHE_ENV) : CommonConstants.HE_JDG_VERTX_CACHE_DEFAULT;

	}

	@Override
	public void start() throws Exception {
		super.start();

		cache = getCache(cacheName);

		listener = new RemoteCacheListener();

		cache.addClientListener(listener);

		log.info("Registered client listener on cache " + cacheName);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		if (listener != null) {
			cache.removeClientListener(listener);
		}

	}

}
