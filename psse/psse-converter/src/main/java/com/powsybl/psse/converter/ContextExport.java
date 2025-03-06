/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
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
    private final boolean isFullExport;
    private final UpdateExport updateExport;
    private final FullExport fullExport;

    ContextExport(boolean isFullExport) {
        this.isFullExport = isFullExport;
        this.updateExport = new UpdateExport();
        this.fullExport = new FullExport();
    }

    boolean isFullExport() {
        return this.isFullExport;
    }

    UpdateExport getUpdateExport() {
        return this.updateExport;
    }

    FullExport getFullExport() {
        return this.fullExport;
    }

    static class UpdateExport {
        private final Map<Integer, Bus> busIBusView;
        private final Map<String, Bus> voltageLevelNodeIdBusView;
        private final Map<VoltageLevel, PsseSubstation> voltageLevelPsseSubstation;

        UpdateExport() {
            this.busIBusView = new HashMap<>();
            this.voltageLevelNodeIdBusView = new HashMap<>();
            this.voltageLevelPsseSubstation = new HashMap<>();
        }

        void addBusIBusView(int busI, Bus busView) {
            this.busIBusView.put(busI, busView);
        }

        Optional<Bus> getBusView(int busI) {
            return this.busIBusView.containsKey(busI) ? Optional.of(this.busIBusView.get(busI)) : Optional.empty();
        }

        // busView can be null
        void addNodeBusView(VoltageLevel voltageLevel, int node, Bus busView) {
            this.voltageLevelNodeIdBusView.put(AbstractConverter.getNodeId(voltageLevel, node), busView);
        }

        Optional<Bus> getBusView(VoltageLevel voltageLevel, int node) {
            return Optional.ofNullable(this.voltageLevelNodeIdBusView.get(AbstractConverter.getNodeId(voltageLevel, node)));
        }

        void addVoltageLevelPsseSubstation(VoltageLevel voltageLevel, PsseSubstation psseSubstation) {
            voltageLevelPsseSubstation.put(voltageLevel, psseSubstation);
        }

        Optional<PsseSubstation> getPsseSubstation(VoltageLevel voltageLevel) {
            return Optional.ofNullable(voltageLevelPsseSubstation.get(voltageLevel));
        }
    }

    static class FullExport {
        private int maxPsseBus;
        private int maxPsseSubstation;
        private final Map<Bus, Integer> busViewBusI;
        private final Map<Integer, Bus> busIBusView;
        private final Map<DanglingLine, Integer> danglingLineBusI;
        private final Map<String, NodeData> voltageLevelNodeIdNodeData;
        private final Map<String, Integer> psseSubstationIdLastPsseNode;
        private final Map<String, Set<VoltageLevel>> psseSubstationIdVoltageLevels;
        private final Map<String, String> equipmentIdCkt;
        private final Map<String, Integer> equipmentBusesIdMaxCkt;

        FullExport() {
            this.maxPsseBus = 0;
            this.maxPsseSubstation = 0;
            this.busViewBusI = new HashMap<>();
            this.busIBusView = new HashMap<>();
            this.danglingLineBusI = new HashMap<>();
            this.voltageLevelNodeIdNodeData = new HashMap<>();
            this.psseSubstationIdLastPsseNode = new HashMap<>();
            this.psseSubstationIdVoltageLevels = new HashMap<>();
            this.equipmentIdCkt = new HashMap<>();
            this.equipmentBusesIdMaxCkt = new HashMap<>();
        }

        int getNewPsseBusI() {
            return ++maxPsseBus;
        }

        int getNewPsseSubstationIs() {
            return ++maxPsseSubstation;
        }

        void addBusIBusView(int busI, Bus busView) {
            this.busIBusView.put(busI, busView);
            if (busView != null) {
                this.busViewBusI.put(busView, busI);
            }
        }

        void addDanglingLineBusI(DanglingLine danglingLine, int busI) {
            this.danglingLineBusI.put(danglingLine, busI);
        }

        Set<Integer> getBusISet() {
            return busIBusView.keySet();
        }

        OptionalInt getBusI(Bus busView) {
            return Optional.ofNullable(this.busViewBusI.get(busView)).map(OptionalInt::of).orElse(OptionalInt.empty());
        }

        OptionalInt getBusI(DanglingLine danglingLine) {
            return Optional.ofNullable(this.danglingLineBusI.get(danglingLine)).map(OptionalInt::of).orElse(OptionalInt.empty());
        }

        Optional<Bus> getBusView(int busI) {
            return Optional.ofNullable(this.busIBusView.get(busI));
        }

        // voltageBusViewBus can be null
        void addNodeData(VoltageLevel voltageLevel, int node, int psseBusI, int psseNode, Bus voltageBusViewBus) {
            this.voltageLevelNodeIdNodeData.put(AbstractConverter.getNodeId(voltageLevel, node), new NodeData(voltageLevel, node, psseBusI, psseNode, voltageBusViewBus));
        }

        OptionalInt getBusI(VoltageLevel voltageLevel, int node) {
            String voltageLevelNodeId = AbstractConverter.getNodeId(voltageLevel, node);
            return Optional.ofNullable(this.voltageLevelNodeIdNodeData.get(voltageLevelNodeId))
                    .map(nodeData -> OptionalInt.of(nodeData.psseBusI))
                    .orElse(OptionalInt.empty());
        }

        OptionalInt getPsseNode(VoltageLevel voltageLevel, int node) {
            String voltageLevelNodeId = AbstractConverter.getNodeId(voltageLevel, node);
            return Optional.ofNullable(this.voltageLevelNodeIdNodeData.get(voltageLevelNodeId))
                    .map(nodeData -> OptionalInt.of(nodeData.psseNode))
                    .orElse(OptionalInt.empty());
        }

        Optional<Bus> getVoltageBus(VoltageLevel voltageLevel, int node) {
            String voltageLevelNodeId = AbstractConverter.getNodeId(voltageLevel, node);
            return Optional.ofNullable(this.voltageLevelNodeIdNodeData.get(voltageLevelNodeId)).map(nodeData -> nodeData.voltageBusViewBus);
        }

        boolean isDeEnergized(VoltageLevel voltageLevel, int node) {
            return getVoltageBus(voltageLevel, node).map(bus -> bus.getV() == 0.0 && bus.getAngle() == 0.0).orElse(true);
        }

        Optional<VoltageLevel> getVoltageLevel(int busI) {
            return this.voltageLevelNodeIdNodeData.values().stream().filter(nodeData -> nodeData.psseBusI == busI).map(nodeData -> nodeData.voltageLevel).findFirst();
        }

        OptionalInt getNode(int busI) {
            return this.voltageLevelNodeIdNodeData.values().stream().filter(nodeData -> nodeData.psseBusI == busI).mapToInt(nodeData -> nodeData.node).findFirst();
        }

        private record NodeData(VoltageLevel voltageLevel, int node, int psseBusI, int psseNode, Bus voltageBusViewBus) {
        }

        int getNewPsseNode(String psseSubstationId) {
            int newPsseNode = getLastPsseNode(psseSubstationId) + 1;
            this.psseSubstationIdLastPsseNode.put(psseSubstationId, newPsseNode);
            return newPsseNode;
        }

        int getLastPsseNode(String psseSubstationId) {
            return this.psseSubstationIdLastPsseNode.getOrDefault(psseSubstationId, 0);
        }

        void addPsseSubstationIdVoltageLevel(String psseSubstationId, VoltageLevel voltageLevel) {
            this.psseSubstationIdVoltageLevels.computeIfAbsent(psseSubstationId, k -> new HashSet<>()).add(voltageLevel);
        }

        List<String> getSortedPsseSubstationIds() {
            return this.psseSubstationIdVoltageLevels.keySet().stream().sorted().toList();
        }

        Set<VoltageLevel> getVoltageLevelSet(String psseSubstationId) {
            return this.psseSubstationIdVoltageLevels.getOrDefault(psseSubstationId, Collections.emptySet());
        }

        boolean isExportedAsNodeBreaker(VoltageLevel voltageLevel) {
            return this.psseSubstationIdVoltageLevels.values().stream().flatMap(Set::stream).toList().contains(voltageLevel);
        }

        String getEquipmentCkt(String equipmentId, IdentifiableType type, int busI) {
            return getEquipmentCkt(null, equipmentId, type, busI, 0, 0);
        }

        String getEquipmentCkt(VoltageLevel voltageLevel, String equipmentId, int busI, int busJ) {
            return getEquipmentCkt(voltageLevel, equipmentId, IdentifiableType.SWITCH, busI, busJ, 0);
        }

        String getEquipmentCkt(String equipmentId, IdentifiableType type, int busI, int busJ) {
            return getEquipmentCkt(null, equipmentId, type, busI, busJ, 0);
        }

        String getEquipmentCkt(String equipmentId, IdentifiableType type, int busI, int busJ, int busK) {
            return getEquipmentCkt(null, equipmentId, type, busI, busJ, busK);
        }

        private String getEquipmentCkt(VoltageLevel voltageLevel, String equipmentId, IdentifiableType type, int busI, int busJ, int busK) {
            if (equipmentIdCkt.containsKey(equipmentId)) {
                return equipmentIdCkt.get(equipmentId);
            }
            String equipmentBusesId = getEquipmentBusesId(equipmentId, getVoltageLevelType(voltageLevel, type), busI, busJ, busK);
            int cktInteger = getNewCkt(equipmentBusesId);
            String cktString = String.format("%02d", cktInteger);
            addCkt(equipmentId, equipmentBusesId, cktInteger, cktString);
            return cktString;
        }

        private void addCkt(String equipmentId, String equipmentBusesId, int maxCkt, String ckt) {
            equipmentBusesIdMaxCkt.put(equipmentBusesId, maxCkt);
            equipmentIdCkt.put(equipmentId, ckt);
        }

        private int getNewCkt(String equipmentBusesId) {
            return equipmentBusesIdMaxCkt.containsKey(equipmentBusesId) ? equipmentBusesIdMaxCkt.get(equipmentBusesId) + 1 : 1;
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
    }
}
