/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MergedXnode extends AbstractExtension<Line> {

    private float rdp; // r divider position 1 -> 2

    private float xdp; // x divider position 1 -> 2

    private double xnodeP1;

    private double xnodeQ1;

    private double xnodeP2;

    private double xnodeQ2;

    private String line1Name;

    private String line2Name;

    private String code;

    public MergedXnode(Line line, float rdp, float xdp, double xnodeP1, double xnodeQ1, double xnodeP2, double xnodeQ2,
                       String line1Name, String line2Name, String code) {
        super(line);
        this.rdp = checkDividerPosition(rdp);
        this.xdp = checkDividerPosition(xdp);
        this.xnodeP1 = checkPowerFlow(xnodeP1);
        this.xnodeQ1 = checkPowerFlow(xnodeQ1);
        this.xnodeP2 = checkPowerFlow(xnodeP2);
        this.xnodeQ2 = checkPowerFlow(xnodeQ2);
        this.line1Name = Objects.requireNonNull(line1Name);
        this.line2Name = Objects.requireNonNull(line2Name);
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

    @Override
    public String getName() {
        return "mergedXnode";
    }

    public float getRdp() {
        return rdp;
    }

    public MergedXnode setRdp(float rdp) {
        this.rdp = checkDividerPosition(rdp);
        return this;
    }

    public float getXdp() {
        return xdp;
    }

    public MergedXnode setXdp(float xdp) {
        this.xdp = checkDividerPosition(xdp);
        return this;
    }

    public double getXnodeP1() {
        return xnodeP1;
    }

    public MergedXnode setXnodeP1(double xNodeP1) {
        this.xnodeP1 = checkPowerFlow(xNodeP1);
        return this;
    }

    public double getXnodeQ1() {
        return xnodeQ1;
    }

    public MergedXnode setXnodeQ1(double xNodeQ1) {
        this.xnodeQ1 = checkPowerFlow(xNodeQ1);
        return this;
    }

    public double getXnodeP2() {
        return xnodeP2;
    }

    public MergedXnode setXnodeP2(double xNodeP2) {
        this.xnodeP2 = checkPowerFlow(xNodeP2);
        return this;
    }

    public double getXnodeQ2() {
        return xnodeQ2;
    }

    public MergedXnode setXnodeQ2(double xNodeQ2) {
        this.xnodeQ2 = checkPowerFlow(xNodeQ2);
        return this;
    }

    public String getLine1Name() {
        return line1Name;
    }

    public MergedXnode setLine1Name(String line1Name) {
        this.line1Name = Objects.requireNonNull(line1Name);
        return this;
    }

    public String getLine2Name() {
        return line2Name;
    }

    public MergedXnode setLine2Name(String line2Name) {
        this.line2Name = Objects.requireNonNull(line2Name);
        return this;
    }

    public String getCode() {
        return code;
    }

    public MergedXnode setCode(String xNodeCode) {
        this.code = Objects.requireNonNull(xNodeCode);
        return this;
    }
}
