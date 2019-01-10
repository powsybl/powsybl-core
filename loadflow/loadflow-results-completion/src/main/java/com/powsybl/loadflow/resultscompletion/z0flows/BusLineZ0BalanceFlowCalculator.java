/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.resultscompletion.z0flows.kruskal.Edge;
import com.powsybl.loadflow.resultscompletion.z0flows.kruskal.KruskalMST;

public class BusLineZ0BalanceFlowCalculator implements TopologyVisitor {

    private Bus bus;
    private Line line;

    private boolean known;
    private double netP;
    private double netQ;

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

    public static void calc(Network network) {
        Z0Buses z0Buses = BusZ0Calculator.calc(network);
        for (int index = 0; index < z0Buses.getSize(); index++) {
            Z0Bus z0Bus = z0Buses.getZ0Bus(index);

            EdgeWeightedGraph graph = EdgeWeightedGraphCalculator.calc(z0Bus);

            KruskalMST mst = calcKruskalMST(graph);
            Z0BusTree tree = calcTree(mst, z0Bus);

            calcOutsideTreeFlow(network, graph, mst);
            calcTreeFlow(network, tree);
        }
    }

    public static KruskalMST calcKruskalMST(EdgeWeightedGraph graph) {

        KruskalMST mst = new KruskalMST(graph);
        return mst;
    }

    public static Z0BusTree calcTree(KruskalMST mst, Z0Bus z0Bus) {

        Z0BusTree tree = new Z0BusTree();

        Bus bus = z0Bus.getBus(0);
        Z0BusTreeNode parent = new Z0BusTreeNode();
        int idOfTreeParent = 0;
        Z0BusTreeLevel pLevel = new Z0BusTreeLevel();
        int level = 0;

        parent.setBus(bus);
        parent.setLevel(level);
        pLevel.addIdOfTreeNodes(idOfTreeParent);

        tree.setRoot(bus.getId());
        tree.addTreeNode(parent);
        tree.addTreeLevel(pLevel);

        while (level < tree.treeLevelSize()) {

            int newLevel = -1;
            Z0BusTreeLevel nLevel = new Z0BusTreeLevel();
            Z0BusTreeLevel levelTreeNodes = tree.getTreeLevel(level);
            for (int idOfTreeNode : levelTreeNodes.getIdOfTreeNodes()) {
                Z0BusTreeNode treeNode = tree.getTreeNode(idOfTreeNode);
                int z0Pos = z0Bus.indexOf(treeNode.getBus());
                for (Edge e : mst.edges()) {
                    int otherPos = -1;
                    if (e.either() == z0Pos) {
                        otherPos = e.other(z0Pos);
                    } else if (e.other(e.either()) == z0Pos) {
                        otherPos = e.either();
                    }

                    if (otherPos == -1) {
                        continue;
                    }

                    Bus otherBus = z0Bus.getBus(otherPos);
                    if (tree.contains(otherBus)) {
                        continue;
                    }

                    if (newLevel == -1) {
                        tree.addTreeLevel(nLevel);
                        newLevel = level + 1;
                    }

                    Z0BusTreeNode otherTreeNode = new Z0BusTreeNode();
                    otherTreeNode.setBus(otherBus);
                    otherTreeNode.setLevel(newLevel);
                    otherTreeNode.setIdOfTreeParent(idOfTreeNode);
                    otherTreeNode.setIdBranchFromParent(e.getLineId());
                    tree.addTreeNode(otherTreeNode);

                    int idOtherTreeNode = tree.treeNodeSize() - 1;
                    nLevel.addIdOfTreeNodes(idOtherTreeNode);

                    treeNode.addIdOfTreeChildren(idOtherTreeNode);
                }
            }
            level++;
        }

        return tree;
    }

    public static void calcOutsideTreeFlow(Network network, EdgeWeightedGraph graph, KruskalMST mst) {

        graph.edges().forEach(e -> {
            if (!Iterables.contains(mst.edges(), e)) {
                String lineId = e.getLineId();
                Line line = network.getLine(lineId);
                line.getTerminal1().setP(0.0);
                line.getTerminal1().setQ(0.0);
                line.getTerminal2().setP(0.0);
                line.getTerminal2().setQ(0.0);

                if (line.getB1() != 0.0 || line.getB2() != 0.0 || line.getG1() != 0.0 || line.getG2() != 0.0) {
                    LOG.error("Line " + line.toString() + " with Bs or Gs");
                }
            }
        });
    }

    public static void calcTreeFlow(Network network, Z0BusTree tree) {

        int lastLevel = tree.treeLevelSize() - 1;
        while (lastLevel >= 1) {

            Z0BusTreeLevel level = tree.getTreeLevel(lastLevel);
            level.getIdOfTreeNodes().forEach(idOfTreeNode -> {
                Z0BusTreeNode treeNode = tree.getTreeNode(idOfTreeNode);
                Bus bus = treeNode.getBus();
                String lineId = treeNode.getIdBranchFromParent();
                Line line = network.getLine(lineId);

                BusLineZ0BalanceFlowCalculator calculator = new BusLineZ0BalanceFlowCalculator(bus, line);
            });
            lastLevel--;
        }
    }

    private void addFlow(Terminal t) {
        if (Double.isNaN(t.getP()) || Double.isNaN(t.getQ())) {
            known = false;
            LOG.error(String.format("flow NaN %10.4f %10.4f %s", t.getP(), t.getQ(), t.getConnectable().getId()));
        } else {
            netP += t.getP();
            netQ += t.getQ();
            LOG.info(String.format("add flow = %10.4f %10.4f %s", t.getP(), t.getQ(), t.getConnectable().getName()));
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
        LOG.info(String.format("assign net flow = %10.4f %10.4f %s", t.getP(), t.getQ(), t.getConnectable().getName()));
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

    private static final Logger LOG = LoggerFactory.getLogger(BusLineZ0BalanceFlowCalculator.class);
}
