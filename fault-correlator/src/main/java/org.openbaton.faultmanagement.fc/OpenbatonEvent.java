package org.openbaton.faultmanagement.fc;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.Action;

import java.io.Serializable;

/**
 * Created by mob on 30.11.15.
 */
public class OpenbatonEvent implements Serializable{
    private Action action;
    private NetworkServiceRecord payload;

    public OpenbatonEvent() {
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public NetworkServiceRecord getPayload() {
        return payload;
    }

    public void setPayload(NetworkServiceRecord payload) {
        this.payload = payload;
    }
}
