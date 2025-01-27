/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.List;
import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForVscConverters;
import com.powsybl.cgmes.conversion.elements.AbstractReactiveLimitsOwnerConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.triplestore.api.PropertyBag;

import static com.powsybl.cgmes.conversion.Conversion.Config.DefaultValue.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class AcDcConverterConversion extends AbstractReactiveLimitsOwnerConversion {

    enum VscRegulation {
        REACTIVE_POWER,
        VOLTAGE
    }

    static final double DEFAULT_LOSS_FACTOR = 0.0;
    private static final double DEFAULT_POWER_FACTOR = 0.8;

    public AcDcConverterConversion(PropertyBag c, HvdcType converterType, String acDcConverterDcTerminalId, Context context) {
        super(CgmesNames.ACDC_CONVERTER, c, context);

        this.converterType = Objects.requireNonNull(converterType);
        this.acDcConverterDcTerminalId = Objects.requireNonNull(acDcConverterDcTerminalId);
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (converterType == null) {
            invalid("Type " + p.getLocal("type"));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        Objects.requireNonNull(converterType);
        if (converterType.equals(HvdcType.VSC)) {
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
        } else if (converterType.equals(HvdcType.LCC)) {

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
        identifiable.addAlias(acDcConverterDcTerminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal");
    }

    static void update(LccConverterStation lccConverter, PropertyBag cgmesDataConverter, double lossFactor, Context context) {
        lccConverter.setLossFactor((float) lossFactor);
        lccConverter.setPowerFactor((float) getPowerFactor(cgmesDataConverter, lccConverter, context));

        // TODO: There are two modes of control: dcVoltage and activePower
        // For dcVoltage, setpoint is targetUdc,
        // For activePower, setpoint is targetPpcc
    }

    private static double getPowerFactor(PropertyBag cgmesDataConverter, LccConverterStation lccConverter, Context context) {
        double p = cgmesDataConverter.asDouble("p");
        double q = cgmesDataConverter.asDouble("q");
        if (Double.isFinite(p) && Double.isFinite(q)) {
            double hypot = Math.hypot(p, q);
            return hypot != 0.0 ? p / hypot : getDefaultPowerFactor(lccConverter, context);
        } else {
            return getDefaultPowerFactor(lccConverter, context);
        }
    }

    private static double getDefaultPowerFactor(LccConverterStation lccConverter, Context context) {
        return defaultValue(Double.NaN, lccConverter.getPowerFactor(), DEFAULT_POWER_FACTOR, DEFAULT_POWER_FACTOR, getDefaultValueSelector(context));
    }

    static void update(VscConverterStation vscConverter, PropertyBag cgmesDataConverter, double lossFactor, Context context) {
        vscConverter.setLossFactor((float) lossFactor);

        VscRegulation vscRegulation = getVscRegulation(cgmesDataConverter, vscConverter, context);
        if (vscRegulation == VscRegulation.VOLTAGE) {
            double targetV = getTargetV(cgmesDataConverter, vscConverter, context);
            if (isValidTargetV(targetV)) {
                vscConverter.setVoltageSetpoint(targetV).setReactivePowerSetpoint(0.0).setVoltageRegulatorOn(true);
                return;
            }
        }
        double targetQ = getValidTargetQ(cgmesDataConverter, vscConverter, context);
        vscConverter.setVoltageRegulatorOn(false) // Turn off before modifying the targetV
                .setVoltageSetpoint(0.0)
                .setReactivePowerSetpoint(targetQ);
    }

    private static VscRegulation getVscRegulation(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        String qPccControl = cgmesDataConverter.getLocal("qPccControl");
        if (qPccControl != null) {
            if (qPccControl.endsWith("voltagePcc")) {
                return VscRegulation.VOLTAGE;
            } else if (qPccControl.endsWith("reactivePcc")) {
                return VscRegulation.REACTIVE_POWER;
            }
        }
        return getDefaultVscRegulation(vscConverter, context);
    }

    private static VscRegulation getDefaultVscRegulation(VscConverterStation vscConverter, Context context) {
        return switch (getDefaultValueSelectorForVscRegulation(context)) {
            case EQ, DEFAULT, EMPTY -> VscRegulation.REACTIVE_POWER;
            case PREVIOUS -> vscConverter.isVoltageRegulatorOn() ? VscRegulation.VOLTAGE : VscRegulation.REACTIVE_POWER;
        };
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelectorForVscRegulation(Context context) {
        return getDefaultValueSelector(List.of(PREVIOUS, DEFAULT), context);
    }

    private static double getTargetV(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        double targetV = cgmesDataConverter.asDouble("targetUpcc");
        return Double.isFinite(targetV) ? targetV : getDefaultTargetV(vscConverter, context);
    }

    private static double getDefaultTargetV(VscConverterStation vscConverter, Context context) {
        return defaultValue(Double.NaN, vscConverter.getVoltageSetpoint(), Double.NaN, Double.NaN, getDefaultValueSelector(context));
    }

    private static boolean isValidTargetV(double targetV) {
        return Double.isFinite(targetV) && targetV > 0.0;
    }

    private static double getValidTargetQ(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        double targetQ = -cgmesDataConverter.asDouble("targetQpcc");
        return Double.isFinite(targetQ) ? targetQ * getTerminalSign(vscConverter) : getDefaultValidTargetQ(vscConverter, context);
    }

    private static int getTerminalSign(VscConverterStation vscConverter) {
        String terminalSign = vscConverter.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN);
        return terminalSign != null ? Integer.parseInt(terminalSign) : 1;
    }

    private static double getDefaultValidTargetQ(VscConverterStation vscConverter, Context context) {
        double previousTargetQ = Double.isFinite(vscConverter.getReactivePowerSetpoint()) ? vscConverter.getReactivePowerSetpoint() : 0.0;
        return defaultValue(0.0, previousTargetQ, 0.0, 0.0, getDefaultValueSelector(context));
    }

    private static Conversion.Config.DefaultValue getDefaultValueSelector(Context context) {
        return getDefaultValueSelector(List.of(PREVIOUS, DEFAULT, EMPTY), context);
    }

    private final HvdcType converterType;
    private final String acDcConverterDcTerminalId;
}
