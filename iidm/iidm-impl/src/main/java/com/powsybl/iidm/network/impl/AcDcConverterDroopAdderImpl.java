
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

public class AcDcConverterDroopAdderImpl implements AcDcConverterDoopAdder {

    double uMin;

    double uMax;

    double k;

    AcDcConverter<?> converter;

    public AcDcConverterDroopAdderImpl(AcDcConverter<?> converter) {
        this.converter = converter;
    }

    @Override
    public AcDcConverterDoopAdder setDroopCoefficient(double k) {
        this.k = k;
        return this;
    }

    @Override
    public AcDcConverterDoopAdder setUMax(double uMax) {
        this.uMax = uMax;
        return this;
    }

    @Override
    public AcDcConverterDoopAdder setUMin(double uMin) {
        this.uMin = uMin;
        return this;
    }

    @Override
    public AcDcConverterDroop add() {
        return new AcDcConverterDroopImpl(uMin, uMax, k, converter);
    }
}
