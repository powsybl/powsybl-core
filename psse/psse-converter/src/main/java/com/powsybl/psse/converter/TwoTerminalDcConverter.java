/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.Objects;

import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcConverter;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcTransmissionLine;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TwoTerminalDcConverter extends AbstractConverter {

    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    public TwoTerminalDcConverter(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseTwoTerminalDc = Objects.requireNonNull(psseTwoTerminalDc);
    }

    public void create() {

        double lossFactor = 0.0;
        String busIdR = getBusId(psseTwoTerminalDc.getRectifier().getIp());
        VoltageLevel voltageLevelR = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseTwoTerminalDc.getRectifier().getIp()));
        LccConverterStationAdder adderR = voltageLevelR.newLccConverterStation()
            .setId(getLccConverterId(psseTwoTerminalDc.getRectifier()))
            .setConnectableBus(busIdR)
            .setLossFactor((float) lossFactor)
            .setPowerFactor((float) getLccConverterPowerFactor(psseTwoTerminalDc.getRectifier()))
            .setBus(psseTwoTerminalDc.getMdc() == 0 ? null : busIdR);
        LccConverterStation cR = adderR.add();

        String busIdI = getBusId(psseTwoTerminalDc.getInverter().getIp());
        VoltageLevel voltageLevelI = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseTwoTerminalDc.getInverter().getIp()));
        LccConverterStationAdder adderI = voltageLevelI.newLccConverterStation()
            .setId(getLccConverterId(psseTwoTerminalDc.getInverter()))
            .setConnectableBus(busIdI)
            .setLossFactor((float) lossFactor)
            .setPowerFactor((float) getLccConverterPowerFactor(psseTwoTerminalDc.getInverter()))
            .setBus(psseTwoTerminalDc.getMdc() == 0 ? null : busIdI);
        LccConverterStation cI = adderI.add();

        HvdcLineAdder adder = getNetwork().newHvdcLine()
            .setId(getTwoTerminalDcId(psseTwoTerminalDc))
            .setR(psseTwoTerminalDc.getRdc())
            .setNominalV(psseTwoTerminalDc.getVschd())
            .setActivePowerSetpoint(getTwoTerminalDcActivePowerSetpoint(psseTwoTerminalDc))
            .setMaxP(getTwoTerminalDcMaxP(psseTwoTerminalDc))
            .setConvertersMode(ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
            .setConverterStationId1(cR.getId())
            .setConverterStationId2(cI.getId());
        adder.add();
    }

    private static double getTwoTerminalDcActivePowerSetpoint(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc) {
        switch (psseTwoTerminalDc.getMdc()) {
            case 1:
                // The desired real power demand
                return psseTwoTerminalDc.getSetvl();
            case 2:
                // It is the current in amps (should divide by 1000 to convert to MW)
                return psseTwoTerminalDc.getSetvl() * psseTwoTerminalDc.getVschd() / 1000.0;
            default:
                return 0.0;
        }
    }

    private static double getTwoTerminalDcMaxP(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc) {
        return getTwoTerminalDcActivePowerSetpoint(psseTwoTerminalDc) * DEFAULT_MAXP_FACTOR;
    }

    // Kimbark's book
    // It is assumed the maximum overlap angle is 60 degree (see
    private static double getLccConverterPowerFactor(PsseTwoTerminalDcConverter converter) {
        return Math.acos(0.5 * (Math.cos(Math.toRadians(converter.getAnmx())) + Math.cos(Math.toRadians(60.0))));
    }

    private static String getLccConverterId(PsseTwoTerminalDcConverter converter) {
        return "LccConverter-" + converter.getIp() + "-" + converter.getId();
    }

    private static String getTwoTerminalDcId(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc) {
        return "TwoTerminalDc-" + psseTwoTerminalDc.getRectifier().getIp() + "-" + psseTwoTerminalDc.getInverter().getIp() + "-" + psseTwoTerminalDc.getName();
    }

    private final PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc;
}
