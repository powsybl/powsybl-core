/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Line;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class MergedXnodeAdderImpl extends AbstractExtensionAdder<Line, MergedXnode>
        implements MergedXnodeAdder {

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

    public MergedXnodeAdderImpl(Line extendable) {
        super(extendable);
    }

    @Override
    protected MergedXnode createExtension(Line extendable) {
        return MergedXnodeImpl.create(extendable)
                .setRdp(rdp).setXdp(xdp)
                .setLine1Name(line1Name).setLine1Fictitious(line1Fictitious).setXnodeP1(xnodeP1).setXnodeQ1(xnodeQ1).setB1dp(b1dp).setG1dp(g1dp)
                .setLine2Name(line2Name).setLine2Fictitious(line2Fictitious).setXnodeP2(xnodeP2).setXnodeQ2(xnodeQ2).setB2dp(b2dp).setG2dp(g2dp)
                .setCode(code);
    }

    @Override
    public MergedXnodeAdderImpl withRdp(float rdp) {
        this.rdp = rdp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withXdp(float xdp) {
        this.xdp = xdp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine1Name(String line1Name) {
        this.line1Name = line1Name;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine1Fictitious(boolean line1Fictitious) {
        this.line1Fictitious = line1Fictitious;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withXnodeP1(double xnodeP1) {
        this.xnodeP1 = xnodeP1;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withXnodeQ1(double xnodeQ1) {
        this.xnodeQ1 = xnodeQ1;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withB1dp(float b1dp) {
        this.b1dp = b1dp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withG1dp(float g1dp) {
        this.g1dp = g1dp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine2Name(String line2Name) {
        this.line2Name = line2Name;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine2Fictitious(boolean line2Fictitious) {
        this.line2Fictitious = line2Fictitious;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withXnodeP2(double xnodeP2) {
        this.xnodeP2 = xnodeP2;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withXnodeQ2(double xnodeQ2) {
        this.xnodeQ2 = xnodeQ2;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withB2dp(float b2dp) {
        this.b2dp = b2dp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withG2dp(float g2dp) {
        this.g2dp = g2dp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withCode(String code) {
        this.code = code;
        return this;
    }
}
