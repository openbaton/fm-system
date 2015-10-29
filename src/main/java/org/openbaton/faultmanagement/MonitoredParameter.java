package org.openbaton.faultmanagement;

/**
 * Created by mob on 29.10.15.
 */
public class MonitoredParameter {
    private String name;
    private int period;
    private Threshold threshold;
    private boolean problem;

    public MonitoredParameter(String name, int period){
        if(name==null || name.isEmpty())
            throw new NullPointerException("The name is null or empty");
        if (period<0)
            throw new NumberFormatException("The period cannot be negative");
        this.name=name;
        this.period=period;
        problem =false;
    }

    public Threshold getThreshold() {
        return threshold;
    }

    public void setThreshold(Threshold threshold) {
        this.threshold = threshold;
    }

    public int getPeriod() {
        return period;
    }

    public String getName() {
        return name;
    }

    public boolean isProblem() {
        return problem;
    }

    public void setProblem(boolean problem) {
        this.problem = problem;
    }
}
