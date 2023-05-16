/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LoadAdderAdapter extends AbstractInjectionAdderAdapter<Load, LoadAdder> implements LoadAdder {

    LoadAdderAdapter(final LoadAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Load add() {
        checkAndSetUniqueId();
        return getIndex().getLoad(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public LoadAdder setLoadType(final LoadType loadType) {
        getDelegate().setLoadType(loadType);
        return this;
    }

    @Override
    public LoadAdder setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public LoadAdder setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public ZipLoadModelAdder newZipModel() {
        return getDelegate().newZipModel();
    }

    @Override
    public ExponentialLoadModelAdder newExponentialModel() {
        return getDelegate().newExponentialModel();
    }
}
