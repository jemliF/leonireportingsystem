package com.leoni.mfa.reporting.config;

/**
 *
 * @author bewa1022
 */
public class Process {

    private String operation;
    private String routeStep;
    private String segment;
    private boolean ISH;
    private boolean ITSH;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRouteStep() {
        return routeStep;
    }

    public void setRouteStep(String routeStep) {
        this.routeStep = routeStep;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Process() {
    }

    public Process(String operation, String routeStep, String segment, boolean ISH, boolean ITSH) {
        this.operation = operation;
        this.routeStep = routeStep;
        this.segment = segment;
        this.ISH = ISH;
        this.ITSH = ITSH;
    }

    public boolean isISH() {
        return ISH;
    }

    public void setISH(boolean ISH) {
        this.ISH = ISH;
    }

    public boolean isITSH() {
        return ITSH;
    }

    public void setITSH(boolean ITSH) {
        this.ITSH = ITSH;
    }

    

}
