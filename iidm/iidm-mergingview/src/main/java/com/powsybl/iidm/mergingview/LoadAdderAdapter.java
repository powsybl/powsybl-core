/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LoadAdderAdapter extends AbstractAdapter<LoadAdder> implements LoadAdder {

    protected LoadAdderAdapter(final LoadAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public LoadAdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public LoadAdderAdapter setBus(final String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public LoadAdderAdapter setConnectableBus(final String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }

    @Override
    public LoadAdderAdapter setId(final String id) {
        getDelegate().setId(id);
        return this;
    }

    @Override
    public LoadAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        getDelegate().setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public LoadAdderAdapter setName(final String name) {
        getDelegate().setName(name);
        return this;
    }

    @Override
    public LoadAdderAdapter setLoadType(final LoadType loadType) {
        getDelegate().setLoadType(loadType);
        return this;
    }

    @Override
    public LoadAdderAdapter setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public LoadAdderAdapter setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public LoadAdapter add() {
        return new LoadAdapter(getDelegate().add(), getIndex());
    }

}
