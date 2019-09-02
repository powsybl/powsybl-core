/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.TieLine;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineImpl extends LineImpl implements TieLine {

    static class HalfLineImpl implements HalfLine {

        String id;
        String name;
        double xnodeP = Double.NaN;
        double xnodeQ = Double.NaN;
        double r = Double.NaN;
        double x = Double.NaN;
        double g1 = Double.NaN;
        double g2 = Double.NaN;
        double b1 = Double.NaN;
        double b2 = Double.NaN;

        @Override
        public String getId() {
            return id;
        }

        void setId(String id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name == null ? id : name;
        }

        void setName(String name) {
            this.name = name;
        }

        @Override
        public double getXnodeP() {
            return xnodeP;
        }

        @Override
        public HalfLineImpl setXnodeP(double xnodeP) {
            this.xnodeP = xnodeP;
            return this;
        }

        @Override
        public double getXnodeQ() {
            return xnodeQ;
        }

        @Override
        public HalfLineImpl setXnodeQ(double xnodeQ) {
            this.xnodeQ = xnodeQ;
            return this;
        }

        @Override
        public double getR() {
            return r;
        }

        @Override
        public HalfLineImpl setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public HalfLineImpl setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public double getG1() {
            return g1;
        }

        @Override
        public HalfLineImpl setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        @Override
        public double getG2() {
            return g2;
        }

        @Override
        public HalfLineImpl setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        @Override
        public double getB1() {
            return b1;
        }

        @Override
        public HalfLineImpl setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        @Override
        public double getB2() {
            return b2;
        }

        @Override
        public HalfLineImpl setB2(double b2) {
            this.b2 = b2;
            return this;
        }
    }

    private String ucteXnodeCode;

    private final HalfLineImpl half1;

    private final HalfLineImpl half2;

    TieLineImpl(String id, String name, String ucteXnodeCode, HalfLineImpl half1, HalfLineImpl half2) {
        super(id, name, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        this.ucteXnodeCode = ucteXnodeCode;
        this.half1 = half1;
        this.half2 = half2;
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
    public double getR() {
        return half1.getR() + half2.getR();
    }

    private ValidationException createNotSupportedForTieLines() {
        return new ValidationException(this, "direct modification of characteristics not supported for tie lines");
    }

    @Override
    public LineImpl setR(double r) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getX() {
        return half1.getX() + half2.getX();
    }

    @Override
    public LineImpl setX(double x) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG1() {
        return half1.getG1() + half2.getG1();
    }

    @Override
    public LineImpl setG1(double g1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB1() {
        return half1.getB1() + half2.getB1();
    }

    @Override
    public LineImpl setB1(double b1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG2() {
        return half1.getG2() + half2.getG2();
    }

    @Override
    public LineImpl setG2(double g2) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB2() {
        return half1.getB2() + half2.getB2();
    }

    @Override
    public LineImpl setB2(double b2) {
        throw createNotSupportedForTieLines();
    }
}
