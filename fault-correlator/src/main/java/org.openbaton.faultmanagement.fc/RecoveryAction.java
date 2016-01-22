package org.openbaton.faultmanagement.fc;

import java.util.Date;

/**
 * Created by mob on 22.01.16.
 */
public class RecoveryAction {
    private String actionName;
    private String vnfmEndpoint;
    private String vimName;
    private String startingTime;

    public RecoveryAction(String actionName, String vnfmEndpoint, String startingTime, String vimName) {
        this.actionName = actionName;
        this.vnfmEndpoint = vnfmEndpoint;
        this.startingTime = startingTime;
        this.vimName = vimName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getVnfmEndpoint() {
        return vnfmEndpoint;
    }

    public void setVnfmEndpoint(String vnfmEndpoint) {
        this.vnfmEndpoint = vnfmEndpoint;
    }

    public String getVimName() {
        return vimName;
    }

    public void setVimName(String vimName) {
        this.vimName = vimName;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }

    @Override
    public String toString() {
        return "RecoveryAction{" +
                "actionName='" + actionName + '\'' +
                ", vnfmEndpoint='" + vnfmEndpoint + '\'' +
                ", vimName='" + vimName + '\'' +
                ", startingTime=" + startingTime +
                '}';
    }
}
