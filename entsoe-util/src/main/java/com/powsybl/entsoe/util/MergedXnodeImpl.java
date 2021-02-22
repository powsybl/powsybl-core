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
public class MergedXnodeImpl extends AbstractExtension<Line> implements MergedXnode {

    private float rdp = Float.NaN; // r divider position 1 -> 2

    private float xdp = Float.NaN; // x divider position 1 -> 2

    private String line1Name;

    private boolean line1Fictitious = false;

    private double xnodeP1;

    private double xnodeQ1;

    private float b1dp = Float.NaN; // b1 divider position 1 -> 2

    private float g1dp = Float.NaN; // g1 divider position 1 -> 2

    private String line2Name;

    private boolean line2Fictitious = false;

    private double xnodeP2;

    private double xnodeQ2;

    private float b2dp = Float.NaN; // b2 divider position 1 -> 2

    private float g2dp = Float.NaN; // g2 divider position 1 -> 2

    private String code;

    public MergedXnodeImpl(Line line, float rdp, float xdp, double xnodeP1, double xnodeQ1, double xnodeP2, double xnodeQ2,
                           String line1Name, String line2Name, String code) {
        super(line);
        this.rdp = checkDividerPosition(rdp);
        this.xdp = checkDividerPosition(xdp);
        this.line1Name = Objects.requireNonNull(line1Name);
        this.xnodeP1 = checkPowerFlow(xnodeP1);
        this.xnodeQ1 = checkPowerFlow(xnodeQ1);
        this.line2Name = Objects.requireNonNull(line2Name);
        this.xnodeP2 = checkPowerFlow(xnodeP2);
        this.xnodeQ2 = checkPowerFlow(xnodeQ2);
        this.code = Objects.requireNonNull(code);
    }

    public MergedXnodeImpl(Line line, float rdp, float xdp,
                           String line1Name, boolean line1Fictitious, double xnodeP1, double xnodeQ1, float b1dp, float g1dp,
                           String line2Name, boolean line2Fictitious, double xnodeP2, double xnodeQ2, float b2dp, float g2dp,
                           String code) {
        this(line, rdp, xdp, xnodeP1, xnodeQ1, xnodeP2, xnodeQ2, line1Name, line2Name, code);
        this.line1Fictitious = line1Fictitious;
        this.b1dp = checkDividerPosition(b1dp);
        this.g1dp = checkDividerPosition(g1dp);
        this.line2Fictitious = line2Fictitious;
        this.b2dp = checkDividerPosition(b2dp);
        this.g2dp = checkDividerPosition(g2dp);
    }

    private float checkDividerPosition(float dp) {
        if (Float.isNaN(dp)) {
            return 0.5f;
        }
        if (dp < 0f || dp > 1f) {
            throw new IllegalArgumentException("Invalid divider position: " + dp);
        }
        return dp;
    }

    private static double checkPowerFlow(double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Power flow is invalid");
        }
        return value;
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
