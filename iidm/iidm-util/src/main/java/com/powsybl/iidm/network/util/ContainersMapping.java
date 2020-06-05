/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

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
                                                  ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2,
                                                  ToDoubleFunction<B> branchToResistance, ToDoubleFunction<B> branchToReactance,
                                                  Predicate<B> branchToIsTransformer) {
        Objects.requireNonNull(buses);
        Objects.requireNonNull(branches);
        Objects.requireNonNull(busToNum);
        Objects.requireNonNull(branchToNum1);
        Objects.requireNonNull(branchToNum2);
        Objects.requireNonNull(branchToResistance);
        Objects.requireNonNull(branchToReactance);
        Objects.requireNonNull(branchToIsTransformer);

        ContainersMapping containersMapping = new ContainersMapping();

        // group buses connected to non impedant lines to voltage levels
        createVoltageLevelMapping(buses, branches, busToNum, branchToNum1, branchToNum2, branchToResistance, branchToReactance, containersMapping);

        // group voltage levels connected by transformers to substations
        createSubstationMapping(branches, branchToNum1, branchToNum2, branchToIsTransformer, containersMapping);

        return containersMapping;
    }

    private static <N, B> void createVoltageLevelMapping(List<N> buses, List<B> branches, ToIntFunction<N> busToNum,
                                                         ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2,
                                                         ToDoubleFunction<B> branchToResistance, ToDoubleFunction<B> branchToReactance,
                                                         ContainersMapping containersMapping) {
        UndirectedGraph<Integer, Object> vlGraph = new Pseudograph<>(Object.class);
        for (N bus : buses) {
            vlGraph.addVertex(busToNum.applyAsInt(bus));
        }
        for (B branch : branches) {
            if (branchToResistance.applyAsDouble(branch) == 0 && branchToReactance.applyAsDouble(branch) == 0) {
                vlGraph.addEdge(branchToNum1.applyAsInt(branch), branchToNum2.applyAsInt(branch));
            }
        }
        for (Set<Integer> busNums : new ConnectivityInspector<>(vlGraph).connectedSets()) {
            String voltageLevelId = "VL" + busNums.iterator().next();
            containersMapping.voltageLevelIdToBusNums.put(voltageLevelId, busNums);
            for (int busNum : busNums) {
                containersMapping.busNumToVoltageLevelId.put(busNum, voltageLevelId);
            }
        }
    }

    private static <B> void createSubstationMapping(List<B> branches, ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2,
                                                    Predicate<B> branchToIsTransformer, ContainersMapping containersMapping) {
        UndirectedGraph<String, Object> sGraph = new Pseudograph<>(Object.class);
        for (String voltageLevelId : containersMapping.voltageLevelIdToBusNums.keySet()) {
            sGraph.addVertex(voltageLevelId);
        }
        for (B branch : branches) {
            if (branchToIsTransformer.test(branch)) {
                sGraph.addEdge(containersMapping.busNumToVoltageLevelId.get(branchToNum1.applyAsInt(branch)),
                        containersMapping.busNumToVoltageLevelId.get(branchToNum2.applyAsInt(branch)));
            }
        }
        int substationNum = 1;
        for (Set<String> voltageLevelIds : new ConnectivityInspector<>(sGraph).connectedSets()) {
            String substationId = "S" + substationNum++;
            for (String voltageLevelId : voltageLevelIds) {
                containersMapping.voltageLevelIdToSubstationId.put(voltageLevelId, substationId);
            }
        }
    }
}
