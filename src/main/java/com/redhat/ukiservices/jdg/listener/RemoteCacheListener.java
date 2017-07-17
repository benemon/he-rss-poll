package com.redhat.ukiservices.jdg.listener;

import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientCacheFailover;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;
import org.infinispan.client.hotrod.event.ClientCacheFailoverEvent;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@ClientListener
public class RemoteCacheListener {

	private static final Logger log = LoggerFactory.getLogger(RemoteCacheListener.class);

	private static final String EVENT_MSG_FORMAT = "%s: %s";

	@ClientCacheEntryExpired
	public void entryExpired(ClientCacheEntryExpiredEvent<String> event) {
		log.info(String.format(EVENT_MSG_FORMAT, "Expired", event.getKey()));
	}

	@ClientCacheEntryRemoved
	public void entryRemoved(ClientCacheEntryRemovedEvent<String> event) {
		log.info(String.format(EVENT_MSG_FORMAT, "Removed", event.getKey()));
	}

	@ClientCacheEntryCreated
	public void entryCreated(ClientCacheEntryCreatedEvent<String> event) {
		log.info(String.format(EVENT_MSG_FORMAT, "Created", event.getKey()));
	}

	@ClientCacheEntryModified
	public void entryModified(ClientCacheEntryModifiedEvent<String> event) {
		log.info(String.format(EVENT_MSG_FORMAT, "Modified", event.getKey()));
	}
	
	@ClientCacheFailover
	public void failover(ClientCacheFailoverEvent event) {
		log.warn(String.format(EVENT_MSG_FORMAT, "Failover event occurred"));
	}
}
