/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.*;
import org.apache.commons.math3.complex.Complex;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
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
                        .setRetained(true)
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
            addAliasesAndProperties(sw);
        } else {
            final LineAdder adder = context.network().newLine()
                    .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                    .setR(r)
                    .setX(x)
                    .setG1(gch / 2)
                    .setG2(gch / 2)
                    .setB1(bch / 2)
                    .setB2(bch / 2);
            identify(adder);
            connect(adder);
            final Line l = adder.add();
            addAliasesAndProperties(l);
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

        // CgmesBoundary node is common to both lines,
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
        mline.addAlias(terminalId(thisEnd), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1);
        mline.addAlias(terminalId(thisEnd == 1 ? 2 : 1),
                Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF1." + CgmesNames.TERMINAL + "_BOUNDARY");
        mline.addAlias(otherc.terminalId(otherEnd), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2);
        mline.addAlias(otherc.terminalId(otherEnd == 1 ? 2 : 1),
                Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF2." + CgmesNames.TERMINAL + "_BOUNDARY");
        context.convertedTerminal(terminalId(thisEnd), mline.getTerminal1(), 1, powerFlow(thisEnd));
        context.convertedTerminal(otherc.terminalId(otherEnd), mline.getTerminal2(), 2, otherc.powerFlow(otherEnd));
        if (mline instanceof TieLine) {
            TieLine tl = (TieLine) mline;
            context.terminalMapping().add(terminalId(thisEnd == 1 ? 2 : 1), tl.getHalf1().getBoundary(), 2);
            context.terminalMapping().add(otherc.terminalId(otherEnd == 1 ? 2 : 1), tl.getHalf2().getBoundary(), 1);
        }
    }

    public void convertLineAndSwitchAtNode(PropertyBag other, String boundaryNode) {
        String otherId = other.getId(CgmesNames.SWITCH);
        String otherName = other.getId("name");
        ACLineSegmentConversion otherc = new ACLineSegmentConversion(other, context);

        // CgmesBoundary node is common to both equipment,
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
        addAliasesAndProperties(mline);
        context.convertedTerminal(terminalId(thisEnd), mline.getTerminal1(), 1, powerFlow(thisEnd));
        context.convertedTerminal(otherc.terminalId(otherEnd), mline.getTerminal2(), 2, otherc.powerFlow(otherEnd));
    }

    private Line createTieLine(String boundaryNode, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        TieLineAdder adder = context.network().newTieLine()
            .setId(boundaryLine1.id + " + " + boundaryLine2.id)
            .setName(boundaryLine1.name + " + " + boundaryLine2.name)
            .newHalfLine1()
                .setId(boundaryLine1.id)
                .setName(boundaryLine1.name)
                .setR(boundaryLine1.r)
                .setX(boundaryLine1.x)
                .setG1(boundaryLine1.g / 2)
                .setG2(boundaryLine1.g / 2)
                .setB1(boundaryLine1.b / 2)
                .setB2(boundaryLine1.b / 2)
                .add()
            .newHalfLine2()
                .setId(boundaryLine2.id)
                .setName(boundaryLine2.name)
                .setR(boundaryLine2.r)
                .setX(boundaryLine2.x)
                .setG1(boundaryLine2.g / 2)
                .setG2(boundaryLine2.g / 2)
                .setB1(boundaryLine2.b / 2)
                .setB2(boundaryLine2.b / 2)
                .add()
            .setUcteXnodeCode(findUcteXnodeCode(boundaryNode));
        identify(adder, boundaryLine1.id + " + " + boundaryLine2.id, boundaryLine1.name + " + " + boundaryLine2.name);
        connect(adder, boundaryLine1.modelIidmVoltageLevelId, boundaryLine1.modelBus, boundaryLine1.modelTconnected,
            boundaryLine1.modelNode, boundaryLine2.modelIidmVoltageLevelId, boundaryLine2.modelBus,
            boundaryLine2.modelTconnected, boundaryLine2.modelNode);
        return adder.add();
    }

    private Line createQuadripole(BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        PiModel pi1 = new PiModel();
        pi1.r = boundaryLine1.r;
        pi1.x = boundaryLine1.x;
        pi1.g1 = boundaryLine1.g / 2.0;
        pi1.b1 = boundaryLine1.b / 2.0;
        pi1.g2 = pi1.g1;
        pi1.b2 = pi1.b1;
        PiModel pi2 = new PiModel();
        pi2.r = boundaryLine2.r;
        pi2.x = boundaryLine2.x;
        pi2.g1 = boundaryLine2.g / 2.0;
        pi2.b1 = boundaryLine2.b / 2.0;
        pi2.g2 = pi2.g1;
        pi2.b2 = pi2.b1;
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

    static class BoundaryLine {
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
    }

    static class PiModel {
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
    }

    static class Quadripole {
        Complex a;
        Complex b;
        Complex c;
        Complex d;

        public static Quadripole from(PiModel pi) {
            Quadripole y1 = Quadripole.fromShuntAdmittance(pi.g1, pi.b1);
            Quadripole z = Quadripole.fromSeriesImpedance(pi.r, pi.x);
            Quadripole y2 = Quadripole.fromShuntAdmittance(pi.g2, pi.b2);
            return y1.cascade(z).cascade(y2);
        }

        public static Quadripole fromSeriesImpedance(double r, double x) {
            Quadripole q = new Quadripole();
            q.a = new Complex(1);
            q.b = new Complex(r, x);
            q.c = new Complex(0);
            q.d = new Complex(1);
            return q;
        }

        public static Quadripole fromShuntAdmittance(double g, double b) {
            Quadripole q = new Quadripole();
            q.a = new Complex(1);
            q.b = new Complex(0);
            q.c = new Complex(g, b);
            q.d = new Complex(1);
            return q;
        }

        public Quadripole cascade(Quadripole q2) {
            Quadripole q1 = this;
            Quadripole qr = new Quadripole();
            qr.a = q1.a.multiply(q2.a).add(q1.b.multiply(q2.c));
            qr.b = q1.a.multiply(q2.b).add(q1.b.multiply(q2.d));
            qr.c = q1.c.multiply(q2.a).add(q1.d.multiply(q2.c));
            qr.d = q1.c.multiply(q2.b).add(q1.d.multiply(q2.d));
            return qr;
        }

        public PiModel toPiModel() {
            PiModel pi = new PiModel();

            // Y2 = (A - 1)/B
            // Y1 = (D - 1)/B
            Complex y1 = d.add(-1).divide(b);
            Complex y2 = a.add(-1).divide(b);

            pi.r = b.getReal();
            pi.x = b.getImaginary();
            pi.g1 = y1.getReal();
            pi.b1 = y1.getImaginary();
            pi.g2 = y2.getReal();
            pi.b2 = y2.getImaginary();
            return pi;
        }
    }
}
