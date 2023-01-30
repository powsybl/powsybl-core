/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import org.jgrapht.graph.DefaultWeightedEdge;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class Z0Edge extends DefaultWeightedEdge {

    public Z0Edge(Line line, Z0Vertex vertex1, Z0Vertex vertex2) {
        this(Objects.requireNonNull(line), null, null, null, vertex1, vertex2);
    }

    public Z0Edge(TwoWindingsTransformer twoWindingsTransformer, Z0Vertex vertex1, Z0Vertex vertex2) {
        this(null, Objects.requireNonNull(twoWindingsTransformer), null, null, vertex1, vertex2);
    }

    public Z0Edge(Leg leg, Z0Vertex vertex1, Z0Vertex vertex2) {
        this(null, null, Objects.requireNonNull(leg), null, vertex1, vertex2);
    }

    public Z0Edge(DanglingLine danglingLine, Z0Vertex vertex1, Z0Vertex vertex2) {
        this(null, null, null, Objects.requireNonNull(danglingLine), vertex1, vertex2);
    }

    private Z0Edge(Line line, TwoWindingsTransformer twoWindingsTransformer, Leg leg, DanglingLine danglingLine, Z0Vertex vertex1, Z0Vertex vertex2) {
        this.line = line;
        this.twoWindingsTransformer = twoWindingsTransformer;
        this.leg = leg;
        this.danglingLine = danglingLine;
        this.vertex1 = Objects.requireNonNull(vertex1);
        this.vertex2 = Objects.requireNonNull(vertex2);
    }

    /***
    public Line getLine() {
        return line;
    }

    public TwoWindingsTransformer getTwoWindingsTransformer() {
        return twoWindingsTransformer;
    }

    public Leg getLeg() {
        return leg;
    }
    ***/

    public DanglingLine getDanglingLine() {
        return danglingLine;
    }

    public Z0Vertex getVertex1() {
        return vertex1;
    }

    public Z0Vertex getVertex2() {
        return vertex2;
    }

    @Override
    protected double getWeight() {
        return line.getX();
    }

    public Terminal getTerminal(Z0Vertex vertex) {
        if (line != null) {
            return vertex.equals(vertex1) ? line.getTerminal1() : line.getTerminal2();
        }
        if (twoWindingsTransformer != null) {
            return vertex.equals(vertex1) ? twoWindingsTransformer.getTerminal1() : twoWindingsTransformer.getTerminal2();
        }
        if (leg != null) {
            return leg.getTerminal();
        }
        if (danglingLine != null) {
            return danglingLine.getTerminal();
        }
        throw new PowsyblException("Unexpected edge '" + this + "'");
    }

    void assignZeroFlowTo() {
        assignFlowTo(vertex1, 0.0, 0.0);
        assignFlowTo(vertex2, 0.0, 0.0);
    }

    void assignFlowTo(Z0Vertex z0Vertex, double p, double q) {
        if (line != null) {
            assignFlowToLine(line, vertex1, z0Vertex, p, q);
            return;
        }
        if (twoWindingsTransformer != null) {
            assignFlowToTwoWindingsTransformer(twoWindingsTransformer, vertex1, z0Vertex, p, q);
            return;
        }
        if (leg != null) {
            assignFlowToLeg(leg, vertex1, z0Vertex, p, q);
            return;
        }
        if (danglingLine != null) {
            assignFlowToDanglingLine(danglingLine, vertex1, z0Vertex, p, q);
            return;
        }
        throw new PowsyblException("Unexpected edge '" + this + "'");
    }

    private static void assignFlowToLine(Line line, Z0Vertex vertex1, Z0Vertex z0Vertex, double p, double q) {
        Objects.requireNonNull(line);
        Objects.requireNonNull(vertex1);
        Objects.requireNonNull(z0Vertex);
        if (vertex1.equals(z0Vertex)) {
            line.getTerminal1().setP(p);
            line.getTerminal1().setQ(q);
        } else {
            line.getTerminal2().setP(p);
            line.getTerminal2().setQ(q);
        }
    }

    private static void assignFlowToTwoWindingsTransformer(TwoWindingsTransformer t2wt, Z0Vertex vertex1,
        Z0Vertex z0Vertex, double p, double q) {
        Objects.requireNonNull(t2wt);
        Objects.requireNonNull(vertex1);
        Objects.requireNonNull(z0Vertex);
        if (vertex1.equals(z0Vertex)) {
            t2wt.getTerminal1().setP(p);
            t2wt.getTerminal1().setQ(q);
        } else {
            t2wt.getTerminal2().setP(p);
            t2wt.getTerminal2().setQ(q);
        }
    }

    private static void assignFlowToLeg(Leg leg, Z0Vertex vertex1, Z0Vertex z0Vertex, double p, double q) {
        Objects.requireNonNull(leg);
        Objects.requireNonNull(vertex1);
        Objects.requireNonNull(z0Vertex);
        if (vertex1.equals(z0Vertex)) {
            leg.getTerminal().setP(p);
            leg.getTerminal().setQ(q);
        }
    }

    private static void assignFlowToDanglingLine(DanglingLine danglingLine, Z0Vertex vertex1, Z0Vertex z0Vertex,
        double p, double q) {
        Objects.requireNonNull(danglingLine);
        Objects.requireNonNull(vertex1);
        Objects.requireNonNull(z0Vertex);
        if (vertex1.equals(z0Vertex)) {
            danglingLine.getTerminal().setP(p);
            danglingLine.getTerminal().setQ(q);
        }
    }

    Z0Vertex otherZ0Vertex(Z0Vertex z0Vertex) {
        return vertex1.equals(z0Vertex) ? vertex2 : vertex1;
    }

    boolean contains(Leg leg) {
        return this.leg != null && this.leg.equals(leg);
    }

    @Override
    public String toString() {
        if (line != null) {
            return line.toString();
        }
        if (twoWindingsTransformer != null) {
            return twoWindingsTransformer.toString();
        }
        if (leg != null) {
            return leg.toString();
        }
        if (danglingLine != null) {
            return danglingLine.toString();
        }
        return "";
    }

    // No serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException();
    }

    private final transient Line line;
    private final transient TwoWindingsTransformer twoWindingsTransformer;
    private final transient Leg leg;
    private final transient DanglingLine danglingLine;
    private final transient Z0Vertex vertex1;
    private final transient Z0Vertex vertex2;
}
