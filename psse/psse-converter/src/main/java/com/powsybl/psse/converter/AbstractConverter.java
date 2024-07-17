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
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseRates;
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
        return "VL" + busNums.stream().min(Comparator.naturalOrder()).orElseThrow(() -> new PsseException("Unexpected empty busNums"));
    }

    static String getBusId(int busNum) {
        return "B" + busNum;
    }

    static OptionalInt extractBusNumber(String configuredBusId) {
        if (configuredBusId.length() <= 1) {
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

    public static String extractTwoTerminalDcName(String twoTerminalDcId) {
        String name = twoTerminalDcId.replace("TwoTerminalDc-", "");
        return name.substring(0, Math.min(12, name.length()));
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

    public static Optional<String> extractCkt(String identifiableId, IdentifiableType identifiableType) {
        return switch (identifiableType) {
            case SWITCH, LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER -> extractCkt(identifiableId, "-");
            case LOAD -> extractCkt(identifiableId, "-L");
            case GENERATOR -> extractCkt(identifiableId, "-G");
            case SHUNT_COMPENSATOR -> {
                Optional<String> ckt = extractCkt(identifiableId, FIXED_SHUNT_TAG);
                yield ckt.isPresent() ? ckt : extractCkt(identifiableId, SWITCHED_SHUNT_TAG);
            }
            case HVDC_LINE -> Optional.of(extractTwoTerminalDcName(identifiableId));
            default -> throw new PsseException("unexpected identifiableType: " + identifiableType.name());
        };
    }

    private static Optional<String> extractCkt(String identifiableId, String subString) {
        int index = identifiableId.lastIndexOf(subString);
        if (index != -1) {
            return Optional.of(identifiableId.substring(index + subString.length()));
        } else {
            return Optional.empty();
        }
    }

    static String getNodeBreakerEquipmentIdBus(String equipmentId, int bus) {
        return equipmentId + "." + bus;
    }

    static String getPsseEquipmentType(Identifiable<?> identifiable) {
        return switch (identifiable.getType()) {
            case LOAD -> PsseEquipmentType.PSSE_LOAD.getTextCode();
            case GENERATOR -> PsseEquipmentType.PSSE_GENERATOR.getTextCode();
            case LINE -> PsseEquipmentType.PSSE_BRANCH.getTextCode();
            case TWO_WINDINGS_TRANSFORMER -> PsseEquipmentType.PSSE_TWO_WINDING.getTextCode();
            case THREE_WINDINGS_TRANSFORMER -> PsseEquipmentType.PSSE_THREE_WINDING.getTextCode();
            case SHUNT_COMPENSATOR -> {
                ShuntCompensator shunt = (ShuntCompensator) identifiable;
                yield isFixedShunt(shunt) ? PsseEquipmentType.PSSE_FIXED_SHUNT.getTextCode() : PsseEquipmentType.PSSE_SWITCHED_SHUNT.getTextCode();
            }
            case HVDC_LINE -> PsseEquipmentType.PSSE_TWO_TERMINAL_DC_LINE.getTextCode();
            default -> throw new PsseException("unexpected identifiableType: " + identifiable.getType().name());
        };
    }

    static boolean isFixedShunt(ShuntCompensator shunt) {
        if (shunt.getId().contains(FIXED_SHUNT_TAG)) {
            return true;
        } else if (shunt.getId().contains(SWITCHED_SHUNT_TAG)) {
            return false;
        } else {
            return shunt.getMaximumSectionCount() == 1
                    && !shunt.isVoltageRegulatorOn()
                    && Double.isNaN(shunt.getTargetV());
        }
    }

    static int getStatus(ShuntCompensator shuntCompensator) {
        return shuntCompensator.getTerminal().isConnected() && shuntCompensator.getTerminal().getBusBreakerView().getBus() != null ? 1 : 0;
    }

    static List<String> getEquipmentListToBeExported(VoltageLevel voltageLevel) {
        List<String> equipmentListToBeExported = new ArrayList<>();
        for (Connectable<?> connectable : voltageLevel.getConnectables()) {
            if (isEquipmentToBeExported(connectable.getType())) {
                if (connectable.getType().equals(IdentifiableType.HVDC_CONVERTER_STATION)) {
                    HvdcConverterStation<?> converterStation = (HvdcConverterStation<?>) connectable;
                    equipmentListToBeExported.add(converterStation.getHvdcLine().getId());
                } else {
                    equipmentListToBeExported.add(connectable.getId());
                }
            }
        }
        return equipmentListToBeExported.stream().sorted().toList();
    }

    static List<Integer> getEquipmentNodes(VoltageLevel voltageLevel, String equipmentId) {
        return getEquipmentTerminals(voltageLevel, equipmentId).stream().map(terminal -> terminal.getNodeBreakerView().getNode()).toList();
    }

    static List<Terminal> getEquipmentTerminals(VoltageLevel voltageLevel, String equipmentId) {
        List<Terminal> terminals = new ArrayList<>();
        Connectable<?> connectable = voltageLevel.getNetwork().getConnectable(equipmentId);
        if (connectable != null) {
            connectable.getTerminals().forEach(terminal -> addVoltageLevelTerminal(voltageLevel, terminal, terminals));
        } else {
            Identifiable<?> identifiable = voltageLevel.getNetwork().getIdentifiable(equipmentId);
            if (identifiable != null && identifiable.getType().equals(IdentifiableType.HVDC_LINE)) {
                HvdcLine hvdcLine = (HvdcLine) identifiable;
                addVoltageLevelTerminal(voltageLevel, hvdcLine.getConverterStation1().getTerminal(), terminals);
                addVoltageLevelTerminal(voltageLevel, hvdcLine.getConverterStation2().getTerminal(), terminals);
            } else {
                throw new PsseException("Unexpected identifiable: " + equipmentId);
            }
        }
        return terminals;
    }

    static ThreeSides getTerminalSide(Terminal terminal) {
        if (terminal.getConnectable().getType().equals(IdentifiableType.HVDC_CONVERTER_STATION)) {
            HvdcConverterStation<?> converterStation = (HvdcConverterStation<?>) terminal.getConnectable();
            return converterStation.equals(converterStation.getHvdcLine().getConverterStation1()) ? ThreeSides.ONE : ThreeSides.TWO;
        } else {
            return terminal.getSide();
        }
    }

    private static void addVoltageLevelTerminal(VoltageLevel voltageLevel, Terminal terminal, List<Terminal> terminals) {
        if (terminal != null && terminal.getVoltageLevel().equals(voltageLevel)) {
            terminals.add(terminal);
        }
    }

    private static boolean isEquipmentToBeExported(IdentifiableType type) {
        return switch (type) {
            case LOAD, GENERATOR, SHUNT_COMPENSATOR, LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, HVDC_CONVERTER_STATION ->
                    true;
            case BUSBAR_SECTION, HVDC_LINE, SWITCH -> false;
            default -> throw new PsseException("Unexpected equipment type: " + type.name());
        };
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

    static Bus getTerminalBus(Terminal terminal) {
        return terminal.getBusView().getBus() != null ? terminal.getBusView().getBus() : terminal.getBusView().getConnectableBus();
    }

    static int getTerminalBusI(Terminal terminal, ContextExport contextExport) {
        if (terminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
            int node = terminal.getNodeBreakerView().getNode();
            return contextExport.getNodeBreakerExport().getNodeBusI(terminal.getVoltageLevel(), node).orElseThrow();
        } else {
            Bus bus = getTerminalBus(terminal);
            return contextExport.getBusBreakerExport().getBusBusI(bus.getId()).orElseThrow();
        }
    }

    static int getRegulatingTerminalNode(Terminal regulatingTerminal, ContextExport contextExport) {
        if (regulatingTerminal == null) {
            return 0;
        } else {
            if (regulatingTerminal.getVoltageLevel().getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                return contextExport.getNodeBreakerExport().getSelectedNode(regulatingTerminal.getVoltageLevel(), regulatingTerminal.getNodeBreakerView().getNode()).orElseThrow();
            } else {
                return 0;
            }
        }
    }

    static int getRegulatingTerminalBusI(Terminal regulatingTerminal, int busI, int previousRegulatingBusI, ContextExport contextExport) {
        int regulatingBusI = getRegulatingTerminalBusI(regulatingTerminal, contextExport);
        return busI == regulatingBusI && previousRegulatingBusI == 0 ? previousRegulatingBusI : regulatingBusI;
    }

    static int getRegulatingTerminalBusI(Terminal regulatingTerminal, ContextExport contextExport) {
        if (regulatingTerminal == null) {
            return 0;
        } else {
            return getTerminalBusI(regulatingTerminal, contextExport);
        }
    }

    static int getStatus(Terminal terminal) {
        return terminal.isConnected() && terminal.getBusView().getBus() != null ? 1 : 0;
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

    static double impedanceToPerUnitForLinesWithDifferentNominalVoltageAtEnds(double impedance, double vnom1, double vnom2, double sbase) {
        return impedance * sbase / (vnom1 * vnom2);
    }

    static double admittanceEnd1ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmission, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return (shuntAdmittance + (1 - vnom2 / vnom1) * admittanceTransmission) * vnom1 * vnom1 / sbase;
    }

    static double admittanceEnd2ToPerUnitForLinesWithDifferentNominalVoltageAtEnds(double admittanceTransmission, double shuntAdmittance, double vnom1, double vnom2, double sbase) {
        return (shuntAdmittance + (1 - vnom1 / vnom2) * admittanceTransmission) * vnom2 * vnom2 / sbase;
    }

    static Complex impedanceToPerUnit(Complex impedance, double vnom, double sbase) {
        return impedance.multiply(sbase / (vnom * vnom));
    }

    static Complex admittanceToPerUnit(Complex admittance, double vnom, double sbase) {
        return admittance.multiply(vnom * vnom / sbase);
    }

    static double powerToShuntAdmittance(double power, double vnom) {
        return power / (vnom * vnom);
    }

    static double shuntAdmittanceToPower(double shuntAdmittance, double vnom) {
        return shuntAdmittance * vnom * vnom;
    }

    static double getHighVm(Bus bus) {
        return bus != null && Double.isFinite(bus.getVoltageLevel().getHighVoltageLimit()) && bus.getVoltageLevel().getHighVoltageLimit() > 0.0 ? bus.getVoltageLevel().getHighVoltageLimit() / bus.getVoltageLevel().getNominalV() : 1.1;
    }

    static double getLowVm(Bus bus) {
        return bus != null && Double.isFinite(bus.getVoltageLevel().getLowVoltageLimit()) && bus.getVoltageLevel().getLowVoltageLimit() > 0.0 ? bus.getVoltageLevel().getLowVoltageLimit() / bus.getVoltageLevel().getNominalV() : 0.9;
    }

    static double getVm(Bus bus) {
        return bus != null && Double.isFinite(bus.getV()) && bus.getV() > 0.0 ? bus.getV() / bus.getVoltageLevel().getNominalV() : 1.0;
    }

    static double getVa(Bus bus) {
        return bus != null && Double.isFinite(bus.getAngle()) ? bus.getAngle() : 0.0;
    }

    static List<Double> getSortedRates(CurrentLimits currentLimits, double nominalV) {
        List<Double> rates = new ArrayList<>();
        rates.add(convertToMva(currentLimits.getPermanentLimit(), nominalV));
        rates.addAll(currentLimits.getTemporaryLimits().stream().map(temporaryLimit -> convertToMva(temporaryLimit.getValue(), nominalV)).toList());
        return rates.stream().sorted().toList();
    }

    static List<Double> getSortedRates(ApparentPowerLimits apparentPowerLimits) {
        List<Double> rates = new ArrayList<>();
        rates.add(apparentPowerLimits.getPermanentLimit());
        rates.addAll(apparentPowerLimits.getTemporaryLimits().stream().map(LoadingLimits.TemporaryLimit::getValue).toList());
        return rates.stream().sorted().toList();
    }

    static List<Double> getSortedRates(ActivePowerLimits activePowerLimits) {
        List<Double> rates = new ArrayList<>();
        rates.add(activePowerLimits.getPermanentLimit());
        rates.addAll(activePowerLimits.getTemporaryLimits().stream().map(LoadingLimits.TemporaryLimit::getValue).toList());
        return rates.stream().sorted().toList();
    }

    private static double convertToMva(double current, double nominalV) {
        return (current / 1000.0) * Math.sqrt(3.0) * nominalV;
    }

    static void setSortedRatesToPsseRates(List<Double> sortedRates, PsseRates rates) {
        rates.setRate1(getRate(sortedRates, 0));
        rates.setRate2(getRate(sortedRates, 1));
        rates.setRate3(getRate(sortedRates, 2));
        rates.setRate4(getRate(sortedRates, 3));
        rates.setRate5(getRate(sortedRates, 4));
        rates.setRate6(getRate(sortedRates, 5));
        rates.setRate7(getRate(sortedRates, 6));
        rates.setRate8(getRate(sortedRates, 7));
        rates.setRate9(getRate(sortedRates, 8));
        rates.setRate10(getRate(sortedRates, 9));
        rates.setRate11(getRate(sortedRates, 10));
        rates.setRate12(getRate(sortedRates, 11));
    }

    private static double getRate(List<Double> sortedRates, int index) {
        return sortedRates.size() > index ? sortedRates.get(index) : 0.0;
    }

    static PsseRates findDefaultRates() {
        PsseRates windingRates = new PsseRates();
        windingRates.setRate1(0.0);
        windingRates.setRate2(0.0);
        windingRates.setRate3(0.0);
        windingRates.setRate4(0.0);
        windingRates.setRate5(0.0);
        windingRates.setRate6(0.0);
        windingRates.setRate7(0.0);
        windingRates.setRate8(0.0);
        windingRates.setRate9(0.0);
        windingRates.setRate10(0.0);
        windingRates.setRate11(0.0);
        windingRates.setRate12(0.0);
        return windingRates;
    }

    private final ContainersMapping containersMapping;
    private final Network network;
}
