package com.redhat.ukiservices.jdg.factory;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

import com.redhat.ukiservices.jdg.model.HEElementCategoryModel;
import com.redhat.ukiservices.jdg.model.HEElementModel;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DataGridClientFactory {

	private static final Logger log = LoggerFactory.getLogger(DataGridClientFactory.class);

	private RemoteCacheManager cacheManager;

	public DataGridClientFactory(String serviceName) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServers(serviceName);
		builder.nearCache().mode(NearCacheMode.INVALIDATED).maxEntries(500);
		builder.marshaller(new ProtoStreamMarshaller());
		cacheManager = new RemoteCacheManager(builder.build());

		registerProtoBufSchema();
	}

	public <T extends Object> RemoteCache<String, T> getCache(String cacheName) {
		return cacheManager.getCache(cacheName);
	}

	private void registerProtoBufSchema() {
		SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(cacheManager);

		String generatedSchema = null;
		try {
			ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
			generatedSchema = protoSchemaBuilder.fileName("heschema.proto").packageName("model")
					.addClass(HEElementModel.class).addClass(HEElementCategoryModel.class).build(serCtx);

			// register the schemas with the server too
			RemoteCache<String, String> metadataCache = cacheManager
					.getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);

			metadataCache.put("heschema.proto", generatedSchema);

		} catch (Exception e1) {

			StringBuilder sb = new StringBuilder();
			sb.append("No schema generated because of Exception");
			log.error(sb.toString(), e1);

		}

		log.info(generatedSchema);
	}

}
