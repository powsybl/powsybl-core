/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractCgmesIidmMapping extends AbstractExtension<Network> implements CgmesIidmMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCgmesIidmMapping.class);

    protected static class EquipmentSide {
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

        protected final String equipmentId;
        protected final int side;
    }

    @Override
    public void addTopologyListener() {
        getExtendable().addListener(new NetworkListener() {
            @Override
            public void onCreation(Identifiable identifiable) {
                if (identifiable instanceof Switch || identifiable instanceof Bus) {
                    invalidateTopology();
                }
            }

            @Override
            public void beforeRemoval(Identifiable identifiable) {
                if (identifiable instanceof Switch || identifiable instanceof Bus) {
                    invalidateTopology();
                }
            }

            @Override
            public void afterRemoval(String id) {
                // do nothing
            }

            @Override
            public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
                // do nothing
            }

            @Override
            public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
                if (identifiable instanceof Switch && "open".equals(attribute)) {
                    invalidateTopology();
                }
            }

            @Override
            public void onElementAdded(Identifiable identifiable, String attribute, Object newValue) {
                if (identifiable instanceof VoltageLevel && "internalConnection".equals(attribute)) {
                    invalidateTopology();
                }
            }

            @Override
            public void onElementRemoved(Identifiable identifiable, String attribute, Object oldValue) {
                if (identifiable instanceof VoltageLevel && "internalConnection".equals(attribute)) {
                    invalidateTopology();
                }
            }
        });
    }

    protected void calculate() {
        equipmentSideTopologicalNodeMap().forEach((equipmentSide, ctn) -> {
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
                    CgmesTopologicalNode cgmesTopologicalNode = unmappedTopologicalNodes().get(ctn);
                    busTopologicalNodeMap().computeIfAbsent(busId, bid -> new HashSet<>()).add(cgmesTopologicalNode);
                    unmappedTopologicalNodes().remove(ctn);
                }
            }
        });
    }

    private boolean canTopologicalNodeBeMapped(String busId, String topologicalNode) {
        // TN has been removed from unmapped collection (that starts with all TNs)
        // and this bus has not received it
        // because no mappings exist for this bus: get(busId) == null
        // or because the TN can not be found in the mappings for this bus: !get(busId).contains(TN)
        CgmesTopologicalNode cgmesTopologicalNode = unmappedTopologicalNodes().get(topologicalNode);
        if (!unmappedTopologicalNodes().containsKey(topologicalNode) && (busTopologicalNodeMap().get(busId) == null || !busTopologicalNodeMap().get(busId).contains(cgmesTopologicalNode))) {
            LOGGER.warn("CGMES topological Node {} is already mapped and not to the given IIDM bus {}", topologicalNode, busId);
            return false;
        }
        return true;
    }

    protected abstract Map<EquipmentSide, String> equipmentSideTopologicalNodeMap();

    protected abstract Map<String, CgmesTopologicalNode> unmappedTopologicalNodes();

    protected abstract Map<String, Set<CgmesTopologicalNode>> busTopologicalNodeMap();

    protected abstract void invalidateTopology();
}
