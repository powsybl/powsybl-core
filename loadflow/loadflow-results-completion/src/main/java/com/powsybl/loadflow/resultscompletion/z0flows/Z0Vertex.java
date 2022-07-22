/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLine.Generation;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class Z0Vertex {

    public Z0Vertex(Bus bus) {
        this(Objects.requireNonNull(bus), null, null, null, null);
    }

    Z0Vertex(ThreeWindingsTransformer threeWindingsTransformer, LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {
        this(null, Objects.requireNonNull(threeWindingsTransformer), null, parameters, lfParameters);
    }

    Z0Vertex(DanglingLine danglingLine) {
        this(null, null, Objects.requireNonNull(danglingLine), null, null);
    }

    private Z0Vertex(Bus bus, ThreeWindingsTransformer threeWindingsTransformer, DanglingLine danglingLine,
        LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {
        this.bus = bus;
        this.threeWindingsTransformer = threeWindingsTransformer;
        this.danglingLine = danglingLine;
        this.parameters = parameters;
        this.lfParameters = lfParameters;
    }

    Bus getBus() {
        return bus;
    }

    DanglingLine getDanglingLine() {
        return danglingLine;
    }

    void calculateBalanceWithImpedance(Set<Z0Edge> edges) {
        resetBalanceWithImpedance();

        if (bus != null) {
            balanceWithImpedanceBus(edges);
            return;
        }
        if (threeWindingsTransformer != null) {
            balanceWithImpedanceThreeWindingsTransformer(edges);
            return;
        }
        if (danglingLine != null) {
            balanceWithImpedanceDanglingLine();
            return;
        }
        throw new PowsyblException("Unexpected vertex '" + this + "'");
    }

    public void calculateBalanceForBus() {
        resetDescendentZ0Flow();
        resetBalanceWithImpedance();
        bus.getConnectedTerminalStream().forEach(this::addTerminalToBalanceWithImpedance);
    }

    private void balanceWithImpedanceBus(Set<Z0Edge> edges) {
        List<Terminal> connectedZ0Terminals = edges.stream().map(e -> e.getTerminal(this)).collect(Collectors.toList());
        bus.getConnectedTerminalStream().filter(t -> !connectedZ0Terminals.contains(t))
            .forEach(this::addTerminalToBalanceWithImpedance);
    }

    private void addTerminalToBalanceWithImpedance(Terminal t) {

        if (t.getConnectable().getType().equals(IdentifiableType.STATIC_VAR_COMPENSATOR)) {
            addToBalanceWithImpedanceOnlyQ(t.getQ());
        } else if (t.getConnectable().getType().equals(IdentifiableType.SHUNT_COMPENSATOR)) {
            ShuntCompensator shunt = (ShuntCompensator) t.getConnectable();
            if (Double.isNaN(shunt.getG()) || shunt.getG() == 0.0) {
                addToBalanceWithImpedanceOnlyQ(t.getQ());
            } else {
                addToBalanceWithImpedance(t.getP(), t.getQ());
            }
        } else {
            addToBalanceWithImpedance(t.getP(), t.getQ());
        }
    }

    private void balanceWithImpedanceThreeWindingsTransformer(Set<Z0Edge> edges) {
        // Edges only contains the connected zero impedance legs
        List<Leg> legsWithImpedance = threeWindingsTransformer.getLegs().stream()
            .filter(leg -> !isContained(edges, leg) && !Z0Checker.isZ0Antenna(leg))
            .collect(Collectors.toList());
        if (!legsWithImpedance.isEmpty()) {
            List<Leg> z0Legs = threeWindingsTransformer.getLegs().stream().filter(leg -> !legsWithImpedance.contains(leg)).collect(Collectors.toList());
            Complex vstar = Z0Tools.getVstarFromZ0Leg(threeWindingsTransformer, z0Legs, parameters, lfParameters);

            legsWithImpedance.forEach(leg -> {
                Complex flow = Z0Tools.getFlowLegAtStarBus(threeWindingsTransformer, leg, vstar, parameters, lfParameters);
                this.addToBalanceWithImpedance(flow.getReal(), flow.getImaginary());
            });
        }
    }

    private static boolean isContained(Set<Z0Edge> edges, Leg leg) {
        return edges.stream().anyMatch(edge -> edge.contains(leg));
    }

    private void balanceWithImpedanceDanglingLine() {
        // Injection at boundary node must be considered
        Generation generation = danglingLine.getGeneration();
        if (generation == null) {
            this.addToBalanceWithImpedance(danglingLine.getP0(), danglingLine.getQ0());
            return;
        }
        if (generation.isVoltageRegulationOn()) {
            double q = Double.isNaN(danglingLine.getTerminal().getQ()) ? danglingLine.getQ0() - generation.getTargetQ() : danglingLine.getTerminal().getQ();
            this.addToBalanceWithImpedance(danglingLine.getP0() - generation.getTargetP(), q);
            return;
        }
        this.addToBalanceWithImpedance(danglingLine.getP0() - generation.getTargetP(), danglingLine.getQ0() - generation.getTargetQ());
    }

    @Override
    public String toString() {
        if (bus != null) {
            return bus.toString();
        }
        if (threeWindingsTransformer != null) {
            return "Star bus of " + threeWindingsTransformer.toString();
        }
        if (danglingLine != null) {
            return "Boundary bus of " + danglingLine.toString();
        }
        return "";
    }

    private void resetBalanceWithImpedance() {
        balanceWithImpedance.reset();
    }

    private void addToBalanceWithImpedance(double p, double q) {
        balanceWithImpedance.add(p, q);
    }

    private void addToBalanceWithImpedanceOnlyQ(double q) {
        balanceWithImpedance.addOnlyQ(q);
    }

    void resetDescendentZ0Flow() {
        descendentZ0Flow.reset();
    }

    void addDescendentZ0Flow(double pflow, double qflow) {
        descendentZ0Flow.add(pflow,  qflow);
    }

    public double getBalanceP() {
        return balanceWithImpedance.p.known && descendentZ0Flow.p.known
            ? balanceWithImpedance.p.value + descendentZ0Flow.p.value
            : Double.NaN;
    }

    public double getBalanceQ() {
        return balanceWithImpedance.q.known && descendentZ0Flow.q.known
            ? balanceWithImpedance.q.value + descendentZ0Flow.q.value
            : Double.NaN;
    }

    private static final class PQData {
        private Data p = new Data();
        private Data q = new Data();

        private void reset() {
            p.reset();
            q.reset();
        }

        private void add(double p, double q) {
            this.p.add(p);
            this.q.add(q);
        }

        private void addOnlyQ(double q) {
            this.q.add(q);
        }

        private static final class Data {
            private double value = Double.NaN;
            private boolean known = false;

            private void reset() {
                value = 0.0;
                known = true;
            }

            private void add(double value) {
                if (Double.isNaN(value)) {
                    known = false;
                } else {
                    this.value += value;
                }
            }
        }
    }

    private final Bus bus;
    private final ThreeWindingsTransformer threeWindingsTransformer; // Defines the star node of the threeWindingsTransformer
    private final DanglingLine danglingLine; // Defines the boundary node of the danglingLine

    private PQData balanceWithImpedance = new PQData(); // accumulated flow and injection from non-z0Edges
    private PQData descendentZ0Flow = new PQData(); // accumulated flow from z0Edge

    private final LoadFlowResultsCompletionParameters parameters;
    private final LoadFlowParameters lfParameters;
}
