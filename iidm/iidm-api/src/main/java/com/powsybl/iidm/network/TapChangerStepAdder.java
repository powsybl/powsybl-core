package com.powsybl.iidm.network;

public interface TapChangerStepAdder<S extends com.powsybl.iidm.network.TapChangerStepAdder<S, A>, A extends TapChangerAdder<A, S, ?>> {
    S setRho(double rho);

    S setR(double r);

    S setX(double x);

    S setG(double g);

    S setB(double b);

    A endStep();
}
