/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.DanglingLineAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdderAdapter extends AbstractAdapter<DanglingLineAdder> implements DanglingLineAdder {

    protected DanglingLineAdderAdapter(final DanglingLineAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public DanglingLineAdderAdapter setNode(final int node) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setBus(final String bus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setConnectableBus(final String connectableBus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setP0(final double p0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setQ0(final double q0) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdderAdapter setUcteXnodeCode(final String ucteXnodeCode) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public DanglingLineAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
