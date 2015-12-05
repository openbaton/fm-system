package org.openbaton.faultmanagement.fc.policymanagement.catalogue;

/**
 * Created by mob on 03.12.15.
 */
public class VNFCInstanceShort {
    private String id;
    private String hostname;

    public VNFCInstanceShort(String id, String hostname) {
        this.id = id;
        this.hostname = hostname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "VNFCInstanceShort{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}
