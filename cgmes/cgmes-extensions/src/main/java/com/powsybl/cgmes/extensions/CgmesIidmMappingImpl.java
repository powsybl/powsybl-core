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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesIidmMappingImpl extends AbstractExtension<Network> implements CgmesIidmMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesIidmMappingImpl.class);

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
    private final Map<String, Set<CgmesTopologicalNode>> busTopologicalNodeMap;
    private final Map<String, CgmesTopologicalNode> unmappedTopologicalNodes;

    // Ideally, each nominal voltage is represented by a single base voltage,
    // for this reason the mapping has been considered 1: 1

    private final Map<Double, BaseVoltageSource> nominalVoltageBaseVoltageMap;

    CgmesIidmMappingImpl(Set<CgmesTopologicalNode> topologicalNodes, Set<BaseVoltageSource> baseVoltages) {
        equipmentSideTopologicalNodeMap = new HashMap<>();
        busTopologicalNodeMap = new HashMap<>();
        unmappedTopologicalNodes = new HashMap<>();
        topologicalNodes.forEach(ctn -> unmappedTopologicalNodes.put(ctn.getCgmesId(), ctn));

        nominalVoltageBaseVoltageMap = new HashMap<>();
        baseVoltages.forEach(bvs -> addBaseVoltage(bvs.getNominalV(), bvs.getCgmesId(), bvs.getSource()));
    }

    @Override
    public Set<CgmesTopologicalNode> getTopologicalNodes(String busId) {
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
    public boolean isTopologicalNodeMapped(String busId) {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.containsKey(busId);
    }

    @Override
    public boolean isTopologicalNodeEmpty() {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.isEmpty();
    }

    @Override
    public CgmesIidmMapping putTopologicalNode(String equipmentId, int side, String topologicalNodeId) {
        equipmentSideTopologicalNodeMap.put(new EquipmentSide(equipmentId, side), topologicalNodeId);
        return this;
    }

    @Override
    public CgmesIidmMapping putTopologicalNode(String busId, String topologicalNodeId, String topologicalNodeName, Source source) {
        // This method is called when the unmapped list has already been completed
        // There are no "pending" TNs to be removed from unmapped
        // The check to see if this TN has also been mapped to a different bus
        // can not be the same that we apply when removing elements from "unmapped"
        if (unmappedTopologicalNodes.containsKey(topologicalNodeId)) {
            throw new PowsyblException("Inconsistency: TN " + topologicalNodeId + " has been considered unmapped, but now a mapping to bus " + busId + " is being added");
        }
        busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(new CgmesTopologicalNode(topologicalNodeId, topologicalNodeName, source));
        return this;
    }

    @Override
    public CgmesIidmMapping putUnmappedTopologicalNode(String topologicalNodeId, String topologicalNodeName, Source source) {
        unmappedTopologicalNodes.computeIfAbsent(topologicalNodeId, ctn -> new CgmesTopologicalNode(topologicalNodeId, topologicalNodeName, source));
        return this;
    }

    @Override
    public Map<String, Set<CgmesTopologicalNode>> topologicalNodesByBusViewBusMap() {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return new HashMap<>(busTopologicalNodeMap);
    }

    @Override
    public Set<CgmesTopologicalNode> getUnmappedTopologicalNodes() {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return new HashSet<>(unmappedTopologicalNodes.values());
    }

    @Override
    public Map<Double, BaseVoltageSource> getBaseVoltages() {
        return Collections.unmodifiableMap(nominalVoltageBaseVoltageMap);
    }

    @Override
    public BaseVoltageSource getBaseVoltage(double nominalVoltage) {
        return nominalVoltageBaseVoltageMap.get(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageMapped(double nominalVoltage) {
        return nominalVoltageBaseVoltageMap.containsKey(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageEmpty() {
        return nominalVoltageBaseVoltageMap.isEmpty();
    }

    @Override
    public CgmesIidmMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source) {
        if (nominalVoltageBaseVoltageMap.containsKey(nominalVoltage)) {
            if (nominalVoltageBaseVoltageMap.get(nominalVoltage).getSource().equals(Source.IGM) && source.equals(Source.BOUNDARY)) {
                LOGGER.info("Nominal voltage {} is already mapped with an {} base voltage. Replaced by a {} base voltage", nominalVoltage, Source.IGM.name(), Source.BOUNDARY.name());
                nominalVoltageBaseVoltageMap.put(nominalVoltage, new BaseVoltageSource(baseVoltageId, nominalVoltage, source));
            } else {
                LOGGER.info("Nominal voltage {} is already mapped and not to the given base voltage {} from {}", nominalVoltage, baseVoltageId, source.name());
            }
        } else {
            nominalVoltageBaseVoltageMap.put(nominalVoltage, new BaseVoltageSource(baseVoltageId, nominalVoltage, source));
        }
        return this;
    }

    public Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap() {
        return new HashMap<>(nominalVoltageBaseVoltageMap);
    }

    private void calculate() {
        equipmentSideTopologicalNodeMap.forEach((equipmentSide, ctn) -> {
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
                if (canTopologicalNodeBeMapped(busId, ctn)) {
                    CgmesTopologicalNode cgmesTopologicalNode = unmappedTopologicalNodes.get(ctn);
                    busTopologicalNodeMap.computeIfAbsent(busId, bid -> new HashSet<>()).add(cgmesTopologicalNode);
                    unmappedTopologicalNodes.remove(ctn);
                }
            }
        });
    }

    private boolean canTopologicalNodeBeMapped(String busId, String topologicalNode) {
        // TN has been removed from unmapped collection (that starts with all TNs)
        // and this bus has not received it
        // because no mappings exist for this bus: get(busId) == null
        // or because the TN can not be found in the mappings for this bus: !get(busId).contains(TN)
        CgmesTopologicalNode cgmesTopologicalNode = unmappedTopologicalNodes.get(topologicalNode);
        if (!unmappedTopologicalNodes.containsKey(topologicalNode) && (busTopologicalNodeMap.get(busId) == null || !busTopologicalNodeMap.get(busId).contains(cgmesTopologicalNode))) {
            LOGGER.warn("CGMES topological Node {} is already mapped and not to the given IIDM bus {}", topologicalNode, busId);
            return false;
        }
        return true;
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
