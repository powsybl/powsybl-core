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
import com.powsybl.psse.model.pf.PssePowerFlowModel;
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
            .setId(getLccConverterId(getNetwork(), psseTwoTerminalDc, psseTwoTerminalDc.getRectifier()))
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
            .setId(getLccConverterId(getNetwork(), psseTwoTerminalDc, psseTwoTerminalDc.getInverter()))
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
            .setId(getTwoTerminalDcId(psseTwoTerminalDc.getName()))
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
        return switch (psseTwoTerminalDc.getMdc()) {
            case 1 ->
                // The desired real power demand
                    Math.abs(psseTwoTerminalDc.getSetvl());
            case 2 ->
                // It is the current in amps (should divide by 1000 to convert to MW)
                    psseTwoTerminalDc.getSetvl() * psseTwoTerminalDc.getVschd() / 1000.0;
            default -> 0.0;
        };
    }

    private static double getTwoTerminalDcMaxP(PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc) {
        return getTwoTerminalDcActivePowerSetpoint(psseTwoTerminalDc) * DEFAULT_MAXP_FACTOR;
    }

    // power factor calculated under assumption that the maximum overlap angle is 60 degree (see Kimbark's book)
    private static double getLccConverterPowerFactor(PsseTwoTerminalDcConverter converter) {
        return 0.5 * (Math.cos(Math.toRadians(converter.getAnmx())) + Math.cos(Math.toRadians(60.0)));
    }

    static void updateTwoTerminalDcTransmissionLines(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        psseModel.getTwoTerminalDcTransmissionLines().forEach(psseTwoTerminalDc -> {
            String hvdcId = getTwoTerminalDcId(psseTwoTerminalDc.getName());
            HvdcLine hvdcLine = network.getHvdcLine(hvdcId);

            int busRectifier = getTerminalBusI(rectifierTerminal(hvdcLine), contextExport);
            int busInverter = getTerminalBusI(inverterTerminal(hvdcLine), contextExport);

            if (hvdcLine == null) {
                psseTwoTerminalDc.setMdc(0);
            } else {
                psseTwoTerminalDc.setMdc(findControlMode(hvdcLine, psseTwoTerminalDc.getMdc()));
            }
            psseTwoTerminalDc.getRectifier().setIp(busRectifier);
            psseTwoTerminalDc.getInverter().setIp(busInverter);
        });
    }

    private static Terminal rectifierTerminal(HvdcLine hvdcLine) {
        return hvdcLine.getConvertersMode().equals(ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) ? hvdcLine.getConverterStation1().getTerminal() : hvdcLine.getConverterStation2().getTerminal();
    }

    private static Terminal inverterTerminal(HvdcLine hvdcLine) {
        return hvdcLine.getConvertersMode().equals(ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) ? hvdcLine.getConverterStation2().getTerminal() : hvdcLine.getConverterStation1().getTerminal();
    }

    private static int findControlMode(HvdcLine hvdcLine, int mdc) {
        if (hvdcLine.getConverterStation1().getTerminal().isConnected() && hvdcLine.getConverterStation1().getTerminal().getBusBreakerView().getBus() != null
                && hvdcLine.getConverterStation2().getTerminal().isConnected() && hvdcLine.getConverterStation2().getTerminal().getBusBreakerView().getBus() != null) {
            return mdc != 0 ? mdc : 1;
        } else {
            return 0;
        }
    }

    private final PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc;
    private final NodeBreakerImport nodeBreakerImport;
}
