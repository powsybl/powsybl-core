/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * A utility class that create IIDM containers, i.e voltage levels and substations from a bus branch model with respect
 * to IIDM container requirements.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ContainersMapping {

    private final Map<Integer, String> busNumToVoltageLevelId = new HashMap<>();

    private final Map<String, Set<Integer>> voltageLevelIdToBusNums = new HashMap<>();

    private final Map<String, String> voltageLevelIdToSubstationId = new HashMap<>();

    public Set<Integer> getBusesSet(String voltageLevelId) {
        return voltageLevelIdToBusNums.containsKey(voltageLevelId) ? voltageLevelIdToBusNums.get(voltageLevelId) : new HashSet<>();
    }

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

    /**
     * @deprecated Not used anymore. Use
     * {@link ContainersMapping#create(List, List, ToIntFunction, ToIntFunction, ToIntFunction,
     * Predicate, Predicate, ToDoubleFunction, Function, IntFunction)} instead.
     */
    @Deprecated(since = "4.9.2")
    public static <N, B> ContainersMapping create(List<N> buses, List<B> branches, ToIntFunction<N> busToNum,
                                                  ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2, ToIntFunction<B> branchToNum3,
                                                  ToDoubleFunction<B> branchToResistance, ToDoubleFunction<B> branchToReactance,
                                                  Predicate<B> branchToIsTransformer, Function<Set<Integer>, String> busesToVoltageLevelId,
                                                  IntFunction<String> substationNumToId) {
        throw new PowsyblException("Deprecated. Not used anymore");
    }

    public static <N, B> ContainersMapping create(List<N> buses, List<B> branches,
                                                  ToIntFunction<N> busToNum, ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2,
                                                  Predicate<B> branchToIsZeroImpedance, Predicate<B> branchToIsTransformer,
                                                  ToDoubleFunction<Integer> busToNominalVoltage, Function<Set<Integer>, String> busesToVoltageLevelId,
                                                  Function<Set<Integer>, String> busesToSubstationId) {

        return create(buses, branches, busToNum, branchToNum1, branchToNum2, branchToIsZeroImpedance, branchToIsTransformer, busToNominalVoltage,
                null, busesToVoltageLevelId, busesToSubstationId);
    }

    public static <N, B> ContainersMapping create(List<N> buses, List<B> branches,
        ToIntFunction<N> busToNum, ToIntFunction<B> branchToNum1, ToIntFunction<B> branchToNum2,
        Predicate<B> branchToIsZeroImpedance, Predicate<B> branchToIsTransformer,
        ToDoubleFunction<Integer> busToNominalVoltage, ToIntFunction<Integer> busToSubstationNumber,
        Function<Set<Integer>, String> busesToVoltageLevelId, Function<Set<Integer>, String> busesToSubstationId) {

        Objects.requireNonNull(buses);
        Objects.requireNonNull(branches);
        Objects.requireNonNull(busToNum);
        Objects.requireNonNull(branchToNum1);
        Objects.requireNonNull(branchToNum2);
        Objects.requireNonNull(branchToIsTransformer);
        Objects.requireNonNull(branchToIsZeroImpedance);

        Objects.requireNonNull(busesToVoltageLevelId);
        Objects.requireNonNull(busesToSubstationId);

        ContainersMapping containersMapping = new ContainersMapping();

        // graph for calculating substations
        // group buses connected by zero impedance lines and transformers to substations
        Graph<Integer, B> sGraph = new Pseudograph<>(null, null, false);
        for (N bus : buses) {
            sGraph.addVertex(busToNum.applyAsInt(bus));
        }

        for (B branch : branches) {
            if (branchToIsZeroImpedance.test(branch) || branchToIsTransformer.test(branch)) {
                sGraph.addEdge(branchToNum1.applyAsInt(branch), branchToNum2.applyAsInt(branch), branch);
            }
        }

        // analyze each substation set
        for (Set<Integer> busNums : new ConnectivityInspector<>(sGraph).connectedSets()) {

            createAndMapSubstationAndVoltageLevelsInside(branchToNum1, branchToNum2, branchToIsZeroImpedance,
                busToNominalVoltage, busToSubstationNumber, busesToVoltageLevelId, busesToSubstationId, busNums, sGraph, containersMapping);
        }

        return containersMapping;
    }

    private static <B> void createAndMapSubstationAndVoltageLevelsInside(ToIntFunction<B> branchToNum1,
        ToIntFunction<B> branchToNum2, Predicate<B> branchToIsZeroImpedance,
        ToDoubleFunction<Integer> busToNominalVoltage, ToIntFunction<Integer> busToSubstationNumber,
        Function<Set<Integer>, String> busesToVoltageLevelId,
        Function<Set<Integer>, String> busesToSubstationId, Set<Integer> substationBusNums,
        Graph<Integer, B> sGraph, ContainersMapping containersMapping) {

        String substationId = busesToSubstationId.apply(substationBusNums);

        // build the graph for splitting substation buses into voltageLevels sets
        // avoid including edges between buses with the same nominal voltage, there are a lot

        Set<B> zeroImpedanceBranchesInsideSubstation = new HashSet<>();
        substationBusNums.forEach(bus -> zeroImpedanceBranchesInsideSubstation
            .addAll(sGraph.edgesOf(bus).stream().filter(branchToIsZeroImpedance::test).collect(Collectors.toSet())));

        Graph<Integer, Object> vlGraph = new Pseudograph<>(Object.class);
        Iterator<Integer> iter = substationBusNums.iterator();
        while (iter.hasNext()) {
            vlGraph.addVertex(iter.next());
        }
        zeroImpedanceBranchesInsideSubstation.forEach(branch -> vlGraph.addEdge(branchToNum1.applyAsInt(branch), branchToNum2.applyAsInt(branch)));

        if (busToNominalVoltage == null) {
            new ConnectivityInspector<>(vlGraph).connectedSets()
                .forEach(voltageLevelIds -> mapSubstationAndVoltageLevel(busesToVoltageLevelId, substationId, voltageLevelIds, containersMapping));
        } else {
            Map<String, Set<Integer>> vls = new HashMap<>();

            new ConnectivityInspector<>(vlGraph).connectedSets()
                .forEach(voltageLevelIds -> vls.merge(getNominalVoltage(voltageLevelIds, busToNominalVoltage, busToSubstationNumber), voltageLevelIds, ContainersMapping::unionSet));

            vls.values().forEach(voltageLevelIds -> mapSubstationAndVoltageLevel(busesToVoltageLevelId, substationId, voltageLevelIds, containersMapping));
        }
    }

    // We only consider in the same voltageLevel buses with the same nominal voltage and with the same substationData (only defined in PSSE)
    private static String getNominalVoltage(Set<Integer> voltageLevelIds, ToDoubleFunction<Integer> busToVoltageLevelNominal, ToIntFunction<Integer> busToSubstationNumber) {
        Objects.requireNonNull(busToVoltageLevelNominal);
        if (voltageLevelIds.isEmpty()) { // should never happen
            throw new PowsyblException("Unexpected empty connected set");
        }
        int bus = voltageLevelIds.iterator().next();
        return busToSubstationNumber == null ? String.valueOf(busToVoltageLevelNominal.applyAsDouble(bus)) :
                String.valueOf(busToVoltageLevelNominal.applyAsDouble(voltageLevelIds.iterator().next())) + "-" + busToSubstationNumber.applyAsInt(bus);
    }

    private static Set<Integer> unionSet(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> unionSet = new HashSet<>(set1);
        unionSet.addAll(set2);
        return unionSet;
    }

    private static void mapSubstationAndVoltageLevel(Function<Set<Integer>, String> busesToVoltageLevelId,
        String substationId, Set<Integer> busNums, ContainersMapping containersMapping) {

        String voltageLevelId = busesToVoltageLevelId.apply(busNums);
        containersMapping.voltageLevelIdToBusNums.put(voltageLevelId, busNums);
        for (int busNum : busNums) {
            containersMapping.busNumToVoltageLevelId.put(busNum, voltageLevelId);
        }
        containersMapping.voltageLevelIdToSubstationId.put(voltageLevelId, substationId);
    }
}
