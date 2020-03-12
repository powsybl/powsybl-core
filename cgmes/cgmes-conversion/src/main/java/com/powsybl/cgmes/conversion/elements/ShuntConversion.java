/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ShuntConversion extends AbstractConductingEquipmentConversion {

    public ShuntConversion(PropertyBag sh, Context context) {
        super("ShuntCompensator", sh, context);
    }

    private int getSections(PropertyBag p, int normalSections) {
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                return fromContinuous(p.asDouble("SSHsections", p.asDouble("SVsections", normalSections)));
            case SV:
                return fromContinuous(p.asDouble("SVsections", p.asDouble("SSHsections", normalSections)));
            default:
                throw new PowsyblException("Unexpected profile used for initial state values");
        }
    }

    @Override
    public void convert() {
        int maximumSections = p.asInt("maximumSections", 0);
        int normalSections = p.asInt("normalSections", 0);
        int sections = getSections(p, normalSections);
        sections = Math.abs(sections);
        maximumSections = Math.max(maximumSections, sections);
        double bPerSection = 0;
        if (p.containsKey(CgmesNames.B_PER_SECTION)) {
            bPerSection = p.asDouble(CgmesNames.B_PER_SECTION, 0.0);
        } else {
            PropertyBags ss = context.cgmes().nonlinearShuntCompensatorPoints(id);
            final int nlsections = sections;
            double sumSections = ss.stream()
                    .filter(s -> s.asInt("sectionNumber") <= nlsections)
                    .map(s -> s.asDouble("b"))
                    .reduce(0.0, Double::sum);
            // Convert to a shunt compensator with a single section
            maximumSections = 1;
            sections = 1;
            bPerSection = sumSections;
        }
        if (bPerSection == 0) {
            float bPerSectionFixed = Float.MIN_VALUE;
            fixed(CgmesNames.B_PER_SECTION, "Can not be zero", bPerSection, bPerSectionFixed);
            bPerSection = bPerSectionFixed;
        }

        ShuntCompensatorAdder adder = voltageLevel().newShuntCompensator()
                .setCurrentSectionCount(sections)
                .setbPerSection(bPerSection)
                .setMaximumSectionCount(maximumSections);
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
        context.convertedTerminal(terminalId(), shunt.getTerminal(), 1, f);
        context.regulatingControlMapping().forShuntCompensators().add(shunt.getId(), p);
    }
}
