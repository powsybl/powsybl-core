/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;

import java.util.Objects;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public final class MergedXnodeImpl extends AbstractExtension<Line> implements MergedXnode {

    private float rdp; // r divider position 1 -> 2

    private float xdp; // x divider position 1 -> 2

    private String line1Name;

    private boolean line1Fictitious;

    private double xnodeP1;

    private double xnodeQ1;

    private float b1dp; // b1 divider position 1 -> 2

    private float g1dp; // g1 divider position 1 -> 2

    private String line2Name;

    private boolean line2Fictitious;

    private double xnodeP2;

    private double xnodeQ2;

    private float b2dp; // b2 divider position 1 -> 2

    private float g2dp; // g2 divider position 1 -> 2

    private String code;

    static class MergedXnodeBuilder {
        MergedXnodeImpl mergedXnode;

        MergedXnodeBuilder(Line line) {
            this.mergedXnode = new MergedXnodeImpl(line);
        }

        MergedXnode build() {
            checkDividerPosition(mergedXnode.rdp);
            checkDividerPosition(mergedXnode.xdp);
            Objects.requireNonNull(mergedXnode.line1Name);
            checkPowerFlow(mergedXnode.xnodeP1);
            checkPowerFlow(mergedXnode.xnodeQ1);
            checkDividerPosition(mergedXnode.b1dp);
            checkDividerPosition(mergedXnode.g1dp);
            Objects.requireNonNull(mergedXnode.line2Name);
            checkPowerFlow(mergedXnode.xnodeP2);
            checkPowerFlow(mergedXnode.xnodeQ2);
            checkDividerPosition(mergedXnode.b2dp);
            checkDividerPosition(mergedXnode.g2dp);
            Objects.requireNonNull(mergedXnode.code);
            return mergedXnode;
        }

        MergedXnodeBuilder setRdp(float rdp) {
            mergedXnode.rdp = rdp;
            return this;
        }

        MergedXnodeBuilder setXdp(float xdp) {
            mergedXnode.xdp = xdp;
            return this;
        }

        MergedXnodeBuilder setLine1Name(String line1Name) {
            mergedXnode.line1Name = line1Name;
            return this;
        }

        MergedXnodeBuilder setLine1Fictitious(boolean line1Fictitious) {
            mergedXnode.line1Fictitious = line1Fictitious;
            return this;
        }

        MergedXnodeBuilder setXnodeP1(double xnodeP1) {
            mergedXnode.xnodeP1 = xnodeP1;
            return this;
        }

        MergedXnodeBuilder setXnodeQ1(double xnodeQ1) {
            mergedXnode.xnodeQ1 = xnodeQ1;
            return this;
        }

        MergedXnodeBuilder setB1dp(float b1dp) {
            mergedXnode.b1dp = b1dp;
            return this;
        }

        MergedXnodeBuilder setG1dp(float g1dp) {
            mergedXnode.g1dp = g1dp;
            return this;
        }

        MergedXnodeBuilder setLine2Name(String line2Name) {
            mergedXnode.line2Name = line2Name;
            return this;
        }

        MergedXnodeBuilder setLine2Fictitious(boolean line2Fictitious) {
            mergedXnode.line2Fictitious = line2Fictitious;
            return this;
        }

        MergedXnodeBuilder setXnodeP2(double xnodeP2) {
            mergedXnode.xnodeP2 = xnodeP2;
            return this;
        }

        MergedXnodeBuilder setXnodeQ2(double xnodeQ2) {
            mergedXnode.xnodeQ2 = xnodeQ2;
            return this;
        }

        MergedXnodeBuilder setB2dp(float b2dp) {
            mergedXnode.b2dp = b2dp;
            return this;
        }

        MergedXnodeBuilder setG2dp(float g2dp) {
            mergedXnode.g2dp = g2dp;
            return this;
        }

        MergedXnodeBuilder setCode(String code) {
            mergedXnode.code = code;
            return this;
        }
    }

    private static float checkDividerPosition(float dp) {
        if (dp < 0f || dp > 1f || Double.isNaN(dp)) {
            throw new IllegalArgumentException("Invalid divider postion: " + dp);
        }
        return dp;
    }

    private static double checkPowerFlow(double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Power flow is invalid");
        }
        return value;
    }

    private MergedXnodeImpl(Line line) {
        super(line);
    }

    @Override
    public float getRdp() {
        return rdp;
    }

    @Override
    public MergedXnodeImpl setRdp(float rdp) {
        this.rdp = checkDividerPosition(rdp);
        return this;
    }

    @Override
    public float getXdp() {
        return xdp;
    }

    @Override
    public MergedXnodeImpl setXdp(float xdp) {
        this.xdp = checkDividerPosition(xdp);
        return this;
    }

    @Override
    public String getLine1Name() {
        return line1Name;
    }

    @Override
    public MergedXnodeImpl setLine1Name(String line1Name) {
        this.line1Name = Objects.requireNonNull(line1Name);
        return this;
    }

    @Override
    public boolean isLine1Fictitious() {
        return line1Fictitious;
    }

    @Override
    public MergedXnodeImpl setLine1Fictitious(boolean line1Fictitious) {
        this.line1Fictitious = line1Fictitious;
        return this;
    }

    @Override
    public double getXnodeP1() {
        return xnodeP1;
    }

    @Override
    public MergedXnodeImpl setXnodeP1(double xnodeP1) {
        this.xnodeP1 = checkPowerFlow(xnodeP1);
        return this;
    }

    @Override
    public double getXnodeQ1() {
        return xnodeQ1;
    }

    @Override
    public MergedXnodeImpl setXnodeQ1(double xnodeQ1) {
        this.xnodeQ1 = checkPowerFlow(xnodeQ1);
        return this;
    }

    @Override
    public MergedXnodeImpl setB1dp(float b1dp) {
        this.b1dp = checkDividerPosition(b1dp);
        return this;
    }

    @Override
    public float getB1dp() {
        return b1dp;
    }

    @Override
    public MergedXnodeImpl setG1dp(float g1dp) {
        this.g1dp = checkDividerPosition(g1dp);
        return this;
    }

    @Override
    public float getG1dp() {
        return g1dp;
    }

    @Override
    public String getLine2Name() {
        return line2Name;
    }

    @Override
    public MergedXnodeImpl setLine2Name(String line2Name) {
        this.line2Name = Objects.requireNonNull(line2Name);
        return this;
    }

    @Override
    public boolean isLine2Fictitious() {
        return line2Fictitious;
    }

    @Override
    public MergedXnodeImpl setLine2Fictitious(boolean line2Fictitious) {
        this.line2Fictitious = line2Fictitious;
        return this;
    }

    @Override
    public double getXnodeP2() {
        return xnodeP2;
    }

    @Override
    public MergedXnodeImpl setXnodeP2(double xnodeP2) {
        this.xnodeP2 = checkPowerFlow(xnodeP2);
        return this;
    }

    @Override
    public double getXnodeQ2() {
        return xnodeQ2;
    }

    @Override
    public MergedXnodeImpl setXnodeQ2(double xnodeQ2) {
        this.xnodeQ2 = checkPowerFlow(xnodeQ2);
        return this;
    }

    @Override
    public MergedXnodeImpl setB2dp(float b2dp) {
        this.b2dp = checkDividerPosition(b2dp);
        return this;
    }

    @Override
    public float getB2dp() {
        return b2dp;
    }

    @Override
    public MergedXnodeImpl setG2dp(float g2dp) {
        this.g2dp = checkDividerPosition(g2dp);
        return this;
    }

    @Override
    public float getG2dp() {
        return g2dp;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public MergedXnodeImpl setCode(String code) {
        this.code = Objects.requireNonNull(code);
        return this;
    }
}
