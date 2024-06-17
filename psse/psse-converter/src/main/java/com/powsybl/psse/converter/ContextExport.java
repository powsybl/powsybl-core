/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseSubstation;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
final class ContextExport {
    private int maxPsseBus;
    private int maxPsseSubstation;
    private final BusBreakerExport busBreakerExport;
    private final NodeBreakerExport nodeBreakerExport;
    private final Map<String, List<EqR>> equipmentTerminalData;
    private final Map<String, String> equipmentCkt;
    private final Map<String, List<String>> equipmentAllCkt;

    ContextExport(int maxPsseBus, int maxPsseSubstation) {
        this.maxPsseBus = maxPsseBus;
        this.maxPsseSubstation = maxPsseSubstation;
        this.busBreakerExport = new BusBreakerExport();
        this.nodeBreakerExport = new NodeBreakerExport();
        this.equipmentTerminalData = new HashMap<>();
        this.equipmentCkt = new HashMap<>();
        this.equipmentAllCkt = new HashMap<>();
    }

    int getNewPsseBusI() {
        return ++maxPsseBus;
    }

    int getNewPsseSubstationIs() {
        return ++maxPsseSubstation;
    }

    BusBreakerExport getBusBreakerExport() {
        return this.busBreakerExport;
    }

    NodeBreakerExport getNodeBreakerExport() {
        return this.nodeBreakerExport;
    }

    boolean isFreePsseBusI(int busI) {
        return busBreakerExport.isFreePsseBusId(busI) && nodeBreakerExport.isFreePsseBusId(busI);
    }

    void addEquipmentEnd(String equipmentId, VoltageLevel voltageLevel, int node, int busI, int end) {
        equipmentTerminalData.computeIfAbsent(equipmentId, k -> new ArrayList<>()).add(new EqR(voltageLevel, node, busI, end));
    }

    void addEquipmentEnd(String equipmentId, VoltageLevel voltageLevel, int busI, int end) {
        equipmentTerminalData.computeIfAbsent(equipmentId, k -> new ArrayList<>()).add(new EqR(voltageLevel, null, busI, end));
    }

    List<Integer> getEquipmentBusesList(String equipmentId, VoltageLevel voltageLevel, int node) {
        List<Integer> busesList = new ArrayList<>();
        if (equipmentTerminalData.containsKey(equipmentId)) {
            int bus = equipmentTerminalData.get(equipmentId).stream().filter(eqR -> eqR.voltageLevel.equals(voltageLevel) && eqR.node == node).map(eqR -> eqR.busI).findFirst().orElseThrow();
            busesList.add(bus);
            addToBusList(busesList, bus, equipmentTerminalData.get(equipmentId).stream().filter(eqR -> eqR.end == 1).map(eqR -> eqR.busI).findFirst().orElse(0));
            addToBusList(busesList, bus, equipmentTerminalData.get(equipmentId).stream().filter(eqR -> eqR.end == 2).map(eqR -> eqR.busI).findFirst().orElse(0));
            addToBusList(busesList, bus, equipmentTerminalData.get(equipmentId).stream().filter(eqR -> eqR.end == 3).map(eqR -> eqR.busI).findFirst().orElse(0));
        }
        return busesList;
    }

    private static void addToBusList(List<Integer> busList, int bus, int busEnd) {
        if (bus != busEnd) {
            busList.add(busEnd);
        }
    }

    String getEquipmentCkt(String equipmentId, IdentifiableType type, int busI) {
        return getEquipmentCkt(null, equipmentId, type, busI, 0, 0);
    }

    String getEquipmentCkt(VoltageLevel voltageLevel, String equipmentId, IdentifiableType type, int busI, int busJ) {
        return getEquipmentCkt(voltageLevel, equipmentId, type, busI, busJ, 0);
    }

    String getEquipmentCkt(String equipmentId, IdentifiableType type, int busI, int busJ) {
        return getEquipmentCkt(null, equipmentId, type, busI, busJ, 0);
    }

