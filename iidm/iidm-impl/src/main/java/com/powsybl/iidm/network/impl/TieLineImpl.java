/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;

/**
 * A tie line is constituted of two half lines, halfLine1 and halfLine2.
 * <p>
 * The tie line is always oriented in the same way, <br>
 * networkModelNode at end1 of halfLine1 - boundaryNode at end2 of halfLine1 -
 * bondaryNode at end1 of halfLine2 - networkModelNode ant end2 of halfLine2 <br>
 * <p>
 * Each halfLine has the attribute originalBoundarySide that defines how the initial line
 * and the parameters are orientated. <br>
 * If boundary.originalBoundarySide is ONE means that the initial line is
 * boundaryNode - networkModelNode and the parameters are defined from boundaryNode
 * to networkModelNode. <br>
 * If boundary.originalBoundarySide is TWO means that the initial line is
 * networkModelNode - boundaryNode and the parameters are defined from networkModelNode to
 * boundaryNode.
 * <p>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TieLineImpl extends LineImpl implements TieLine {

    static class HalfLineImpl implements HalfLine {
        private final String id;
        private final String name;
        private boolean fictitious;
        private double r;
        private double x;
        private double g1;
        private double g2;
        private double b1;
        private double b2;

        private final HalfLineBoundaryImpl boundary;

        TieLineImpl parent;

        HalfLineImpl(String id, String name, boolean fictitious, double r, double x, double g1, double b1, double g2,
            double b2, Branch.Side side, Branch.Side originalBoundarySide) {
            this.id = Objects.requireNonNull(id);
            this.name = name;
            this.fictitious = fictitious;
            this.r = r;
            this.x = x;
            this.g1 = g1;
            this.b1 = b1;
            this.g2 = g2;
            this.b2 = b2;
            this.boundary = new HalfLineBoundaryImpl(this, side, originalBoundarySide);
        }

        TieLineImpl getParent() {
            return parent;
        }

        private void setParent(TieLineImpl parent) {
            this.parent = parent;
        }

        private void notifyUpdate(String attribute, Object oldValue, Object newValue) {
            if (Objects.nonNull(parent)) {
                parent.notifyUpdate(() -> getHalfLineAttribute() + "." + attribute, oldValue, newValue);
            }
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name == null ? id : name;
        }

        @Override
        public double getR() {
            return r;
        }

        @Override
        public HalfLineImpl setR(double r) {
            double oldValue = this.r;
            this.r = r;
            notifyUpdate("r", oldValue, r);
            return this;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public HalfLineImpl setX(double x) {
            double oldValue = this.x;
            this.x = x;
            notifyUpdate("x", oldValue, x);
            return this;
        }

        @Override
        public double getG1() {
            return g1;
        }

        @Override
        public HalfLineImpl setG1(double g1) {
            double oldValue = this.g1;
            this.g1 = g1;
            notifyUpdate("g1", oldValue, g1);
            return this;
        }

        @Override
        public double getG2() {
            return g2;
        }

        @Override
        public HalfLineImpl setG2(double g2) {
            double oldValue = this.g2;
            this.g2 = g2;
            notifyUpdate("g2", oldValue, g2);
            return this;
        }

        @Override
        public double getB1() {
            return b1;
        }

        @Override
        public HalfLineImpl setB1(double b1) {
            double oldValue = this.b1;
            this.b1 = b1;
            notifyUpdate("b1", oldValue, b1);
            return this;
        }

        @Override
        public double getB2() {
            return b2;
        }

        @Override
        public HalfLineImpl setB2(double b2) {
            double oldValue = this.b2;
            this.b2 = b2;
            notifyUpdate("b2", oldValue, b2);
            return this;
        }

        @Override
        public HalfLineBoundaryImpl getBoundary() {
            return boundary;
        }

        @Override
        public boolean isFictitious() {
            return fictitious;
        }

        @Override
        public HalfLineImpl setFictitious(boolean fictitious) {
            boolean oldValue = this.fictitious;
            this.fictitious = fictitious;
            notifyUpdate("fictitious", oldValue, fictitious);
            return this;
        }

        private String getHalfLineAttribute() {
            return this == parent.half1 ? "half1" : "half2";
        }
    }

    private final String ucteXnodeCode;

    private final HalfLineImpl half1;

    private final HalfLineImpl half2;

    TieLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, String ucteXnodeCode, HalfLineImpl half1, HalfLineImpl half2) {
        super(network, id, name, fictitious, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        this.ucteXnodeCode = ucteXnodeCode;
        this.half1 = attach(half1);
        this.half2 = attach(half2);
    }

    private HalfLineImpl attach(HalfLineImpl half) {
        half.setParent(this);
        return half;
    }

    @Override
    public boolean isTieLine() {
        return true;
    }

    @Override
    public String getUcteXnodeCode() {
        return ucteXnodeCode;
    }

    @Override
    public HalfLineImpl getHalf1() {
        return half1;
    }

    @Override
    public HalfLineImpl getHalf2() {
        return half2;
    }

    @Override
    public HalfLineImpl getHalf(Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unknown branch side " + side);
        }
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getR() {
        LinkData.BranchAdmittanceMatrix adm = equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y12().negate().reciprocal().getReal();
    }

    private ValidationException createNotSupportedForTieLines() {
        return new ValidationException(this, "direct modification of characteristics not supported for tie lines");
    }

    @Override
    public LineImpl setR(double r) {
        throw createNotSupportedForTieLines();
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getX() {
        LinkData.BranchAdmittanceMatrix adm = equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y12().negate().reciprocal().getImaginary();
    }

    @Override
    public LineImpl setX(double x) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG1() {
        LinkData.BranchAdmittanceMatrix adm = equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y11().add(adm.y12()).getReal();
    }

    @Override
    public LineImpl setG1(double g1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB1() {
        LinkData.BranchAdmittanceMatrix adm = equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y11().add(adm.y12()).getImaginary();
    }

    @Override
    public LineImpl setB1(double b1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG2() {
        LinkData.BranchAdmittanceMatrix adm = equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y22().add(adm.y21()).getReal();
    }

    @Override
    public LineImpl setG2(double g2) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB2() {
        LinkData.BranchAdmittanceMatrix adm = equivalentBranchAdmittanceMatrix(half1, half2);
        return adm.y22().add(adm.y21()).getImaginary();
    }

    @Override
    public LineImpl setB2(double b2) {
        throw createNotSupportedForTieLines();
    }

    // zero impedance half lines should be supported
    private static LinkData.BranchAdmittanceMatrix equivalentBranchAdmittanceMatrix(HalfLineImpl half1,
        HalfLineImpl half2) {

        BranchAdmittanceMatrix adm1 = LinkData.calculateBranchAdmittance(half1.getR(), half1.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(half1.getG1(), half1.getB1()), new Complex(half1.getG2(), half1.getB2()));
        BranchAdmittanceMatrix adm2 = LinkData.calculateBranchAdmittance(half2.getR(), half2.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(half2.getG1(), half2.getB1()), new Complex(half2.getG2(), half2.getB2()));

        if (zeroImpedanceLine(adm1)) {
            return adm2;
        } else if (zeroImpedanceLine(adm2)) {
            return adm1;
        } else {
            return LinkData.kronChain(adm1, half1.boundary.getOriginalBoundarySide(),
                adm2, half2.boundary.getOriginalBoundarySide());
        }
    }

    private static boolean zeroImpedanceLine(BranchAdmittanceMatrix adm) {
        if (adm.y12().getReal() == 0.0 && adm.y12().getImaginary() == 0.0) {
            return true;
        } else {
            return adm.y21().getReal() == 0.0 && adm.y22().getImaginary() == 0.0;
        }
    }
}
