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
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;

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
        ShuntCompensatorAdder adder = voltageLevel().newShuntCompensator().setSectionCount(0);
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
        connectWithOnlyEq(adder);
        ShuntCompensator shunt = adder.add();
        addAliasesAndProperties(shunt);

        convertedTerminalsWithOnlyEq(shunt.getTerminal());
        context.regulatingControlMapping().forShuntCompensators().add(shunt.getId(), p);
        shunt.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_SECTIONS, String.valueOf(normalSections));
    }

    public static void update(ShuntCompensator shuntCompensator, PropertyBag cgmesData, Context context) {
        updateTerminals(shuntCompensator, context, shuntCompensator.getTerminal());
        updateSections(shuntCompensator, cgmesData, context);

        boolean controlEnabled = cgmesData.asBoolean(CgmesNames.CONTROL_ENABLED, false);
        updateRegulatingControl(shuntCompensator, controlEnabled, context);
    }

    private static void updateSections(ShuntCompensator shuntCompensator, PropertyBag cgmesData, Context context) {
        int normalSections = Integer.parseInt(shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_SECTIONS));
        DefaultValueInteger defaultSections = getDefaultSections(shuntCompensator, normalSections);
        int sections = getSections(cgmesData, context).orElse(defaultValue(defaultSections, context));
        shuntCompensator.setSectionCount(Math.min(sections, shuntCompensator.getMaximumSectionCount()));
    }

    private static OptionalInt getSections(PropertyBag cgmesData, Context context) {
        double sections = switch (context.config().getProfileForInitialValuesShuntSectionsTapPositions()) {
            case SSH -> cgmesData.asDouble("SSHsections", cgmesData.asDouble("SVsections"));
            case SV -> cgmesData.asDouble("SVsections", cgmesData.asDouble("SSHsections"));
        };
        return Double.isFinite(sections) ? OptionalInt.of(Math.abs(fromContinuous(sections))) : OptionalInt.empty();
    }

    private static DefaultValueInteger getDefaultSections(ShuntCompensator shuntCompensator, int normalSections) {
        return new DefaultValueInteger(normalSections, shuntCompensator.getSectionCount(), 0, 0);
    }

    private static void updateRegulatingControl(ShuntCompensator shuntCompensator, boolean controlEnabled, Context context) {
        if (isDefaultRegulatingControl(shuntCompensator, controlEnabled)) {
            setDefaultRegulatingControl(shuntCompensator);
            return;
        }

        DefaultValueDouble defaultTargetV = getDefaultTargetV(shuntCompensator);
        DefaultValueDouble defaultTargetDeadband = getDefaultTargetDeadband(shuntCompensator);
        DefaultValueBoolean defaultRegulatingOn = getDefaultRegulatingOn(shuntCompensator);

        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(shuntCompensator, context);
        double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetV, context));
        double targetDeadband = cgmesRegulatingControl.map(propertyBag -> findTargetDeadband(propertyBag, defaultTargetDeadband, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultTargetDeadband, context));
        boolean enabled = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED, context)).orElse(defaultValue(defaultRegulatingOn, context));

        shuntCompensator.setTargetV(targetV)
                .setTargetDeadband(targetDeadband)
                .setVoltageRegulatorOn(controlEnabled && enabled && isValidTargetV(targetV) && isValidTargetDeadband(targetDeadband));
    }

    private static DefaultValueDouble getDefaultTargetV(ShuntCompensator shuntCompensator) {
        return new DefaultValueDouble(null, shuntCompensator.getTargetV(), Double.NaN, Double.NaN);
    }

    private static DefaultValueDouble getDefaultTargetDeadband(ShuntCompensator shuntCompensator) {
        return new DefaultValueDouble(null, shuntCompensator.getTargetDeadband(), 0.0, 0.0);
    }

    private static DefaultValueBoolean getDefaultRegulatingOn(ShuntCompensator shuntCompensator) {
        return new DefaultValueBoolean(null, shuntCompensator.isVoltageRegulatorOn(), false, false);
    }

    private static boolean isDefaultRegulatingControl(ShuntCompensator shuntCompensator, boolean controlEnabled) {
        String regulatingControlId = shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL);
        return regulatingControlId == null && controlEnabled;
    }

    private static void setDefaultRegulatingControl(ShuntCompensator shuntCompensator) {
        shuntCompensator.setTargetV(Optional.ofNullable(shuntCompensator.getRegulatingTerminal().getBusView().getBus())
                        .map(Bus::getV)
                        .filter(v -> !Double.isNaN(v))
                        .orElse(shuntCompensator.getRegulatingTerminal().getVoltageLevel().getNominalV()))
                .setTargetDeadband(0.0)
                .setVoltageRegulatorOn(true); // SSH controlEnabled attribute is true when this method is called
    }
}
