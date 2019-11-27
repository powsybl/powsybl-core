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
import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class GeneratorToExternalNetworkInjection extends IidmToCgmes {

    public GeneratorToExternalNetworkInjection() {
        ignore("p");
        ignore("q");
        // Changes in energy source are ignored
        // In CGMES generating units with difFerent energy source are separate types
        // This would be a major update
        // It would require changing the class of the generating unit linked to the
        // synchronous machine related to this IIDM generator
        ignore("energySource");


        simpleUpdate("minP", "cim:ExternalNetworkInjection.minP", CgmesSubset.EQUIPMENT);
        simpleUpdate("maxP", "cim:ExternalNetworkInjection.maxP", CgmesSubset.EQUIPMENT);

        computedValueUpdate("targetP", "cim:ExternalNetworkInjection.p", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::pFromTargetP);
        computedValueUpdate("targetQ", "cim:ExternalNetworkInjection.q", CgmesSubset.STEADY_STATE_HYPOTHESIS, this::qFromTargetQ);
        computedValueUpdate("reactiveLimits", "cim:ExternalNetworkInjection.minQ", CgmesSubset.EQUIPMENT, this::minQFromReactiveLimits);
        computedValueUpdate("reactiveLimits", "cim:ExternalNetworkInjection.maxQ", CgmesSubset.EQUIPMENT, this::maxQFromReactiveLimits);

        simpleUpdate("voltageRegulatorOn", "cim:RegulatingCondEq.controlEnabled", CgmesSubset.STEADY_STATE_HYPOTHESIS);

        // The change of the sub-object reactiveLimits will be a not-so-simple change
        // If the reactiveLimits kind is MIN_MAX,
        // values could be written directly as attributes of the SynchronousMachine:
        // cim:SynchronousMachine.minQ in CgmesSubset.EQUIPMENT
        // cim:SynchronousMachine.maxQ in CgmesSubset.EQUIPMENT
//        unsupported("reactiveLimits");
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

    private String maxQFromReactiveLimits(Identifiable id) {
        requireGenerator(id);
        Generator g = (Generator) id;
        ReactiveLimits r = g.getReactiveLimits();
        if (g.getReactiveLimits() instanceof MinMaxReactiveLimits) {
            MinMaxReactiveLimits l = (MinMaxReactiveLimits) g.getReactiveLimits();
            return Double.toString(l.getMaxQ());
        }
        return null;
    }

    private String minQFromReactiveLimits(Identifiable id) {
        requireGenerator(id);
        Generator g = (Generator) id;
        if (g.getReactiveLimits() instanceof MinMaxReactiveLimits) {
            MinMaxReactiveLimits l = (MinMaxReactiveLimits) g.getReactiveLimits();
            return Double.toString(l.getMinQ());
        }
        return null;
    }

    private void requireGenerator(Identifiable id) {
        if (!(id instanceof Generator)) {
            throw new ClassCastException("Expected Generator, got " + id.getClass().getSimpleName());
        }
    }
}
