/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
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

        computedSubjectUpdate("targetV", "cim:RegulatingControl.targetValue", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::regulatingControlId);
        computedSubjectUpdate("voltageRegulatorOn", "cim:RegulatingControl.enabled", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::regulatingControlId);

        // The change of the sub-object reactiveLimits will be a not-so-simple change
        // If the reactiveLimits kind is MIN_MAX,
        // values could be written directly as attributes of the SynchronousMachine:
        // cim:SynchronousMachine.minQ in CgmesSubset.EQUIPMENT
        // cim:SynchronousMachine.maxQ in CgmesSubset.EQUIPMENT
        unsupported("reactiveLimits");
        // Changes related to sub-object in CGMES (RegulatingControl)
//        unsupported("targetV");
//        unsupported("voltageRegulatorOn");
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

    // FIXME elena: current implementation is inefficient, need to be amended
    private String regulatingControlId(Identifiable id, CgmesModelTripleStore cgmes) {
        requireGenerator(id);
        PropertyBags synchronousMachines = cgmes.synchronousMachines();
        for (PropertyBag pb : synchronousMachines) {
            if (pb.getId("SynchronousMachine").equals(id.getId())) {
                return pb.getId("RegulatingControl");
            } else {
                continue;
            }
        }
        return null;
    }

    private void requireGenerator(Identifiable id) {
        if (!(id instanceof Generator)) {
            throw new ClassCastException("Expected Generator, got " + id.getClass().getSimpleName());
        }
    }
}
