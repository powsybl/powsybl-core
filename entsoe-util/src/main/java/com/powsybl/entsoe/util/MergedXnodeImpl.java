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

    private float rdp; // r divider position 1 -> 2

    private float xdp; // x divider position 1 -> 2

    private double xnodeP1;

    private double xnodeQ1;

    private double xnodeP2;

    private double xnodeQ2;

    private String line1Name;

    private boolean line1Fictitious;

    private double line1G1;

    private double line1B1;

    private double line1G2;

    private String line2Name;

    private boolean line2Fictitious;

    private double line1B2;

    private double line2G1;

    private double line2B1;

    private double line2G2;

    private double line2B2;

    private String code;

    public MergedXnodeImpl(Line line, float rdp, float xdp, double xnodeP1, double xnodeQ1, double xnodeP2, double xnodeQ2,
                           String line1Name, boolean line1Fictitious, double line1B1, double line1B2, double line1G1, double line1G2,
                           String line2Name, boolean line2Fictitious, double line2B1, double line2B2, double line2G1, double line2G2,
                           String code) {
        super(line);
        this.rdp = checkDividerPosition(rdp);
        this.xdp = checkDividerPosition(xdp);
        this.xnodeP1 = checkPowerFlow(xnodeP1);
        this.xnodeQ1 = checkPowerFlow(xnodeQ1);
        this.xnodeP2 = checkPowerFlow(xnodeP2);
        this.xnodeQ2 = checkPowerFlow(xnodeQ2);
        this.line1Name = Objects.requireNonNull(line1Name);
        this.line1Fictitious = line1Fictitious;
        this.line1B1 = checkValue(line1B1);
        this.line1B2 = checkValue(line1B2);
        this.line1G1 = checkValue(line1G1);
        this.line1G2 = checkValue(line1G2);
        this.line2Name = Objects.requireNonNull(line2Name);
        this.line2Fictitious = line2Fictitious;
        this.line2B1 = checkValue(line2B1);
        this.line2B2 = checkValue(line2B2);
        this.line2G1 = checkValue(line2G1);
        this.line2G2 = checkValue(line2G2);
        this.code = Objects.requireNonNull(code);
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

    private static double checkValue(double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Value is undefined");
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
    public double getLine1G1() {
        return line1G1;
    }

    @Override
    public MergedXnodeImpl setLine1G1(double line1G1) {
        this.line1G1 = checkValue(line1G1);
        return this;
    }

    @Override
    public double getLine1B1() {
        return line1B1;
    }

    @Override
    public MergedXnodeImpl setLine1B1(double line1B1) {
        this.line1B1 = checkValue(line1B1);
        return this;
    }

    @Override
    public double getLine1G2() {
        return line1G2;
    }

    @Override
    public MergedXnodeImpl setLine1G2(double line1G2) {
        this.line1G2 = checkValue(line1G2);
        return this;
    }

    @Override
    public double getLine1B2() {
        return line1B2;
    }

    @Override
    public MergedXnodeImpl setLine1B2(double line1B2) {
        this.line1B2 = checkValue(line1B2);
        return this;
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
    public double getLine2G1() {
        return line2G1;
    }

    @Override
    public MergedXnodeImpl setLine2G1(double line2G1) {
        this.line2G1 = checkValue(line2G1);
        return this;
    }

    @Override
    public double getLine2B1() {
        return line2B1;
    }

    @Override
    public MergedXnodeImpl setLine2B1(double line2B1) {
        this.line2B1 = checkValue(line2B1);
        return this;
    }

    @Override
    public double getLine2G2() {
        return line2G2;
    }

    @Override
    public MergedXnodeImpl setLine2G2(double line2G2) {
        this.line2G2 = checkValue(line2G2);
        return this;
    }

    @Override
    public double getLine2B2() {
        return line2B2;
    }

    @Override
    public MergedXnodeImpl setLine2B2(double line2B2) {
        this.line2B2 = checkValue(line2B2);
        return this;
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
