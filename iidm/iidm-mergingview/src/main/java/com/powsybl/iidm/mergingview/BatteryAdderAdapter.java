/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BatteryAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BatteryAdderAdapter extends AbstractIdentifiableAdderAdapter<BatteryAdder> implements BatteryAdder {

    BatteryAdderAdapter(final BatteryAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public BatteryAdapter add() {
        checkAndSetUniqueId();
        return new BatteryAdapter(getDelegate().add(), getIndex());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public BatteryAdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public BatteryAdderAdapter setBus(final String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public BatteryAdderAdapter setConnectableBus(final String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }

    @Override
    public BatteryAdderAdapter setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public BatteryAdderAdapter setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public BatteryAdderAdapter setMinP(final double minP) {
        getDelegate().setMinP(minP);
        return this;
    }

    @Override
    public BatteryAdderAdapter setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }
}
