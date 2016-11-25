package com.leoni.mfa.reporting.config;

/**
 *
 * @author bewa1022
 */
public class WorkShift {

    private int number;
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public WorkShift(boolean enabled) {
        this.enabled = enabled;
    }

    public WorkShift(int number, boolean enabled) {
        this.number = number;
        this.enabled = enabled;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "WorkShift{" + "number=" + number + ", enabled=" + enabled + '}';
    }

}
