/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcConverter;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcTransmissionLine;
import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_TWO_TERMINAL_DC_LINE;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TwoTerminalDcConverter extends AbstractConverter {

    private static final double DEFAULT_MAXP_FACTOR = 1.2;

    TwoTerminalDcConverter(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc, ContainersMapping containerMapping, Network network, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseTwoTerminalDc = Objects.requireNonNull(psseTwoTerminalDc);
        this.nodeBreakerImport = nodeBreakerImport;
    }

    void create() {

        double lossFactor = 0.0;
        String busIdR = getBusId(psseTwoTerminalDc.getRectifier().getIp());
        VoltageLevel voltageLevelR = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseTwoTerminalDc.getRectifier().getIp()));
        LccConverterStationAdder adderR = voltageLevelR.newLccConverterStation()
            .setId(getLccConverterId(psseTwoTerminalDc, psseTwoTerminalDc.getRectifier()))
            .setName(psseTwoTerminalDc.getName())
            .setLossFactor((float) lossFactor)
            .setPowerFactor((float) getLccConverterPowerFactor(psseTwoTerminalDc.getRectifier()));

        String equipmentIdR = getNodeBreakerEquipmentId(PSSE_TWO_TERMINAL_DC_LINE, psseTwoTerminalDc.getRectifier().getIp(), psseTwoTerminalDc.getName());
        OptionalInt nodeR = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentIdR, psseTwoTerminalDc.getRectifier().getIp()));
        if (nodeR.isPresent()) {
            adderR.setNode(nodeR.getAsInt());
        } else {
            adderR.setConnectableBus(busIdR);
            adderR.setBus(psseTwoTerminalDc.getMdc() == 0 ? null : busIdR);
        }
        LccConverterStation cR = adderR.add();

        String busIdI = getBusId(psseTwoTerminalDc.getInverter().getIp());
        VoltageLevel voltageLevelI = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseTwoTerminalDc.getInverter().getIp()));
        LccConverterStationAdder adderI = voltageLevelI.newLccConverterStation()
            .setId(getLccConverterId(psseTwoTerminalDc, psseTwoTerminalDc.getInverter()))
            .setName(psseTwoTerminalDc.getName())
            .setLossFactor((float) lossFactor)
            .setPowerFactor((float) getLccConverterPowerFactor(psseTwoTerminalDc.getInverter()));

        String equipmentIdI = getNodeBreakerEquipmentId(PSSE_TWO_TERMINAL_DC_LINE, psseTwoTerminalDc.getInverter().getIp(), psseTwoTerminalDc.getName());
        OptionalInt nodeI = nodeBreakerImport.getNode(getNodeBreakerEquipmentIdBus(equipmentIdI, psseTwoTerminalDc.getInverter().getIp()));
        if (nodeI.isPresent()) {
            adderI.setNode(nodeI.getAsInt());
        } else {
            adderI.setConnectableBus(busIdI);
            adderI.setBus(psseTwoTerminalDc.getMdc() == 0 ? null : busIdI);
        }
        LccConverterStation cI = adderI.add();

        HvdcLineAdder adder = getNetwork().newHvdcLine()
            .setId(getTwoTerminalDcId(psseTwoTerminalDc))
            .setName(psseTwoTerminalDc.getName())
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
                return Math.abs(psseTwoTerminalDc.getSetvl());
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

    // power factor calculated under assumption that that the maximum overlap angle is 60 degree (see Kimbark's book)
    private static double getLccConverterPowerFactor(PsseTwoTerminalDcConverter converter) {
        return 0.5 * (Math.cos(Math.toRadians(converter.getAnmx())) + Math.cos(Math.toRadians(60.0)));
    }

    private String getLccConverterId(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc, PsseTwoTerminalDcConverter converter) {
        return Identifiables.getUniqueId("LccConverter-" + psseTwoTerminalDc.getRectifier().getIp() + "-" + psseTwoTerminalDc.getInverter().getIp() + "-" + converter.getIp(),
            id -> getNetwork().getLccConverterStation(id) != null);
    }

    private String getTwoTerminalDcId(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc) {
        return Identifiables.getUniqueId("TwoTerminalDc-" + psseTwoTerminalDc.getRectifier().getIp() + "-" + psseTwoTerminalDc.getInverter().getIp(),
            id -> getNetwork().getHvdcLine(id) != null);
    }

    private final PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc;
    private final NodeBreakerImport nodeBreakerImport;
}
