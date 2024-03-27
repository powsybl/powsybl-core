/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public final class HvdcUtils {

    public static double getConverterStationTargetP(HvdcConverterStation<?> station) {
        // for a LCC converter station, we are in load convention.
        // If the converter station is at side 1 and is a rectifier, p should be positive.
        // If the converter station is at side 1 and is an inverter, p should be negative.
        // If the converter station is at side 2 and is a rectifier, p should be positive.
        // If the converter station is at side 2 and is an inverter, p should be negative.
        boolean disconnectedAtOtherSide = station.getOtherConverterStation().map(otherConverterStation -> {
            Bus bus = otherConverterStation.getTerminal().getBusView().getBus();
            return bus == null;
        }).orElse(true); // it means there is no HVDC line connected to station
        return disconnectedAtOtherSide ? 0.0 : getSign(station) * getAbsoluteValuePAc(station);
    }

    public static double getLccConverterStationLoadTargetQ(LccConverterStation lccCs) {
        // Load convention.
        // If the converter station is at side 1 and is rectifier, p should be positive.
        // If the converter station is at side 1 and is inverter, p should be negative.
        // If the converter station is at side 2 and is rectifier, p should be positive.
        // If the converter station is at side 2 and is inverter, p should be negative.
        double pCs = getConverterStationTargetP(lccCs);
        return Math.abs(
            pCs * Math.tan(Math.acos(lccCs.getPowerFactor()))); // A LCC station always consumes reactive power.
    }

    public static double getSign(HvdcConverterStation<?> station) {
        // This method gives the sign of PAc.
        boolean isConverterStationRectifier = isRectifier(station);
        double sign;
        if (station instanceof LccConverterStation) { // load convention.
            sign = isConverterStationRectifier ? 1 : -1;
        } else if (station instanceof VscConverterStation) { // generator convention.
            sign = isConverterStationRectifier ? -1 : 1;
        } else {
            throw new PowsyblException("Unknown HVDC converter station type: " + station.getClass().getSimpleName());
        }
        return sign;
    }

    public static boolean isRectifier(HvdcConverterStation<?> station) {
        Objects.requireNonNull(station);
        HvdcLine line = station.getHvdcLine();
        return line.getConverterStation1() == station && line.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER
            || line.getConverterStation2() == station && line.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
    }

    private static double getAbsoluteValuePAc(HvdcConverterStation<?> station) {
        boolean isConverterStationRectifier = isRectifier(station);
        if (isConverterStationRectifier) {
            return station.getHvdcLine().getActivePowerSetpoint();
        } else {
            // the converter station is inverter.
            AtomicReference<Double> absoluteValueInverterPAc = new AtomicReference<>((double) 0);
            Optional<? extends HvdcConverterStation<?>> otherStation = station.getOtherConverterStation();
            otherStation.ifPresent(os -> absoluteValueInverterPAc.set(getAbsoluteValueInverterPAc(os.getLossFactor(), station.getLossFactor(), station.getHvdcLine())));
            return absoluteValueInverterPAc.get();
        }
    }

    private static double getHvdcLineLosses(double rectifierPDc, double nominalV, double r) {
        // This method computes the losses due to the HVDC line.
        // The active power value on rectifier DC side is known as the HVDC active power set point minus the losses related
        // to AC/DC conversion (rectifier conversion), the voltage is approximated to the nominal voltage as attribute of the HVDC line.
        // In an HVDC, as a branch with two sides, the difference between pDc1 and pDc2 can be computed with the assumptions:
        // I = (V1 - V2) / R and pDc1 = I * V1 and pDc2 = I * V2 and V1 = nominalV
        // we simply obtain that the absolute value of the difference is equal to R * pDc1 * pDc1 / (V1 * V1) if side 1 is rectifier side.
        return r * rectifierPDc * rectifierPDc / (nominalV * nominalV);
    }

    private static double getAbsoluteValueInverterPAc(double rectifierLossFactor, double inverterLossFactor,
                                                      HvdcLine hvdcLine) {
        // On inverter side, absolute value of PAc of a VSC converter station should be computed in three step:
        // 1) compute the losses related to the rectifier conversion.
        // 2) compute the losses related to the HVDC line itself (R i^2).
        // 3) compute the losses related to the inverter conversion.
        double rectifierPDc = hvdcLine.getActivePowerSetpoint() * (1 - rectifierLossFactor / 100); // rectifierPDc positive.
        double inverterPDc = rectifierPDc - getHvdcLineLosses(rectifierPDc, hvdcLine.getNominalV(), hvdcLine.getR());
        return inverterPDc * (1 - inverterLossFactor / 100); // always positive.
    }

    private HvdcUtils() {
    }
}
