/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesIidmMappingImpl extends AbstractExtension<Network> implements CgmesIidmMapping {

    private final Map<EquipmentEnd, String> equipmentEndTopologicalNodeMap;
    private final Map<String, Set<String>> busTopologicalNodeMap;
    private final Set<String> unmapped;

    CgmesIidmMappingImpl(Set<String> topologicalNodes) {
        equipmentEndTopologicalNodeMap = new HashMap<>();
        busTopologicalNodeMap = new HashMap<>();
        unmapped = new HashSet<>();
        unmapped.addAll(Objects.requireNonNull(topologicalNodes));
    }

    @Override
    public Set<String> get(String busId) {
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.get(busId);
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
        equipmentEndTopologicalNodeMap.put(new EquipmentEnd(equipmentId, side), topologicalNodeId);
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
        equipmentEndTopologicalNodeMap.forEach((equipmentEnd, tn) -> {
            Identifiable i = getExtendable().getIdentifiable(equipmentEnd.equipmentId);
            if (i instanceof Injection) {
                String busId = ((Injection) i).getTerminal().getBusBreakerView().getBus().getId();
                checkAlreadyMapped(busId, tn);
                busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(tn);
                unmapped.remove(tn);
            } else if (i instanceof Branch) {
                if (equipmentEnd.end == 1) {
                    String busId = ((Branch) i).getTerminal1().getBusBreakerView().getBus().getId();
                    checkAlreadyMapped(busId, tn);
                    busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(tn);
                    unmapped.remove(tn);
                } else if (equipmentEnd.end == 2) {
                    String busId = ((Branch) i).getTerminal2().getBusBreakerView().getBus().getId();
                    checkAlreadyMapped(busId, tn);
                    busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(tn);
                    unmapped.remove(tn);
                }
            } else if (i instanceof ThreeWindingsTransformer) {
                if (equipmentEnd.end == 1) {
                    String busId = ((ThreeWindingsTransformer) i).getTerminal(ThreeWindingsTransformer.Side.ONE).getBusBreakerView().getBus().getId();
                    checkAlreadyMapped(busId, tn);
                    busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(tn);
                    unmapped.remove(tn);
                } else if (equipmentEnd.end == 2) {
                    String busId = ((ThreeWindingsTransformer) i).getTerminal(ThreeWindingsTransformer.Side.TWO).getBusBreakerView().getBus().getId();
                    checkAlreadyMapped(busId, tn);
                    busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(tn);
                    unmapped.remove(tn);
                } else if (equipmentEnd.end == 3) {
                    String busId = ((ThreeWindingsTransformer) i).getTerminal(ThreeWindingsTransformer.Side.THREE).getBusBreakerView().getBus().getId();
                    checkAlreadyMapped(busId, tn);
                    busTopologicalNodeMap.computeIfAbsent(busId, b -> new HashSet<>()).add(tn);
                    unmapped.remove(tn);
                }
            }
        });
    }

    private void checkAlreadyMapped(String busId, String topologicalNodeId) {
        if (!unmapped.contains(topologicalNodeId) && (busTopologicalNodeMap.get(busId) == null || !busTopologicalNodeMap.get(busId).contains(topologicalNodeId))) {
            throw new PowsyblException("TopologicalNode " + topologicalNodeId + " is already mapped to another bus");
        }
    }

    private class EquipmentEnd {
        EquipmentEnd(String equipmentId, int end) {
            this.equipmentId = Objects.requireNonNull(equipmentId);
            this.end = end;
        }

        private final String equipmentId;
        private final int end;
    }
}
