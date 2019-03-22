package com.powsybl.iidm.network;

import com.powsybl.commons.extensions.AbstractExtension;

import java.util.Objects;

public class SwitchExt extends AbstractExtension<Switch> {

    public static final int DEFAULT_SWICH_MAX_CURRENT = 999999;
    private Switch sw;
    private float currentLimit;


    public SwitchExt(Switch sw, float currentLimit) {
        this.sw = Objects.requireNonNull(sw);
        this.currentLimit = currentLimit;
    }

    public Switch getSw() {
        return this.sw;
    }

    public void setSw(Switch sw) {
        this.sw = Objects.requireNonNull(sw);
    }

    public float getCurrentLimit() {
        return currentLimit;
    }

    public void setCurrentLimit(float currentLimit) {
        this.currentLimit = currentLimit;
    }

    @Override
    public String getName() {
        return "SwitchExt";
    }

}
