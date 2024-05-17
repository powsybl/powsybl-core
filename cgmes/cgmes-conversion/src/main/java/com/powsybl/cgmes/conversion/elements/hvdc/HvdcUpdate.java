/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import com.powsybl.cgmes.conversion.*;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.*;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.cgmes.conversion.elements.hvdc.AcDcConverterConversion.DEFAULT_POWER_FACTOR;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class HvdcUpdate extends AbstractIdentifiedObjectConversion {
    enum VscRegulation {
        REACTIVE_POWER,
        VOLTAGE
    }

    private static final String TARGET_PPCC = "targetPpcc";
    private static final String POLE_LOSS_P = "poleLossP";
    private static final String OPERATING_MODE = "operatingMode";
    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    private final String hvdcLineId;

    public HvdcUpdate(String hvdcLineId, PropertyBags acDcConverters, Context context) {
        super("AcDcConverters", acDcConverters, context);
        this.hvdcLineId = Objects.requireNonNull(hvdcLineId);
    }

    @Override
    public boolean valid() {
        throw new ConversionException("Unsupported method");
    }

    @Override
    public void convert() {
        throw new ConversionException("Unsupported method");
    }

    @Override
    public void update(Network network) {
        HvdcLine hvdcLine = network.getHvdcLine(hvdcLineId);
        if (hvdcLine == null) {
            return;
        }
        PropertyBag p1 = findEndPropertyBag(hvdcLine.getConverterStation1().getId());
        PropertyBag p2 = findEndPropertyBag(hvdcLine.getConverterStation2().getId());
        if (p1 == null || p2 == null) {
            return;
        }
        updateHvdcLine(hvdcLine, p1, p2, context);
    }

    private PropertyBag findEndPropertyBag(String acDcConverterId) {
        return ps.stream().filter(p -> acDcConverterId.equals(p.getId("ACDCConverter"))).findFirst().orElse(null);
    }

    // poleLossP is the active power loss at a DC Pole
    // for lossless operation: P(DC) = P(AC) => lossFactor = 0
    // for rectifier operation (conversion from AC to DC) with losses: P(DC) = P(AC) - poleLossP
    // In IIDM, for rectifier operation P(DC) / P(AC) = 1 - lossFactor / 100
    // => P(DC) / P(AC) = 1 - poleLossP / P(AC) = 1 - lossFactor / 100
    // for inverter operation (conversion from DC to AC) with losses: P(DC) = P(AC) + poleLossP
    // In IIDM, for inverter operation P(AC) / P(DC) = 1 - lossFactor / 100
    // => P(AC) / P(DC) = 1 - poleLossP / P(DC) = 1 - poleLossP / (P(AC) + poleLossP) = 1 - lossFactor / 100
    private void updateHvdcLine(HvdcLine hvdcLine, PropertyBag cconverter1, PropertyBag cconverter2, Context context) {
        HvdcConverterStation.HvdcType hvdcType = findHvdcType(hvdcLine);
        HvdcLine.ConvertersMode operatingMode = decodeMode(hvdcType, cconverter1, cconverter2);

        double poleLossP1 = cconverter1.asDouble(POLE_LOSS_P, 0.0);
        double poleLossP2 = cconverter2.asDouble(POLE_LOSS_P, 0.0);

        // load sign convention is used i.e. positive sign means flow out from a node
        // i.e. pACx >= 0 if converterx is rectifier and pACx <= 0 if converterx is
        // inverter

        double pAC1 = getPAc(cconverter1);
        double pAC2 = getPAc(cconverter2);

        LossFactor lossFactor = new LossFactor(context, operatingMode, pAC1, pAC2, poleLossP1, poleLossP2);
        lossFactor.compute();

        // arbitrary value because there is no maxP attribute in CGMES
        double maxP = getMaxP(pAC1, pAC2, operatingMode);
        missing("maxP", maxP);
        double activePowerSetpoint = getActivePowerSetpoint(pAC1, pAC2, poleLossP1, poleLossP2, operatingMode);

        hvdcLine.setConvertersMode(operatingMode);
        hvdcLine.setActivePowerSetpoint(activePowerSetpoint);
        hvdcLine.setMaxP(maxP);

        if (hvdcType.equals(HvdcConverterStation.HvdcType.LCC)) {
            updateLccConverterStation((LccConverterStation) hvdcLine.getConverterStation1(), cconverter1, lossFactor.getLossFactor1());
            updateLccConverterStation((LccConverterStation) hvdcLine.getConverterStation2(), cconverter2, lossFactor.getLossFactor2());
        } else {
            updateVscConverterStation((VscConverterStation) hvdcLine.getConverterStation1(), cconverter1, lossFactor.getLossFactor1(), context);
            updateVscConverterStation((VscConverterStation) hvdcLine.getConverterStation2(), cconverter2, lossFactor.getLossFactor2(), context);
        }
    }

    private static HvdcConverterStation.HvdcType findHvdcType(HvdcLine hvdcLine) {
        if (hvdcLine.getConverterStation1().getHvdcType().equals(HvdcConverterStation.HvdcType.LCC)
                && hvdcLine.getConverterStation2().getHvdcType().equals(HvdcConverterStation.HvdcType.LCC)) {
            return HvdcConverterStation.HvdcType.LCC;
        } else if (hvdcLine.getConverterStation1().getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)
                && hvdcLine.getConverterStation2().getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
            return HvdcConverterStation.HvdcType.VSC;
        } else {
            throw new CgmesModelException("Unexpected Hvdc configuration type. HvdcConverterStation1: "
                    + hvdcLine.getConverterStation1().getHvdcType().name() + " HvdcConverterStation2: "
                    + hvdcLine.getConverterStation2().getHvdcType().name());
        }
    }

    private static HvdcLine.ConvertersMode decodeMode(HvdcConverterStation.HvdcType converterType, PropertyBag cconverter1, PropertyBag cconverter2) {
        String mode1 = cconverter1.getLocal(OPERATING_MODE);
        String mode2 = cconverter2.getLocal(OPERATING_MODE);

        if (converterType.equals(HvdcConverterStation.HvdcType.LCC)) {
            if (inverter(mode1) && rectifier(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            } else if (rectifier(mode1) && inverter(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            } else if (cconverter1.asDouble(TARGET_PPCC) == 0 && cconverter2.asDouble(TARGET_PPCC) == 0) {
                // Both ends are rectifier or inverter
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            } else {
                LOG.warn("Undefined converter mode for the HVDC, assumed to be of type \"Side1 Rectifier - Side2 Inverter\"");
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            }
        } else {
            if (cconverter1.asDouble(TARGET_PPCC) > 0 || cconverter2.asDouble(TARGET_PPCC) < 0) {
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            } else {
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            }
        }
    }

    private static boolean inverter(String operatingMode) {
        return operatingMode != null && operatingMode.toLowerCase().endsWith("inverter");
    }

    private static boolean rectifier(String operatingMode) {
        return operatingMode != null && operatingMode.toLowerCase().endsWith("rectifier");
    }

    private static double getPAc(PropertyBag p) {
        // targetPpcc is the real power injection target in the AC grid in CGMES
        return Double.isNaN(p.asDouble(TARGET_PPCC)) ? 0 : p.asDouble(TARGET_PPCC);
    }

    private static double getMaxP(double pAC1, double pAC2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            if (pAC1 != 0) {
                return DEFAULT_MAXP_FACTOR * pAC1;
            }
            return DEFAULT_MAXP_FACTOR * Math.abs(pAC2);
        }
        if (pAC2 != 0) {
            return DEFAULT_MAXP_FACTOR * pAC2;
        }
        return DEFAULT_MAXP_FACTOR * Math.abs(pAC1);
    }

    private static double getActivePowerSetpoint(double pAC1, double pAC2, double poleLossP1, double poleLossP2,
                                                 HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            if (pAC1 != 0) {
                return pAC1;
            } else if (pAC2 != 0) {
                return Math.abs(pAC2) + poleLossP2 + poleLossP1;
            }
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)) {
            if (pAC2 != 0) {
                return pAC2;
            } else if (pAC1 != 0) {
                return Math.abs(pAC1) + poleLossP1 + poleLossP2;
            }
        }
        return 0;
    }

    private void updateLccConverterStation(LccConverterStation lccConverter, PropertyBag cconverter, double lossFactor) {
        lccConverter.setLossFactor((float) lossFactor);
        lccConverter.setPowerFactor((float) getPowerFactor(cconverter));

        // TODO: There are two modes of control: dcVoltage and activePower
        // For dcVoltage, setpoint is targetUdc,
        // For activePower, setpoint is targetPpcc
    }

    private static double getPowerFactor(PropertyBag cconverter) {
        double p = cconverter.asDouble("p");
        double q = cconverter.asDouble("q");
        if (Double.isFinite(p) && Double.isFinite(q)) {
            double hypot = Math.hypot(p, q);
            return hypot != 0.0 ? p / hypot : DEFAULT_POWER_FACTOR;
        } else {
            return DEFAULT_POWER_FACTOR;
        }
    }

    private static void updateVscConverterStation(VscConverterStation vscConverter, PropertyBag cconverter, double lossFactor, Context context) {
        vscConverter.setLossFactor((float) lossFactor);

        int terminalSign = Integer.getInteger(vscConverter.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "terminalSign"), 1);
        String vscRegulation = cconverter.getLocal("qPccControl");
        double voltageSetpoint = cconverter.asDouble("targetUpcc");
        double reactivePowerSetpoint = -cconverter.asDouble("targetQpcc");
        RC rc = new RC(vscRegulation, voltageSetpoint, reactivePowerSetpoint, terminalSign);
        updateRegulatingControl(vscConverter, rc, context);
    }

    private static void updateRegulatingControl(VscConverterStation vscConverter, RC rc, Context context) {
        if (rc == null) {
            return;
        }
        VscRegulation vscRegulation = decodeVscRegulation(rc.vscRegulation);
        if (vscRegulation == VscRegulation.VOLTAGE) {
            setRegulatingControlVoltage(rc, vscConverter, context);
        } else if (vscRegulation == VscRegulation.REACTIVE_POWER) {
            setRegulatingControlReactivePower(rc, vscConverter, context);
        } else {
            String what = rc.vscRegulation;
            if (rc.vscRegulation == null) {
                what = "EmptyVscRegulation";
            }
            context.ignored(what, "Unsupported regulation mode for vscConverter " + vscConverter.getId());
        }
    }

    private static void setRegulatingControlVoltage(RC rc, VscConverterStation vscConverter, Context context) {
        vscConverter
                .setVoltageSetpoint(rc.voltageSetpoint)
                .setReactivePowerSetpoint(0.0)
                .setVoltageRegulatorOn(true);
    }

    private static void setRegulatingControlReactivePower(RC rc, VscConverterStation vscConverter, Context context) {
        vscConverter
                .setVoltageSetpoint(0.0)
                .setReactivePowerSetpoint(rc.reactivePowerSetpoint * rc.terminalSign)
                .setVoltageRegulatorOn(false);
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

    private static final Logger LOG = LoggerFactory.getLogger(HvdcUpdate.class);
}