    String getEquipmentCkt(String equipmentId, IdentifiableType type, int busI, int busJ, int busK) {
        return getEquipmentCkt(null, equipmentId, type, busI, busJ, busK);
    }

    private String getEquipmentCkt(VoltageLevel voltageLevel, String equipmentId, IdentifiableType type, int busI, int busJ, int busK) {
        if (equipmentCkt.containsKey(equipmentId)) {
            return equipmentCkt.get(equipmentId);
        }
        String equipmentBusesId = getEquipmentBusesId(equipmentId, getVoltageLevelType(voltageLevel, type), busI, busJ, busK);
        Optional<String> optCkt = AbstractConverter.extractCkt(equipmentId, type);
        if (optCkt.isPresent() && cktIsFree(equipmentBusesId, optCkt.get())) {
            addCkt(equipmentId, equipmentBusesId, optCkt.get());
            return optCkt.get();
        }
        String ckt = getNewCkt(equipmentBusesId);
        addCkt(equipmentId, equipmentBusesId, ckt);
        return ckt;
    }

    private boolean cktIsFree(String equipmentBusesId, String ckt) {
        if (equipmentAllCkt.containsKey(equipmentBusesId)) {
            return !equipmentAllCkt.get(equipmentBusesId).contains(ckt);
        } else {
            return true;
        }
    }

    private void addCkt(String equipmentId, String equipmentBusesId, String ckt) {
        equipmentAllCkt.computeIfAbsent(equipmentBusesId, k -> new ArrayList<>()).add(ckt);
        equipmentCkt.put(equipmentId, ckt);
    }

    private String getNewCkt(String equipmentBusesId) {
        for (int i = 1; i <= 99; i++) {
            String ckt = String.format("%02d", i);
            if (cktIsFree(equipmentBusesId, ckt)) {
                return ckt;
            }
        }
        throw new PsseException("unable to obtain a ckt for " + equipmentBusesId);
    }

    // switches must be unique inside the voltageLevel
    private static String getVoltageLevelType(VoltageLevel voltageLevel, IdentifiableType type) {
        return voltageLevel != null ? voltageLevel.getId() + "-" + type.name() : type.name();
    }

    private static String getEquipmentBusesId(String equipmentId, String type, int busI, int busJ, int busK) {
        List<Integer> sortedBuses = Stream.of(busI, busJ, busK).filter(bus -> bus != 0).sorted().toList();
        if (sortedBuses.size() == 1) {
            return type + "-" + sortedBuses.get(0);
        } else if (sortedBuses.size() == 2) {
            return type + "-" + sortedBuses.get(0) + "-" + sortedBuses.get(1);
        } else if (sortedBuses.size() == 3) {
            return type + "-" + sortedBuses.get(0) + "-" + sortedBuses.get(1) + "-" + sortedBuses.get(2);
        } else {
            throw new PsseException("All the buses are zero. EquipmentId: " + equipmentId);
        }
    }

    static class BusBreakerExport {
        private final Map<String, BusR> buses;
        private final Set<Integer> usedPsseBusI;

        BusBreakerExport() {
            this.buses = new HashMap<>();
            this.usedPsseBusI = new HashSet<>();
        }

        void addBus(String busViewId, int busI, int type) {
            buses.put(busViewId, new BusR(busI, type));
            usedPsseBusI.add(busI);
        }

        List<String> getBuses() {
            return buses.keySet().stream().sorted().toList();
        }

        OptionalInt getBusBusI(String busViewId) {
            return buses.containsKey(busViewId) ? OptionalInt.of(buses.get(busViewId).busI) : OptionalInt.empty();
        }

        OptionalInt getBusType(String busViewId) {
            return buses.containsKey(busViewId) ? OptionalInt.of(buses.get(busViewId).type) : OptionalInt.empty();
        }

