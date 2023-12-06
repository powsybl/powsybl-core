package com.powsybl.iidm.network;

public interface PhaseTapChangerStepAdder extends TapChangerStepAdder<PhaseTapChangerStepAdder, PhaseTapChangerAdder>{
    PhaseTapChangerStepAdder setAlpha(double alpha);
}
