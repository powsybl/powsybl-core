/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class LineAdderAdapter implements LineAdder {

    private final MergingViewIndex index;

    private String id;

    private String name;

    private boolean ensureIdUnicity;

    private boolean fictitious;

    private Integer node1;

    private String bus1;

    private String connectableBus1;

    private String voltageLevelId1;

    private Integer node2;

    private String bus2;

    private String connectableBus2;

    private String voltageLevelId2;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g1 = Double.NaN;

    private double b1 = Double.NaN;

    private double g2 = Double.NaN;

    private double b2 = Double.NaN;

    LineAdderAdapter(final MergingViewIndex index) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    @Override
    public Line add() {
        Line newLine;
        final Network n1 = checkAndGetNetwork1();
        final Network n2 = checkAndGetNetwork2();
        if (n1 == n2) {
            newLine = index.getLine(addLine(n1));
        } else {
            throw new PowsyblException("Should not create a line between two sub-networks: create a tie line");
        }
        return newLine;
    }

    private Line addLine(final Network network) {
        LineAdder adder = network.newLine()
                    .setId(id)
                    .setEnsureIdUnicity(ensureIdUnicity)
                    .setName(name)
                    .setFictitious(fictitious)
                    .setR(r)
                    .setX(x)
                    .setG1(g1)
                    .setB1(b1)
                    .setG2(g2)
                    .setB2(b2)
                    .setVoltageLevel1(voltageLevelId1)
                    .setVoltageLevel2(voltageLevelId2)
                    .setBus1(bus1)
                    .setConnectableBus1(connectableBus1)
                    .setBus2(bus2)
                    .setConnectableBus2(connectableBus2);
        if (node1 != null) {
            adder.setNode1(node1);
        }
        if (node2 != null) {
            adder.setNode2(node2);
        }
        return adder.add();
    }

    private Network checkAndGetNetwork1() {
        if (voltageLevelId1 == null) {
            throw new PowsyblException("First voltage level is not set");
        }
        Network network = index.getNetwork(n -> n.getVoltageLevel(voltageLevelId1) != null);
        if (network == null) {
            throw new PowsyblException("First voltage level '" + voltageLevelId1 + "' not found");
        }
        return network;
    }

    private Network checkAndGetNetwork2() {
        if (voltageLevelId2 == null) {
            throw new PowsyblException("Second voltage level is not set");
        }
        Network network = index.getNetwork(n -> n.getVoltageLevel(voltageLevelId2) != null);
        if (network == null) {
            throw new PowsyblException("Second voltage level '" + voltageLevelId2 + "' not found");
        }
        return network;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public LineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public LineAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public LineAdder setG1(double g1) {
        this.g1 = g1;
        return this;
    }

    @Override
    public LineAdder setB1(double b1) {
        this.b1 = b1;
        return this;
    }

    @Override
    public LineAdder setG2(double g2) {
        this.g2 = g2;
        return this;
    }

    @Override
    public LineAdder setB2(double b2) {
        this.b2 = b2;
        return this;
    }

    @Override
    public LineAdder setVoltageLevel1(String voltageLevelId1) {
        this.voltageLevelId1 = voltageLevelId1;
        return this;
    }

    @Override
    public LineAdder setNode1(int node1) {
        this.node1 = node1;
        return this;
    }

    @Override
    public LineAdder setBus1(String bus1) {
        this.bus1 = bus1;
        return this;
    }

    @Override
    public LineAdder setConnectableBus1(String connectableBus1) {
        this.connectableBus1 = connectableBus1;
        return this;
    }

    @Override
    public LineAdder setVoltageLevel2(String voltageLevelId2) {
        this.voltageLevelId2 = voltageLevelId2;
        return this;
    }

    @Override
    public LineAdder setNode2(int node2) {
        this.node2 = node2;
        return this;
    }

    @Override
    public LineAdder setBus2(String bus2) {
        this.bus2 = bus2;
        return this;
    }

    @Override
    public LineAdder setConnectableBus2(String connectableBus2) {
        this.connectableBus2 = connectableBus2;
        return this;
    }

    @Override
    public LineAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public LineAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        this.ensureIdUnicity = ensureIdUnicity;
        return this;
    }

    @Override
    public LineAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public LineAdder setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }
}
