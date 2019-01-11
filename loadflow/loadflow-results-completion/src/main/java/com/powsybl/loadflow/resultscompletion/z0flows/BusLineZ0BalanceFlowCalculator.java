/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.List;

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
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.TwoWindingsTransformer;

public class BusLineZ0BalanceFlowCalculator implements TopologyVisitor {

    private Bus     bus;
    private Line    line;

    private boolean known;
    private double  netP;
    private double  netQ;

    public BusLineZ0BalanceFlowCalculator(Bus bus, Line line) {
        this.bus = bus;
        this.line = line;

        this.netP = 0.0;
        this.netQ = 0.0;

        this.known = true;

        bus.visitConnectedEquipments(this);
        if (this.known) {
            setZ0Flows();
        }
    }

    private void addFlow(Terminal t) {
        if (Double.isNaN(t.getP()) || Double.isNaN(t.getQ())) {
            known = false;
            LOG.error(String.format("flow NaN %10.4f %10.4f %s", t.getP(), t.getQ(),
                    t.getConnectable().getId()));
        } else {
            netP += t.getP();
            netQ += t.getQ();
            LOG.info(String.format("add flow = %10.4f %10.4f %s", t.getP(), t.getQ(),
                    t.getConnectable().getName()));
        }
    }

    private void addFlowQ(Terminal t) {
        if (Double.isNaN(t.getQ())) {
            known = false;
            LOG.error(String.format("flowQ NaN %s", t.getConnectable().getId()));
        } else {
            netQ += t.getQ();
        }
    }

    private void setZ0Flows() {
        Terminal t = Terminals.get(line, bus);
        t.setP(-netP);
        t.setQ(-netQ);
        LOG.info(String.format("assign net flow = %10.4f %10.4f %s", t.getP(), t.getQ(),
                t.getConnectable().getName()));
        Terminal othert = Terminals.getOther(line, bus);
        othert.setP(netP);
        othert.setQ(netQ);
    }

    @Override
    public void visitBusbarSection(BusbarSection section) {

    }

    @Override
    public void visitLine(Line line, Side side) {
        if (line.equals(this.line)) {
            return;
        }
        addFlow(line.getTerminal(side));
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Side side) {
        addFlow(transformer.getTerminal(side));
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
            ThreeWindingsTransformer.Side side) {
        addFlow(transformer.getTerminal(side));
    }

    @Override
    public void visitGenerator(Generator generator) {
        addFlow(generator.getTerminal());
    }

    @Override
    public void visitLoad(Load load) {
        addFlow(load.getTerminal());
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {
        addFlowQ(sc.getTerminal());
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
        addFlow(danglingLine.getTerminal());
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        addFlowQ(staticVarCompensator.getTerminal());
    }

    private Bus                 root;
    private List<Z0Vertex>      vertexs;
    private List<List<Bus>>     levels;

    private static final Logger LOG = LoggerFactory.getLogger(BusLineZ0BalanceFlowCalculator.class);
}
