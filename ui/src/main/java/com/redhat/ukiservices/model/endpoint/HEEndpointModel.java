package com.redhat.ukiservices.model.endpoint;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;

public class HEEndpointModel implements Serializable {

    private static final long serialVersionUID = 5856721991365340740L;

    private String label;

    private String url;

    private long pollingPeriod;

    private boolean active;

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the polling period
     */
    public long getPollingPeriod() {
        return pollingPeriod;
    }

    /**
     * @param pollingPeriod
     */
    public void setPollingPeriod(long pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.put("label", this.getLabel());
        obj.put("url", this.getUrl());
        obj.put("pollingPeriod", this.getPollingPeriod());
        obj.put("active", this.isActive());
        return obj;
    }
}
