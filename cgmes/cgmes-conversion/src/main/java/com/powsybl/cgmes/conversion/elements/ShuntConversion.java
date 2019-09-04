/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModelAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ShuntConversion extends AbstractConductingEquipmentConversion {

    public ShuntConversion(PropertyBag sh, Context context) {
        super("ShuntCompensator", sh, context);
    }

    @Override
    public void convert() {
        int maximumSections = p.asInt("maximumSections", 0);
        int normalSections = p.asInt("normalSections", 0);
        int sections = fromContinuous(p.asDouble("SVsections", p.asDouble("SSHsections", normalSections)));
        sections = Math.abs(sections);
        maximumSections = Math.max(maximumSections, sections);
        ShuntCompensatorAdder adder = voltageLevel().newShuntCompensator();
        if (p.getId("type").toLowerCase().equals("linearshuntcompensator")) {
            double bPerSection = p.asDouble("bPerSection");
            if (bPerSection == 0) {
                float bPerSectionFixed = Float.MIN_VALUE;
                fixed(CgmesNames.B_PER_SECTION, "Can not be zero", bPerSection, bPerSectionFixed);
                bPerSection = bPerSectionFixed;
            }
            adder.newShuntCompensatorLinearModel()
                    .setbPerSection(bPerSection)
                    .setMaximumSectionCount(maximumSections)
                    .add();
        } else if (p.getId("type").toLowerCase().equals("nonlinearshuntcompensator")) {
            ShuntCompensatorNonLinearModelAdder modelAdder = adder.newShuntCompensatorNonLinearModel();
            PropertyBags ss = context.cgmes().nonlinearShuntCompensatorPoints(id);
            for (PropertyBag s : ss) {
                modelAdder.beginSection()
                        .setSectionNumber(s.asInt("sectionNumber"))
                        .setB(s.asDouble("b"))
                        .endSection();
            }
            modelAdder.add();
        } else {
            throw new CgmesModelException("Unexpected shunt compensator type: " + p.get("type"));
        }

        adder.setCurrentSectionCount(sections);
        identify(adder);
        connect(adder);
        ShuntCompensator shunt = adder.add();

        // At a shunt terminal, only Q can be set
        PowerFlow f = powerFlow();
        if (f.defined()) {
            double q = f.q();
            if (context.config().changeSignForShuntReactivePowerFlowInitialState()) {
                q = -q;
            }
            f = new PowerFlow(Double.NaN, q);
        }
        convertedTerminal(terminalId(), shunt.getTerminal(), 1, f);
    }
}
