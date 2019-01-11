/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;

public class EdgeWeightedGraphCalculator {

    private Bus   bus;
    private Z0Bus z0Bus;

    public EdgeWeightedGraphCalculator(Bus bus, Z0Bus z0Bus) {
        this.bus = bus;
        this.z0Bus = z0Bus;
    }

    public void calc(SimpleWeightedGraph graph) {

        bus.getLineStream().forEach(line -> {
            Side side = Side.ONE;
            if (line.getTerminal2().getBusView().getBus().equals(bus)) {
                side = Side.TWO;
            }
            visitLine(graph, line, side);
        });
    }

    private void visitLine(SimpleWeightedGraph graph, Line line, Side side) {

        if (side.equals(Side.TWO)) {
            return;
        }

        Bus other = line.getTerminal2().getBusView().getBus();
        if (!z0Bus.busInZ0Bus(other)) {
            return;
        }

        graph.addVertex(bus);
        graph.addVertex(other);
        graph.addEdge(bus, other, new Z0LineWeightEdge(line));
    }

    private static final Logger LOG = LoggerFactory.getLogger(EdgeWeightedGraphCalculator.class);
}
