/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BusbarSectionAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusbarSectionAdderAdapter extends AbstractAdapter<BusbarSectionAdder> implements BusbarSectionAdder {

    protected BusbarSectionAdderAdapter(final BusbarSectionAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public BusbarSectionAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusbarSectionAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusbarSectionAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusbarSectionAdderAdapter setNode(final int node) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public BusbarSectionAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
