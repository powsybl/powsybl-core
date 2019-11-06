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
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public BusbarSectionAdderAdapter setId(final String id) {
        getDelegate().setId(id);
        return this;
    }

    @Override
    public BusbarSectionAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        getDelegate().setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public BusbarSectionAdderAdapter setName(final String name) {
        getDelegate().setName(name);
        return this;
    }

    @Override
    public BusbarSectionAdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public BusbarSectionAdapter add() {
        return new BusbarSectionAdapter(getDelegate().add(), getIndex());
    }
}
