/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.powsybl.psse.converter.AbstractConverter.getSubstationIdFromBuses;
import static com.powsybl.psse.converter.AbstractConverter.getSubstationIdFromPsseSubstationIds;

/**
 * Maps PSS/E node containers to IIDM containers.
 * <p>
 * As in the CGMES node container mapping, source substations connected by transformers are merged into one IIDM
 * substation because all transformer ends must belong to it. Source voltage levels remain distinct: for RAWX
 * node-breaker data the source voltage-level discriminator is the {@code isub} containing the bus.
 */
final class PsseNodeContainerMapping {

    private PsseNodeContainerMapping() {
    }

    static ContainersMapping create(PssePowerFlowModel psseModel, Map<Integer, PsseBus> busNumToPsseBus,
                                    PsseImporter.PerUnitContext perUnitContext,
                                    NodeBreakerValidation nodeBreakerValidation) {
        List<Edge> adjacency = new ArrayList<>();
        addTransformerAdjacency(psseModel, busNumToPsseBus, adjacency);
        addSourceSubstationAdjacency(nodeBreakerValidation, adjacency);

        return ContainersMapping.create(psseModel.getBuses(), adjacency,
                PsseBus::getI,
                Edge::bus1,
                Edge::bus2,
                Edge::zeroImpedance,
                Edge::transformer,
                bus -> VoltageLevelConverter.getNominalV(getBus(busNumToPsseBus, bus),
                        perUnitContext.ignoreBaseVoltage()),
                bus -> getBus(busNumToPsseBus, bus).getArea(),
                nodeBreakerValidation::getSourceVoltageLevel,
                AbstractConverter::getVoltageLevelId,
                buses -> getSubstationId(nodeBreakerValidation, buses));
    }

    private static void addTransformerAdjacency(PssePowerFlowModel psseModel,
                                                Map<Integer, PsseBus> busNumToPsseBus,
                                                List<Edge> adjacency) {
        psseModel.getTransformers().forEach(transformer -> {
            if (transformer.getK() == 0) {
                addIfBusesExist(busNumToPsseBus, adjacency, transformer.getI(), transformer.getJ());
            } else if (busNumToPsseBus.containsKey(transformer.getI())
                    && busNumToPsseBus.containsKey(transformer.getJ())
                    && busNumToPsseBus.containsKey(transformer.getK())) {
                adjacency.add(new Edge(transformer.getI(), transformer.getJ(), true, false));
                adjacency.add(new Edge(transformer.getI(), transformer.getK(), true, false));
            }
        });
    }

    private static void addIfBusesExist(Map<Integer, PsseBus> buses, List<Edge> adjacency, int bus1, int bus2) {
        if (buses.containsKey(bus1) && buses.containsKey(bus2)) {
            adjacency.add(new Edge(bus1, bus2, true, false));
        }
    }

    private static void addSourceSubstationAdjacency(NodeBreakerValidation nodeBreakerValidation,
                                                     List<Edge> adjacency) {
        nodeBreakerValidation.getValidSubstations().forEach(substation -> {
            List<Integer> buses = nodeBreakerValidation.getBuses(substation);
            if (buses.size() >= 2) {
                int firstBus = buses.get(0);
                for (int index = 1; index < buses.size(); index++) {
                    adjacency.add(new Edge(firstBus, buses.get(index), true, false));
                }
            }
        });
    }

    private static PsseBus getBus(Map<Integer, PsseBus> buses, int bus) {
        PsseBus psseBus = buses.get(bus);
        if (psseBus == null) {
            throw new PsseException("Bus " + bus + " not found");
        }
        return psseBus;
    }

    private static String getSubstationId(NodeBreakerValidation nodeBreakerValidation, Set<Integer> buses) {
        Set<Integer> sourceSubstationIds = nodeBreakerValidation.getValidSubstationsIds(buses);
        return sourceSubstationIds.isEmpty() ? getSubstationIdFromBuses(buses)
                : getSubstationIdFromPsseSubstationIds(sourceSubstationIds);
    }

    private record Edge(int bus1, int bus2, boolean transformer, boolean zeroImpedance) {
    }
}
