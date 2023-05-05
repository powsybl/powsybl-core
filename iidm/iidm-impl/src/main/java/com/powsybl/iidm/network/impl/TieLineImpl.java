/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.TieLineUtil;

import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TieLineImpl extends AbstractIdentifiable<TieLine> implements TieLine {

    @Override
    public NetworkImpl getNetwork() {
        if (removed) {
            throw new PowsyblException("Cannot access network of removed tie line " + id);
        }
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "Tie Line";
    }

    private DanglingLineImpl half1;

    private DanglingLineImpl half2;

    private final Ref<NetworkImpl> networkRef;

    private boolean removed = false;

    TieLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = network;
    }

    void attachDanglingLines(DanglingLineImpl half1, DanglingLineImpl half2) {
        this.half1 = attach(half1);
        this.half2 = attach(half2);
    }

    private DanglingLineImpl attach(DanglingLineImpl half) {
        half.setTieLine(this);
        return half;
    }

    @Override
    public String getUcteXnodeCode() {
        return Optional.ofNullable(half1.getUcteXnodeCode()).orElseGet(() -> half2.getUcteXnodeCode());
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
    public DanglingLineImpl getHalf(Branch.Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new IllegalStateException("Unknown branch side " + side);
        }
    }

    @Override
    public DanglingLine getHalf(String voltageLevelId) {
        if (half1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return half1;
        }
        if (half2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return half2;
        }
        return null;
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getR() {
        return TieLineUtil.getR(half1, half2);
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getX() {
        return TieLineUtil.getX(half1, half2);
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(half1, half2);
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(half1, half2);
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(half1, half2);
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(half1, half2);
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);

        // Remove dangling lines
        half1.removeTieLine();
        half2.removeTieLine();

        // Remove this tie line from the network
        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }
}
