/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.psse.model.PsseException;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.ContainersMapping;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractConverter {

    enum PsseEquipmentType {
        PSSE_LOAD("L"),
        PSSE_FIXED_SHUNT("F"),
        PSSE_GENERATOR("M"),
        PSSE_BRANCH("B"),
        PSSE_TWO_WINDING("2"),
        PSSE_THREE_WINDING("3"),
        PSSE_SWITCHED_SHUNT("s"),
        PSSE_INDUCTION_MACHINE("I"),
        PSSE_TWO_TERMINAL_DC_LINE("D"),
        PSSE_VSC_DC_LINE("V"),
        PSSE_MULTI_TERMINAL_LINE("N"),
        PSSE_FACTS_DEVICE("A");

        private final String textCode;

        PsseEquipmentType(String textCode) {
            this.textCode = textCode;
        }

        private String getTextCode() {
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
        return "VL" + busNums.stream().min(Comparator.naturalOrder()).orElseThrow(() -> new PsseException("Unexpected empty busNums"));
    }

    static String getBusId(int busNum) {
        return "B" + busNum;
    }

    static String getNodeBreakerEquipmentIdBus(String equipmentId, int bus) {
        return equipmentId + "." + bus;
    }

    // EquipmentId must be independent of the bus order
    static String getNodeBreakerEquipmentId(String type, int busI, int busJ, int busK, String id) {
        List<Integer> sortedBuses = List.of(busI, busJ, busK).stream().sorted().toList();
        int bus1 = sortedBuses.get(0);
        int bus2 = sortedBuses.get(1);
        int bus3 = sortedBuses.get(2);

        // after sorting, zeros will be at the beginning
        if (bus1 == 0 && bus2 == 0) {
            return type + "." + bus3 + "." + id;
        } else if (bus1 == 0) {
            return type + "." + bus2 + "." + bus3 + "." + id;
        } else {
            return type + "." + bus1 + "." + bus2 + "." + "." + bus3 + id;
        }
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, String id) {
        return equipmentType.getTextCode() + "." + busI + "." + id;
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, int busJ, String id) {
        List<Integer> sortedBuses = List.of(busI, busJ).stream().sorted().toList();
        int bus1 = sortedBuses.get(0);
        int bus2 = sortedBuses.get(1);
        return equipmentType.getTextCode() + "." + bus1 + "." + bus2 + "." + id;
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, int busJ, int busK, String id) {
        List<Integer> sortedBuses = List.of(busI, busJ, busK).stream().sorted().toList();
        int bus1 = sortedBuses.get(0);
        int bus2 = sortedBuses.get(1);
        int bus3 = sortedBuses.get(2);
        return equipmentType.getTextCode() + "." + bus1 + "." + bus2 + "." + bus3 + "." + id;
    }

    static int obtainBus(NodeBreakerExport nodeBreakerExport, String equipmentId, int bus) {
        return nodeBreakerExport.getEquipmentIdBusBus(getNodeBreakerEquipmentIdBus(equipmentId, bus)).orElseGet(() -> bus);
    }

    static Terminal obtainTerminalNode(Network network, String voltageLevelId, int node) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        return voltageLevel != null ? obtainTerminalNode(voltageLevel, node) : null;
    }

    static Terminal obtainTerminalNode(VoltageLevel voltageLevel, int node) {
        return voltageLevel.getNodeBreakerView().getOptionalTerminal(node)
                .orElseGet(() -> Networks.getEquivalentTerminal(voltageLevel, node));
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

    private final ContainersMapping containersMapping;
    private final Network network;
}