        private boolean isFreePsseBusId(int busI) {
            return !usedPsseBusI.contains(busI);
        }

        private record BusR(int busI, int type) {
        }
    }

    static class NodeBreakerExport {
        private final Map<VoltageLevel, PsseSubstation> voltageLevels;
        private final Map<String, NodeR> nodes;
        private final Map<String, BusR> buses;
        private final Map<Integer, BusIR> isolatedBusesI;
        private final Set<Integer> usedPsseBusI;
        private final Map<String, SelectedNodeR> selectedNodes;

        NodeBreakerExport() {
            this.voltageLevels = new HashMap<>();
            this.nodes = new HashMap<>();
            this.buses = new HashMap<>();
            this.isolatedBusesI = new HashMap<>();
            this.usedPsseBusI = new HashSet<>();
            this.selectedNodes = new HashMap<>();
        }

        void addVoltageLevels(List<VoltageLevel> voltageLevels) {
            voltageLevels.forEach(voltageLevel -> this.voltageLevels.put(voltageLevel, null));
        }

        void addVoltageLevel(VoltageLevel voltageLevel, PsseSubstation psseSubstation) {
            voltageLevels.put(voltageLevel, psseSubstation);
        }

        List<VoltageLevel> getVoltageLevels() {
            return voltageLevels.keySet().stream().sorted(Comparator.comparing(Identifiable::getId)).toList();
        }

        Optional<PsseSubstation> getPsseSubstationMappedToVoltageLevel(VoltageLevel voltageLevel) {
            return Optional.ofNullable(voltageLevels.get(voltageLevel));
        }

        void addNodes(String busViewBusId, VoltageLevel voltageLevel, List<Integer> nodes, int busI, Integer busCopy, int type) {
            nodes.forEach(node -> this.nodes.put(getNodeId(voltageLevel, node), new NodeR(voltageLevel, node, busI, isInService(type), busViewBusId)));
            buses.put(busViewBusId, new BusR(busI, busCopy, type));
            usedPsseBusI.add(busI);
        }

        void addIsolatedNode(VoltageLevel voltageLevel, int node, int busI, Integer busCopy) {
            nodes.put(getNodeId(voltageLevel, node), new NodeR(voltageLevel, node, busI, false, null));
            isolatedBusesI.put(busI, new BusIR(voltageLevel, busCopy));
            usedPsseBusI.add(busI);
        }

        void addNodes(String busViewBusId, VoltageLevel voltageLevel, List<Integer> nodes, int busI, int type) {
            nodes.forEach(node -> this.nodes.put(getNodeId(voltageLevel, node), new NodeR(voltageLevel, node, busI, isInService(type), busViewBusId)));
            buses.put(busViewBusId, new BusR(busI, null, type));
            usedPsseBusI.add(busI);
        }

        void addIsolatedNode(VoltageLevel voltageLevel, int node, int busI) {
            nodes.put(getNodeId(voltageLevel, node), new NodeR(voltageLevel, node, busI, false, null));
            isolatedBusesI.put(busI, new BusIR(voltageLevel, null));
            usedPsseBusI.add(busI);
        }

        List<String> getNodes(VoltageLevel voltageLevel) {
            return nodes.keySet().stream().filter(voltageLevelNodeId -> nodes.get(voltageLevelNodeId).voltageLevel.equals(voltageLevel)).sorted().toList();
        }

        OptionalInt getNodeNi(String voltageLevelNodeId) {
            return nodes.containsKey(voltageLevelNodeId) ? OptionalInt.of(nodes.get(voltageLevelNodeId).node) : OptionalInt.empty();
        }

        OptionalInt getNodeBusI(String voltageLevelNodeId) {
            return nodes.containsKey(voltageLevelNodeId) ? OptionalInt.of(nodes.get(voltageLevelNodeId).busI) : OptionalInt.empty();
        }

