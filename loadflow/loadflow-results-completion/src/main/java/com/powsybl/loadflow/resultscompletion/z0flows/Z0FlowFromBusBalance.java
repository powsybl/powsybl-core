/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
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
                        t.getConnectable().getNameOrId(),
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
                        t.getConnectable().getNameOrId(),
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
    public void visitLine(Line line, TwoSides side) {
        if (line.equals(this.line)) {
            return;
        }
        if (!line.getTerminal1().isConnected() || !line.getTerminal2().isConnected()) {
            return;
        }
        addFlow(line.getTerminal(side));
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
        addFlow(transformer.getTerminal(side));
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer,
            ThreeSides side) {
        addFlow(transformer.getTerminal(side));
    }

    @Override
    public void visitGenerator(Generator generator) {
        addFlow(generator.getTerminal());
    }

    @Override
    public void visitBattery(Battery battery) {
        addFlow(battery.getTerminal());
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

    @Override
    public void visitGround(Ground ground) {
        addFlow(ground.getTerminal());
    }

    private final Bus bus;
    private final Line line;

    private boolean known;
    private double netP;
    private double netQ;

    private static final Logger LOG = LoggerFactory.getLogger(Z0FlowFromBusBalance.class);
}
