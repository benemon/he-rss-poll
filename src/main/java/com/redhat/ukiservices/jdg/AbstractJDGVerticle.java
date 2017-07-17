package com.redhat.ukiservices.jdg;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.factory.DataGridClientFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class AbstractJDGVerticle extends AbstractVerticle {

	private static final String JDG_CONNECTION_STRING_FORMAT = "%s:%s";

	protected DataGridClientFactory dgClientFactory;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);

		String host = System.getenv(CommonConstants.JDG_SERVICE_HOST_ENV) != null
				? System.getenv(CommonConstants.JDG_SERVICE_HOST_ENV) : CommonConstants.JDG_SERVICE_HOST_DEFAULT;
		String port = System.getenv(CommonConstants.JDG_SERVICE_PORT_ENV) != null
				? System.getenv(CommonConstants.JDG_SERVICE_PORT_ENV) : CommonConstants.JDG_SERVICE_PORT_DEFAULT;

		dgClientFactory = new DataGridClientFactory(String.format(JDG_CONNECTION_STRING_FORMAT, host, port));
	}

	protected <T extends Object> RemoteCache<String, T> getCache(String cacheName) {
		return dgClientFactory.getCache(cacheName);
	}

	protected RemoteCacheManager getCacheManager() {
		return dgClientFactory.getCacheManager();
	}

}
