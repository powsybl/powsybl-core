/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.TieLineUtil;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TieLineImpl extends AbstractBranch<Line> implements TieLine {

    @Override
    protected String getTypeDescription() {
        return "AC Line";
    }

    private DanglingLineImpl half1;

    private DanglingLineImpl half2;

    TieLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
        super(network, id, name, fictitious);
    }

    void attachDanglingLines(DanglingLineImpl half1, DanglingLineImpl half2) {
        this.half1 = attach(half1, Side.ONE);
        this.half2 = attach(half2, Side.TWO);
    }

    private DanglingLineImpl attach(DanglingLineImpl half, Side side) {
        half.setParent(this, side);
        return half;
    }

    @Override
    public boolean isTieLine() {
        return true;
    }

    @Override
    public String getUcteXnodeCode() {
        return half1.getUcteXnodeCode();
    }

    @Override
    public DanglingLineImpl getHalf1() {
        return half1;
    }

    @Override
    public DanglingLineImpl getHalf2() {
        return half2;
    }

    @Override
    public DanglingLineImpl getHalf(Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unknown branch side " + side);
        }
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getR() {
        return TieLineUtil.getR(half1, half2);
    }

    private ValidationException createNotSupportedForTieLines() {
        return new ValidationException(this, "direct modification of characteristics not supported for tie lines");
    }

    @Override
    public LineImpl setR(double r) {
        throw createNotSupportedForTieLines();
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getX() {
        return TieLineUtil.getX(half1, half2);
    }

    @Override
    public LineImpl setX(double x) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(half1, half2);
    }

    @Override
    public LineImpl setG1(double g1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(half1, half2);
    }

    @Override
    public LineImpl setB1(double b1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(half1, half2);
    }

    @Override
    public LineImpl setG2(double g2) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(half1, half2);
    }

    @Override
    public LineImpl setB2(double b2) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public void remove() {
        half1.removeFromParent();
        half2.removeFromParent();
        super.remove();
    }
}
