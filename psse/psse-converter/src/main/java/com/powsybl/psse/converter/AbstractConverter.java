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
import com.powsybl.psse.model.pf.*;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.util.ContainersMapping;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractConverter {

    private static final String FIXED_SHUNT_TAG = "-SH";
    private static final String SWITCHED_SHUNT_TAG = "-SwSH";
    private static final String TWO_TERMINAL_DC_TAG = "TwoTerminalDc-";
    private static final String VSC_DC_TRANSMISSION_LINE_TAG = "VscDcTransmissionLine-";
    private static final String FACTS_DEVICE_TAG = "FactsDevice-";
    private static final int MAX_BUS_LENGTH = 12;
    private static final int MAX_BRANCH_LENGTH = 40;
    private static final int MAX_PSSE_NODE_BY_SUBSTATION = 998;

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

    AbstractConverter(Network network) {
        this.containersMapping = null;
        this.network = Objects.requireNonNull(network);
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

    static String getSubstationIdFromPsseSubstationIds(Set<Integer> busNumbers) {
        return getSubstationId("Sub", busNumbers);
    }

    static String getSubstationIdFromBuses(Set<Integer> busNumbers) {
        return getSubstationId("S", busNumbers);
    }

    private static String getSubstationId(String tag, Set<Integer> numbers) {
        if (numbers.isEmpty()) {
            throw new PsseException("Unexpected empty numbers");
        }
        List<Integer> sortedNumbers = numbers.stream().sorted().toList();
        String substationId = tag + sortedNumbers.get(0);
        for (int i = 1; i < sortedNumbers.size(); i++) {
            substationId = substationId.concat(String.format("-%d", sortedNumbers.get(i)));
        }
        return substationId;
    }

    static String getVoltageLevelId(Set<Integer> busNumbers) {
        if (busNumbers.isEmpty()) {
            throw new PsseException("Unexpected empty busNumbers");
        }
        List<Integer> sortedBusNumbers = busNumbers.stream().sorted().toList();
        String voltageLevelId = "VL" + sortedBusNumbers.get(0);
        for (int i = 1; i < sortedBusNumbers.size(); i++) {
            voltageLevelId = voltageLevelId.concat(String.format("-%d", sortedBusNumbers.get(i)));
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
        return TWO_TERMINAL_DC_TAG + name;
    }

    public static String extractTwoTerminalDcName(String twoTerminalDcId) {
        String name = twoTerminalDcId.replace(TWO_TERMINAL_DC_TAG, "");
        return name.substring(0, Math.min(12, name.length()));
    }

    static String getLccConverterId(Network network, PsseTwoTerminalDcTransmissionLine psseTwoTerminalDc, PsseTwoTerminalDcConverter converter) {
        return Identifiables.getUniqueId("LccConverter-" + converter.getIp() + "-" + psseTwoTerminalDc.getName(), id -> network.getLccConverterStation(id) != null);
    }

    static String getVscDcTransmissionLineId(String name) {
        return VSC_DC_TRANSMISSION_LINE_TAG + name;
    }

    public static String extractVscDcTransmissionLineName(String vscDcTransmissionLineId) {
        String name = vscDcTransmissionLineId.replace(VSC_DC_TRANSMISSION_LINE_TAG, "");
        return name.substring(0, Math.min(12, name.length()));
    }

    static String getVscConverterId(Network network, PsseVoltageSourceConverterDcTransmissionLine psseVscDcTransmissionLine, PsseVoltageSourceConverter converter) {
        return Identifiables.getUniqueId("VscConverter-" + converter.getIbus() + "-" + psseVscDcTransmissionLine.getName(), id -> network.getVscConverterStation(id) != null);
    }

    static String getSwitchId(String voltageLevelId, PsseSubstation.PsseSubstationSwitchingDevice switchingDevice) {
        return voltageLevelId + "-Sw-" + switchingDevice.getNi() + "-" + switchingDevice.getNj() + "-" + switchingDevice.getCkt();
    }

    static String busbarSectionId(String voltageLevelId, int node) {
        return String.format("%s-Busbar-%d", voltageLevelId, node);
    }

    static String getFactsDeviceId(String name) {
        return FACTS_DEVICE_TAG + name;
    }

    public static String extractFactsDeviceName(String factsDeviceId) {
        String name = factsDeviceId.replace(FACTS_DEVICE_TAG, "");
        return name.substring(0, Math.min(12, name.length()));
    }

    static String getNodeId(VoltageLevel voltageLevel, int node) {
        return voltageLevel.getId() + "-" + node;
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

    static boolean isTwoTerminalDcTransmissionLine(HvdcLine hvdcLine) {
        return hvdcLine.getConverterStation1().getHvdcType() == HvdcConverterStation.HvdcType.LCC;
    }

    static boolean isVscDcTransmissionLine(HvdcLine hvdcLine) {
        return !isTwoTerminalDcTransmissionLine(hvdcLine);
    }

    static List<String> getEquipmentListToBeExported(VoltageLevel voltageLevel) {
        List<String> equipmentListToBeExported = new ArrayList<>();
        for (Connectable<?> connectable : voltageLevel.getConnectables()) {
            if (isEquipmentToBeExported(connectable.getType())) {
                if (connectable.getType() == IdentifiableType.HVDC_CONVERTER_STATION) {
                    HvdcConverterStation<?> converterStation = (HvdcConverterStation<?>) connectable;
                    equipmentListToBeExported.add(converterStation.getHvdcLine().getId());
                } else if (connectable.getType() == IdentifiableType.DANGLING_LINE) {
                    DanglingLine danglingLine = (DanglingLine) connectable;
                    if (danglingLine.isPaired()) {
                        TieLine tieLine = danglingLine.getTieLine().orElseThrow();
                        equipmentListToBeExported.add(tieLine.getId());
                    } else {
                        equipmentListToBeExported.add(connectable.getId());
                    }
                } else {
                    equipmentListToBeExported.add(connectable.getId());
                }
            }
        }
        return equipmentListToBeExported.stream().sorted().toList();
    }

    private static boolean isEquipmentToBeExported(IdentifiableType type) {
        return switch (type) {
            case LOAD, GENERATOR, SHUNT_COMPENSATOR, LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, HVDC_CONVERTER_STATION, STATIC_VAR_COMPENSATOR, DANGLING_LINE, BATTERY ->
                    true;
            case BUSBAR_SECTION, HVDC_LINE, SWITCH, TIE_LINE -> false;
            default -> throw new PsseException("Unexpected equipment type: " + type.name());
        };
    }

    static List<Terminal> getEquipmentTerminals(VoltageLevel voltageLevel, String equipmentId) {
        List<Terminal> terminals = new ArrayList<>();
        Connectable<?> connectable = voltageLevel.getNetwork().getConnectable(equipmentId);
        if (connectable != null) {
            terminals.addAll(connectable.getTerminals());
        } else {
            Identifiable<?> identifiable = voltageLevel.getNetwork().getIdentifiable(equipmentId);
            if (identifiable != null && identifiable.getType() == IdentifiableType.HVDC_LINE) {
                HvdcLine hvdcLine = (HvdcLine) identifiable;
                terminals.add(hvdcLine.getConverterStation1().getTerminal());
                terminals.add(hvdcLine.getConverterStation2().getTerminal());
            } else if (identifiable != null && identifiable.getType() == IdentifiableType.TIE_LINE) {
                TieLine tieLine = (TieLine) identifiable;
                terminals.add(tieLine.getDanglingLine1().getTerminal());
                terminals.add(tieLine.getDanglingLine2().getTerminal());
            } else {
                throw new PsseException("Unexpected identifiable: " + equipmentId);
            }
        }
        return terminals;
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, String id) {
        return getNodeBreakerEquipmentId(equipmentType, busI, 0, 0, id);
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, int busJ, String id) {
        return getNodeBreakerEquipmentId(equipmentType, busI, busJ, 0, id);
    }

    static String getNodeBreakerEquipmentId(PsseEquipmentType equipmentType, int busI, int busJ, int busK, String id) {
        return getNodeBreakerEquipmentId(equipmentType.getTextCode(), busI, busJ, busK, id);
    }

    // EquipmentId must be independent of the bus order
    static String getNodeBreakerEquipmentId(String type, int busI, int busJ, int busK, String id) {
        List<Integer> sortedBuses = Stream.of(busI, busJ, busK).sorted().toList();
        int bus1 = sortedBuses.get(0);
        int bus2 = sortedBuses.get(1);
        int bus3 = sortedBuses.get(2);

        return type + "." + bus1 + "." + bus2 + "." + bus3 + "." + id;
    }

    static String getPsseEquipmentType(Identifiable<?> identifiable) {
        return switch (identifiable.getType()) {
            case LOAD, BATTERY -> PsseEquipmentType.PSSE_LOAD.getTextCode();
            case GENERATOR -> PsseEquipmentType.PSSE_GENERATOR.getTextCode();
            case LINE, TIE_LINE, DANGLING_LINE -> PsseEquipmentType.PSSE_BRANCH.getTextCode();
            case TWO_WINDINGS_TRANSFORMER -> PsseEquipmentType.PSSE_TWO_WINDING.getTextCode();
            case THREE_WINDINGS_TRANSFORMER -> PsseEquipmentType.PSSE_THREE_WINDING.getTextCode();
            case SHUNT_COMPENSATOR -> {
                ShuntCompensator shunt = (ShuntCompensator) identifiable;
                yield isFixedShunt(shunt) ? PsseEquipmentType.PSSE_FIXED_SHUNT.getTextCode() : PsseEquipmentType.PSSE_SWITCHED_SHUNT.getTextCode();
            }
            case HVDC_LINE -> {
                HvdcLine hvdcLine = (HvdcLine) identifiable;
                yield isTwoTerminalDcTransmissionLine(hvdcLine) ? PsseEquipmentType.PSSE_TWO_TERMINAL_DC_LINE.getTextCode() : PsseEquipmentType.PSSE_VSC_DC_LINE.getTextCode();
            }
            case STATIC_VAR_COMPENSATOR -> PsseEquipmentType.PSSE_FACTS_DEVICE.getTextCode();
            default -> throw new PsseException("unexpected identifiableType: " + identifiable.getType().name());
        };
    }

    // busI, busJ, busK and busEnd attributes are required to support lines and transformers within a voltageLevel
    static String getNodeBreakerEquipmentIdBus(String equipmentId, int bus, int end) {
        return equipmentId + "." + bus + "." + end;
    }

    static String getNodeBreakerEquipmentIdBus(String equipmentId, int busI, int busJ, int busK, int bus, String busEnd) {
        return equipmentId + "." + bus + "." + getBusEnd(busI, busJ, busK, bus, busEnd);
    }

    private static int getBusEnd(int busI, int busJ, int busK, int bus, String busEnd) {
        if (busI > 0 && busJ == 0 && busK == 0 && busI == bus) {
            return 1;
        }
        List<String> buses = new ArrayList<>();
        if (busI > 0 && busJ > 0 && busK == 0) {
            buses.add(String.format("%07d.I", busI));
            buses.add(String.format("%07d.J", busJ));
        }
        if (busI > 0 && busJ > 0 && busK > 0) {
            buses.add(String.format("%07d.I", busI));
            buses.add(String.format("%07d.J", busJ));
            buses.add(String.format("%07d.K", busK));
        }
        List<String> sortedBuses = buses.stream().sorted().toList();
        int index = sortedBuses.indexOf(String.format("%07d.%s", bus, busEnd));
        if (index == -1) {
            throw new PsseException("Unexpected attributes busI: " + busI + " busJ: " + busJ + " busK: " + busK + " bus: " + bus + " busEnd: " + busEnd);
        }
        return index + 1;
    }

    static Terminal findTerminalNode(Network network, String voltageLevelId, int node) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        return voltageLevel != null ? findTerminalNode(voltageLevel, node) : null;
    }

    static Terminal findTerminalNode(VoltageLevel voltageLevel, int node) {
        return voltageLevel.getNodeBreakerView().getOptionalTerminal(node)
                .orElseGet(() -> Networks.getEquivalentTerminal(voltageLevel, node));
    }

    static Optional<Bus> findConnectedBusViewNode(VoltageLevel voltageLevel, int node) {
        return Optional.ofNullable(findTerminalNode(voltageLevel, node))
                .map(Terminal::getBusView)
                .map(Terminal.BusView::getBus);
    }

    static Bus getTerminalConnectableBusView(Terminal terminal) {
        return terminal.getBusView().getBus() != null ? terminal.getBusView().getBus() : terminal.getBusView().getConnectableBus();
    }

    static int getTerminalBusI(Terminal terminal, ContextExport contextExport) {
        if (contextExport.getFullExport().isExportedAsNodeBreaker(terminal.getVoltageLevel())) {
            return contextExport.getFullExport().getBusI(terminal.getVoltageLevel(), terminal.getNodeBreakerView().getNode()).orElseThrow();
        } else {
            Bus bus = getTerminalConnectableBusView(terminal);
            return contextExport.getFullExport().getBusI(bus).orElseThrow();
        }
    }

    static int getRegulatingTerminalNode(Terminal regulatingTerminal, ContextExport contextExport) {
        if (regulatingTerminal == null) {
            return 0;
        } else {
            if (contextExport.getFullExport().isExportedAsNodeBreaker(regulatingTerminal.getVoltageLevel())) {
                return contextExport.getFullExport().getPsseNode(regulatingTerminal.getVoltageLevel(), regulatingTerminal.getNodeBreakerView().getNode()).orElseThrow();
            } else {
                return 0;
            }
        }
    }

    // zero can be used for local regulation
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

    static int getStatus(Terminal terminal, ContextExport contextExport) {
        if (contextExport.getFullExport().isExportedAsNodeBreaker(terminal.getVoltageLevel())) {
            return contextExport.getFullExport().isDeEnergized(terminal.getVoltageLevel(), terminal.getNodeBreakerView().getNode()) ? 0 : 1;
        } else {
            return getUpdatedStatus(terminal);
        }
    }

    static int getStatus(Terminal terminal1, Terminal terminal2, ContextExport contextExport) {
        return getStatus(terminal1, contextExport) == 1 && getStatus(terminal2, contextExport) == 1 ? 1 : 0;
    }

    static int getUpdatedStatus(Terminal terminal) {
        return terminal.isConnected() && terminal.getBusView().getBus() != null ? 1 : 0;
    }

    static int getUpdatedStatus(Terminal terminal1, Terminal terminal2) {
        return getUpdatedStatus(terminal1) == 1 && getUpdatedStatus(terminal2) == 1 ? 1 : 0;
    }

    static int findBusViewBusType(Bus bus) {
        if (bus == null || !bus.isInMainConnectedComponent()) {
            return 4;
        }
        SlackTerminal slackTerminal = bus.getVoltageLevel().getExtension(SlackTerminal.class);
        if (slackTerminal != null
                && slackTerminal.getTerminal().getBusView().getBus() != null
                && bus.getId().equals(slackTerminal.getTerminal().getBusView().getBus().getId())) {
            return 3;
        }
        return bus.getGeneratorStream().anyMatch(AbstractConverter::withLocalRegulatingControl) ? 2 : 1;
    }

    private static boolean withLocalRegulatingControl(Generator generator) {
        Bus generatorBus = generator.getTerminal().getBusView().getBus();
        Bus regulatedBus = generator.getRegulatingTerminal().getBusView().getBus();
        return generator.isVoltageRegulatorOn() && generatorBus != null && regulatedBus != null && generatorBus.getId().equals(regulatedBus.getId());
    }

    // node numbers in psse must be between 1 and 999
    // node psse 999 is used for mapping the node 0 of iidm
    static boolean exportVoltageLevelAsNodeBreaker(VoltageLevel voltageLevel) {
        return voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER
                && voltageLevel.getNodeBreakerView().getSwitchCount() > 0
                && maxNode(voltageLevel) <= getMaxPsseNodeBySubstation();
    }

    static int getMaxPsseNodeBySubstation() {
        return MAX_PSSE_NODE_BY_SUBSTATION;
    }

    private static int maxNode(VoltageLevel voltageLevel) {
        return Arrays.stream(voltageLevel.getNodeBreakerView().getNodes()).max().orElse(0);
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
        return bus != null ? getVm(bus.getV() / bus.getVoltageLevel().getNominalV()) : 1.0;
    }

    static double getVm(double v) {
        return Double.isFinite(v) && v > 0.0 ? v : 1.0;
    }

    static double getVa(Bus bus) {
        return bus != null ? getVa(bus.getAngle()) : 0.0;
    }

    static double getVa(double a) {
        return Double.isFinite(a) ? a : 0.0;
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

    static PsseBus createDefaultBus() {
        PsseBus psseBus = new PsseBus();
        psseBus.setI(0);
        psseBus.setName("");
        psseBus.setBaskv(0.0);
        psseBus.setIde(1);
        psseBus.setArea(1);
        psseBus.setZone(1);
        psseBus.setOwner(1);
        psseBus.setVm(1.0);
        psseBus.setVa(0.0);
        psseBus.setNvhi(1.1);
        psseBus.setNvlo(0.9);
        psseBus.setEvhi(1.1);
        psseBus.setEvlo(0.9);
        return psseBus;
    }

    // first character must not be a minus sign
    static String fixBusName(String name) {
        String fixedName = name.startsWith("-") ? "_" + name.substring(1) : name;
        return fixedName.length() > MAX_BUS_LENGTH ? fixedName.substring(0, MAX_BUS_LENGTH) : fixedName;
    }

    static PsseLoad createDefaultLoad() {
        PsseLoad psseLoad = new PsseLoad();
        psseLoad.setI(0);
        psseLoad.setId("1");
        psseLoad.setStatus(1);
        psseLoad.setArea(1);
        psseLoad.setZone(1);
        psseLoad.setPl(0.0);
        psseLoad.setQl(0.0);
        psseLoad.setIp(0.0);
        psseLoad.setIq(0.0);
        psseLoad.setYp(0.0);
        psseLoad.setYq(0.0);
        psseLoad.setOwner(1);
        psseLoad.setScale(1);
        psseLoad.setIntrpt(0);
        psseLoad.setDgenp(0.0);
        psseLoad.setDgenq(0.0);
        psseLoad.setDgenm(0);
        psseLoad.setLoadtype("");
        return psseLoad;
    }

    static PsseNonTransformerBranch createDefaultNonTransformerBranch() {
        PsseNonTransformerBranch psseLine = new PsseNonTransformerBranch();
        psseLine.setI(0);
        psseLine.setJ(0);
        psseLine.setCkt("1");
        psseLine.setR(0.0);
        psseLine.setX(0.0);
        psseLine.setB(0.0);
        psseLine.setName("");
        psseLine.setRates(createDefaultRates());
        psseLine.setGi(0.0);
        psseLine.setBi(0.0);
        psseLine.setGj(0.0);
        psseLine.setBj(0.0);
        psseLine.setSt(1);
        psseLine.setMet(1);
        psseLine.setLen(0.0);
        psseLine.setOwnership(createDefaultOwnership());
        return psseLine;
    }

    static String fixNonTransformerBranchName(String name) {
        return name.substring(0, Math.min(MAX_BRANCH_LENGTH, name.length()));
    }

    static PsseGenerator createDefaultGenerator() {
        PsseGenerator psseGenerator = new PsseGenerator();
        psseGenerator.setI(0);
        psseGenerator.setId("1");
        psseGenerator.setPg(0.0);
        psseGenerator.setQg(0.0);
        psseGenerator.setQt(9999.0);
        psseGenerator.setQb(-9999.0);
        psseGenerator.setVs(1.0);
        psseGenerator.setIreg(0);
        psseGenerator.setNreg(0);
        psseGenerator.setMbase(100.0);
        psseGenerator.setZr(0.0);
        psseGenerator.setZx(1.0);
        psseGenerator.setRt(0.0);
        psseGenerator.setXt(0.0);
        psseGenerator.setGtap(1.0);
        psseGenerator.setStat(1);
        psseGenerator.setRmpct(100.0);
        psseGenerator.setPt(9999.0);
        psseGenerator.setPb(-9999.0);
        psseGenerator.setBaslod(0);
        psseGenerator.setOwnership(createDefaultOwnership());
        psseGenerator.setWmod(0);
        psseGenerator.setWpf(1.0);
        return psseGenerator;
    }

    static PsseRates createDefaultRates() {
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

    static PsseOwnership createDefaultOwnership() {
        PsseOwnership psseOwnership = new PsseOwnership();
        psseOwnership.setO1(1);
        psseOwnership.setF1(1.0);
        psseOwnership.setO2(0);
        psseOwnership.setF2(1.0);
        psseOwnership.setO3(0);
        psseOwnership.setF3(1.0);
        psseOwnership.setO4(0);
        psseOwnership.setF4(1.0);
        return psseOwnership;
    }

    static double currentInAmpsToMw(double current, double nominalV) {
        return current * nominalV / 1000.0;
    }

    private final ContainersMapping containersMapping;
    private final Network network;
}
