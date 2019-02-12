/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevelAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class VoltageLevelConversion extends AbstractIdentifiedObjectConversion {
    public VoltageLevelConversion(PropertyBag vl, Context context) {
        super("VoltageLevel", vl, context);
        cgmesSubstationId = p.getId("Substation");
        iidmSubstationId = context.substationIdMapping().iidm(cgmesSubstationId);
        substation = context.network().getSubstation(iidmSubstationId);
    }

    @Override
    public boolean valid() {
        if (substation == null) {
            missing(String.format("Substation %s (IIDM id: %s)",
                    cgmesSubstationId,
                    iidmSubstationId));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        String baseVoltage = p.getId("BaseVoltage");
        double nominalVoltage = p.asDouble("nominalVoltage");
        double lowVoltageLimit = p.asDouble("lowVoltageLimit");
        double highVoltageLimit = p.asDouble("highVoltageLimit");

        // Missing elements in the boundary file
        if (Double.isNaN(nominalVoltage)) {
            String bv = String.format("BaseVoltage %s", baseVoltage);
            missing(bv);
            throw new CgmesModelException(String.format("nominalVoltage not found for %s", bv));
        }

        VoltageLevel voltageLevel = context.network().getVoltageLevel(iidmId());
        if (voltageLevel == null) {
            VoltageLevelAdder adder = substation.newVoltageLevel()
                    .setNominalV(nominalVoltage)
                    .setTopologyKind(
                            context.nodeBreaker()
                                    ? TopologyKind.NODE_BREAKER
                                    : TopologyKind.BUS_BREAKER)
                    .setLowVoltageLimit(lowVoltageLimit)
                    .setHighVoltageLimit(highVoltageLimit);
            identify(adder);
            adder.add();
        }
    }

    private final String cgmesSubstationId;
    private final String iidmSubstationId;
    private final Substation substation;
}