        Optional<Boolean> getNodeIsInService(String voltageLevelNodeId) {
            return nodes.containsKey(voltageLevelNodeId) ? Optional.of(nodes.get(voltageLevelNodeId).isInService) : Optional.empty();
        }

        Optional<String> getNodeBusViewBusId(String voltageLevelNodeId) {
            return nodes.containsKey(voltageLevelNodeId) ? Optional.ofNullable(nodes.get(voltageLevelNodeId).busViewBusId) : Optional.empty();
        }

        List<String> getBuses() {
            return buses.keySet().stream().sorted().toList();
        }

        OptionalInt getBusBusI(String busViewId) {
            return buses.containsKey(busViewId) ? OptionalInt.of(buses.get(busViewId).busI) : OptionalInt.empty();
        }

        Optional<Integer> getBusBusCopy(String busViewId) {
            return buses.containsKey(busViewId) ? Optional.ofNullable(buses.get(busViewId).busCopy) : Optional.empty();
        }

        OptionalInt getBusType(String busViewId) {
            return buses.containsKey(busViewId) ? OptionalInt.of(buses.get(busViewId).type) : OptionalInt.empty();
        }

        OptionalInt getNodeBusI(VoltageLevel voltageLevel, int node) {
            return nodes.containsKey(getNodeId(voltageLevel, node)) ? OptionalInt.of(nodes.get(getNodeId(voltageLevel, node)).busI) : OptionalInt.empty();
        }

        Optional<Boolean> isNodeInService(VoltageLevel voltageLevel, int node) {
            return nodes.containsKey(getNodeId(voltageLevel, node)) ? Optional.of(nodes.get(getNodeId(voltageLevel, node)).isInService) : Optional.empty();
        }

        List<Integer> getIsolatedBusesI() {
            return isolatedBusesI.keySet().stream().sorted().toList();
        }

        Optional<VoltageLevel> getIsolatedBusIVoltageLevel(int busI) {
            return isolatedBusesI.containsKey(busI) ? Optional.of(isolatedBusesI.get(busI).voltageLevel) : Optional.empty();
        }

        Optional<Integer> getIsolatedBusIBusCopy(int busI) {
            return isolatedBusesI.containsKey(busI) ? Optional.ofNullable(isolatedBusesI.get(busI).busCopy) : Optional.empty();
        }

        void addSelectedNode(VoltageLevel voltageLevel, Set<Integer> nodes, int selectedNode) {
            nodes.forEach(node -> selectedNodes.put(getNodeId(voltageLevel, node), new SelectedNodeR(voltageLevel, selectedNode)));
        }

        List<Integer> getSelectedNodes(VoltageLevel voltageLevel) {
            return new HashSet<>(selectedNodes.values()).stream().filter(value -> value.voltageLevel.equals(voltageLevel)).map(value -> value.node).sorted().toList();
        }

        OptionalInt getSelectedNode(VoltageLevel voltageLevel, int node) {
            return selectedNodes.containsKey(getNodeId(voltageLevel, node)) ? OptionalInt.of(selectedNodes.get(getNodeId(voltageLevel, node)).node) : OptionalInt.empty();
        }

        private static boolean isInService(int type) {
            return type != 4;
        }

        private static String getNodeId(VoltageLevel voltageLevel, int node) {
            return voltageLevel.getId() + "-" + node;
        }

        private boolean isFreePsseBusId(int busI) {
            return !usedPsseBusI.contains(busI);
        }

        private record SelectedNodeR(VoltageLevel voltageLevel, int node) {
        }

        private record NodeR(VoltageLevel voltageLevel, int node, int busI, boolean isInService, String busViewBusId) {
        }

        private record BusR(int busI, Integer busCopy, int type) {
        }

        private record BusIR(VoltageLevel voltageLevel, Integer busCopy) {
        }
    }

    private record EqR(VoltageLevel voltageLevel, Integer node, int busI, int end) {
    }
}
