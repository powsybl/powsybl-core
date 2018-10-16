/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.ext;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class TieLineExt extends AbstractExtension<Line> {

    private final Line line;

    private String ucteXnodeCode;

    private final HalfLine half1;

    private final HalfLine half2;

    public TieLineExt(Line line, String ucteXnodeCode, TieLineExt.HalfLineImpl half1, TieLineExt.HalfLineImpl half2) {
        this.line = Objects.requireNonNull(line);
        this.ucteXnodeCode = ucteXnodeCode;
        this.half1 = checkHalfLine(half1, 1);
        this.half2 = checkHalfLine(half2, 2);
    }

    public TieLineExt(Line line, String ucteXnodeCode, TieLine.HalfLine half1, TieLine.HalfLine half2) {
        this(line, ucteXnodeCode, convert(half1), convert(half2));
    }

    private static TieLineExt.HalfLineImpl convert(TieLine.HalfLine half) {
        HalfLineImpl halfLine = new HalfLineImpl();
        halfLine.setId(half.getId())
                .setName(half.getName())
                .setR(half.getR())
                .setX(half.getX())
                .setG1(half.getG1())
                .setB1(half.getB1())
                .setG2(half.getG2())
                .setB2(half.getB2())
                .setXnodeP(half.getXnodeP())
                .setXnodeQ(half.getXnodeQ());
        return halfLine;
    }

    private HalfLine checkHalfLine(HalfLine halfLine, int side) {
        if (halfLine.getId() == null) {
            throw new PowsyblException("id is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getR())) {
            throw new PowsyblException("r is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getX())) {
            throw new PowsyblException("x is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getG1())) {
            throw new PowsyblException("g1 is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getB1())) {
            throw new PowsyblException("b1 is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getG2())) {
            throw new PowsyblException("g2 is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getB2())) {
            throw new PowsyblException("b2 is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getXnodeP())) {
            throw new PowsyblException("xnodeP is not set for half line " + side);
        }
        if (Double.isNaN(halfLine.getXnodeQ())) {
            throw new PowsyblException("xnodeQ is not set for half line " + side);
        }
        return halfLine;
    }

    @Override
    public String getName() {
        return "tieLine";
    }

    @Override
    public Line getExtendable() {
        return this.line;
    }

    /**
     * Get the UCTE Xnode code corresponding to this line in the case where the
     * line is a boundary, return null otherwise.
     */
    public String getUcteXnodeCode() {
        return ucteXnodeCode;
    }

    /**
     * Get first half of the line characteristics
     */
    public HalfLine getHalf1() {
        return half1;
    }

    /**
     * Get second half of the line characteristics
     */
    public HalfLine getHalf2() {
        return half2;
    }

    public double getR() {
        return half1.getR() + half2.getR();
    }


    public double getX() {
        return half1.getX() + half2.getX();
    }


    public double getG1() {
        return half1.getG1() + half2.getG1();
    }


    public double getB1() {
        return half1.getB1() + half2.getB1();
    }


    public double getG2() {
        return half1.getG2() + half2.getG2();
    }

    public double getB2() {
        return half1.getB2() + half2.getB2();
    }

    public interface HalfLine extends LineCharacteristics<HalfLine> {

        String getId();

        String getName();

        /**
         * Get Xnode active power consumption in MW.
         */
        double getXnodeP();

        /**
         * Set Xnode active power consumption in MW.
         */
        HalfLine setXnodeP(double p);

        /**
         * Get Xnode reactive power consumption in MVar.
         */
        double getXnodeQ();

        /**
         * Set Xnode reactive power consumption in MVar.
         */
        HalfLine setXnodeQ(double q);

    }

    public static class HalfLineImpl implements HalfLine {

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

        public HalfLineImpl setId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public String getName() {
            return name == null ? id : name;
        }

        public HalfLineImpl setName(String name) {
            this.name = name;
            return this;
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

}
