/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ACLineSegmentConversion extends AbstractConductingEquipmentConversion {

    public ACLineSegmentConversion(PropertyBag line, Conversion.Context context) {
        super(CgmesNames.AC_LINE_SEGMENT, line, context, 2);
    }

    @Override
    public boolean valid() {
        // An AC line segment end voltage level may be null
        // (when it is in the boundary and the boundary nodes are not converted)
        // So we do not use the generic validity check for conducting equipment
        // We only ensure we have nodes at both ends
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
            convertLineOnBoundary(1);
        } else if (isBoundary(2)) {
            convertLineOnBoundary(2);
        } else {
            convertLine();
        }
    }

    private void convertLineOnBoundary(int boundarySide) {
        String boundaryNode = nodeId(boundarySide);
        List<PropertyBag> lines = context.boundary().linesAtNode(boundaryNode);

        // If we have created buses and substations for boundary nodes,
        // convert as regular lines both lines at boundary node
        if (context.config().convertBoundary()) {
            // Convert this line
            convertLine();
            if (lines.size() == 2) {
                // Convert the other line
                PropertyBag other = lines.get(0).getId(CgmesNames.AC_LINE_SEGMENT).equals(id)
                        ? lines.get(1)
                        : lines.get(0);
                new ACLineSegmentConversion(other, context).convertLine();
            }
        } else {
            if (lines.size() == 2) {
                convertMergedLinesAtNode(lines, boundaryNode);
            } else {
                convertDanglingLine(boundarySide);
            }
        }
    }

    private void convertLine() {
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double bch = p.asDouble("bch");
        double gch = p.asDouble("gch", 0.0);

        String busId1 = busId(1);
        String busId2 = busId(2);
        final Line l = context.network().newLine()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setBus1(terminalConnected(1) ? busId1 : null)
                .setBus2(terminalConnected(2) ? busId2 : null)
                .setConnectableBus1(busId1)
                .setConnectableBus2(busId2)
                .setVoltageLevel1(iidmVoltageLevelId(1))
                .setVoltageLevel2(iidmVoltageLevelId(2))
                .setR(r)
                .setX(x)
                .setG1(gch / 2)
                .setG2(gch / 2)
                .setB1(bch / 2)
                .setB2(bch / 2)
                .add();

        convertedTerminals(l.getTerminal1(), l.getTerminal2());
    }

    private void convertDanglingLine(int boundarySide) {
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
        // There should be some equipment at boundarySide to model exchange through that point
        // But we have observed, for the test case conformity/miniBusBranch,
        // that the ACLineSegment:
        // _5150a037-e241-421f-98b2-fe60e5c90303 XQ1-N1
        // ends in a boundary node where there is no other line,
        // does not have energy consumer or equivalent injection
        if (terminalConnected(boundarySide) && !context.boundary().hasPowerFlow(boundaryNode)) {
            missing("Equipment for modeling consumption/injection at boundary node");
        }

        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double bch = p.asDouble("bch");
        double gch = p.asDouble("gch", 0.0);
        boolean connected = terminalConnected(modelSide);
        DanglingLine dl = voltageLevel(modelSide).newDanglingLine()
                .setId(iidmId())
                .setName(iidmName())
                .setEnsureIdUnicity(false)
                .setBus(connected ? busId(modelSide) : null)
                .setConnectableBus(busId(modelSide))
                .setR(r)
                .setX(x)
                .setG(gch)
                .setB(bch)
                .setUcteXnodeCode(findUcteXnodeCode(boundaryNode))
                .setP0(f.p())
                .setQ0(f.q())
                .add();

        convertedTerminal(terminalId(modelSide), dl.getTerminal(), 1, powerFlow(modelSide));

        // If we do not have power flow at model side and we can compute it,
        // do it and assign the result at the terminal of the dangling line
        if (context.config().computeFlowsAtBoundaryDanglingLines()
                && connected
                && !powerFlow(modelSide).defined()
                && context.boundary().hasVoltage(boundaryNode)) {
            double v = context.boundary().vAtBoundary(boundaryNode);
            double angle = context.boundary().angleAtBoundary(boundaryNode);
            // The net sum of power flow "entering" at boundary is "exiting"
            // through the line, we have to change the sign of the sum of flows
            // at the node when we consider flow at line end
            SV svboundary = new SV(-f.p(), -f.q(), v, angle);
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

    private String findUcteXnodeCode(String boundaryNode) {
        return context.boundary().nameAtBoundary(boundaryNode);
    }

    private void convertMergedLinesAtNode(List<PropertyBag> lines, String boundaryNode) {
        PropertyBag other = lines.get(0).getId(CgmesNames.AC_LINE_SEGMENT).equals(id)
                ? lines.get(1)
                : lines.get(0);
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

        boolean mt1connected = terminalConnected(thisEnd);
        boolean mt2connected = otherc.terminalConnected(otherEnd);
        String mbus1 = busId(thisEnd);
        String mbus2 = otherc.busId(otherEnd);

        double lineR = p.asDouble("r");
        double lineX = p.asDouble("x");
        double lineGch = p.asDouble("gch", 0);
        double lineBch = p.asDouble("bch", 0);
        double otherR = other.asDouble("r");
        double otherX = other.asDouble("x");
        double otherGch = other.asDouble("gch", 0);
        double otherBch = other.asDouble("bch", 0);

        String id1 = context.namingStrategy().getId("Line", id);
        String id2 = context.namingStrategy().getId("Line", otherId);
        String name1 = context.namingStrategy().getName("Line", name);
        String name2 = context.namingStrategy().getName("Line", otherName);

        Line mline;
        if (context.config().mergeLinesUsingQuadripole()) {
            PiModel pi1 = new PiModel();
            pi1.r = lineR;
            pi1.x = lineX;
            pi1.g1 = lineGch / 2.0;
            pi1.b1 = lineBch / 2.0;
            pi1.g2 = pi1.g1;
            pi1.b2 = pi1.b1;
            PiModel pi2 = new PiModel();
            pi2.r = otherR;
            pi2.x = otherX;
            pi2.g1 = otherGch / 2.0;
            pi2.b1 = otherBch / 2.0;
            pi2.g2 = pi2.g1;
            pi2.b2 = pi2.b1;
            PiModel pim = Quadripole.from(pi1).cascade(Quadripole.from(pi2)).toPiModel();
            mline = context.network().newLine()
                    .setId(id1 + " + " + id2)
                    .setName(name1 + " + " + name2)
                    .setEnsureIdUnicity(false)
                    .setBus1(mt1connected ? mbus1 : null)
                    .setBus2(mt2connected ? mbus2 : null)
                    .setConnectableBus1(mbus1)
                    .setConnectableBus2(mbus2)
                    .setVoltageLevel1(iidmVoltageLevelId(thisEnd))
                    .setVoltageLevel2(otherc.iidmVoltageLevelId(otherEnd))
                    .setR(pim.r)
                    .setX(pim.x)
                    .setG1(pim.g1)
                    .setG2(pim.g2)
                    .setB1(pim.b1)
                    .setB2(pim.b2)
                    .add();
        } else {
            mline = context.network().newTieLine()
                    .setId(id1 + " + " + id2)
                    .setName(name1 + " + " + name2)
                    .setEnsureIdUnicity(false)
                    .setBus1(mt1connected ? mbus1 : null)
                    .setBus2(mt2connected ? mbus2 : null)
                    .setConnectableBus1(mbus1)
                    .setConnectableBus2(mbus2)
                    .setVoltageLevel1(iidmVoltageLevelId(thisEnd))
                    .setVoltageLevel2(otherc.iidmVoltageLevelId(otherEnd))
                    .line1()
                    .setId(id1)
                    .setName(name1)
                    .setR(lineR)
                    .setX(lineX)
                    .setG1(lineGch / 2)
                    .setG2(lineGch / 2)
                    .setB1(lineBch / 2)
                    .setB2(lineBch / 2)
                    .setXnodeP(0)
                    .setXnodeQ(0)
                    .line2()
                    .setId(id2)
                    .setName(name2)
                    .setR(otherR)
                    .setX(otherX)
                    .setG1(otherGch / 2)
                    .setG2(otherGch / 2)
                    .setB1(otherBch / 2)
                    .setB2(otherBch / 2)
                    .setXnodeP(0)
                    .setXnodeQ(0)
                    .setUcteXnodeCode(findUcteXnodeCode(boundaryNode))
                    .add();
        }

        convertedTerminal(terminalId(thisEnd), mline.getTerminal1(), 1, powerFlow(thisEnd));
        convertedTerminal(
                otherc.terminalId(otherEnd),
                mline.getTerminal2(),
                2,
                otherc.powerFlow(otherEnd));
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
