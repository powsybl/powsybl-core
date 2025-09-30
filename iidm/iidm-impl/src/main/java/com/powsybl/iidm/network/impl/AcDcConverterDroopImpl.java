package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AcDcConverter;

public class AcDcConverterDroopImpl extends AbstractAcDcConverterDroop {

    AcDcConverterDroopImpl(double uMin, double uMax, double k, AcDcConverter<?> converter) {
        super(uMin, uMax, k, converter);
    }
}
