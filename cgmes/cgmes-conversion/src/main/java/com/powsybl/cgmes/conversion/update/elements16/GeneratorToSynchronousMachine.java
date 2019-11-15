package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;

public class GeneratorToSynchronousMachine extends IidmToCgmes {

    public GeneratorToSynchronousMachine() {
        ignore("p");
        ignore("q");
        // Changes in energy source are ignored
        // In CGMES generating units with diferent energy source are separate types
        // This would be a major update
        // It would require changing the class of the generating unit linked to the
        // synchronous machine related to this IIDM generator
        ignore("energySource");

        simpleUpdate("ratedS", "cim:RotatingMachine.ratedS", CgmesSubset.EQUIPMENT);
        simpleUpdate("minP", "cim:GeneratingUnit.minOperatingP", CgmesSubset.STEADY_STATE_HYPOTHESIS);
        simpleUpdate("maxP", "cim:GeneratingUnit.maxOperatingP", CgmesSubset.STEADY_STATE_HYPOTHESIS);

        computedValueUpdate("targetP", "cim:RotatingMachine.p", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::pFromTargetP);
        computedValueUpdate("targetQ", "cim:RotatingMachine.q", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::qFromTargetQ);

        // The change of the sub-object reactiveLimits will be a not-so-simple change
        // If the reactiveLimits kind is MIN_MAX,
        // values could be written directly as attributes of the SynchronousMachine:
        // cim:SynchronousMachine.minQ in CgmesSubset.EQUIPMENT
        // cim:SynchronousMachine.maxQ in CgmesSubset.EQUIPMENT
        unsupported("reactiveLimits");
        // Changes related to sub-object in CGMES (RegulatingControl)
        unsupported("targetV");
        unsupported("voltageRegulatorOn");
    }

    private String pFromTargetP(Identifiable id) {
        requireGenerator(id);
        Generator g = (Generator) id;
        return Double.toString(-g.getTargetP());
    }

    private String qFromTargetQ(Identifiable id) {
        requireGenerator(id);
        Generator g = (Generator) id;
        return Double.toString(-g.getTargetQ());
    }

    private void requireGenerator(Identifiable id) {
        if (!(id instanceof Generator)) {
            throw new ClassCastException("Expected Generator, got " + id.getClass().getSimpleName());
        }
    }
}
