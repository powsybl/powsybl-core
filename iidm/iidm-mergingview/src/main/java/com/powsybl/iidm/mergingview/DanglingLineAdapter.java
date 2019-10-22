/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdapter extends AbstractIdentifiableAdapter<DanglingLine> implements DanglingLine {

    protected DanglingLineAdapter(final DanglingLine delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ConnectableType getType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getP0() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdapter setP0(final double p0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getQ0() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdapter setQ0(final double q0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getR() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getX() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getG() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getB() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public String getUcteXnodeCode() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
