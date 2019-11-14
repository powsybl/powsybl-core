package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;

public class GeneratorToSynchronousMachine extends IidmToCgmes {

    GeneratorToSynchronousMachine() {
        addSimpleUpdate("ratedS", "cim:RotatingMachine.ratedS", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("minQ", "cim:SynchronousMachine.minQ", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("maxQ", "cim:SynchronousMachine.maxQ", CgmesSubset.EQUIPMENT, false);
        addSimpleUpdate("targetP", "cim:RotatingMachine.p", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("p", "cim:RotatingMachine.p", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("q", "cim:RotatingMachine.q", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("targetQ", "cim:RotatingMachine.q", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("maxP", "cim:GeneratingUnit.maxOperatingP", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
        addSimpleUpdate("minP", "cim:GeneratingUnit.minOperatingP", CgmesSubset.STEADY_STATE_HYPOTHESIS, false);
    }

}
