/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BusAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusAdderAdapter extends AbstractAdapter<BusAdder> implements BusAdder {

    protected BusAdderAdapter(final BusAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public BusAdapter add() {
        return getIndex().getBus(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public BusAdderAdapter setId(final String id) {
        getDelegate().setId(id);
        return this;
    }

    @Override
    public BusAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        getDelegate().setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public BusAdderAdapter setName(final String name) {
        getDelegate().setName(name);
        return this;
    }
}
