/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.Objects;

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

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Z0FlowFromBusBalance implements TopologyVisitor {

    public Z0FlowFromBusBalance(Bus bus, Line line) {
        this.bus = Objects.requireNonNull(bus);
        this.line = Objects.requireNonNull(line);

        this.netP = 0.0;
        this.netQ = 0.0;

        this.known = true;
    }

    public void complete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Z0 flow for line {} from balance at bus {}", line, bus);
        }
        bus.visitConnectedEquipments(this);
        if (this.known) {
            completeZ0Flow();
        }
    }

    private void addFlow(Terminal t) {
        if (Double.isNaN(t.getP()) || Double.isNaN(t.getQ())) {
            known = false;
            LOG.warn("Z0 flow    unknown P, Q flow at {}", t.getConnectable());
        } else {
            netP += t.getP();
            netQ += t.getQ();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Z0 flow    P,Q %10.4f %10.4f %s %s",
                        t.getP(), t.getQ(),
                        t.getConnectable().getName(),
                        t.getConnectable().getId()));
            }
        }
    }

    private void addFlowQ(Terminal t) {
        if (Double.isNaN(t.getQ())) {
            known = false;
            LOG.warn("Z0 flow    unknown Q flow at {}", t.getConnectable());
        } else {
            netQ += t.getQ();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Z0 flow    Q   %10s %10.4f %s %s",
                        "-",
                        t.getQ(),
                        t.getConnectable().getName(),
                        t.getConnectable().getId()));
            }
        }
    }

    private void completeZ0Flow() {
        Terminal t = BranchTerminal.ofBus(line, bus);
        Objects.requireNonNull(t);
        t.setP(-netP);
        t.setQ(-netQ);
        Terminal other = BranchTerminal.ofOtherBus(line, bus);
        Objects.requireNonNull(other);
        other.setP(netP);
        other.setQ(netQ);
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Z0 flow    Z0  %10.4f %10.4f", t.getP(), t.getQ()));
        }
    }

    @Override
    public void visitBusbarSection(BusbarSection section) {
        // Nothing to do
    }

    @Override
    public void visitLine(Line line, Side side) {
        if (line.equals(this.line)) {
            return;
        }
        if (!line.getTerminal1().isConnected() || !line.getTerminal2().isConnected()) {
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

    private final Bus bus;
    private final Line line;

    private boolean known;
    private double netP;
    private double netQ;

    private static final Logger LOG = LoggerFactory.getLogger(Z0FlowFromBusBalance.class);
}
