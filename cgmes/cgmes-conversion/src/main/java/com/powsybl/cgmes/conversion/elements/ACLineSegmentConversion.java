/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesLineBoundaryNodeAdder;
import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.iidm.network.*;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ACLineSegmentConversion extends AbstractBranchConversion implements EquipmentAtBoundaryConversion {

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
        convertLine();
    }

    @Override
    public void convertAtBoundary() {
        if (isBoundary(1)) {
            convertLineAtBoundary(1);
        } else if (isBoundary(2)) {
            convertLineAtBoundary(2);
        } else {
            throw new ConversionException("Boundary must be at one end of the line");
        }
    }

    @Override
    public BoundaryLine asBoundaryLine(String boundaryNode) {
        BoundaryLine boundaryLine = super.createBoundaryLine(boundaryNode);
        double r = p.asDouble("r");
        double x = p.asDouble("x");
        double g = p.asDouble("gch", 0);
        double b = p.asDouble("bch", 0);
        boundaryLine.setParameters(r, x, g / 2, b / 2, g / 2, b / 2);
        return boundaryLine;
    }

    public static void convertBoundaryLines(Context context, String boundaryNode, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        Line mline;
        if (context.config().mergeBoundariesUsingTieLines()) {
            mline = createTieLine(context, boundaryNode, boundaryLine1, boundaryLine2);
        } else {
            mline = createLine(context, boundaryLine1, boundaryLine2);
        }

        mline.addAlias(boundaryLine1.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1);
        mline.addAlias(boundaryLine1.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF1." + CgmesNames.TERMINAL + "_BOUNDARY");
        mline.addAlias(boundaryLine2.getModelTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2);
        mline.addAlias(boundaryLine2.getBoundaryTerminalId(), Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "HALF2." + CgmesNames.TERMINAL + "_BOUNDARY");

        context.convertedTerminal(boundaryLine1.getModelTerminalId(), mline.getTerminal1(), 1, boundaryLine1.getModelPowerFlow());
        context.convertedTerminal(boundaryLine2.getModelTerminalId(), mline.getTerminal2(), 2, boundaryLine2.getModelPowerFlow());

        if (mline instanceof TieLine) {
            TieLine tl = (TieLine) mline;
            context.terminalMapping().add(boundaryLine1.getBoundaryTerminalId(), tl.getHalf1().getBoundary(), 2);
            context.terminalMapping().add(boundaryLine2.getBoundaryTerminalId(), tl.getHalf2().getBoundary(), 1);
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

    // TODO support transformer + Line
    private static Line createTieLine(Context context, String boundaryNode, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        TieLineAdder adder = context.network().newTieLine()
            .setId(boundaryLine1.getId() + " + " + boundaryLine2.getId())
            .setName(boundaryLine1.getName() + " + " + boundaryLine2.getName())
            .newHalfLine1(boundaryLine1.getBoundarySide())
                .setId(boundaryLine1.getId())
                .setName(boundaryLine1.getName())
                .setR(boundaryLine1.getR())
                .setX(boundaryLine1.getX())
                .setG1(boundaryLine1.getG1())
                .setG2(boundaryLine1.getG2())
                .setB1(boundaryLine1.getB1())
                .setB2(boundaryLine1.getB2())
                .add()
            .newHalfLine2(boundaryLine2.getBoundarySide())
                .setId(boundaryLine2.getId())
                .setName(boundaryLine2.getName())
                .setR(boundaryLine2.getR())
                .setX(boundaryLine2.getX())
                .setG1(boundaryLine2.getG1())
                .setG2(boundaryLine2.getG2())
                .setB1(boundaryLine2.getB1())
                .setB2(boundaryLine2.getB2())
                .add()
            .setUcteXnodeCode(findUcteXnodeCode(context, boundaryNode));
        identify(context, adder, boundaryLine1.getId() + " + " + boundaryLine2.getId(), boundaryLine1.getName() + " + " + boundaryLine2.getName());
        connect(context, adder, boundaryLine1.getModelIidmVoltageLevelId(), boundaryLine1.getModelBus(), boundaryLine1.isModelTconnected(),
            boundaryLine1.getModelNode(), boundaryLine2.getModelIidmVoltageLevelId(), boundaryLine2.getModelBus(),
            boundaryLine2.isModelTconnected(), boundaryLine2.getModelNode());
        TieLine tieLine = adder.add();
        if (context.boundary().isHvdc(boundaryNode) || context.boundary().lineAtBoundary(boundaryNode) != null) {
            tieLine.newExtension(CgmesLineBoundaryNodeAdder.class)
                    .setHvdc(context.boundary().isHvdc(boundaryNode))
                    .setLineEnergyIdentificationCodeEic(context.boundary().lineAtBoundary(boundaryNode))
                    .add();
        }
        return tieLine;
    }

    // TODO support transformer + Line
    private static Line createLine(Context context, BoundaryLine boundaryLine1, BoundaryLine boundaryLine2) {
        PiModel pi1 = new PiModel();
        pi1.r = boundaryLine1.getR();
        pi1.x = boundaryLine1.getX();
        pi1.g1 = boundaryLine1.getG1();
        pi1.b1 = boundaryLine1.getB1();
        pi1.g2 = boundaryLine1.getG2();
        pi1.b2 = boundaryLine1.getB2();
        PiModel pi2 = new PiModel();
        pi2.r = boundaryLine2.getR();
        pi2.x = boundaryLine2.getX();
        pi2.g1 = boundaryLine2.getG1();
        pi2.b1 = boundaryLine2.getB1();
        pi2.g2 = boundaryLine2.getG2();
        pi2.b2 = boundaryLine2.getB2();
        PiModel pim = Quadripole.from(pi1).cascade(Quadripole.from(pi2)).toPiModel();
        LineAdder adder = context.network().newLine()
            .setR(pim.r)
            .setX(pim.x)
            .setG1(pim.g1)
            .setG2(pim.g2)
            .setB1(pim.b1)
            .setB2(pim.b2);
        identify(context, adder, boundaryLine1.getId() + " + " + boundaryLine2.getId(), boundaryLine1.getName() + " + " + boundaryLine2.getName());
        connect(context, adder, boundaryLine1.getModelIidmVoltageLevelId(), boundaryLine1.getModelBus(), boundaryLine1.isModelTconnected(),
            boundaryLine1.getModelNode(), boundaryLine2.getModelIidmVoltageLevelId(), boundaryLine2.getModelBus(),
            boundaryLine2.isModelTconnected(), boundaryLine2.getModelNode());
        return adder.add();
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
