/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForVscConverters;
import com.powsybl.cgmes.conversion.elements.AbstractReactiveLimitsOwnerConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;

import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class HvdcConverterConversion extends AbstractReactiveLimitsOwnerConversion {

    private final PropertyBag converter;

    enum VscRegulation {
        REACTIVE_POWER,
        VOLTAGE
    }

    private static final String TARGET_QPCC = "targetQpcc";
    private static final String REACTIVE_PCC = "reactivePcc";
    private static final String VOLTAGE_PCC = "voltagePcc";
    private static final String TARGET_UPCC = "targetUpcc";

    private static final double DEFAULT_LOSS_FACTOR = 0.0;
    private static final double DEFAULT_POWER_FACTOR = 0.8;

    public HvdcConverterConversion(PropertyBag converter, Context context) {
        super(CgmesNames.ACDC_CONVERTER, converter, context);

        this.converter = converter;
    }

    @Override
    public void convert() {
        if (HvdcType.VSC == getHvdcType()) {
            VscConverterStationAdder adder = voltageLevel().newVscConverterStation()
                    .setLossFactor((float) DEFAULT_LOSS_FACTOR);
            identify(adder);
            connectWithOnlyEq(adder);
            RegulatingControlMappingForVscConverters.initialize(adder);
            VscConverterStation c = adder.add();

            addAliasesAndProperties(c);
            convertedTerminalsWithOnlyEq(c.getTerminal());
            convertReactiveLimits(c);
            context.regulatingControlMapping().forVscConverters().add(c.getId(), p);
        } else {
            LccConverterStationAdder adder = voltageLevel().newLccConverterStation()
                    .setLossFactor((float) DEFAULT_LOSS_FACTOR)
                    .setPowerFactor((float) DEFAULT_POWER_FACTOR);
            identify(adder);
            connectWithOnlyEq(adder);
            LccConverterStation c = adder.add();

            addAliasesAndProperties(c);
            convertedTerminalsWithOnlyEq(c.getTerminal());
        }
    }

    @Override
    protected void addAliasesAndProperties(Identifiable<?> identifiable) {
        super.addAliasesAndProperties(identifiable);
        identifiable.addAlias(converter.getId(DC_TERMINAL1), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1);
        identifiable.addAlias(converter.getId(DC_TERMINAL2), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2);
    }

    private HvdcType getHvdcType() {
        if (VS_CONVERTER.equals(converter.getLocal("type"))) {
            return HvdcType.VSC;
        } else {
            return HvdcType.LCC;
        }
    }

    static void update(LccConverterStation lccConverter, PropertyBag cgmesDataConverter, double lossFactor, Context context) {
        lccConverter.setLossFactor((float) lossFactor);
        lccConverter.setPowerFactor((float) getPowerFactor(cgmesDataConverter, lccConverter, context));
    }

    private static double getPowerFactor(PropertyBag cgmesDataConverter, LccConverterStation lccConverter, Context context) {
        double defaultPowerFactor = getDefaultPowerFactor(lccConverter, context);

        double p = cgmesDataConverter.asDouble("p");
        double q = cgmesDataConverter.asDouble("q");
        double powerFactor = p / Math.hypot(p, q);
        if (Double.isNaN(powerFactor)) {
            return defaultPowerFactor;
        }
        return powerFactor;
    }

    private static double getDefaultPowerFactor(LccConverterStation lccConverter, Context context) {
        return getDefaultValue(null, (double) lccConverter.getPowerFactor(), DEFAULT_POWER_FACTOR, DEFAULT_POWER_FACTOR, context);
    }

    static void update(VscConverterStation vscConverter, PropertyBag cgmesDataConverter, double lossFactor, Context context) {
        vscConverter.setLossFactor((float) lossFactor);

        VscRegulation vscRegulation = getVscRegulation(cgmesDataConverter, vscConverter, context);
        if (vscRegulation == VscRegulation.VOLTAGE) {
            double defaultTargetV = getDefaultTargetV(vscConverter, context);
            double targetV = findTargetV(cgmesDataConverter, TARGET_UPCC, defaultTargetV, DefaultValueUse.NOT_DEFINED);
            if (isValidTargetV(targetV)) {
                // TargetV must be valid before enabling regulation,
                vscConverter.setVoltageSetpoint(targetV)
                        .setReactivePowerSetpoint(0.0)
                        .setVoltageRegulatorOn(true);
                return;
            }
        }

        // Regulation must be turned off before assigning potentially invalid values,
        // to ensure consistency with the applied checks
        double targetQ = getValidTargetQ(cgmesDataConverter, vscConverter, context);
        vscConverter.setVoltageRegulatorOn(false)
                .setVoltageSetpoint(0.0)
                .setReactivePowerSetpoint(targetQ);
    }

    private static VscRegulation getVscRegulation(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        String qPccControl = cgmesDataConverter.getLocal("qPccControl");
        if (qPccControl != null) {
            if (qPccControl.endsWith(VOLTAGE_PCC)) {
                return VscRegulation.VOLTAGE;
            } else if (qPccControl.endsWith(REACTIVE_PCC)) {
                return VscRegulation.REACTIVE_POWER;
            }
        }
        return getDefaultVscRegulation(vscConverter, context);
    }

    private static VscRegulation getDefaultVscRegulation(VscConverterStation vscConverter, Context context) {
        boolean defaultVoltageRegulationOn = getDefaultVoltageRegulationOn(vscConverter, context);
        return defaultVoltageRegulationOn ? VscRegulation.VOLTAGE : VscRegulation.REACTIVE_POWER;
    }

    private static boolean getDefaultVoltageRegulationOn(VscConverterStation vscConverter, Context context) {
        return getDefaultValue(false, vscConverter.isVoltageRegulatorOn(), false, false, context);
    }

    private static double getDefaultTargetV(VscConverterStation vscConverter, Context context) {
        return getDefaultValue(null, vscConverter.getVoltageSetpoint(), Double.NaN, Double.NaN, context);
    }

    // targetQ = - targetQpcc then we considered - terminalSign
    private static double getValidTargetQ(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        double defaultTargetQ = getDefaultTargetQ(vscConverter, context);
        return findTargetQ(cgmesDataConverter, TARGET_QPCC, -findTerminalSign(vscConverter), defaultTargetQ, DefaultValueUse.NOT_VALID);
    }

    private static double getDefaultTargetQ(VscConverterStation vscConverter, Context context) {
        double previousTargetQ = Double.isFinite(vscConverter.getReactivePowerSetpoint()) ? vscConverter.getReactivePowerSetpoint() : 0.0;
        return getDefaultValue(0.0, previousTargetQ, 0.0, 0.0, context);
    }
}
