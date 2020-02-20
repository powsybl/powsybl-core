/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class DcLineSegmentConversion extends AbstractIdentifiedObjectConversion {

    DcLineSegmentConversion(PropertyBag l, HvdcLine.ConvertersMode mode, double r, double ratedUdc,
        DcLineSegmentConverter converter1, DcLineSegmentConverter converter2, Context context) {
        super("DCLineSegment", l, context);

        this.mode = mode;
        this.r = r;
        this.ratedUdc = ratedUdc;
        this.converter1 = converter1;
        this.converter2 = converter2;
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void convert() {

        // arbitrary value because there is no maxP attribute in CGMES
        double maxP = getMaxP(converter1.pAC, converter2.pAC, mode);
        missing("maxP", maxP);

        HvdcLineAdder adder = context.network().newHvdcLine()
            .setR(r)
            .setNominalV(ratedUdc)
            .setActivePowerSetpoint(
                getPDc(converter1.pAC, converter2.pAC, converter1.poleLossP, converter2.poleLossP, mode))
            .setMaxP(maxP)
            .setConvertersMode(mode)
            .setConverterStationId1(converter1.converterId)
            .setConverterStationId2(converter2.converterId);
        identify(adder);
        adder.add();
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

    private static double getPDc(double pAC1, double pAC2, double poleLossP1, double poleLossP2,
        HvdcLine.ConvertersMode mode) {
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

    static class DcLineSegmentConverter {
        String converterId;
        double poleLossP;
        double pAC;

        DcLineSegmentConverter(String stationId, double poleLossP, double pAC) {
            this.converterId = stationId;
            this.poleLossP = poleLossP;
            this.pAC = pAC;
        }
    }

    private final HvdcLine.ConvertersMode mode;
    private final double r;
    private final double ratedUdc;
    private final DcLineSegmentConverter converter1;
    private final DcLineSegmentConverter converter2;
}
