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
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

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

        boolean controlEnabled = cgmesData.asBoolean("controlEnabled", false);
        updateRegulatingControl(shuntCompensator, controlEnabled, context);
    }

    private static void updateSections(ShuntCompensator shuntCompensator, PropertyBag cgmesData, Context context) {
        int normalSections = Integer.parseInt(shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_SECTIONS));
        int sections = getSections(cgmesData, context).orElse(findDefaultSections(shuntCompensator, normalSections, context));
        shuntCompensator.setSectionCount(Math.min(sections, shuntCompensator.getMaximumSectionCount()));
    }

    private static OptionalInt getSections(PropertyBag cgmesData, Context context) {
        double sections = switch (context.config().getProfileForInitialValuesShuntSectionsTapPositions()) {
            case SSH -> cgmesData.asDouble("SSHsections", cgmesData.asDouble("SVsections"));
            case SV -> cgmesData.asDouble("SVsections", cgmesData.asDouble("SSHsections"));
        };
        return Double.isFinite(sections) ? OptionalInt.of(Math.abs(fromContinuous(sections))) : OptionalInt.empty();
    }

    private static int findDefaultSections(ShuntCompensator shuntCompensator, int normalSections, Context context) {
        return defaultValue(normalSections, shuntCompensator.getSectionCount(), 0, 0, getDefaultValueSelectorForSections(context));
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelectorForSections(Context context) {
        return getDefaultValueSelector(List.of(EQ, PREVIOUS, DEFAULT), context);
    }

    private static void updateRegulatingControl(ShuntCompensator shuntCompensator, boolean controlEnabled, Context context) {
        if (isDefaultRegulatingControl(shuntCompensator, controlEnabled)) {
            setDefaultRegulatingControl(shuntCompensator);
            return;
        }

        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(shuntCompensator, context);
        double targetV = cgmesRegulatingControl.map(ShuntConversion::findTargetV).orElseGet(() -> findDefaultTargetV(shuntCompensator, context));
        double targetDeadband = cgmesRegulatingControl.map(propertyBag -> findTargetDeadband(propertyBag, shuntCompensator, context)).orElseGet(() -> findDefaultTargetDeadband(shuntCompensator, context));
        boolean enabled = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, shuntCompensator, context)).orElse(findDefaultRegulatingOn(shuntCompensator, context));

        shuntCompensator.setTargetV(targetV)
                .setTargetDeadband(targetDeadband)
                .setVoltageRegulatorOn(controlEnabled && enabled && isValidTargetV(targetV) && isValidTargetDeadband(targetDeadband));
    }

    private static boolean isDefaultRegulatingControl(ShuntCompensator shuntCompensator, boolean controlEnabled) {
        String regulatingControlId = shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL);
        return regulatingControlId == null && controlEnabled;
    }

    private static Optional<PropertyBag> findCgmesRegulatingControl(ShuntCompensator shuntCompensator, Context context) {
        String regulatingControlId = shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL);
        return regulatingControlId != null ? Optional.ofNullable(context.regulatingControl(regulatingControlId)) : Optional.empty();
    }

    private static void setDefaultRegulatingControl(ShuntCompensator shuntCompensator) {
        shuntCompensator.setTargetV(Optional.ofNullable(shuntCompensator.getRegulatingTerminal().getBusView().getBus())
                        .map(Bus::getV)
                        .filter(v -> !Double.isNaN(v))
                        .orElseGet(() -> shuntCompensator.getRegulatingTerminal().getVoltageLevel().getNominalV()))
                .setTargetDeadband(0.0)
                .setVoltageRegulatorOn(true); // SSH controlEnabled attribute is true when this method is called
    }

    private static double findTargetV(PropertyBag regulatingControl) {
        return regulatingControl.asDouble("targetValue");
    }

    private static double findDefaultTargetV(ShuntCompensator shuntCompensator, Context context) {
        return defaultValue(Double.NaN, shuntCompensator.getTargetV(), Double.NaN, Double.NaN, getDefaultValueSelector(context));
    }

    private static boolean isValidTargetV(double targetV) {
        return Double.isFinite(targetV) && targetV > 0.0;
    }

    private static double findTargetDeadband(PropertyBag regulatingControl, ShuntCompensator shuntCompensator, Context context) {
        return regulatingControl.containsKey("targetDeadband") ? regulatingControl.asDouble("targetDeadband") : findDefaultTargetDeadband(shuntCompensator, context);
    }

    private static double findDefaultTargetDeadband(ShuntCompensator shuntCompensator, Context context) {
        return defaultValue(Double.NaN, shuntCompensator.getTargetDeadband(), 0.0, 0.0, getDefaultValueSelector(context));
    }

    private static boolean isValidTargetDeadband(double targetDeadband) {
        return Double.isFinite(targetDeadband);
    }

    private static boolean findRegulatingOn(PropertyBag regulatingControl, ShuntCompensator shuntCompensator, Context context) {
        return regulatingControl.asBoolean("enabled").orElse(findDefaultRegulatingOn(shuntCompensator, context));
    }

    private static boolean findDefaultRegulatingOn(ShuntCompensator shuntCompensator, Context context) {
        return defaultValue(false, shuntCompensator.isVoltageRegulatorOn(), false, false, getDefaultValueSelector(context));
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelector(Context context) {
        return getDefaultValueSelector(List.of(PREVIOUS, DEFAULT, EMPTY), context);
    }
}
