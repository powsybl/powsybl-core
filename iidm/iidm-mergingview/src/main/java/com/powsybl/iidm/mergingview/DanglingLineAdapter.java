/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdapter extends AbstractInjectionAdapter<DanglingLine> implements DanglingLine {

    DanglingLineAdapter(final DanglingLine delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public double getP0() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLine setP0(final double p0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getQ0() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLine setQ0(final double q0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getR() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLine setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getX() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLine setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getG() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLine setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getB() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLine setB(final double b) {
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
