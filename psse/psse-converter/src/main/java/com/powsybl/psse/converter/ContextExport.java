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
    private final LinkExport linkExport;
    private final UpdateExport updateExport;
    private final FullExport fullExport;

    ContextExport(boolean isFullExport) {
        this.isFullExport = isFullExport;
        this.linkExport = new LinkExport();
        this.updateExport = new UpdateExport();
        this.fullExport = new FullExport();
    }

    boolean isFullExport() {
        return this.isFullExport;
    }

    LinkExport getLinkExport() {
        return this.linkExport;
    }

    UpdateExport getUpdateExport() {
        return this.updateExport;
    }

    FullExport getFullExport() {
        return this.fullExport;
    }

    static class LinkExport {
        private final Map<Bus, Integer> busViewToBusI;
        private final Map<Integer, Bus> busIToBusView;
        private final Map<DanglingLine, Integer> danglingLineToBusI;
        private final Map<String, Integer> voltageLevelNodeIdToBusI;

        LinkExport() {
            this.busViewToBusI = new HashMap<>();
            this.busIToBusView = new HashMap<>();
            this.danglingLineToBusI = new HashMap<>();
            this.voltageLevelNodeIdToBusI = new HashMap<>();
        }

        void addBusViewBusIDoubleLink(Bus busView, int busI) {
            this.busViewToBusI.put(busView, busI);
            this.busIToBusView.put(busI, busView);
        }

        void addDanglingLineBusILink(DanglingLine danglingLine, int busI) {
            this.danglingLineToBusI.put(danglingLine, busI);
        }

        Set<Integer> getBusISet() {
            return busIToBusView.keySet();
        }

        OptionalInt getBusI(Bus busView) {
            return this.busViewToBusI.containsKey(busView) ? OptionalInt.of(this.busViewToBusI.get(busView)) : OptionalInt.empty();
        }

        OptionalInt getBusI(DanglingLine danglingLine) {
            return this.danglingLineToBusI.containsKey(danglingLine) ? OptionalInt.of(this.danglingLineToBusI.get(danglingLine)) : OptionalInt.empty();
        }

        Optional<Bus> getBusView(int busI) {
            return this.busIToBusView.containsKey(busI) ? Optional.of(this.busIToBusView.get(busI)) : Optional.empty();
        }

        void addNodeBusILink(VoltageLevel voltageLevel, int node, int busI) {
            this.voltageLevelNodeIdToBusI.put(AbstractConverter.getNodeId(voltageLevel, node), busI);
        }

        OptionalInt getBusI(VoltageLevel voltageLevel, int node) {
            String voltageLevelNodeId = AbstractConverter.getNodeId(voltageLevel, node);
            return this.voltageLevelNodeIdToBusI.containsKey(voltageLevelNodeId) ? OptionalInt.of(this.voltageLevelNodeIdToBusI.get(voltageLevelNodeId)) : OptionalInt.empty();
        }

        Optional<Bus> getBusView(VoltageLevel voltageLevel, int node) {
            OptionalInt busI = getBusI(voltageLevel, node);
            return busI.isPresent() ? getBusView(busI.getAsInt()) : Optional.empty();
        }
    }

    static class UpdateExport {
        private final Map<VoltageLevel, PsseSubstation> voltageLevelPsseSubstation;

        UpdateExport() {
            this.voltageLevelPsseSubstation = new HashMap<>();
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
        private final Map<String, Integer> internalConnectionNodeRepresentative;
        private final Map<String, String> equipmentIdCkt;
        private final Map<String, Integer> equipmentBusesIdMaxCkt;

        FullExport() {
            this.maxPsseBus = 0;
            this.maxPsseSubstation = 0;
            this.internalConnectionNodeRepresentative = new HashMap<>();
            this.equipmentIdCkt = new HashMap<>();
            this.equipmentBusesIdMaxCkt = new HashMap<>();
        }

        int getNewPsseBusI() {
            return ++maxPsseBus;
        }

        int getNewPsseSubstationIs() {
            return ++maxPsseSubstation;
        }

        void addInternalConnectionNodeRepresentativeNode(VoltageLevel voltageLevel, Set<Integer> internalNodes, int representativeNode) {
            internalNodes.forEach(node -> this.internalConnectionNodeRepresentative.put(AbstractConverter.getNodeId(voltageLevel, node), representativeNode));
        }

        int getRepresentativeNode(VoltageLevel voltageLevel, int node) {
            return getRepresentativeNodeOfInternalConnectionNode(voltageLevel, node).orElse(node);
        }

        private OptionalInt getRepresentativeNodeOfInternalConnectionNode(VoltageLevel voltageLevel, int node) {
            return internalConnectionNodeRepresentative.containsKey(AbstractConverter.getNodeId(voltageLevel, node)) ? OptionalInt.of(internalConnectionNodeRepresentative.get(AbstractConverter.getNodeId(voltageLevel, node))) : OptionalInt.empty();
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
