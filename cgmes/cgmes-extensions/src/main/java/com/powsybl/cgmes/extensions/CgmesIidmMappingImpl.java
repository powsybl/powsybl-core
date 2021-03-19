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

    // TODO(Luma) Improve serialization by not writing calculated bus ids

    // Ideally we would like to store all the mappings (Terminal --> TopologicalNode)
    // present in the original CGMES TP file
    // In IIDM we already have, for each (Equiment, Side) (IIDM Terminal) an alias for the CGMES Terminal
    // With that information we could always obtain the (Equipment, Side) --> TopologicalNode
    // That would be the basis for the Bus --> TopologicalNode that we finally need

    // But storing all Terminal --> TopologicalNode is a lot of information
    // Even if we store (TopologicalNode --> Set<Terminal>)

    // So instead we try to reduce it to <Bus> -> Set<TopologicalNode>
    // that we expect to be close to a 1:1 mapping

    // BUT bus identifiers in node/breaker models are calculated
    // And do not appear anywhere in the XIIDM file,
    // so we are storing "virtual references" to buses that are not serialized with the Network

    // A solution is to serialize bus references without using the bus identifier
    // That can be achieved by choosing any IIDM "terminal" inside the bus to refer to it
    // An IIDM "terminal" can be serialized as an (equipmentId, side)

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
        // This method is called when the unmapped list has already been completed
        // There are no "pending" TNs to be removed from unmapped
        // The check to see if this TN has also been mapped to a different bus
        // can not be the same that we apply when removing elements from "unmapped"
        if (unmapped.contains(topologicalNodeId)) {
            throw new PowsyblException("Inconsistency: TN " + topologicalNodeId + " has been considered unmapped, but now a mapping to bus " + busId + " is being added");
        }
        busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(topologicalNodeId);
        return this;
    }

    @Override
    public Map<String, Set<String>> topologicalNodesByBusViewBusMap() {
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
                // BusView Buses should be considered
                // And it is ok that a Eq,Side does not have a mapping to a BusView bus (it is "disconnected")
                // but had always a mapping to a BusBreakerView bus (at bus/breaker level even disconnected terminals receive a configured bus)
                Bus bus = t.getBusView().getBus();
                if (bus == null) {
                    return;
                }
                String busId = t.getBusView().getBus().getId();
                checkAlreadyMapped(busId, tn);
                busTopologicalNodeMap.computeIfAbsent(busId, bid -> new HashSet<>()).add(tn);
                unmapped.remove(tn);
            }
        });
    }

    private void checkAlreadyMapped(String busId, String topologicalNodeId) {
        // TN has been removed from unmapped collection (that starts with all TNs)
        // and this bus has not received it
        // because no mappings exist for this bus: get(busId) == null
        // or because the TN can not be found in the mappings for this bus: !get(busId).contains(TN)
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
