/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.AbstractReactiveLimitsOwnerConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

import static com.powsybl.cgmes.conversion.Conversion.ALIAS_DC_TERMINAL1;
import static com.powsybl.cgmes.conversion.Conversion.ALIAS_DC_TERMINAL2;
import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class HvdcConverterConversion extends AbstractReactiveLimitsOwnerConversion {

    private final PropertyBag converter;

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
        identifiable.addAlias(converter.getId(DC_TERMINAL1), ALIAS_DC_TERMINAL1);
        identifiable.addAlias(converter.getId(DC_TERMINAL2), ALIAS_DC_TERMINAL2);
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

        double defaultLocalTargetQ = getDefaultValue(null, vscConverter.getLocalTargetQ(), Double.NaN, Double.NaN, context);
        double localTargetQ = cgmesDataConverter.asDouble("q");
        localTargetQ = Double.isNaN(localTargetQ) ? defaultLocalTargetQ : localTargetQ;
        vscConverter.setLocalTargetQ(localTargetQ);

        double defaultLocalTargetV = getDefaultLocalTargetV(vscConverter, context);

        Optional<Terminal> regulatingTerminal = Optional.ofNullable(vscConverter.getVoltageRegulation()).map(VoltageRegulation::getTerminal);
        RegulationMode mode = getVscRegulationMode(cgmesDataConverter, vscConverter, context);
        if (mode == RegulationMode.VOLTAGE) {
            double defaultTargetV = getDefaultTargetV(vscConverter, context);
            double targetV = findTargetV(cgmesDataConverter, TARGET_UPCC, defaultTargetV, DefaultValueUse.NOT_DEFINED);
            if (isValidTargetV(targetV)) {
                // TargetV must be valid before enabling regulation,
                VoltageRegulationBuilder voltageRegulationBuilder = vscConverter.newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE);
                if (regulatingTerminal.isPresent() && regulatingTerminal.get() != vscConverter.getTerminal()) {
                    voltageRegulationBuilder
                        .withTerminal(regulatingTerminal.get())
                        .withTargetValue(targetV);
                    vscConverter.setLocalTargetV(defaultLocalTargetV);
                } else {
                    vscConverter.setLocalTargetV(targetV);
                }
                voltageRegulationBuilder.build();
            }
        } else if (mode == RegulationMode.REACTIVE_POWER) {
            double targetQ = getValidTargetQ(cgmesDataConverter, vscConverter, context);
            vscConverter.newVoltageRegulation()
                .withMode(RegulationMode.REACTIVE_POWER)
                // always set the terminal in case of reactive power regulation
                .withTerminal(regulatingTerminal.orElse(vscConverter.getTerminal()))
                .withTargetValue(targetQ)
                .build();
            vscConverter.setLocalTargetV(defaultLocalTargetV);
        }
    }

    private static RegulationMode getVscRegulationMode(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        String qPccControl = cgmesDataConverter.getLocal("qPccControl");
        if (qPccControl != null) {
            if (qPccControl.endsWith(VOLTAGE_PCC)) {
                return RegulationMode.VOLTAGE;
            } else if (qPccControl.endsWith(REACTIVE_PCC)) {
                return RegulationMode.REACTIVE_POWER;
            }
        }
        return getDefaultVscRegulationMode(vscConverter, context);
    }

    private static RegulationMode getDefaultVscRegulationMode(VscConverterStation vscConverter, Context context) {
        boolean defaultVoltageRegulationOn = getDefaultVoltageRegulationOn(vscConverter, context);
        return defaultVoltageRegulationOn ? RegulationMode.VOLTAGE : RegulationMode.REACTIVE_POWER;
    }

    private static boolean getDefaultVoltageRegulationOn(VscConverterStation vscConverter, Context context) {
        return getDefaultValue(false, vscConverter.isRegulatingWithMode(RegulationMode.VOLTAGE), false, false, context);
    }

    private static double getDefaultLocalTargetV(VscConverterStation vscConverter, Context context) {
        return getDefaultValue(null, vscConverter.getLocalTargetV(), Double.NaN, Double.NaN, context);
    }

    private static double getDefaultTargetV(VscConverterStation vscConverter, Context context) {
        return getDefaultValue(null, vscConverter.getRegulatingTargetV(), Double.NaN, Double.NaN, context);
    }

    // targetQ = - targetQpcc then we considered - terminalSign
    private static double getValidTargetQ(PropertyBag cgmesDataConverter, VscConverterStation vscConverter, Context context) {
        double defaultTargetQ = getDefaultTargetQ(vscConverter, context);
        return findTargetQ(cgmesDataConverter, TARGET_QPCC, findTerminalSign(vscConverter), defaultTargetQ, DefaultValueUse.NOT_VALID);
    }

    private static double getDefaultTargetQ(VscConverterStation vscConverter, Context context) {
        double previousTargetQ = Double.isFinite(vscConverter.getRegulatingTargetQ()) ? vscConverter.getRegulatingTargetQ() : 0.0;
        return getDefaultValue(0.0, previousTargetQ, 0.0, 0.0, context);
    }
}
