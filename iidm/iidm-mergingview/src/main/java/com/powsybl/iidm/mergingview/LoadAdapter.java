/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadModel;
import com.powsybl.iidm.network.LoadType;

import java.util.Optional;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LoadAdapter extends AbstractInjectionAdapter<Load> implements Load {

    LoadAdapter(final Load delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public LoadType getLoadType() {
        return getDelegate().getLoadType();
    }

    @Override
    public Load setLoadType(final LoadType loadType) {
        getDelegate().setLoadType(loadType);
        return this;
    }

    @Override
    public double getP0() {
        return getDelegate().getP0();
    }

    @Override
    public Load setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public double getQ0() {
        return getDelegate().getQ0();
    }

    @Override
    public Optional<LoadModel> getModel() {
        return getDelegate().getModel();
    }

    @Override
    public Load setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }
}
