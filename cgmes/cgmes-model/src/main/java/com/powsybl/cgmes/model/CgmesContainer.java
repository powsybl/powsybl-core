package com.powsybl.cgmes.model;

public class CgmesContainer {
    CgmesContainer(String voltageLevel, String substation) {
        this.voltageLevel = voltageLevel;
        this.substation = substation;
    }

    public String substation() {
        return substation;
    }

    public String voltageLevel() {
        return voltageLevel;
    }

    private final String voltageLevel;
    private final String substation;
}
