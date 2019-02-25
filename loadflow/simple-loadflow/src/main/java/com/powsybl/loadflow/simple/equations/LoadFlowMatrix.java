/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple.equations;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.*;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.SparseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public final class LoadFlowMatrix {

    private LoadFlowMatrix() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowMatrix.class);

    private static Bus getBus(Branch b, Branch.Side s) {
        return b.getTerminal(s).getBusView().getBus();
    }

    private static Bus getBus1(Branch b) {
        return getBus(b, Branch.Side.ONE);
    }

    private static Bus getBus2(Branch b) {
        return getBus(b, Branch.Side.TWO);
    }

    public static SparseStore<Double> build(Network network) {
        List<Bus> buses = network.getBusView().getBusStream().collect(ImmutableList.toImmutableList());

        Map<Bus, Integer> index = new HashMap<>();
        IntStream.range(0, buses.size()).forEach(i -> index.put(buses.get(i), i));

        SparseStore<Double> a = SparseStore.PRIMITIVE.make(2L * buses.size(), 2L * buses.size());

        Set<Bus> busGenerators = new HashSet<>();
        for (Generator generator : network.getGenerators()) {
            Bus bus = generator.getTerminal().getBusView().getBus();
            busGenerators.add(bus);
            int num = index.get(bus);
            int rowV = 2 * num + 1; //index of reactive power on that node
        }

        for (Branch branch : network.getBranches()) {
            FlowEquations eq = new FlowEquations(branch);

            LOGGER.info("{}", eq);

            Bus bus1 = getBus1(branch);
            Bus bus2 = getBus2(branch);
            int num1 = index.get(bus1);
            int num2 = index.get(bus2);

            int rowP1 = 2 * num1;
            int rowQ1 = 2 * num1 + 1;
            int rowP2 = 2 * num2;
            int rowQ2 = 2 * num2 + 1;
            int colPh1 = 2 * num1;
            int colV1 = 2 * num1 + 1;
            int colPh2 = 2 * num2;
            int colV2 = 2 * num2 + 1;

            a.add(rowP1, colPh1, eq.dp1dph1());
            a.add(rowP1, colPh2, eq.dp1dph2());
            a.add(rowP1, colV1, eq.dp1dv1());
            a.add(rowP1, colV2, eq.dp1dv2());

            if (!busGenerators.contains(bus1)) {
                a.add(rowQ1, colPh1, eq.dq1dph1());
                a.add(rowQ1, colPh2, eq.dq1dph2());
                a.add(rowQ1, colV1, eq.dq1dv1());
                a.add(rowQ1, colV2, eq.dq1dv2());
            }

            a.add(rowP2, colPh1, eq.dp2dph1());
            a.add(rowP2, colPh2, eq.dp2dph2());
            a.add(rowP2, colV1, eq.dp2dv1());
            a.add(rowP2, colV2, eq.dp2dv2());

            if (!busGenerators.contains(bus2)) {
                a.add(rowQ2, colPh1, eq.dq2dph1());
                a.add(rowQ2, colPh2, eq.dq2dph2());
                a.add(rowQ2, colV1, eq.dq2dv1());
                a.add(rowQ2, colV2, eq.dq2dv2());
            }
        }

        return a;
    }

    public static PrimitiveDenseStore buildRhs(Network network) {
        return null;
    }

    public static SparseStore<Double> buildDc(Network network) {
        List<Bus> buses = network.getBusView().getBusStream().collect(ImmutableList.toImmutableList());

        Map<Bus, Integer> index = new HashMap<>();
        IntStream.range(0, buses.size()).forEach(i -> index.put(buses.get(i), i));

        SparseStore<Double> a = SparseStore.PRIMITIVE.make(buses.size(), buses.size());

        for (Branch branch : network.getBranches()) {

            Bus bus1 = getBus1(branch);
            Bus bus2 = getBus2(branch);

            if (bus1 == null || bus2 == null) {
                continue;
            }

            DcFlowEquationsImpl eq = new DcFlowEquationsImpl(branch);
            LOGGER.info("{}", eq);

            int num1 = index.get(bus1);
            int num2 = index.get(bus2);

            int rowP1 = num1;
            int rowP2 = num2;
            int colPh1 = num1;
            int colPh2 = num2;

            if (rowP1 != 0) {
                a.add(rowP1, colPh1, eq.dp1dph1());
                a.add(rowP1, colPh2, eq.dp1dph2());
            }
            if (rowP2 != 0) {
                a.add(rowP2, colPh1, eq.dp2dph1());
                a.add(rowP2, colPh2, eq.dp2dph2());
            }
        }

        a.set(0, 0, 1);

        return a;
    }

    public static PrimitiveDenseStore buildDcRhs(Network network) {
        List<Bus> buses = network.getBusView().getBusStream().collect(ImmutableList.toImmutableList());

        Map<Bus, Integer> index = new HashMap<>();
        IntStream.range(0, buses.size()).forEach(i -> index.put(buses.get(i), i));

        PrimitiveDenseStore rhs = PrimitiveDenseStore.FACTORY.makeZero(buses.size(), 1);

        for (Generator gen : network.getGenerators()) {
            Bus bus = gen.getTerminal().getBusView().getBus();
            int num = index.get(bus);
            if (num == 0) {
                continue;
            }
            rhs.set(num, 0, gen.getTargetP());
        }

        for (Load load : network.getLoads()) {
            Bus bus = load.getTerminal().getBusView().getBus();
            int num = index.get(bus);
            if (num == 0) {
                continue;
            }
            rhs.set(num, 0, load.getP0());
        }

        return rhs;
    }

    public static void updateNetwork(Network network, MatrixStore<Double> lhs) {
        List<Bus> buses = network.getBusView().getBusStream().collect(ImmutableList.toImmutableList());

        Map<Bus, Integer> index = new HashMap<>();
        IntStream.range(0, buses.size()).forEach(i -> index.put(buses.get(i), i));

        for (Bus bus : buses) {
            int num = index.get(bus);
            bus.setAngle(lhs.get(num, 0));
        }

        for (Branch branch : network.getBranches()) {
            DcFlowEquations eq = DcFlowEquations.of(branch);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(eq.toString());
            }
            branch.getTerminal1().setP(eq.p1());
            branch.getTerminal2().setP(eq.p2());
        }
    }
}
