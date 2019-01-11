package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;

public class Z0FlowsCompletion {

    public Z0FlowsCompletion(Network network) {
        super();
        this.network = network;
    }

    public void complete() {
        List<Z0Bus> z0Buses = busZ0Calulator(network);
        for (int index = 0; index < z0Buses.size(); index++) {
            Z0Bus z0Bus = z0Buses.get(index);

            SimpleWeightedGraph graph = edgeWeightedGraphCalculator(z0Bus);
            KruskalMinimumSpanningTree mst = new KruskalMinimumSpanningTree(graph);
            calcTree(mst, z0Bus);

            calcOutsideTreeFlow(network, graph, mst);
            calcTreeFlow(network);
        }
    }

    private List<Z0Bus> busZ0Calulator(Network network) {
        List<Z0Bus> z0Buses = new ArrayList<>();
        network.getBusView().getBusStream().forEach(bus -> {
            BusZ0Calculator calculator = new BusZ0Calculator(bus);
            calculator.calc(z0Buses);
        });

        return z0Buses;
    }

    private SimpleWeightedGraph edgeWeightedGraphCalculator(Z0Bus z0Bus) {

        SimpleWeightedGraph graph = new SimpleWeightedGraph(Object.class);

        for (int pos = 0; pos < z0Bus.size(); pos++) {
            Bus bus = z0Bus.getBus(pos);
            EdgeWeightedGraphCalculator calculator = new EdgeWeightedGraphCalculator(bus, z0Bus);
            calculator.calc(graph);
        }
        return graph;
    }

    private void calcTree(KruskalMinimumSpanningTree mst, Z0Bus z0Bus) {

        levels = new ArrayList<List<Bus>>();
        parent = new HashMap<Bus, Line>();

        List<Bus> levelBuses = new ArrayList<Bus>();
        levelBuses.add(z0Bus.getBus(0));
        levels.add(levelBuses);
        
        int level = 0;
        while (level < levels.size()) {

            List<Bus> newLevelBuses = new ArrayList<Bus>();

            List<Bus> buses = levels.get(level);
            for (Bus bus : buses) {
                Iterator<Z0LineWeightEdge> it = mst.getSpanningTree().getEdges().iterator();
                while (it.hasNext()) {

                    Z0LineWeightEdge e = it.next();
                    Bus otherBus = null;
                    if (e.getLine().getTerminal1().getBusView().getBus().equals(bus)) {
                        otherBus = e.getLine().getTerminal2().getBusView().getBus();
                    } else if (e.getLine().getTerminal2().getBusView().getBus().equals(bus)) {
                        otherBus = e.getLine().getTerminal1().getBusView().getBus();
                    } else {
                        continue;
                    }

                    if (parent.containsValue(e.getLine())) {
                        continue;
                    }

                    newLevelBuses.add(otherBus);
                    parent.put(otherBus, e.getLine());
                }
            }
            if (newLevelBuses.size() > 0) {
                levels.add(newLevelBuses);
            }
            level++;
        }
    }

    private void calcOutsideTreeFlow(Network network, SimpleWeightedGraph graph,
            KruskalMinimumSpanningTree mst) {
        Iterator<Z0LineWeightEdge> it = graph.edgeSet().iterator();
        while (it.hasNext()) {

            Z0LineWeightEdge e = it.next();
            if (!Iterables.contains(mst.getSpanningTree().getEdges(), e)) {
                String lineId = e.getLine().getId();
                Line line = network.getLine(lineId);
                line.getTerminal1().setP(0.0);
                line.getTerminal1().setQ(0.0);
                line.getTerminal2().setP(0.0);
                line.getTerminal2().setQ(0.0);

                if (line.getB1() != 0.0 || line.getB2() != 0.0 || line.getG1() != 0.0
                        || line.getG2() != 0.0) {
                    LOG.error("Line " + line.toString() + " with Bs or Gs");
                }
            }
        }
    }

    private void calcTreeFlow(Network network) {

        int lastLevel = levels.size() - 1;
        while (lastLevel >= 1) {
            levels.get(lastLevel).forEach(bus -> {
                Line line = parent.get(bus);
                BusLineZ0BalanceFlowCalculator calculator = new BusLineZ0BalanceFlowCalculator(bus,
                        line);
            });
            lastLevel--;
        }
    }

    private Network             network;
    private List<List<Bus>>     levels;
    private Map<Bus,Line>       parent;

    private static final Logger LOG = LoggerFactory.getLogger(Z0FlowsCompletion.class);

}
