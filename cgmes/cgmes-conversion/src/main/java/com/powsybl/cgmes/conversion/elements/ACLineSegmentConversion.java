/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.TieLineAdder;
import com.powsybl.iidm.network.util.SV;
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

    public String boundaryNode() {
        // Only one of the end points can be in the boundary
        if (isBoundary(1)) {
            return nodeId(1);
        } else if (isBoundary(2)) {
            return nodeId(2);
        }
        return null;
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
            convertToDanglingLine(boundarySide);
        }
    }

    private void convertLine() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double bch = p.asDouble("bch");
        double gch = p.asDouble("gch", 0.0);
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
        convertedTerminals(l.getTerminal1(), l.getTerminal2());
    }

    private void convertToDanglingLine(int boundarySide) {
        // Non-boundary side (other side) of the line
        int modelSide = 3 - boundarySide;
        String boundaryNode = nodeId(boundarySide);

        // check again boundary node is correct
        assert isBoundary(boundarySide) && !isBoundary(modelSide);

        PowerFlow f = new PowerFlow(0, 0);
        // Only consider potential power flow at boundary side if that side is connected
        if (terminalConnected(boundarySide) && context.boundary().hasPowerFlow(boundaryNode)) {
            f = context.boundary().powerFlowAtNode(boundaryNode);
        }
        // There should be some equipment at boundarySide to model exchange through that
        // point
        // But we have observed, for the test case conformity/miniBusBranch,
        // that the ACLineSegment:
        // _5150a037-e241-421f-98b2-fe60e5c90303 XQ1-N1
        // ends in a boundary node where there is no other line,
        // does not have energy consumer or equivalent injection
        if (terminalConnected(boundarySide)
                && !context.boundary().hasPowerFlow(boundaryNode)
                && context.boundary().equivalentInjectionsAtNode(boundaryNode).isEmpty()) {
            missing("Equipment for modeling consumption/injection at boundary node");
        }

        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double bch = p.asDouble("bch");
        double gch = p.asDouble("gch", 0.0);
        DanglingLineAdder dlAdder = voltageLevel(modelSide).newDanglingLine()
                .setEnsureIdUnicity(false)
                .setR(r)
                .setX(x)
                .setG(gch)
                .setB(bch)
                .setUcteXnodeCode(findUcteXnodeCode(boundaryNode));
        identify(dlAdder);
        connect(dlAdder, modelSide);
        EquivalentInjectionConversion equivalentInjectionConversion = getEquivalentInjectionConversionForDanglingLine(boundaryNode);
        DanglingLine dl;
        if (equivalentInjectionConversion != null) {
            dl = equivalentInjectionConversion.convertOverDanglingLine(dlAdder, f);
            equivalentInjectionConversion.convertReactiveLimits(dl.getGeneration());
        } else {
            dl = dlAdder.setP0(f.p())
                    .setQ0(f.q())
                    .newGeneration()
                        .setTargetP(0.0)
                        .setTargetQ(0.0)
                        .setTargetV(Double.NaN)
                        .setVoltageRegulationOn(false)
                    .add()
                    .add();
        }
        context.convertedTerminal(terminalId(modelSide), dl.getTerminal(), 1, powerFlow(modelSide));

        // If we do not have power flow at model side and we can compute it,
        // do it and assign the result at the terminal of the dangling line
        if (context.config().computeFlowsAtBoundaryDanglingLines()
                && terminalConnected(modelSide)
                && !powerFlow(modelSide).defined()
                && context.boundary().hasVoltage(boundaryNode)) {
            double v = context.boundary().vAtBoundary(boundaryNode);
            double angle = context.boundary().angleAtBoundary(boundaryNode);
            // The net sum of power flow "entering" at boundary is "exiting"
            // through the line, we have to change the sign of the sum of flows
            // at the node when we consider flow at line end
            double p = dl.getP0() - dl.getGeneration().getTargetP();
            double q = dl.getQ0() - dl.getGeneration().getTargetQ();
            SV svboundary = new SV(-p, -q, v, angle);
            // The other side power flow must be computed taking into account
            // the same criteria used for ACLineSegment: total shunt admittance
            // is divided in 2 equal shunt admittance at each side of series impedance
            double g = dl.getG() / 2;
            double b = dl.getB() / 2;
            SV svmodel = svboundary.otherSide(dl.getR(), dl.getX(), g, b, g, b, 1);
            dl.getTerminal().setP(svmodel.getP());
            dl.getTerminal().setQ(svmodel.getQ());
        }
    }

    private EquivalentInjectionConversion getEquivalentInjectionConversionForDanglingLine(String boundaryNode) {
        List<PropertyBag> eis = context.boundary().equivalentInjectionsAtNode(boundaryNode);
        if (eis.isEmpty()) {
            return null;
        } else if (eis.size() > 1) {
            // This should not happen
            // We have decided to create a dangling line,
            // so only one MAS at this boundary point,
            // so there must be only one equivalent injection
            invalid("Multiple equivalent injections at boundary node");
            return null;
        } else {
            return new EquivalentInjectionConversion(eis.get(0), context);
        }
    }

    private String findUcteXnodeCode(String boundaryNode) {
        return context.boundary().nameAtBoundary(boundaryNode);
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
        connect(adder, boundaryLine1.tsoIidmVoltageLevelId, boundaryLine1.tsoBus, boundaryLine1.tsoTconnected,
            boundaryLine1.tsoNode, boundaryLine2.tsoIidmVoltageLevelId, boundaryLine2.tsoBus,
            boundaryLine2.tsoTconnected, boundaryLine2.tsoNode);
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
        connect(adder, boundaryLine1.tsoIidmVoltageLevelId, boundaryLine1.tsoBus, boundaryLine1.tsoTconnected,
            boundaryLine1.tsoNode, boundaryLine2.tsoIidmVoltageLevelId, boundaryLine2.tsoBus,
            boundaryLine2.tsoTconnected, boundaryLine2.tsoNode);
        return adder.add();
    }

    private BoundaryLine fillBoundaryLineFromLine(ACLineSegmentConversion ac, PropertyBag p, String id, String name, int tsoEnd) {
        BoundaryLine boundaryLine = new BoundaryLine();

        boundaryLine.tsoIidmVoltageLevelId = ac.iidmVoltageLevelId(tsoEnd);
        boundaryLine.tsoTconnected = ac.terminalConnected(tsoEnd);
        boundaryLine.tsoBus = ac.busId(tsoEnd);
        boundaryLine.tsoNode = -1;
        if (context.nodeBreaker()) {
            boundaryLine.tsoNode = ac.iidmNode(tsoEnd);
        }

        boundaryLine.r = p.asDouble("r");
        boundaryLine.x = p.asDouble("x");
        boundaryLine.g = p.asDouble("gch", 0);
        boundaryLine.b = p.asDouble("bch", 0);

        boundaryLine.id = context.namingStrategy().getId("Line", id);
        boundaryLine.name = context.namingStrategy().getName("Line", name);

        return boundaryLine;
    }

    private BoundaryLine fillBoundaryLineFromSwitch(ACLineSegmentConversion ac, String id, String name, int tsoEnd) {
        BoundaryLine boundaryLine = new BoundaryLine();

        boundaryLine.tsoIidmVoltageLevelId = ac.iidmVoltageLevelId(tsoEnd);
        boundaryLine.tsoTconnected = ac.terminalConnected(tsoEnd);
        boundaryLine.tsoBus = ac.busId(tsoEnd);
        boundaryLine.tsoNode = -1;
        if (context.nodeBreaker()) {
            boundaryLine.tsoNode = ac.iidmNode(tsoEnd);
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
        String tsoIidmVoltageLevelId;
        String tsoBus;
        boolean tsoTconnected;
        int tsoNode;
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
