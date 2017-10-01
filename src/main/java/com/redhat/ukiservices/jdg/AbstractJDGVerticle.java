package com.redhat.ukiservices.jdg;

import com.redhat.ukiservices.common.CommonConstants;
import com.redhat.ukiservices.jdg.model.element.HEElementCategoryModel;
import com.redhat.ukiservices.jdg.model.element.HEElementModel;
import com.redhat.ukiservices.jdg.security.LoginHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

public abstract class AbstractJDGVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(AbstractJDGVerticle.class);

    private static final String JDG_CONNECTION_STRING_FORMAT = "%s:%s";
    private static final String JDG_SASL_MECHANISM = "DIGEST-MD5";
    private static final String JDG_REALM = "ApplicationRealm";

    protected RemoteCacheManager cacheManager;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        String host = System.getenv(CommonConstants.JDG_SERVICE_HOST_ENV) != null
                ? System.getenv(CommonConstants.JDG_SERVICE_HOST_ENV) : CommonConstants.JDG_SERVICE_HOST_DEFAULT;
        String port = System.getenv(CommonConstants.JDG_SERVICE_PORT_ENV) != null
                ? System.getenv(CommonConstants.JDG_SERVICE_PORT_ENV) : CommonConstants.JDG_SERVICE_PORT_DEFAULT;
        String username = System.getenv(CommonConstants.JDG_SERVICE_USER_NAME_ENV) != null
                ? System.getenv(CommonConstants.JDG_SERVICE_USER_NAME_ENV) : CommonConstants.JDG_SERVICE_USER_NAME_DEFAULT;
        String password = System.getenv(CommonConstants.JDG_SERVICE_PASSWORD_ENV) != null
                ? System.getenv(CommonConstants.JDG_SERVICE_PASSWORD_ENV) : CommonConstants.JDG_SERVICE_PASSWORD_DEFAULT;
        String jdgServerName = System.getenv(CommonConstants.JDG_SERVICE_SERVER_NAME_ENV) != null
                ? System.getenv(CommonConstants.JDG_SERVICE_SERVER_NAME_ENV) : CommonConstants.JDG_SERVICE_SERVER_NAME_DEFAULT;


        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServers(String.format(JDG_CONNECTION_STRING_FORMAT, host, port));
        builder.nearCache().mode(NearCacheMode.INVALIDATED).maxEntries(25);
        builder.marshaller(new ProtoStreamMarshaller());
        builder.security().authentication()
                .serverName(jdgServerName)
                .saslMechanism(JDG_SASL_MECHANISM)
                .callbackHandler(new LoginHandler(username, password.toCharArray(), JDG_REALM))
                .enable();


        cacheManager = new RemoteCacheManager(builder.build());

        this.registerProtoBufSchema();
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

        log.debug(generatedSchema);
    }

    protected <T extends Object> RemoteCache<String, T> getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

}
