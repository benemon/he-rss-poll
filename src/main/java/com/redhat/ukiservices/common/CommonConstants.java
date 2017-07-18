package com.redhat.ukiservices.common;

public interface CommonConstants {

	public static final String OK = "OK";

	public static final String NOT_OK = "OK";

	public static final String HE_RSS_URL_LIST_ENV = "HE_RSS_URL_LIST";
	public static final String HE_RSS_URL = "HE_RSS_URL";
	public static final String HE_RSS_URL_POLL_PERIOD = "HE_RSS_POLL_PERIOD";
	
	public static final String HE_RSS_URL_DEFAULT = "http://m.highways.gov.uk/feeds/rss/UnplannedEvents.xml";

	public static final String POLL_PERIOD_ENV = "HE_RSS_POLL_PERIOD";
	public static final String POLL_PERIOD_DEFAULT = "300000";

	public static final String VERTX_EVENT_BUS_HE_RSS_JDG_PUT = "he.rss.jdg.put";
	public static final String VERTX_EVENT_BUS_HE_RSS_JDG_SEARCH = "he.rss.process.jdg.search";
	public static final String VERTX_EVENT_BUS_HE_RSS_JDG_GET = "he.rss.process.jdg.get";

	public static final String JDG_SERVICE_HOST_ENV = "DATAGRID_HOTROD_SERVICE_HOST";
	public static final String JDG_SERVICE_PORT_ENV = "DATAGRID_HOTROD_SERVICE_PORT";
	public static final String JDG_SERVICE_HOST_DEFAULT = "localhost";
	public static final String JDG_SERVICE_PORT_DEFAULT = "11222";

	public static final String HE_JDG_VERTX_CACHE_ENV = "HE_JDG_VERTX_CACHE";
	public static final String HE_JDG_VERTX_CACHE_DEFAULT = "default";

	public static final String JDG_SEARCH_TERM_KEY = "term";
	public static final String JDG_SEARCH_ACTION_KEY = "action";
	public static final String JDG_SEARCH_ACTION_COUNT = "count";

}
