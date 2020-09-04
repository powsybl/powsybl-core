/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.util.*;
import java.util.function.*;

/**
 * A utility class that create IIDM containers, i.e voltage levels and substations from a bus branch model with respect
 * to IIDM container requirements.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContainersMapping {

    private final Map<Integer, String> busNumToVoltageLevelId = new HashMap<>();

    private final Map<String, Set<Integer>> voltageLevelIdToBusNums = new HashMap<>();

    private final Map<String, String> voltageLevelIdToSubstationId = new HashMap<>();

    public String getVoltageLevelId(int num) {
        String voltageLevelId = busNumToVoltageLevelId.get(num);
        if (voltageLevelId == null) {
            throw new PowsyblException("Bus " + num + " not found");
        }
        return voltageLevelId;
    }

    public String getSubstationId(String voltageLevelId) {
        String substationId = voltageLevelIdToSubstationId.get(voltageLevelId);
        if (substationId == null) {
            throw new PowsyblException("Voltage level '" + voltageLevelId + "' not found");
        }
        return substationId;
    }

    public static <N, B> ContainersMapping create(List<N> buses, List<B> branches, ToIntFunction<N> busToNum,
                                                  ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2, ToIntFunction<B> branchToNum3,
                                                  ToDoubleFunction<B> branchToResistance, ToDoubleFunction<B> branchToReactance,
                                                  Predicate<B> branchToIsTransformer, Function<Set<Integer>, String> busesToVoltageLevelId,
                                                  IntFunction<String> substationNumToId) {
        Objects.requireNonNull(buses);
        Objects.requireNonNull(branches);
        Objects.requireNonNull(busToNum);
        Objects.requireNonNull(branchToNum1);
        Objects.requireNonNull(branchToNum2);
        Objects.requireNonNull(branchToResistance);
        Objects.requireNonNull(branchToReactance);
        Objects.requireNonNull(branchToIsTransformer);
        Objects.requireNonNull(busesToVoltageLevelId);
        Objects.requireNonNull(substationNumToId);

        ContainersMapping containersMapping = new ContainersMapping();

        // group buses connected to non impedant lines to voltage levels
        createVoltageLevelMapping(buses, branches, busToNum, branchToNum1, branchToNum2, branchToResistance, branchToReactance,
                busesToVoltageLevelId, containersMapping);

        // group voltage levels connected by transformers to substations
        createSubstationMapping(branches, branchToNum1, branchToNum2, branchToNum3, branchToIsTransformer, substationNumToId, containersMapping);

        return containersMapping;
    }

    private static <N, B> void createVoltageLevelMapping(List<N> buses, List<B> branches, ToIntFunction<N> busToNum,
                                                         ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2,
                                                         ToDoubleFunction<B> branchToResistance, ToDoubleFunction<B> branchToReactance,
                                                         Function<Set<Integer>, String> busesToVoltageLevelId, ContainersMapping containersMapping) {
        Graph<Integer, Object> vlGraph = new Pseudograph<>(Object.class);
        for (N bus : buses) {
            vlGraph.addVertex(busToNum.applyAsInt(bus));
        }
        for (B branch : branches) {
            if (branchToResistance.applyAsDouble(branch) == 0 && branchToReactance.applyAsDouble(branch) == 0) {
                vlGraph.addEdge(branchToNum1.applyAsInt(branch), branchToNum2.applyAsInt(branch));
            }
        }
        for (Set<Integer> busNums : new ConnectivityInspector<>(vlGraph).connectedSets()) {
            String voltageLevelId = busesToVoltageLevelId.apply(busNums);
            containersMapping.voltageLevelIdToBusNums.put(voltageLevelId, busNums);
            for (int busNum : busNums) {
                containersMapping.busNumToVoltageLevelId.put(busNum, voltageLevelId);
            }
        }
    }

    private static <B> void createSubstationMapping(List<B> branches, ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2, ToIntFunction<B> branchToNum3,
                                                    Predicate<B> branchToIsTransformer, IntFunction<String> substationNumToId,
                                                    ContainersMapping containersMapping) {
        Graph<String, Object> sGraph = new Pseudograph<>(Object.class);
        for (String voltageLevelId : containersMapping.voltageLevelIdToBusNums.keySet()) {
            sGraph.addVertex(voltageLevelId);
        }
        for (B branch : branches) {
            if (branchToIsTransformer.test(branch)) {
                sGraph.addEdge(containersMapping.busNumToVoltageLevelId.get(branchToNum1.applyAsInt(branch)),
                        containersMapping.busNumToVoltageLevelId.get(branchToNum2.applyAsInt(branch)));
                // Three winding Tfo
                if (branchToNum3.applyAsInt(branch) != 0) {
                    sGraph.addEdge(containersMapping.busNumToVoltageLevelId.get(branchToNum1.applyAsInt(branch)),
                            containersMapping.busNumToVoltageLevelId.get(branchToNum3.applyAsInt(branch)));
                }
            }
        }
        int substationNum = 1;
        for (Set<String> voltageLevelIds : new ConnectivityInspector<>(sGraph).connectedSets()) {
            String substationId = substationNumToId.apply(substationNum++);
            for (String voltageLevelId : voltageLevelIds) {
                containersMapping.voltageLevelIdToSubstationId.put(voltageLevelId, substationId);
            }
        }
    }
}
