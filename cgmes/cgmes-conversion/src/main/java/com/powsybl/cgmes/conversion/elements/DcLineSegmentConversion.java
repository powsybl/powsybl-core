/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class DcLineSegmentConversion extends AbstractIdentifiedObjectConversion {

    private static final String TARGET_PPCC = "targetPpcc";

    public DcLineSegmentConversion(PropertyBag l, Context context) {
        super("DCLineSegment", l, context);

        iconverter1 = context.dc().converterAt(l.getId("DCTerminal1"));
        iconverter2 = context.dc().converterAt(l.getId("DCTerminal2"));
        cconverter1 = context.dc().cgmesConverterFor(iconverter1);
        cconverter2 = context.dc().cgmesConverterFor(iconverter2);
    }

    private static double getPAc(PropertyBag p) {
        return Double.isNaN(p.asDouble(TARGET_PPCC)) ? 0 : p.asDouble(TARGET_PPCC); // targetPpcc is the real power injection target in the AC grid in CGMES
    }

    private void updateLossFactor1(double pAC1, double poleLossP1, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) { // pAC1 > 0
            iconverter1.setLossFactor((float) (poleLossP1 / pAC1) * 100);
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && Math.abs(pAC1) + poleLossP1 != 0) { // pAC1 < 0
            iconverter1.setLossFactor((float) (poleLossP1 / (Math.abs(pAC1) + poleLossP1)) * 100);
        }
    }

    private void updateLossFactor1FromPAC2(double pAC2, double poleLossP1, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && (Math.abs(pAC2) + poleLossP2 + poleLossP1) != 0) { // pAC2 < 0
            // lossFactor1 = poleLossP1 / pAC1 * 100
            // pAC1 = pDC + poleLossP1 = pAC2 + poleLossP2 + poleLossP1
            iconverter1.setLossFactor((float) (poleLossP1 / (Math.abs(pAC2) + poleLossP2 + poleLossP1)) * 100);
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && (pAC2 - poleLossP2) != 0) { // pAC2 > 0
            // lossFactor1 = poleLossP1 / pDC * 100
            // pDC = pAC2 - poleLossP2
            iconverter1.setLossFactor((float) (poleLossP1 / (pAC2 - poleLossP2)) * 100);
        }
    }

    private void updateLossFactor2(double pAC2, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)) { // pAC2 > 0
            iconverter2.setLossFactor((float) (poleLossP2 / pAC2) * 100);
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && Math.abs(pAC2) + poleLossP2 != 0) { // pAC2 < 0
            iconverter2.setLossFactor((float) (poleLossP2 / (Math.abs(pAC2) + poleLossP2)) * 100);
        }
    }

    private void updateLossFactor2FromPAC1(double pAC1, double poleLossP1, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && (pAC1 - poleLossP1) != 0) { // pAC1 > 0
            // lossFactor2 = poleLossP2 / pDC * 100
            // pDC = pAC1 - poleLossP1
            iconverter2.setLossFactor((float) (poleLossP2 / (pAC1 - poleLossP1)) * 100);
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && (Math.abs(pAC1) + poleLossP1 + poleLossP2) != 0) { // pAC1 < 0
            // lossFactor2 = poleLossP2 / (pDC + poleLossP2) * 100
            // pDC = pAC1 + poleLossP1
            iconverter2.setLossFactor((float) (poleLossP2 / (Math.abs(pAC1) + poleLossP1 + poleLossP2)) * 100);
        }
    }

    private void updatePowerFactor() {
        if (iconverter1.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
            double powerFactor1 = iconverter1.getTerminal().getP() / Math.hypot(iconverter1.getTerminal().getP(), iconverter1.getTerminal().getQ());
            if (!Double.isNaN(powerFactor1)) {
                ((LccConverterStation) iconverter1).setPowerFactor((float) powerFactor1);
            }
            double powerFactor2 = iconverter2.getTerminal().getP() / Math.hypot(iconverter2.getTerminal().getP(), iconverter2.getTerminal().getQ());
            if (!Double.isNaN(powerFactor2)) {
                ((LccConverterStation) iconverter2).setPowerFactor((float) powerFactor2);
            }
        }
    }

    private void updateConverterStations(double pAC1, double pAC2, double poleLossP1, double poleLossP2, HvdcLine.ConvertersMode mode) {

        // update loss factors
        if (pAC1 != 0 && pAC2 != 0) {
            // we only keep one as we are not sure if pAC1 and pAC2 are consistent
            if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) { // we ignore pAC2
                updateLossFactor1(pAC1, poleLossP1, mode);
                updateLossFactor2FromPAC1(pAC1, poleLossP1, poleLossP2, mode);
            } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)) { // we ignore pAC1
                updateLossFactor1FromPAC2(pAC2, poleLossP1, poleLossP2, mode);
                updateLossFactor2(pAC2, poleLossP2, mode);
            }
        } else if (pAC1 != 0) { // pAC2 == 0
            updateLossFactor1(pAC1, poleLossP1, mode);
            updateLossFactor2FromPAC1(pAC1, poleLossP1, poleLossP2, mode);
        } else if (pAC2 != 0) { // pAC1 == 0
            updateLossFactor1FromPAC2(pAC2, poleLossP1, poleLossP2, mode);
            updateLossFactor2(pAC2, poleLossP2, mode);
        } // else (i.e. pAC1 == 0 && pAC2 == 0) do nothing: loss factors are null and stations are probably disconnected

        // update power factors
        updatePowerFactor();
    }

    private static double getPDc(double pAC1, double pAC2, double poleLossP1, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            if (pAC1 != 0) {
                return pAC1 - poleLossP1;
            } else if (pAC2 != 0) {
                return Math.abs(pAC2) + poleLossP2;
            }
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)) {
            if (pAC2 != 0) {
                return Math.abs(pAC2) - poleLossP2;
            } else if (pAC1 != 0) {
                return pAC1 + poleLossP1;
            }
        }
        return 0;
    }

    private static double getMaxP(double pAC1, double pAC2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            if (pAC1 != 0) {
                return 1.2 * pAC1;
            }
            return 1.2 * pAC2;
        }
        if (pAC2 != 0) {
            return 1.2 * pAC2;
        }
        return 1.2 * pAC1;
    }

    @Override
    public boolean valid() {
        if (iconverter1 == null || iconverter2 == null) {
            if (iconverter1 == null) {
                missing("Converter1");
            } else {
                iconverter1.remove();
            }
            if (iconverter2 == null) {
                missing("Converter2");
            } else {
                iconverter2.remove();
            }
        } else if (!iconverter1.getHvdcType().equals(iconverter2.getHvdcType())) {
            iconverter1.remove();
            iconverter2.remove();
            invalid(String.format("HVDC converter station %s and HVDC converter station %s are linked and of different types",
                    iconverter1.getId(), iconverter2.getId()));
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void convert() {
        Objects.requireNonNull(iconverter1);
        Objects.requireNonNull(iconverter2);

        HvdcLine.ConvertersMode mode = decodeMode();

        // poleLossP is the active power loss at a DC Pole
        // for lossless operation: P(DC) = P(AC) => lossFactor = 0
        // for rectifier operation (conversion from AC to DC) with losses: P(DC) = P(AC) - poleLossP
        // In IIDM, for rectifier operation P(DC) / P(AC) = 1 - lossFactor / 100
        // => P(DC) / P(AC) = 1 - poleLossP / P(AC) = 1 - lossFactor / 100
        // for inverter operation (conversion from DC to AC) with losses: P(DC) = P(AC) + poleLossP
        // In IIDM, for inverter operation P(AC) / P(DC) = 1 - lossFactor / 100
        // => P(AC) / P(DC) = 1 - poleLossP / P(DC) = 1 - poleLossP / (P(AC) + poleLossP) = 1 - lossFactor / 100
        double poleLossP1 = cconverter1.asDouble("poleLossP");
        double poleLossP2 = cconverter2.asDouble("poleLossP");

        // load sign convention is used i.e. positive sign means flow out from a node
        // i.e. pACx >= 0 if converterx is rectifier and pACx <= 0 if converterx is inverter
        double pAC1 = getPAc(cconverter1);
        double pAC2 = getPAc(cconverter2);

        updateConverterStations(pAC1, pAC2, poleLossP1, poleLossP2, mode);

        double maxP = getMaxP(pAC1, pAC2, mode); // arbitrary value because there is no maxP attribute in CGMES
        missing("maxP", maxP);

        HvdcLineAdder adder = context.network().newHvdcLine()
                .setR(r())
                .setNominalV(ratedUdc())
                .setActivePowerSetpoint(getPDc(pAC1, pAC2, poleLossP1, poleLossP2, mode))
                .setMaxP(maxP)
                .setConvertersMode(mode)
                .setConverterStationId1(iconverter1.getId())
                .setConverterStationId2(iconverter2.getId());
        identify(adder);
        adder.add();
    }

    private double r() {
        double r = p.asDouble("r", 0);
        if (r < 0) {
            double r1 = 0.1;
            fixed("resistance", "was negative", r, r1);
            r = r1;
        }
        return r;
    }

    private double ratedUdc() {
        double ratedUdc1 = cconverter1.asDouble(CgmesNames.RATED_UDC);
        double ratedUdc2 = cconverter2.asDouble(CgmesNames.RATED_UDC);
        if (ratedUdc1 != 0) {
            return ratedUdc1;
        }
        return ratedUdc2;
    }

    private HvdcLine.ConvertersMode decodeMode() {
        String mode1 = cconverter1.getLocal("operatingMode");
        String mode2 = cconverter2.getLocal("operatingMode");

        if (iconverter1.getHvdcType().equals(HvdcConverterStation.HvdcType.LCC)) {
            if (inverter(mode1) && rectifier(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            } else if (rectifier(mode1) && inverter(mode2)) {
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            }
        } else {
            if (cconverter1.asDouble(TARGET_PPCC) > 0 || cconverter2.asDouble(TARGET_PPCC) < 0) {
                return HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            } else {
                return HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
            }
        }
        throw new PowsyblException("Unexpected HVDC type: " + iconverter1.getHvdcType());
    }

    private static boolean inverter(String operatingMode) {
        return operatingMode.toLowerCase().endsWith("inverter");
    }

    private static boolean rectifier(String operatingMode) {
        return operatingMode.toLowerCase().endsWith("rectifier");
    }

    private HvdcConverterStation<?> iconverter1;
    private HvdcConverterStation<?> iconverter2;
    private PropertyBag cconverter1;
    private PropertyBag cconverter2;
}
