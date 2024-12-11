/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.*;
import java.util.stream.Stream;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseSubstation;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcConverter;
import com.powsybl.psse.model.pf.PsseTwoTerminalDcTransmissionLine;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.util.ContainersMapping;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractConverter {

    private static final String FIXED_SHUNT_TAG = "-SH";
    private static final String SWITCHED_SHUNT_TAG = "-SwSH";

    enum PsseEquipmentType {
        PSSE_LOAD("L"),
        PSSE_FIXED_SHUNT("F"),
        PSSE_GENERATOR("M"),
        PSSE_BRANCH("B"),
        PSSE_TWO_WINDING("2"),
        PSSE_THREE_WINDING("3"),
        PSSE_SWITCHED_SHUNT("S"),
        PSSE_INDUCTION_MACHINE("I"),
        PSSE_TWO_TERMINAL_DC_LINE("D"),
        PSSE_VSC_DC_LINE("V"),
        PSSE_MULTI_TERMINAL_LINE("N"),
        PSSE_FACTS_DEVICE("A");

        private final String textCode;

        PsseEquipmentType(String textCode) {
            this.textCode = textCode;
        }

        String getTextCode() {
            return textCode;
        }
    }

    AbstractConverter(ContainersMapping containersMapping, Network network) {
        this.containersMapping = Objects.requireNonNull(containersMapping);
        this.network = Objects.requireNonNull(network);
    }

    ContainersMapping getContainersMapping() {
        return containersMapping;
    }

    Network getNetwork() {
        return network;
    }

    static String getVoltageLevelId(Set<Integer> busNums) {
        if (busNums.isEmpty()) {
            throw new PsseException("Unexpected empty busNums");
        }
        List<Integer> sortedBusNums = busNums.stream().sorted().toList();
        String voltageLevelId = "VL" + sortedBusNums.get(0);
        for (int i = 1; i < sortedBusNums.size(); i++) {
            voltageLevelId = voltageLevelId.concat(String.format("-%d", sortedBusNums.get(i)));
        }
        return voltageLevelId;
    }

    static List<Integer> extractBusesFromVoltageLevelId(String voltageLevelId) {
        List<Integer> buses = new ArrayList<>();
        if (voltageLevelId.length() <= 2 || !voltageLevelId.startsWith("VL")) {
            return buses;
        }
        List<String> busesText = Arrays.stream(voltageLevelId.substring(2).split("-")).toList();
        if (!busesText.stream().allMatch(busText -> busText.matches("[1-9]\\d*"))) {
            return buses;
        }
        busesText.forEach(busText -> buses.add(Integer.parseInt(busText)));
        return buses;
    }

    static String getBusId(int busNum) {
        return "B" + busNum;
    }

    static OptionalInt extractBusNumber(String configuredBusId) {
        if (configuredBusId.length() <= 1 || !configuredBusId.startsWith("B")) {
            return OptionalInt.empty();
        }
        String busNumber = configuredBusId.substring(1);
        return busNumber.matches("[1-9]\\d*") ? OptionalInt.of(Integer.parseInt(busNumber)) : OptionalInt.empty();
    }

    static String getFixedShuntId(int busI, String fixedShuntId) {
        return getBusId(busI) + FIXED_SHUNT_TAG + fixedShuntId;
    }

    static String getGeneratorId(int busI, String generatorId) {
        return getBusId(busI) + "-G" + generatorId;
    }

    static String getLineId(int busI, int busJ, String ckt) {
        return "L-" + busI + "-" + busJ + "-" + ckt;
    }

    static String getLoadId(int busI, String loadId) {
        return getBusId(busI) + "-L" + loadId;
    }

    static String getSwitchedShuntId(int busI, String id) {
        return getBusId(busI) + SWITCHED_SHUNT_TAG + id;
    }

    static String getTransformerId(int busI, int busJ, String ckt) {
        return "T-" + busI + "-" + busJ + "-" + ckt;
    }

    static String getTransformerId(int busI, int busJ, int busK, String ckt) {
        return "T-" + busI + "-" + busJ + "-" + busK + "-" + ckt;
    }

    // we can not use rectifierIp and inverterIp as it is managed with only one end in substationData
    // In Psse each two-terminal dc line must have a unique name (up to 12 characters)
    static String getTwoTerminalDcId(String name) {
        return "TwoTerminalDc-" + name;
    }

    static String getLccConverterId(Network network, PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc, PsseTwoTerminalDcConverter converter) {
        return Identifiables.getUniqueId("LccConverter-" + converter.getIp() + "-" + psseTwoTerminalDc.getName(), id -> network.getLccConverterStation(id) != null);
    }

    static String getSwitchId(String voltageLevelId, PsseSubstation.PsseSubstationSwitchingDevice switchingDevice) {
        return voltageLevelId + "-Sw-" + switchingDevice.getNi() + "-" + switchingDevice.getNj() + "-" + switchingDevice.getCkt();
    }

    static String busbarSectionId(String voltageLevelId, int node) {
        return String.format("%s-Busbar-%d", voltageLevelId, node);
    }

    static String getNodeId(VoltageLevel voltageLevel, int node) {
        return voltageLevel.getId() + "-" + node;
    }

    static int getStatus(ShuntCompensator shuntCompensator) {
        return shuntCompensator.getTerminal().isConnected() && shuntCompensator.getTerminal().getBusBreakerView().getBus() != null ? 1 : 0;
    }

    static String getNodeBreakerEquipmentIdBus(String equipmentId, int bus) {
        return equipmentId + "." + bus;
    }

    // EquipmentId must be independent of the bus order
    static String getNodeBreakerEquipmentId(String type, int busI, int busJ, int busK, String id) {
        List<Integer> sortedBuses = Stream.of(busI, busJ, busK).sorted().toList();
        int bus1 = sortedBuses.get(0);
        int bus2 = sortedBuses.get(1);
        int bus3 = sortedBuses.get(2);

        // after sorting, zeros will be at the beginning
        if (bus1 == 0 && bus2 == 0) {
            return type + "." + bus3 + "." + id;
        } else if (bus1 == 0) {
            return type + "." + bus2 + "." + bus3 + "." + id;
        } else {
            return type + "." + bus1 + "." + bus2 + "." + bus3 + "." + id;
        }
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, String id) {
        return equipmentType.getTextCode() + "." + busI + "." + id;
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, int busJ, String id) {
        List<Integer> sortedBuses = Stream.of(busI, busJ).sorted().toList();
        int bus1 = sortedBuses.get(0);
        int bus2 = sortedBuses.get(1);
        return equipmentType.getTextCode() + "." + bus1 + "." + bus2 + "." + id;
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, int busJ, int busK, String id) {
        List<Integer> sortedBuses = Stream.of(busI, busJ, busK).sorted().toList();
        int bus1 = sortedBuses.get(0);
        int bus2 = sortedBuses.get(1);
        int bus3 = sortedBuses.get(2);
        return equipmentType.getTextCode() + "." + bus1 + "." + bus2 + "." + bus3 + "." + id;
    }

    static Terminal findTerminalNode(Network network, String voltageLevelId, int node) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        return voltageLevel != null ? findTerminalNode(voltageLevel, node) : null;
    }

    static Terminal findTerminalNode(VoltageLevel voltageLevel, int node) {
        return voltageLevel.getNodeBreakerView().getOptionalTerminal(node)
                .orElseGet(() -> Networks.getEquivalentTerminal(voltageLevel, node));
    }

    static Bus findBusViewNode(VoltageLevel voltageLevel, int node) {
        Terminal terminal = findTerminalNode(voltageLevel, node);
        return terminal != null ? getTerminalBusView(terminal) : null;
    }

    static Bus getTerminalBusView(Terminal terminal) {
        return terminal.getBusView().getBus() != null ? terminal.getBusView().getBus() : terminal.getBusView().getConnectableBus();
    }

    static int getStatus(Terminal terminal) {
        return terminal.isConnected() && terminal.getBusView().getBus() != null ? 1 : 0;
    }

    static int findBusViewBusType(VoltageLevel voltageLevel, Bus bus) {
        if (!bus.isInMainConnectedComponent()) {
            return 4;
        }
        SlackTerminal slackTerminal = voltageLevel.getExtension(SlackTerminal.class);
        if (slackTerminal != null
                && slackTerminal.getTerminal().getBusView().getBus() != null
                && bus.getId().equals(slackTerminal.getTerminal().getBusView().getBus().getId())) {
            return 3;
        }
        return bus.getGeneratorStream().anyMatch(AbstractConverter::withLocalRegulatingControl) ? 2 : 1;
    }

    private static boolean withLocalRegulatingControl(Generator generator) {
        return generator.isVoltageRegulatorOn()
                && generator.getTerminal().getBusView().getBus().equals(generator.getRegulatingTerminal().getBusView().getBus());
    }

    static boolean exportVoltageLevelAsNodeBreaker(VoltageLevel voltageLevel) {
        return voltageLevel.getTopologyKind().equals(TopologyKind.NODE_BREAKER);
    }

    static Complex impedanceToEngineeringUnits(Complex impedance, double vnom, double sbase) {
        return impedance.multiply(vnom * vnom / sbase);
    }

    static double impedanceToEngineeringUnits(double impedance, double vnom, double sbase) {
        return impedance * vnom * vnom / sbase;
    }

    static double impedanceToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double impedance, double vnom1, double vnom2, double sbase) {
        return impedance * vnom1 * vnom2 / sbase;
    }

    static Complex admittanceToEngineeringUnits(Complex admittance, double vnom, double sbase) {
        return admittance.multiply(sbase / (vnom * vnom));
    }

    static double admittanceEnd1ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmissionEu, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return shuntAdmittance * sbase / (vnom1 * vnom1) - (1 - vnom2 / vnom1) * admittanceTransmissionEu;
    }

    static double admittanceEnd2ToEngineeringUnitsForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmissionEu, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return shuntAdmittance * sbase / (vnom2 * vnom2) - (1 - vnom1 / vnom2) * admittanceTransmissionEu;
    }

    static double admittanceToEngineeringUnits(double admittance, double vnom, double sbase) {
        return admittance * sbase / (vnom * vnom);
    }

    static double powerToShuntAdmittance(double power, double vnom) {
        return power / (vnom * vnom);
    }

    static double shuntAdmittanceToPower(double shuntAdmittance, double vnom) {
        return shuntAdmittance * vnom * vnom;
    }

    static double getVm(Bus bus) {
        return bus != null && Double.isFinite(bus.getV()) && bus.getV() > 0.0 ? bus.getV() / bus.getVoltageLevel().getNominalV() : 1.0;
    }

    static double getVa(Bus bus) {
        return bus != null && Double.isFinite(bus.getAngle()) ? bus.getAngle() : 0.0;
    }

    static double currentInAmpsToMw(double current, double nominalV) {
        return current * nominalV / 1000.0;
    }

    private final ContainersMapping containersMapping;
    private final Network network;
}
