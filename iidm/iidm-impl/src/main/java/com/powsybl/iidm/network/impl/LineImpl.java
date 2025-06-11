/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.util.ConnectionElementsContainer;
import com.powsybl.iidm.network.util.SwitchPredicates;

import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LineImpl extends AbstractConnectableBranch<Line> implements Line {

    private double r;

    private double x;

    private double g1;

    private double b1;

    private double g2;

    private double b2;

    LineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, double r, double x, double g1, double b1, double g2, double b2) {
        super(network, id, name, fictitious);
        this.r = r;
        this.x = x;
        this.g1 = g1;
        this.b1 = b1;
        this.g2 = g2;
        this.b2 = b2;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public LineImpl setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public LineImpl setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = this.x;
        this.x = x;
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG1() {
        return g1;
    }

    @Override
    public LineImpl setG1(double g1) {
        ValidationUtil.checkG1(this, g1);
        double oldValue = this.g1;
        this.g1 = g1;
        notifyUpdate("g1", oldValue, g1);
        return this;
    }

    @Override
    public double getB1() {
        return b1;
    }

    @Override
    public LineImpl setB1(double b1) {
        ValidationUtil.checkB1(this, b1);
        double oldValue = this.b1;
        this.b1 = b1;
        notifyUpdate("b1", oldValue, b1);
        return this;
    }

    @Override
    public double getG2() {
        return g2;
    }

    @Override
    public LineImpl setG2(double g2) {
        ValidationUtil.checkG2(this, g2);
        double oldValue = this.g2;
        this.g2 = g2;
        notifyUpdate("g2", oldValue, g2);
        return this;
    }

    @Override
    public double getB2() {
        return b2;
    }

    @Override
    public LineImpl setB2(double b2) {
        ValidationUtil.checkB2(this, b2);
        double oldValue = this.b2;
        this.b2 = b2;
        notifyUpdate("b2", oldValue, b2);
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "AC line";
    }

    @Override
    public boolean connect(boolean propagateDisconnectionIfNeeded) {
        return connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER, null, propagateDisconnectionIfNeeded);
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate, ThreeSides side,
                           boolean propagateDisconnectionIfNeeded, boolean connectFromHere,
                           ConnectionElementsContainer connectionElementsContainer) {
        return ConnectDisconnectUtil.connectAllTerminals(
            this,
            getTerminals(side),
            isTypeSwitchToOperate,
            connectionElementsContainer,
            connectFromHere,
            propagateDisconnectionIfNeeded,
            getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public boolean disconnect(boolean propagateDisconnectionIfNeeded) {
        return disconnect(SwitchPredicates.IS_CLOSED_BREAKER, null, propagateDisconnectionIfNeeded);
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable, ThreeSides side,
                              boolean propagateDisconnectionIfNeeded, boolean disconnectFromHere,
                              ConnectionElementsContainer connectionElementsContainer) {
        return ConnectDisconnectUtil.disconnectAllTerminals(
            this,
            getTerminals(side),
            isSwitchOpenable,
            connectionElementsContainer,
            disconnectFromHere,
            propagateDisconnectionIfNeeded,
            getNetwork().getReportNodeContext().getReportNode());
    }
}
