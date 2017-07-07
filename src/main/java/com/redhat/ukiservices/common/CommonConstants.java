package com.redhat.ukiservices.common;

public interface CommonConstants {

	public static final String OK = "OK";

	public static final String NOT_OK = "OK";

	public static final String HE_RSS_URL_LIST_ENV = "HE_RSS_URL_LIST";
	public static final String HE_RSS_URL_DEFAULT = "http://m.highways.gov.uk/feeds/rss/UnplannedEvents.xml";

	public static final String POLL_PERIOD_ENV = "HE_RSS_POLL_PERIOD";
	public static final String POLL_PERIOD_DEFAULT = "60000";

	public static final String VERTX_EVENT_BUS_HE_RSS_PROCESS = "he.rss.process";

}
