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

import java.util.*;

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
        if (!getContainersMapping().isBusDefined(psseTwoTerminalDc.getRectifier().getIp()) || !getContainersMapping().isBusDefined(psseTwoTerminalDc.getInverter().getIp())) {
            return;
        }
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
                    currentInAmpsToMw(psseTwoTerminalDc.getSetvl(), psseTwoTerminalDc.getVschd());
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

    static void create(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        network.getHvdcLines().forEach(hvdcLine -> {
            if (isTwoTerminalDcTransmissionLine(hvdcLine)) {
                psseModel.addTwoTerminalDcTransmissionLines(Collections.singletonList(createTwoTerminalDcTransmissionLine(hvdcLine, contextExport)));
            }
        });
        psseModel.replaceAllTwoTerminalDcTransmissionLines(psseModel.getTwoTerminalDcTransmissionLines().stream().sorted(Comparator.comparing(PsseTwoTerminalDcTransmissionLine::getName)).toList());
    }

    private static PsseTwoTerminalDcTransmissionLine createTwoTerminalDcTransmissionLine(HvdcLine hvdcLine, ContextExport contextExport) {
        PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc = new PsseTwoTerminalDcTransmissionLine();

        psseTwoTerminalDc.setName(extractTwoTerminalDcName(hvdcLine.getId()));
        psseTwoTerminalDc.setMdc(findControlMode(hvdcLine, psseTwoTerminalDc.getMdc(), contextExport));
        psseTwoTerminalDc.setRdc(hvdcLine.getR());
        psseTwoTerminalDc.setSetvl(getVl(hvdcLine));
        psseTwoTerminalDc.setVschd(hvdcLine.getNominalV());
        psseTwoTerminalDc.setVcmod(0.0);
        psseTwoTerminalDc.setRcomp(0.0);
        psseTwoTerminalDc.setDelti(0.0);
        psseTwoTerminalDc.setMeter("I");
        psseTwoTerminalDc.setDcvmin(0.0);
        psseTwoTerminalDc.setCccitmx(20);
        psseTwoTerminalDc.setCccacc(1.0);

        if (hvdcLine.getConvertersMode() == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) {
            psseTwoTerminalDc.setRectifier(findConverter((LccConverterStation) hvdcLine.getConverterStation1(), contextExport));
            psseTwoTerminalDc.setInverter(findConverter((LccConverterStation) hvdcLine.getConverterStation2(), contextExport));
        } else {
            psseTwoTerminalDc.setRectifier(findConverter((LccConverterStation) hvdcLine.getConverterStation2(), contextExport));
            psseTwoTerminalDc.setInverter(findConverter((LccConverterStation) hvdcLine.getConverterStation1(), contextExport));
        }

        return psseTwoTerminalDc;
    }

    private static double getVl(HvdcLine hvdcLine) {
        return Double.isFinite(hvdcLine.getActivePowerSetpoint()) ? hvdcLine.getActivePowerSetpoint() : 0.0;
    }

    private static PsseTwoTerminalDcConverter findConverter(LccConverterStation lccConverter, ContextExport contextExport) {
        PsseTwoTerminalDcConverter converter = createDefaultConverter();
        converter.setIp(getTerminalBusI(lccConverter.getTerminal(), contextExport));
        converter.setAnmx(getAmnx(lccConverter.getPowerFactor()));
        return converter;
    }

    private static double getAmnx(double powerFactor) {
        double cosValue = powerFactor * 2.0 - Math.cos(Math.toRadians(60.0));
        cosValue = cosValue < -1.0 ? -1.0 : cosValue;
        cosValue = cosValue > 1.0 ? 1.0 : cosValue;
        return Math.toDegrees(Math.acos(cosValue));
    }

    private static PsseTwoTerminalDcConverter createDefaultConverter() {
        PsseTwoTerminalDcConverter converter = new PsseTwoTerminalDcConverter();
        converter.setIp(0);
        converter.setNb(0);
        converter.setAnmx(0.0);
        converter.setAnmn(0.0);
        converter.setRc(0.0);
        converter.setXc(0.0);
        converter.setEbas(0.0);
        converter.setTr(1.0);
        converter.setTap(1.0);
        converter.setTmx(1.5);
        converter.setTmn(0.51);
        converter.setStp(0.00625);
        converter.setIc(0);
        converter.setNd(0);
        converter.setIf(0);
        converter.setIt(0);
        converter.setId("1");
        converter.setXcap(0.0);
        return converter;
    }

    static void update(Network network, PssePowerFlowModel psseModel) {
        psseModel.getTwoTerminalDcTransmissionLines().forEach(psseTwoTerminalDc -> {
            String hvdcId = getTwoTerminalDcId(psseTwoTerminalDc.getName());
            HvdcLine hvdcLine = network.getHvdcLine(hvdcId);

            if (hvdcLine == null) {
                psseTwoTerminalDc.setMdc(0);
            } else {
                psseTwoTerminalDc.setMdc(findUpdatedControlMode(hvdcLine, psseTwoTerminalDc.getMdc()));
            }
        });
    }

    private static int findControlMode(HvdcLine hvdcLine, int mdc, ContextExport contextExport) {
        return findControlMode(getStatus(hvdcLine.getConverterStation1().getTerminal(), contextExport),
                getStatus(hvdcLine.getConverterStation2().getTerminal(), contextExport), mdc);
    }

    private static int findUpdatedControlMode(HvdcLine hvdcLine, int mdc) {
        return findControlMode(getUpdatedStatus(hvdcLine.getConverterStation1().getTerminal()),
                getUpdatedStatus(hvdcLine.getConverterStation2().getTerminal()), mdc);
    }

    private static int findControlMode(int status1, int status2, int mdc) {
        if (status1 == 1 && status2 == 1) {
            return mdc != 0 ? mdc : 1;
        } else {
            return 0;
        }
    }

    private final PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc;
    private final NodeBreakerImport nodeBreakerImport;
}
