package org.openbaton.faultmanagement;

/**
 * Created by mob on 30.10.15.
 */
public class VirtualDeploymentUnitShort {
    private String id;
    private String name;

    public VirtualDeploymentUnitShort(String id, String name){
        if(id==null || name==null)
            throw new NullPointerException("VDUshort name or id cannot be null");
        this.id=id;
        this.name=name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
