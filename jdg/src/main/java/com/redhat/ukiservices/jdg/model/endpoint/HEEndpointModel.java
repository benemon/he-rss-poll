package com.redhat.ukiservices.jdg.model.endpoint;

import io.vertx.core.json.JsonObject;
import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoMessage;

import java.io.Serializable;

@ProtoDoc("@Indexed")
@ProtoMessage(name = "HEEndpointModel")
public class HEEndpointModel implements Serializable {

    private static final long serialVersionUID = 5856721991365340740L;

    private String label;

    private String url;

    private long pollingPeriod;

    private boolean active;

    /**
     * @return the label
     */
    @ProtoField(number = 1, required = true)
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
    @ProtoField(number = 2, required = true)
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
    @ProtoField(number = 3, required = true)
    public long getPollingPeriod() {
        return pollingPeriod;
    }

    /**
     * @param pollingPeriod
     */
    public void setPollingPeriod(long pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

    @ProtoField(number = 4, required = true)
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
