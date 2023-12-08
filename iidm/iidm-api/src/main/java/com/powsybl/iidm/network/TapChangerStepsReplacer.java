package com.powsybl.iidm.network;

public interface TapChangerStepsReplacer<S extends TapChangerStepsReplacer<S, A>, A extends TapChangerStepAdder<A, S>> {
    A beginStep();

    void replaceSteps();
}
