package com.powsybl.iidm.network;

public interface AcDcConverterDroop {

    double getUMin();

    double getUMax();

    double getDroopCoefficient();
}
