/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForVscConverters;
import com.powsybl.cgmes.conversion.elements.AbstractReactiveLimitsOwnerConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.triplestore.api.PropertyBag;

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
        DefaultValueDouble defaultPowerFactor = getDefaultPowerFactor(lccConverter);

        double p = cgmesDataConverter.asDouble("p");
        double q = cgmesDataConverter.asDouble("q");
        if (Double.isFinite(p) && Double.isFinite(q)) {
            double hypot = Math.hypot(p, q);
            return hypot != 0.0 ? p / hypot : defaultValue(defaultPowerFactor, context);
        } else {
            return defaultValue(defaultPowerFactor, context);
        }
    }

    private static DefaultValueDouble getDefaultPowerFactor(LccConverterStation lccConverter) {
        return new DefaultValueDouble(null, (double) lccConverter.getPowerFactor(), DEFAULT_POWER_FACTOR, DEFAULT_POWER_FACTOR);
    }

    static void update(VscConverterStation vscConverter, PropertyBag cgmesDataConverter, double lossFactor, Context context) {
        vscConverter.setLossFactor((float) lossFactor);

        VscRegulation vscRegulation = getVscRegulation(cgmesDataConverter, vscConverter, context);
        if (vscRegulation == VscRegulation.VOLTAGE) {
            DefaultValueDouble defaultTargetV = getDefaultTargetV(vscConverter);
            double targetV = findTargetV(cgmesDataConverter, "targetUpcc", defaultTargetV, DefaultValueUse.NOT_DEFINED, context);
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
        DefaultValueBoolean defaultVoltageRegulationOn = getDefaultVoltageRegulationOn(vscConverter);
        return defaultValue(defaultVoltageRegulationOn, context) ? VscRegulation.VOLTAGE : VscRegulation.REACTIVE_POWER;
    }

    private static DefaultValueBoolean getDefaultVoltageRegulationOn(VscConverterStation vscConverter) {
        return new DefaultValueBoolean(false, vscConverter.isVoltageRegulatorOn(), false, false);
    }

    private static DefaultValueDouble getDefaultTargetV(VscConverterStation vscConverter) {
        return new DefaultValueDouble(null, vscConverter.getVoltageSetpoint(), Double.NaN, Double.NaN);
    }

    // targetQ = - targetQpcc then we considered - terminalSign
    private static double getValidTargetQ(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        DefaultValueDouble defaultTargetQ = getDefaultTargetQ(vscConverter);
        return findTargetQ(cgmesDataConverter, "targetQpcc", -findTerminalSign(vscConverter), defaultTargetQ, DefaultValueUse.NOT_VALID, context);
    }

    private static DefaultValueDouble getDefaultTargetQ(VscConverterStation vscConverter) {
        double previousTargetQ = Double.isFinite(vscConverter.getReactivePowerSetpoint()) ? vscConverter.getReactivePowerSetpoint() : 0.0;
        return new DefaultValueDouble(0.0, previousTargetQ, 0.0, 0.0);
    }

    private final HvdcType converterType;
    private final String acDcConverterDcTerminalId;
}
