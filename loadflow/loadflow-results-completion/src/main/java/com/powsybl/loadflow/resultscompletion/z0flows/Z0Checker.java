/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.DanglingLineData;
import com.powsybl.iidm.network.util.LegData;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class Z0Checker {

    public Z0Checker(LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {
        this.threshold = parameters.getZ0ThresholdDiffVoltageAngle();
        this.parameters = parameters;
        this.lfParameters = lfParameters;
    }

    Graph<Z0Vertex, Z0Edge> getZ0Graph() {
        return z0Graph;
    }

    public boolean z0GraphContains(Bus bus) {
        Objects.requireNonNull(bus);
        return z0VertexIds.containsKey(bus.getId());
    }

    // A line is considered Z0 (null impedance) if and only if
    // it is connected at both ends and the voltage at end buses are the same

    public boolean isZ0(Line line) {
        if (!line.getTerminal1().isConnected() || !line.getTerminal2().isConnected()) {
            return false;
        }
        Bus b1 = line.getTerminal1().getBusView().getBus();
        Bus b2 = line.getTerminal2().getBusView().getBus();
        Objects.requireNonNull(b1);
        Objects.requireNonNull(b2);
        boolean r = Math.abs(b1.getV() - b2.getV()) < threshold
                && Math.abs(b1.getAngle() - b2.getAngle()) < threshold;
        if (r) {
            LOGGER.debug("Line Z0 {} ({}) dV = {}, dA = {}", line.getNameOrId(), line.getId(), Math.abs(b1.getV() - b2.getV()), Math.abs(b1.getAngle() - b2.getAngle()));
        }
        return r;
    }

    public static boolean isZ0Antenna(Line line) {
        return line.getR() == 0.0 && line.getX() == 0.0
            && ((line.getTerminal1().isConnected() && !line.getTerminal2().isConnected())
                || (!line.getTerminal1().isConnected() && line.getTerminal2().isConnected()));
    }

    public boolean isZ0(TwoWindingsTransformer t2wt) {
        if (!t2wt.getTerminal1().isConnected() || !t2wt.getTerminal2().isConnected()) {
            return false;
        }
        Bus b1 = t2wt.getTerminal1().getBusView().getBus();
        Bus b2 = t2wt.getTerminal2().getBusView().getBus();
        Objects.requireNonNull(b1);
        Objects.requireNonNull(b2);

        boolean r = Math.abs(b1.getV() * BranchData.rho(t2wt) - b2.getV()) < threshold
            && Math.abs(b1.getAngle() + BranchData.alphaDegrees(t2wt) - b2.getAngle()) < threshold;
        if (r) {
            LOGGER.debug("TwoWindingsTransformer Z0 {} ({}) dV = {}, dA = {}", t2wt.getNameOrId(), t2wt.getId(),
                Math.abs(b1.getV() * BranchData.rho(t2wt) - b2.getV()),
                Math.abs(b1.getAngle() + BranchData.alphaDegrees(t2wt) - b2.getAngle()));
        }
        return r;
    }

    public static boolean isZ0Antenna(TwoWindingsTransformer t2wt) {
        return t2wt.getR() == 0.0 && t2wt.getX() == 0.0
            && ((t2wt.getTerminal1().isConnected() && !t2wt.getTerminal2().isConnected())
                || (!t2wt.getTerminal1().isConnected() && t2wt.getTerminal2().isConnected()));
    }

    public List<Leg> getZ0Legs(ThreeWindingsTransformer t3wt) {
        List<Leg> connectedLegs = t3wt.getLegs().stream().filter(leg -> leg.getTerminal().isConnected()).collect(Collectors.toList());
        List<List<Leg>> legConfigurations = generateLegConfigurations(connectedLegs);

        List<Leg> z0Legs = legConfigurations.stream().filter(legConfiguration -> isZ0(t3wt, legConfiguration, t3wt.getRatedU0()))
            .findFirst().orElse(Collections.emptyList());

        if (!z0Legs.isEmpty()) {
            LOGGER.debug("ThreeWindingsTransformer Z0 {} ({}) Z0 Legs = {}", t3wt.getNameOrId(), t3wt.getId(), z0Legs);
        }
        return z0Legs;
    }

    public List<Leg> getZ0AntennaLegs(ThreeWindingsTransformer t3wt) {
        if (t3wt.getLegs().stream().anyMatch(leg -> leg.getTerminal().isConnected())) {
            return t3wt.getLegs().stream()
                .filter(Z0Checker::isZ0Antenna)
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static boolean isZ0Antenna(Leg leg) {
        return leg.getR() == 0.0 && leg.getX() == 0.0 && !leg.getTerminal().isConnected();
    }

    public static boolean isZ0(DanglingLine dl) {
        if (dl.getTerminal().isConnected()) {
            return DanglingLineData.isZ0(dl);
        } else {
            return false;
        }
    }

    private static List<List<Leg>> generateLegConfigurations(List<Leg> connectedLegs) {
        List<List<Leg>> legConfigurations = new ArrayList<>();
        if (connectedLegs.size() == 3) {
            legConfigurations.add(connectedLegs);
        }
        if (connectedLegs.size() >= 2) {
            for (int i = 0; i < connectedLegs.size() - 1; i++) {
                for (int j = i + 1; j < connectedLegs.size(); j++) {
                    legConfigurations.add(Arrays.asList(connectedLegs.get(i), connectedLegs.get(j)));
                }
            }
        }
        if (!connectedLegs.isEmpty()) {
            for (Leg leg : connectedLegs) {
                legConfigurations.add(Collections.singletonList(leg));
            }
        }
        return legConfigurations;
    }

    private boolean isZ0(ThreeWindingsTransformer t3wt, List<Leg> legConfiguration, double ratedU0) {
        if (legConfiguration.isEmpty()) {
            return false;
        } else if (legConfiguration.size() >= 2) {
            return areAllWindingsZ0(t3wt, legConfiguration, ratedU0, threshold);
        } else {
            return isWindingZ0(legConfiguration.get(0));
        }
    }

    private static boolean areAllWindingsZ0(ThreeWindingsTransformer t3wt, List<Leg> legConfiguration, double ratedU0, double threshold) {

        Bus b0 = legConfiguration.get(0).getTerminal().getBusView().getBus();
        Objects.requireNonNull(b0);
        double vStar = b0.getV() * LegData.rho(legConfiguration.get(0), ratedU0);
        double angleStar = b0.getAngle() + LegData.alphaDegrees(legConfiguration.get(0), Z0Tools.getPhaseAngleClock(t3wt, legConfiguration.get(0)));

        for (int i = 1; i < legConfiguration.size(); i++) {
            Bus b = legConfiguration.get(i).getTerminal().getBusView().getBus();
            Objects.requireNonNull(b);

            if (Math.abs(b.getV() * LegData.rho(legConfiguration.get(i), ratedU0) - vStar) >= threshold ||
                Math.abs(b.getAngle() + LegData.alphaDegrees(legConfiguration.get(i), Z0Tools.getPhaseAngleClock(t3wt, legConfiguration.get(i))) - angleStar) >= threshold) {
                return false;
            }
        }

        return true;
    }

    // Different check
    private static boolean isWindingZ0(Leg leg) {
        return leg.getR() == 0.0 && leg.getX() == 0.0;
    }

    public void addToZ0Graph(Line line) {
        addToGraph(new Z0Edge(line,
            createZ0Vertex(line.getTerminal1().getBusView().getBus()),
            createZ0Vertex(line.getTerminal2().getBusView().getBus())));
    }

    public void addToZ0Graph(TwoWindingsTransformer t2wt) {
        addToGraph(new Z0Edge(t2wt,
            createZ0Vertex(t2wt.getTerminal1().getBusView().getBus()),
            createZ0Vertex(t2wt.getTerminal2().getBusView().getBus())));
    }

    public void addToZ0Graph(ThreeWindingsTransformer t3wt, List<Leg> z0Legs) {
        if (z0Legs.isEmpty()) {
            return;
        }
        Z0Vertex starVertex = createZ0Vertex(t3wt);
        for (Leg z0Leg : z0Legs) {
            addToGraph(new Z0Edge(z0Leg, createZ0Vertex(z0Leg.getTerminal().getBusView().getBus()), starVertex));
        }
    }

    public void addToZ0Graph(DanglingLine dl) {
        addToGraph(new Z0Edge(dl, createZ0Vertex(dl.getTerminal().getBusView().getBus()), createZ0Vertex(dl)));
    }

    private void addToGraph(Z0Edge z0Edge) {
        z0Graph.addVertex(z0Edge.getVertex1());
        z0Graph.addVertex(z0Edge.getVertex2());
        z0Graph.addEdge(z0Edge.getVertex1(), z0Edge.getVertex2(), z0Edge);
    }

    private Z0Vertex createZ0Vertex(Bus bus) {
        if (z0VertexIds.containsKey(bus.getId())) {
            return z0VertexIds.get(bus.getId());
        }
        Z0Vertex z0Vertex = new Z0Vertex(bus);
        z0VertexIds.put(bus.getId(), z0Vertex);
        return z0Vertex;
    }

    private Z0Vertex createZ0Vertex(ThreeWindingsTransformer t3wt) {
        if (z0VertexIds.containsKey(t3wt.getId())) {
            return z0VertexIds.get(t3wt.getId());
        }
        Z0Vertex z0Vertex = new Z0Vertex(t3wt, parameters, lfParameters);
        z0VertexIds.put(t3wt.getId(), z0Vertex);
        return z0Vertex;
    }

    private Z0Vertex createZ0Vertex(DanglingLine dl) {
        if (z0VertexIds.containsKey(dl.getId())) {
            return z0VertexIds.get(dl.getId());
        }
        Z0Vertex z0Vertex = new Z0Vertex(dl);
        z0VertexIds.put(dl.getId(), z0Vertex);
        return z0Vertex;
    }

    private final double threshold;
    private final LoadFlowResultsCompletionParameters parameters;
    private final LoadFlowParameters lfParameters;

    private final Graph<Z0Vertex, Z0Edge> z0Graph = new Pseudograph<>(Z0Edge.class);
    private final Map<String, Z0Vertex> z0VertexIds = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Z0Checker.class);
}
