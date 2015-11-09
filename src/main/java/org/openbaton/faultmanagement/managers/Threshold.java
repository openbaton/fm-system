package org.openbaton.faultmanagement.managers;

/**
 * Created by mob on 29.10.15.
 */
public class Threshold {
    private int value;
    private String type;

    public Threshold(String type,int value){
        this.type=type;
        this.value=value;
    }

    public int getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
