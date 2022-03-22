/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesIidmMappingImpl extends AbstractCgmesIidmMapping {

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
        if (topologicalNodeId == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Missing Topological Node for equipment {} side {}", equipmentId, side);
            }
        } else {
            equipmentSideTopologicalNodeMap.put(new EquipmentSide(equipmentId, side), topologicalNodeId);
        }
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
    protected Map<EquipmentSide, String> equipmentSideTopologicalNodeMap() {
        return equipmentSideTopologicalNodeMap;
    }

    @Override
    protected Map<String, CgmesTopologicalNode> unmappedTopologicalNodes() {
        return unmappedTopologicalNodes;
    }

    @Override
    protected Map<String, Set<CgmesTopologicalNode>> busTopologicalNodeMap() {
        return busTopologicalNodeMap;
    }

    @Override
    protected void invalidateTopology() {
        equipmentSideTopologicalNodeMap.clear();
        busTopologicalNodeMap.clear();
        unmappedTopologicalNodes.clear();
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
}
