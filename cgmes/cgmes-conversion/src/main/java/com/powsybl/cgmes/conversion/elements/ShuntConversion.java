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
            PropertyBags ss = context.nonlinearShuntCompensatorPoints(id);
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

        Boolean controlEnabled = cgmesData.asBoolean(CgmesNames.CONTROL_ENABLED).orElse(null);
        updateRegulatingControl(shuntCompensator, controlEnabled, context);
    }

    private static void updateSections(ShuntCompensator shuntCompensator, PropertyBag cgmesData, Context context) {
        int defaultSections = getDefaultSections(shuntCompensator, getNormalSections(shuntCompensator), context);
        int sections = getSections(cgmesData).orElse(defaultSections);
        shuntCompensator.setSectionCount(Math.min(sections, shuntCompensator.getMaximumSectionCount()));
        getSolvedSections(cgmesData).ifPresentOrElse(shuntCompensator::setSolvedSectionCount, shuntCompensator::unsetSolvedSectionCount);
    }

    private static Integer getNormalSections(ShuntCompensator shuntCompensator) {
        String property = shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_SECTIONS);
        return property != null ? Integer.parseInt(property) : null;
    }

    private static OptionalInt getSections(PropertyBag cgmesData) {
        double sections = cgmesData.asDouble("SSHsections", cgmesData.asDouble("SVsections"));
        return Double.isFinite(sections) ? OptionalInt.of(Math.abs(fromContinuous(sections))) : OptionalInt.empty();
    }

    private static OptionalInt getSolvedSections(PropertyBag cgmesData) {
        double sections = cgmesData.asDouble("SVsections");
        return Double.isFinite(sections) ? OptionalInt.of(Math.abs(fromContinuous(sections))) : OptionalInt.empty();
    }

    private static int getDefaultSections(ShuntCompensator shuntCompensator, Integer normalSections, Context context) {
        return getDefaultValue(normalSections, shuntCompensator.getSectionCount(), 0, 0, context);
    }

    private static void updateRegulatingControl(ShuntCompensator shuntCompensator, Boolean controlEnabled, Context context) {
        // When the equipment is participating in regulating control (controlEnabled is true),
        // but no regulating control data is found, default regulation data will be created

        boolean defaultRegulatingOn = getDefaultRegulatingOn(shuntCompensator, context);
        boolean updatedControlEnabled = controlEnabled != null ? controlEnabled : defaultRegulatingOn;
        if (isDefaultRegulatingControl(shuntCompensator, updatedControlEnabled)) {
            setDefaultRegulatingControl(shuntCompensator);
            return;
        }

        double defaultTargetV = getDefaultTargetV(shuntCompensator, context);
        double defaultTargetDeadband = getDefaultTargetDeadband(shuntCompensator, context);

        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(shuntCompensator, context);
        double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetV);
        double targetDeadband = cgmesRegulatingControl.map(propertyBag -> findTargetDeadband(propertyBag, defaultTargetDeadband, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetDeadband);
        boolean enabled = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

        setRegulation(shuntCompensator, targetV, targetDeadband, updatedControlEnabled && enabled && isValidTargetV(targetV) && isValidTargetDeadband(targetDeadband));
    }

    // Regulation values (targetV and targetDeadband) must be valid before enabling it,
    // and regulation must be turned off before assigning potentially invalid values,
    // to ensure consistency with the applied checks
    private static void setRegulation(ShuntCompensator shuntCompensator, double targetV, double targetDeadband, boolean regulatingOn) {
        if (regulatingOn) {
            shuntCompensator.setTargetV(targetV)
                    .setTargetDeadband(targetDeadband)
                    .setVoltageRegulatorOn(true);
        } else {
            shuntCompensator
                    .setVoltageRegulatorOn(false)
                    .setTargetV(targetV)
                    .setTargetDeadband(targetDeadband);
        }
    }

    private static double getDefaultTargetV(ShuntCompensator shuntCompensator, Context context) {
        return getDefaultValue(null, shuntCompensator.getTargetV(), Double.NaN, Double.NaN, context);
    }

    private static double getDefaultTargetDeadband(ShuntCompensator shuntCompensator, Context context) {
        return getDefaultValue(null, shuntCompensator.getTargetDeadband(), 0.0, 0.0, context);
    }

    private static boolean getDefaultRegulatingOn(ShuntCompensator shuntCompensator, Context context) {
        return getDefaultValue(null, shuntCompensator.isVoltageRegulatorOn(), false, false, context);
    }

    private static boolean isDefaultRegulatingControl(ShuntCompensator shuntCompensator, boolean controlEnabled) {
        String regulatingControlId = shuntCompensator.getProperty(Conversion.PROPERTY_REGULATING_CONTROL);
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
