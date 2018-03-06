package com.powsybl.shortcircuit;

public class FaultResult {
    private String id;
    private float threePhaseFaultCurrent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getThreePhaseFaultCurrent() {
        return threePhaseFaultCurrent;
    }

    public void setThreePhaseFaultCurrent(float threePhaseFaultCurrent) {
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
    }

}
