/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlUpdate;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class ShuntConversion extends AbstractConductingEquipmentConversion {

    private static final String SECTION_NUMBER = "sectionNumber";

    public ShuntConversion(PropertyBag sh, Context context) {
        super(CgmesNames.SHUNT_COMPENSATOR, sh, context);
    }

    @Override
    public void convert() {
        int maximumSections = p.asInt("maximumSections", 0);
        int normalSections = p.asInt("normalSections", 0);
        ShuntCompensatorAdder adder = voltageLevel().newShuntCompensator().setSectionCount(normalSections);
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

        context.convertedTerminal(terminalId(), shunt.getTerminal(), 1);
        context.regulatingControlMapping().forShuntCompensators().add(shunt.getId(), p);

        shunt.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalSections", String.valueOf(normalSections));
    }

    @Override
    public void update(Network network) {
        // super.update(network); // TODO JAM delete
        ShuntCompensator shunt = network.getShuntCompensator(id);
        if (shunt == null) {
            return;
        }
        updateSections(shunt);

        String controlId = shunt.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
        boolean controlEnabled = p.asBoolean("controlEnabled", false);
        updateRegulatingControl(shunt, controlEnabled, controlId);
    }

    private void updateSections(ShuntCompensator shunt) {
        int normalSections = getNormalSections(shunt.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "normalSections"));
        int sections = getSections(p, normalSections);
        sections = Math.abs(sections);
        shunt.setSectionCount(Math.min(sections, shunt.getMaximumSectionCount()));
    }

    private static int getNormalSections(String normalSections) {
        return normalSections != null ? Integer.parseInt(normalSections) : 0;
    }

    private int getSections(PropertyBag p, int normalSections) {
        return switch (context.config().getProfileForInitialValuesShuntSectionsTapPositions()) {
            case SSH -> fromContinuous(p.asDouble("SSHsections", p.asDouble("SVsections", normalSections)));
            case SV -> fromContinuous(p.asDouble("SVsections", p.asDouble("SSHsections", normalSections)));
        };
    }

    private void updateRegulatingControl(ShuntCompensator shuntCompensator, boolean controlEnabled, String controlId) {

        // This equipment is not participating in its regulating control
        // We will not create default regulation data
        // But will try to store information about corresponding regulating control
        if (!controlEnabled) {
            if (controlId != null) {
                RegulatingControlUpdate.RegulatingControl control = context.regulatingControlUpdate().getRegulatingControl(controlId).orElse(null);
                if (control != null) {
                    setRegulatingControl(shuntCompensator, control, controlEnabled);
                }
            }
            return;
        }
        // The equipment is participating in regulating control (cgmesRc.controlEnabled)
        // But no regulating control information has been found
        // We create default regulation data
        if (controlId == null) {
            LOG.trace("Regulating control Id not present for shunt compensator {}", shuntCompensator.getId());
            setDefaultRegulatingControl(shuntCompensator);
            return;
        }
        RegulatingControlUpdate.RegulatingControl control = context.regulatingControlUpdate().getRegulatingControl(controlId).orElse(null);
        if (control == null) {
            context.missing(String.format("Regulating control %s", controlId));
            setDefaultRegulatingControl(shuntCompensator);
            return;
        }
        // Finally, equipment participates in it regulating control,
        // and the regulating control information is present in the CGMES model
        setRegulatingControl(shuntCompensator, control, controlEnabled);
    }

    private void setDefaultRegulatingControl(ShuntCompensator shuntCompensator) {
        shuntCompensator.setTargetV(Optional.ofNullable(shuntCompensator.getTerminal().getBusView().getBus())
                        .map(Bus::getV)
                        .filter(v -> !Double.isNaN(v))
                        .orElseGet(() -> shuntCompensator.getTerminal().getVoltageLevel().getNominalV()))
                .setTargetDeadband(0.0)
                .setVoltageRegulatorOn(true); // SSH controlEnabled attribute is true when this method is called
    }

    private void setRegulatingControl(ShuntCompensator shuntCompensator, RegulatingControlUpdate.RegulatingControl control, boolean controlEnabled) {
        shuntCompensator.setTargetV(control.getTargetValue())
                .setTargetDeadband(control.getTargetDeadband());
        if (control.getTargetValue() > 0) {
            // For the IIDM regulating control to be enabled
            // both the equipment participation in the control and
            // the regulating control itself should be enabled
            shuntCompensator.setVoltageRegulatorOn(control.getEnabled() && controlEnabled);
        } else {
            shuntCompensator.setVoltageRegulatorOn(false);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ShuntConversion.class);
}
