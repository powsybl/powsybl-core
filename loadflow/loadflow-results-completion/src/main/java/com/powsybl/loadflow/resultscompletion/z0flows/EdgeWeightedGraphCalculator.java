/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.resultscompletion.z0flows.kruskal.Edge;

public class EdgeWeightedGraphCalculator implements TopologyVisitor {

    private Bus bus;
    private Z0Bus z0Bus;
    private EdgeWeightedGraph graph;

    public EdgeWeightedGraphCalculator(Bus bus, Z0Bus z0Bus, EdgeWeightedGraph graph) {
        this.bus = bus;
        this.z0Bus = z0Bus;
        this.graph = graph;

        bus.visitConnectedEquipments(this);
    }

    public static EdgeWeightedGraph calc(Z0Bus z0Bus) {

        EdgeWeightedGraph graph = new EdgeWeightedGraph(z0Bus.size());

        for (int pos = 0; pos < z0Bus.size(); pos++) {
            Bus bus = z0Bus.getBus(pos);
            EdgeWeightedGraphCalculator visitor = new EdgeWeightedGraphCalculator(bus, z0Bus, graph);
        }
        return graph;
    }

    @Override
    public void visitBusbarSection(BusbarSection section) {

    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {

    }

    @Override
    public void visitGenerator(Generator generator) {

    }

    @Override
    public void visitLine(Line line, Side side) {

        if (side.equals(Side.TWO)) {
            return;
        }

        Bus other = line.getTerminal2().getBusView().getBus();
        if (!z0Bus.busInZ0Bus(other)) {
            return;
        }

        String lineId = line.getId();
        int v = z0Bus.indexOf(bus);
        int w = z0Bus.indexOf(other);
        double weight = line.getX();

        Edge e = new Edge(lineId, v, w, weight);
        graph.addEdge(e);
    }

    @Override
    public void visitLoad(Load load) {

    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {

    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {

    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
            ThreeWindingsTransformer.Side side) {

        Bus other1 = null;
        Bus other2 = null;
        if (side.equals(ThreeWindingsTransformer.Side.ONE)) {
            other1 = transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getBusView()
                    .getBus();
            other2 = transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getBusView()
                    .getBus();
        } else if (side.equals(ThreeWindingsTransformer.Side.TWO)) {
            other1 = transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getBusView()
                    .getBus();
            other2 = transformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getBusView()
                    .getBus();
        } else if (side.equals(ThreeWindingsTransformer.Side.THREE)) {
            other1 = transformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getBusView()
                    .getBus();
            other2 = transformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getBusView()
                    .getBus();
        }

        if (z0Bus.busInZ0Bus(other1) || z0Bus.busInZ0Bus(other2)) {
            LOG.error("Fail in ThreeWindingsTransformer " + transformer.getId());
        }
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Side side) {

        Side otherSide = side.equals(Side.ONE) ? Side.TWO : Side.ONE;
        Bus other = transformer.getTerminal(otherSide).getBusView().getBus();
        if (z0Bus.busInZ0Bus(other)) {
            LOG.error("Fail in TwoWindingsTransformer " + transformer.getId());
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(EdgeWeightedGraphCalculator.class);
}
