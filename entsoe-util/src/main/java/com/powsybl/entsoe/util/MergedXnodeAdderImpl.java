/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class MergedXnodeAdderImpl<T extends Identifiable<T>> extends AbstractExtensionAdder<T, MergedXnode<T>>
        implements MergedXnodeAdder<T> {

    private double rdp = Double.NaN; // r divider position 1 -> 2

    private double xdp = Double.NaN; // x divider position 1 -> 2

    private String line1Name;

    private boolean line1Fictitious = false;

    private double xnodeP1;

    private double xnodeQ1;

    private double b1dp = Double.NaN; // b1 divider position 1 -> 2

    private double g1dp = Double.NaN; // g1 divider position 1 -> 2

    private String line2Name;

    private boolean line2Fictitious = false;

    private double xnodeP2;

    private double xnodeQ2;

    private double b2dp = Double.NaN; // b2 divider position 1 -> 2

    private double g2dp = Double.NaN; // g2 divider position 1 -> 2

    private String code;

    public MergedXnodeAdderImpl(T extendable) {
        super(extendable);
    }

    @Override
    protected MergedXnode createExtension(T extendable) {
        if (extendable instanceof Line || extendable instanceof TieLine) {
            return new MergedXnodeImpl(extendable, rdp, xdp,
                    line1Name, line1Fictitious, xnodeP1, xnodeQ1, b1dp, g1dp,
                    line2Name, line2Fictitious, xnodeP2, xnodeQ2, b2dp, g2dp,
                    code);
        }
        throw new PowsyblException(extendable.getId() + " is not a Line or a Tie Line.");
    }

    @Override
    public MergedXnodeAdderImpl withRdp(double rdp) {
        this.rdp = rdp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withXdp(double xdp) {
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
    public MergedXnodeAdderImpl withB1dp(double b1dp) {
        this.b1dp = b1dp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withG1dp(double g1dp) {
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
    public MergedXnodeAdderImpl withB2dp(double b2dp) {
        this.b2dp = b2dp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withG2dp(double g2dp) {
        this.g2dp = g2dp;
        return this;
    }

    @Override
    public MergedXnodeAdderImpl withCode(String code) {
        this.code = code;
        return this;
    }
}
