/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesIidmMappingImpl extends AbstractExtension<Network> implements CgmesIidmMapping {

    private final Map<EquipmentSide, String> equipmentSideTopologicalNodeMap;
    private final Map<String, Set<String>> busTopologicalNodeMap;
    private final Set<String> unmapped;

    CgmesIidmMappingImpl(Set<String> topologicalNodes) {
        equipmentSideTopologicalNodeMap = new HashMap<>();
        busTopologicalNodeMap = new HashMap<>();
        unmapped = new HashSet<>();
        unmapped.addAll(Objects.requireNonNull(topologicalNodes));
    }

    @Override
    public Set<String> getTopologicalNodes(String busId) {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.get(busId);
    }

    @Override
    public String getTopologicalNode(String equipmentId, int side) {
        return equipmentSideTopologicalNodeMap.get(new EquipmentSide(equipmentId, side));
    }

    @Override
    public boolean isMapped(String busId) {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.containsKey(busId);
    }

    @Override
    public boolean isEmpty() {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.isEmpty();
    }

    @Override
    public CgmesIidmMapping put(String equipmentId, int side, String topologicalNodeId) {
        equipmentSideTopologicalNodeMap.put(new EquipmentSide(equipmentId, side), topologicalNodeId);
        return this;
    }

    @Override
    public CgmesIidmMapping put(String busId, String topologicalNodeId) {
        checkAlreadyMapped(busId, topologicalNodeId);
        busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(topologicalNodeId);
        unmapped.remove(topologicalNodeId);
        return this;
    }

    @Override
    public Map<String, Set<String>> toMap() {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return new HashMap<>(busTopologicalNodeMap);
    }

    @Override
    public Set<String> getUnmappedTopologicalNodes() {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return new HashSet<>(unmapped);
    }

    private void calculate() {
        equipmentSideTopologicalNodeMap.forEach((equipmentSide, tn) -> {
            Identifiable i = getExtendable().getIdentifiable(equipmentSide.equipmentId);
            if (i instanceof Connectable) {
                Connectable c = (Connectable) i;
                Terminal t = (Terminal) c.getTerminals().get(equipmentSide.side - 1);
                String busId = t.getBusBreakerView().getBus().getId();
                checkAlreadyMapped(busId, tn);
                busTopologicalNodeMap.computeIfAbsent(busId, bid -> new HashSet<>()).add(tn);
                unmapped.remove(tn);
            }
        });
    }

    private void checkAlreadyMapped(String busId, String topologicalNodeId) {
        if (!unmapped.contains(topologicalNodeId) && (busTopologicalNodeMap.get(busId) == null || !busTopologicalNodeMap.get(busId).contains(topologicalNodeId))) {
            throw new PowsyblException("TopologicalNode " + topologicalNodeId + " is already mapped to another bus");
        }
    }

    private static class EquipmentSide {
        EquipmentSide(String equipmentId, int side) {
            this.equipmentId = Objects.requireNonNull(equipmentId);
            this.side = side;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof EquipmentSide)) {
                return false;
            }
            EquipmentSide other = (EquipmentSide) o;
            return this.equipmentId.equals(other.equipmentId) && this.side == other.side;
        }

        @Override
        public int hashCode() {
            return Objects.hash(equipmentId, side);
        }

        private final String equipmentId;
        private final int side;
    }
}
