package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;

public class GeneratorToSynchronousMachine extends IidmToCgmes {

    GeneratorToSynchronousMachine() {
        addSimpleUpdate("ratedS", "cim:RotatingMachine.ratedS", "_EQ", false);
        addSimpleUpdate("minQ", "cim:SynchronousMachine.minQ", "_EQ", false);
        addSimpleUpdate("maxQ", "cim:SynchronousMachine.maxQ", "_EQ", false);
        addSimpleUpdate("targetP", "cim:RotatingMachine.p", "_SSH", false);
        addSimpleUpdate("p", "cim:RotatingMachine.p", "_SSH", false);
        addSimpleUpdate("q", "cim:RotatingMachine.q", "_SSH", false);
        addSimpleUpdate("targetQ", "cim:RotatingMachine.q", "_SSH", false);
        addSimpleUpdate("maxP", "cim:GeneratingUnit.maxOperatingP", "_EQ", false);
        addSimpleUpdate("minP", "cim:GeneratingUnit.minOperatingP", "_EQ", false);
    }

}
