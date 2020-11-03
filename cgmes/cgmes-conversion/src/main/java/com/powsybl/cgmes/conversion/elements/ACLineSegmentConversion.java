/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Quadripole;
import com.powsybl.iidm.network.util.Quadripole.PiModel;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ACLineSegmentConversion extends AbstractBranchConversion {

    public ACLineSegmentConversion(PropertyBag line, Context context) {
        super(CgmesNames.AC_LINE_SEGMENT, line, context);
    }

    @Override
    public boolean valid() {
        // An AC line segment end voltage level may be null
        // (when it is in the boundary and the boundary nodes are not converted)
        // So we do not use the generic validity check for conducting equipment
        // or branch. We only ensure we have nodes at both ends
        for (int k = 1; k <= 2; k++) {
            if (nodeId(k) == null) {
                missing(nodeIdPropertyName() + k);
                return false;
            }
        }
        return true;
    }

    @Override
    public void convert() {
        if (isBoundary(1)) {
            convertLineAtBoundary(1);
        } else if (isBoundary(2)) {
            convertLineAtBoundary(2);
        } else {
            convertLine();
        }
    }

    private void convertLineAtBoundary(int boundarySide) {
        // If we have created buses and substations for boundary nodes,
        // convert as a regular line
        if (context.config().convertBoundary()) {
            convertLine();
        } else {
            double r = p.asDouble("r");
            double x = p.asDouble("x");
            double gch = p.asDouble("gch", 0.0);
            double bch = p.asDouble("bch");

            convertToDanglingLine(boundarySide, r, x, gch, bch);
        }
    }

    private void convertLine() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double bch = p.asDouble("bch");
        double gch = p.asDouble("gch", 0.0);
        if (isZeroImpedanceInsideVoltageLevel(r, x, bch, gch)) {
            // Convert to switch
            Switch sw;
            boolean open = !(terminalConnected(1) && terminalConnected(2));
            if (context.nodeBreaker()) {
                VoltageLevel.NodeBreakerView.SwitchAdder adder;
                adder = voltageLevel().getNodeBreakerView().newSwitch()
                        .setKind(SwitchKind.BREAKER)
                        .setFictitious(true);
                identify(adder);
                connect(adder, open);
                sw = adder.add();
            } else {
                VoltageLevel.BusBreakerView.SwitchAdder adder;
                adder = voltageLevel().getBusBreakerView().newSwitch()
                        .setFictitious(true);
                identify(adder);
                connect(adder, open);
                sw = adder.add();
            }
            addAliases(sw);
        } else {
            final LineAdder adder = context.network().newLine()
                    .setEnsureIdUnicity(false)
                    .setR(r)
                    .setX(x)
                    .setG1(gch / 2)
                    .setG2(gch / 2)
                    .setB1(bch / 2)
                    .setB2(bch / 2);
            identify(adder);
            connect(adder);
            final Line l = adder.add();
            addAliases(l);
            convertedTerminals(l.getTerminal1(), l.getTerminal2());
        }
    }

    private boolean isZeroImpedanceInsideVoltageLevel(double r, double x, double bch, double gch) {
        return r == 0.0 && x == 0.0 && voltageLevel(1) == voltageLevel(2);
    }

    public void convertMergedLinesAtNode(PropertyBag other, String boundaryNode) {
        String otherId = other.getId(CgmesNames.AC_LINE_SEGMENT);
        String otherName = other.getId("name");
        ACLineSegmentConversion otherc = new ACLineSegmentConversion(other, context);

        // Boundary node is common to both lines,
        // identify the end that will be preserved for this line and the other line
        int thisEnd = 1;
        if (nodeId(1).equals(boundaryNode)) {
            thisEnd = 2;
        }
        int otherEnd = 1;
        if (otherc.nodeId(1).equals(boundaryNode)) {
            otherEnd = 2;
        }

        BoundaryLine boundaryLine1 = fillBoundaryLineFromLine(this, p, id, name, thisEnd);
        BoundaryLine boundaryLine2 = fillBoundaryLineFromLine(otherc, other, otherId, otherName, otherEnd);

        Line mline;
        if (context.config().mergeBoundariesUsingTieLines()) {
            mline = createTieLine(boundaryNode, boundaryLine1, boundaryLine2);
        } else {
            mline = createQuadripole(boundaryLine1, boundaryLine2);
        }
        addAliases(mline);
        context.convertedTerminal(terminalId(thisEnd), mline.getTerminal1(), 1, powerFlow(thisEnd));
        context.convertedTerminal(otherc.terminalId(otherEnd), mline.getTerminal2(), 2, otherc.powerFlow(otherEnd));
    }

    public void convertLineAndSwitchAtNode(PropertyBag other, String boundaryNode) {
        String otherId = other.getId(CgmesNames.SWITCH);
        String otherName = other.getId("name");
        ACLineSegmentConversion otherc = new ACLineSegmentConversion(other, context);

        // Boundary node is common to both equipment,
        // identify the end that will be preserved for this line and the other equipment
        int thisEnd = 1;
        if (nodeId(1).equals(boundaryNode)) {
            thisEnd = 2;
        }
        int otherEnd = 1;
        if (otherc.nodeId(1).equals(boundaryNode)) {
            otherEnd = 2;
        }

        BoundaryLine boundaryLine1 = fillBoundaryLineFromLine(this, p, id, name, thisEnd);
        BoundaryLine boundaryLine2 = fillBoundaryLineFromSwitch(otherc, otherId, otherName, otherEnd);

        Line mline;
        if (context.config().mergeBoundariesUsingTieLines()) {
            mline = createTieLine(boundaryNode, boundaryLine1, boundaryLine2);
        } else {
            mline = createQuadripole(boundaryLine1, boundaryLine2);
        }
        addAliases(mline);
        context.convertedTerminal(terminalId(thisEnd), mline.getTerminal1(), 1, powerFlow(thisEnd));
        context.convertedTerminal(otherc.terminalId(otherEnd), mline.getTerminal2(), 2, otherc.powerFlow(otherEnd));
    }

    private Line createTieLine(String boundaryNode, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        TieLineAdder adder = context.network().newTieLine()
            .setId(boundaryLine1.id + " + " + boundaryLine2.id)
            .setName(boundaryLine1.name + " + " + boundaryLine2.name)
            .line1()
            .setId(boundaryLine1.id)
            .setName(boundaryLine1.name)
            .setR(boundaryLine1.r)
            .setX(boundaryLine1.x)
            .setG1(boundaryLine1.g / 2)
            .setG2(boundaryLine1.g / 2)
            .setB1(boundaryLine1.b / 2)
            .setB2(boundaryLine1.b / 2)
            .setXnodeP(0)
            .setXnodeQ(0)
            .line2()
            .setId(boundaryLine2.id)
            .setName(boundaryLine2.name)
            .setR(boundaryLine2.r)
            .setX(boundaryLine2.x)
            .setG1(boundaryLine2.g / 2)
            .setG2(boundaryLine2.g / 2)
            .setB1(boundaryLine2.b / 2)
            .setB2(boundaryLine2.b / 2)
            .setXnodeP(0)
            .setXnodeQ(0)
            .setUcteXnodeCode(findUcteXnodeCode(boundaryNode));
        identify(adder, boundaryLine1.id + " + " + boundaryLine2.id, boundaryLine1.name + " + " + boundaryLine2.name);
        connect(adder, boundaryLine1.modelIidmVoltageLevelId, boundaryLine1.modelBus, boundaryLine1.modelTconnected,
            boundaryLine1.modelNode, boundaryLine2.modelIidmVoltageLevelId, boundaryLine2.modelBus,
            boundaryLine2.modelTconnected, boundaryLine2.modelNode);
        return adder.add();
    }

    private Line createQuadripole(BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        PiModel pi1 = PiModel.from(boundaryLine1);
        PiModel pi2 = PiModel.from(boundaryLine2);
        PiModel pim = Quadripole.from(pi1).cascade(Quadripole.from(pi2)).toPiModel();
        LineAdder adder = context.network().newLine()
            .setR(pim.r)
            .setX(pim.x)
            .setG1(pim.g1)
            .setG2(pim.g2)
            .setB1(pim.b1)
            .setB2(pim.b2);
        identify(adder, boundaryLine1.id + " + " + boundaryLine2.id, boundaryLine1.name + " + " + boundaryLine2.name);
        connect(adder, boundaryLine1.modelIidmVoltageLevelId, boundaryLine1.modelBus, boundaryLine1.modelTconnected,
            boundaryLine1.modelNode, boundaryLine2.modelIidmVoltageLevelId, boundaryLine2.modelBus,
            boundaryLine2.modelTconnected, boundaryLine2.modelNode);
        return adder.add();
    }

    private BoundaryLine fillBoundaryLineFromLine(ACLineSegmentConversion ac, PropertyBag p, String id, String name, int modelEnd) {
        BoundaryLine boundaryLine = new BoundaryLine();

        boundaryLine.modelIidmVoltageLevelId = ac.iidmVoltageLevelId(modelEnd);
        boundaryLine.modelTconnected = ac.terminalConnected(modelEnd);
        boundaryLine.modelBus = ac.busId(modelEnd);
        boundaryLine.modelNode = -1;
        if (context.nodeBreaker()) {
            boundaryLine.modelNode = ac.iidmNode(modelEnd);
        }

        boundaryLine.r = p.asDouble("r");
        boundaryLine.x = p.asDouble("x");
        boundaryLine.g = p.asDouble("gch", 0);
        boundaryLine.b = p.asDouble("bch", 0);

        boundaryLine.id = context.namingStrategy().getId("Line", id);
        boundaryLine.name = context.namingStrategy().getName("Line", name);

        return boundaryLine;
    }

    private BoundaryLine fillBoundaryLineFromSwitch(ACLineSegmentConversion ac, String id, String name, int modelEnd) {
        BoundaryLine boundaryLine = new BoundaryLine();

        boundaryLine.modelIidmVoltageLevelId = ac.iidmVoltageLevelId(modelEnd);
        boundaryLine.modelTconnected = ac.terminalConnected(modelEnd);
        boundaryLine.modelBus = ac.busId(modelEnd);
        boundaryLine.modelNode = -1;
        if (context.nodeBreaker()) {
            boundaryLine.modelNode = ac.iidmNode(modelEnd);
        }

        boundaryLine.r = 0.0;
        boundaryLine.x = 0.0;
        boundaryLine.g = 0.0;
        boundaryLine.b = 0.0;
        boundaryLine.id = context.namingStrategy().getId(CgmesNames.SWITCH, id);
        boundaryLine.name = context.namingStrategy().getName(CgmesNames.SWITCH, name);

        return boundaryLine;
    }

    static class BoundaryLine implements LineCharacteristics<BoundaryLine> {
        String id;
        String name;
        String modelIidmVoltageLevelId;
        String modelBus;
        boolean modelTconnected;
        int modelNode;
        double r;
        double x;
        double g;
        double b;

        @Override
        public double getR() {
            return r;
        }

        @Override
        public BoundaryLine setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public BoundaryLine setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public double getG1() {
            return this.g / 2;
        }

        @Override
        public BoundaryLine setG1(double g1) {
            throw new PowsyblException("setG1 not supported on BoundaryLine for ACLineSegmentConversion");
        }

        @Override
        public double getG2() {
            return this.g / 2;
        }

        @Override
        public BoundaryLine setG2(double g2) {
            throw new PowsyblException("setG2 not supported on BoundaryLine for ACLineSegmentConversion");
        }

        @Override
        public double getB1() {
            return this.b / 2;
        }

        @Override
        public BoundaryLine setB1(double b1) {
            throw new PowsyblException("setB1 not supported on BoundaryLine for ACLineSegmentConversion");
        }

        @Override
        public double getB2() {
            return this.b / 2;
        }

        @Override
        public BoundaryLine setB2(double b2) {
            throw new PowsyblException("setB2 not supported on BoundaryLine for ACLineSegmentConversion");
        }
    }
}
