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
import com.powsybl.iidm.network.ShuntCompensatorNonLinearModelAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Comparator;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class ShuntConversion extends AbstractConductingEquipmentConversion {

    private static final String SECTION_NUMBER = "sectionNumber";

    public ShuntConversion(PropertyBag sh, Context context) {
        super("ShuntCompensator", sh, context);
    }

    private int getSections(PropertyBag p, int normalSections) {
        switch (context.config().getProfileForInitialValuesShuntSectionsTapPositions()) {
            case SSH:
                return fromContinuous(p.asDouble("SSHsections", p.asDouble("SVsections", normalSections)));
            case SV:
                return fromContinuous(p.asDouble("SVsections", p.asDouble("SSHsections", normalSections)));
            default:
                throw new PowsyblException("Unexpected profile used for initial values");
        }
    }

    @Override
    public void convert() {
        int maximumSections = p.asInt("maximumSections", 0);
        int normalSections = p.asInt("normalSections", 0);
        int sections = getSections(p, normalSections);
        sections = Math.abs(sections);
        maximumSections = Math.max(maximumSections, sections);
        ShuntCompensatorAdder adder = voltageLevel().newShuntCompensator().setSectionCount(sections);
        String shuntType = p.getId("type");
        if ("LinearShuntCompensator".equals(shuntType)) {
            double bPerSection = p.asDouble(CgmesNames.B_PER_SECTION, Float.MIN_VALUE);
            double gPerSection = p.asDouble("gPerSection", Double.NaN);
            adder.newLinearModel()
                    .setBPerSection(bPerSection)
                    .setGPerSection(gPerSection)
                    .setMaximumSectionCount(maximumSections)
                    .add();
        } else if ("NonlinearShuntCompensator".equals(shuntType)) {
            ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
            PropertyBags ss = context.cgmes().nonlinearShuntCompensatorPoints(id);
            ss.stream()
                    .filter(s -> s.asInt(SECTION_NUMBER) > 0)
                    .sorted(Comparator.comparing(s -> s.asInt(SECTION_NUMBER)))
                    .forEach(sec -> {
                        int sectionNumber = sec.asInt(SECTION_NUMBER);
                        modelAdder.beginSection()
                                .setB(ss.stream().filter(s -> s.asInt(SECTION_NUMBER) <= sectionNumber).map(s -> s.asDouble("b")).reduce(0.0, Double::sum))
                                .setG(ss.stream().filter(s -> s.asInt(SECTION_NUMBER) <= sectionNumber).map(s -> s.asDouble("g")).reduce(0.0, Double::sum))
                                .endSection();
                    });
            modelAdder.add();
        } else {
            throw new IllegalStateException("Unexpected shunt type: " + shuntType);
        }
        identify(adder);
        connect(adder);
        ShuntCompensator shunt = adder.add();
        addAliasesAndProperties(shunt);

        PowerFlow f = powerFlowSV();
        context.convertedTerminal(terminalId(), shunt.getTerminal(), 1, f);
        context.regulatingControlMapping().forShuntCompensators().add(shunt.getId(), p);
    }
}
