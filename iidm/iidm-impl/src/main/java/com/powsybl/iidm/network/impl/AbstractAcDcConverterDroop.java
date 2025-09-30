package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AcDcConverter;
import com.powsybl.iidm.network.AcDcConverterDroop;

public abstract class AbstractAcDcConverterDroop implements AcDcConverterDroop {
    protected final double uMin;

    protected final double uMax;

    protected final double k;

    protected final AcDcConverter<?> converter;

    AbstractAcDcConverterDroop(double uMin, double uMax, double k, AcDcConverter<?> converter) {
        this.uMin = uMin;
        this.uMax = uMax;
        this.k = k;
        this.converter = converter;
        converter.addDroop(this);
    }

    @Override
    public double getUMin() {
        return uMin;
    }

    @Override
    public double getUMax() {
        return uMax;
    }

    @Override
    public double getDroopCoefficient() {
        return k;
    }
}
