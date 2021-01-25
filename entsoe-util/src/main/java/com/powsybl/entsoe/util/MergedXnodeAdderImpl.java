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

    private double xnodeP1;

    private double xnodeQ1;

    private double xnodeP2;

    private double xnodeQ2;

    private String line1Name;

    private boolean line1Fictitious;

    private double line1B1;

    private double line1B2;

    private double line1G1;

    private double line1G2;

    private String line2Name;

    private boolean line2Fictitious;

    private double line2B1;

    private double line2B2;

    private double line2G1;

    private double line2G2;

    private String code;

    public MergedXnodeAdderImpl(Line extendable) {
        super(extendable);
    }

    @Override
    protected MergedXnode createExtension(Line extendable) {
        return new MergedXnodeImpl(extendable, rdp, xdp, xnodeP1, xnodeQ1, xnodeP2, xnodeQ2,
                line1Name, line1Fictitious, line1B1, line1B2, line1G1, line1G2,
                line2Name, line2Fictitious, line2B1, line2B2, line2G1, line2G2,
                code);
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
    public MergedXnodeAdderImpl withLine1B1(double line1B1) {
        this.line1B1 = line1B1;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine1B2(double line1B2) {
        this.line1B2 = line1B2;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine1G1(double line1G1) {
        this.line1G1 = line1G1;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine1G2(double line1G2) {
        this.line1G2 = line1G2;
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
    public MergedXnodeAdderImpl withLine2B1(double line2B1) {
        this.line2B1 = line2B1;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine2B2(double line2B2) {
        this.line2B2 = line2B2;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine2G1(double line2G1) {
        this.line2G1 = line2G1;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withLine2G2(double line2G2) {
        this.line2G2 = line2G2;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withCode(String code) {
        this.code = code;
        return this;
    }
}
