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

    static void update(LccConverterStation lccConverter, PropertyBag cgmesDataAcDcConverter, double lossFactor) {
        lccConverter.setLossFactor((float) lossFactor);
        lccConverter.setPowerFactor((float) getPowerFactor(cgmesDataAcDcConverter));

        // TODO: There are two modes of control: dcVoltage and activePower
        // For dcVoltage, setpoint is targetUdc,
        // For activePower, setpoint is targetPpcc
    }

    private static double getPowerFactor(PropertyBag cgmesDataAcDcConverter) {
        double p = cgmesDataAcDcConverter.asDouble("p");
        double q = cgmesDataAcDcConverter.asDouble("q");
        if (Double.isFinite(p) && Double.isFinite(q)) {
            double hypot = Math.hypot(p, q);
            return hypot != 0.0 ? p / hypot : DEFAULT_POWER_FACTOR;
        } else {
            return DEFAULT_POWER_FACTOR;
        }
    }

    static void update(VscConverterStation vscConverter, PropertyBag cconverter, double lossFactor, Context context) {
        vscConverter.setLossFactor((float) lossFactor);

        int terminalSign = getTerminalSign(vscConverter.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN));
        String vscRegulation = cconverter.getLocal("qPccControl");
        double voltageSetpoint = cconverter.asDouble("targetUpcc");
        double reactivePowerSetpoint = -cconverter.asDouble("targetQpcc");
        RC rc = new RC(vscRegulation, voltageSetpoint, reactivePowerSetpoint, terminalSign);
        updateRegulatingControl(vscConverter, rc, context);
    }

    private static int getTerminalSign(String terminalSign) {
        return terminalSign != null ? Integer.parseInt(terminalSign) : 1;
    }

    private static void updateRegulatingControl(VscConverterStation vscConverter, RC rc, Context context) {
        if (rc == null) {
            return;
        }
        VscRegulation vscRegulation = decodeVscRegulation(rc.vscRegulation);
        if (vscRegulation == VscRegulation.VOLTAGE) {
            setRegulatingControlVoltage(rc, vscConverter);
        } else if (vscRegulation == VscRegulation.REACTIVE_POWER) {
            setRegulatingControlReactivePower(rc, vscConverter);
        } else {
            String what = rc.vscRegulation;
            if (rc.vscRegulation == null) {
                what = "EmptyVscRegulation";
            }
            context.ignored(what, "Unsupported regulation mode for vscConverter " + vscConverter.getId());
        }
    }

    private static void setRegulatingControlVoltage(RC rc, VscConverterStation vscConverter) {
        vscConverter
                .setVoltageSetpoint(rc.voltageSetpoint)
                .setReactivePowerSetpoint(0.0)
                .setVoltageRegulatorOn(true);
    }

    private static void setRegulatingControlReactivePower(RC rc, VscConverterStation vscConverter) {
        vscConverter
                .setVoltageRegulatorOn(false) // Turn off before modifying the targetV
                .setVoltageSetpoint(0.0)
                .setReactivePowerSetpoint(rc.reactivePowerSetpoint * rc.terminalSign);
    }

    private static VscRegulation decodeVscRegulation(String qPccControl) {
        if (qPccControl != null) {
            if (qPccControl.endsWith("voltagePcc")) {
                return VscRegulation.VOLTAGE;
            } else if (qPccControl.endsWith("reactivePcc")) {
                return VscRegulation.REACTIVE_POWER;
            }
        }
        return null;
    }

    private record RC(String vscRegulation, double voltageSetpoint, double reactivePowerSetpoint, int terminalSign) {
    }

    private final HvdcType converterType;
    private final String acDcConverterDcTerminalId;
}
