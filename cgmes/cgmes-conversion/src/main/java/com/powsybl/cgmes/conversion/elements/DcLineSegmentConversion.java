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

    private void updateConverterStations(double pAC) {

        // poleLossP is the active power loss at a DC Pole
        // for lossless operation: P(DC) = P(AC) => lossFactor = 0
        // for rectifier operation with losses: P(DC) = P(AC) - poleLossP => P(DC) / P(AC) = 1 - poleLossP1 / P(AC) = 1 - lossFactor
        // for inverter operation with losses: P(DC) = P(AC) + poleLossP => P(AC) / P(DC) = 1 - poleLossP / P(DC) = 1 - poleLossP / (P(AC) + poleLossP) = 1 - lossFactor
        double poleLossP1 = cconverter1.asDouble("poleLossP");
        double poleLossP2 = cconverter2.asDouble("poleLossP");

        float lossFactor = 0;
        if (decodeMode().equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && pAC != 0) {
            lossFactor = (float) (poleLossP1 / pAC) * 100;
        } else if (decodeMode().equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && pAC + poleLossP1 != 0) {
            lossFactor = (float) (poleLossP1 / (pAC + poleLossP1)) * 100;
        }
        iconverter1.setLossFactor(lossFactor);

        lossFactor = 0;
        if (decodeMode().equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && pAC != 0) {
            lossFactor = (float) (poleLossP2 / Math.abs(pAC)) * 100;
        } else if (decodeMode().equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && Math.abs(pAC) + poleLossP2 != 0) {
            lossFactor = (float) (poleLossP2 / (Math.abs(pAC) + poleLossP2)) * 100;
        }
        iconverter2.setLossFactor(lossFactor);

        if (iconverter1.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
            ((LccConverterStation) iconverter1).setPowerFactor(pAC == 0 || Double.isNaN(iconverter1.getTerminal().getQ()) ? 0.8f : (float) Math.abs(iconverter1.getTerminal().getQ() / pAC));
            ((LccConverterStation) iconverter2).setPowerFactor(pAC == 0 || Double.isNaN(iconverter2.getTerminal().getQ()) ? 0.8f : (float) Math.abs(iconverter2.getTerminal().getQ() / pAC));
        }
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

        double pAC1 = Double.isNaN(cconverter1.asDouble(TARGET_PPCC)) ? 0 : cconverter1.asDouble(TARGET_PPCC);
        double pAC2 = Double.isNaN(cconverter2.asDouble(TARGET_PPCC)) ? 0 : cconverter2.asDouble(TARGET_PPCC);
        double pAC = Math.abs(decodeMode().equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && pAC1 != 0 || pAC2 == 0 ? pAC1 : pAC2);
        updateConverterStations(pAC);

        double maxP = pAC * 1.2; // arbitrary value because there is no maxP attribute in CGMES
        missing("maxP", maxP);

        HvdcLineAdder adder = context.network().newHvdcLine()
                .setR(r())
                .setNominalV(ratedUdc())
                .setActivePowerSetpoint(pAC)
                .setMaxP(maxP)
                .setConvertersMode(decodeMode())
                .setConverterStationId1(iconverter1.getId())
                .setConverterStationId2(iconverter2.getId());
        identify(adder);
        adder.add();
    }

    private double r() {
        double r = p.asDouble("r", 0);
        if (r < 0) {
            double r1 = 0.1;
            fixed("resistance", "was zero", r, r1);
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
