package com.redhat.ukiservices.jdg;

import org.infinispan.client.hotrod.RemoteCache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.ukiservices.jdg.factory.DataGridClientFactory;
import com.redhat.ukiservices.jdg.model.HEElementModel;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class AbstractJDGVerticle extends AbstractVerticle {

	protected RemoteCache<String, HEElementModel> remoteCache;

	protected Gson gson;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);

		DataGridClientFactory dgClientFactory = new DataGridClientFactory("localhost:11222");

		remoteCache = dgClientFactory.getCache("rep-he");

		gson = new GsonBuilder().setDateFormat("EEE, d MMM yyyy HH:mm:ss z").create();
	}

}
