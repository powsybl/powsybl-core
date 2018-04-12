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

    private final Line line;

    private float rdp; // r divider position 1 -> 2

    private float xdp; // x divider position 1 -> 2

    private float xnodeP1;

    private float xnodeQ1;

    private float xnodeP2;

    private float xnodeQ2;

    private String code;

    public MergedXnode(Line line, float rdp, float xdp, float xnodeP1, float xnodeQ1, float xnodeP2, float xnodeQ2, String code) {
        this.line = Objects.requireNonNull(line);
        this.rdp = checkDividerPosition(rdp);
        this.xdp = checkDividerPosition(xdp);
        this.xnodeP1 = checkPowerFlow(xnodeP1);
        this.xnodeQ1 = checkPowerFlow(xnodeQ1);
        this.xnodeP2 = checkPowerFlow(xnodeP2);
        this.xnodeQ2 = checkPowerFlow(xnodeQ2);
        this.code = Objects.requireNonNull(code);
    }

    private static float checkDividerPosition(float dp) {
        if (dp < 0 || dp > 1) {
            throw new IllegalArgumentException("Invalid divider postion: " + dp);
        }
        return dp;
    }

    private static float checkPowerFlow(float value) {
        if (Float.isNaN(value)) {
            throw new IllegalArgumentException("Power flow is invalid");
        }
        return value;
    }

    @Override
    public String getName() {
        return "mergedXnode";
    }

    @Override
    public Line getExtendable() {
        return line;
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

    public float getXnodeP1() {
        return xnodeP1;
    }

    public MergedXnode setXnodeP1(float xNodeP1) {
        this.xnodeP1 = checkPowerFlow(xNodeP1);
        return this;
    }

    public float getXnodeQ1() {
        return xnodeQ1;
    }

    public MergedXnode setXnodeQ1(float xNodeQ1) {
        this.xnodeQ1 = checkPowerFlow(xNodeQ1);
        return this;
    }

    public float getXnodeP2() {
        return xnodeP2;
    }

    public MergedXnode setXnodeP2(float xNodeP2) {
        this.xnodeP2 = checkPowerFlow(xNodeP2);
        return this;
    }

    public float getXnodeQ2() {
        return xnodeQ2;
    }

    public MergedXnode setXnodeQ2(float xNodeQ2) {
        this.xnodeQ2 = checkPowerFlow(xNodeQ2);
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
