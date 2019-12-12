/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BatteryAdderAdapter extends AbstractInjectionAdderAdapter<BatteryAdder> implements BatteryAdder {

    BatteryAdderAdapter(final BatteryAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Battery add() {
        checkAndSetUniqueId();
        return getIndex().getBattery(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public BatteryAdder setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public BatteryAdder setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public BatteryAdder setMinP(final double minP) {
        getDelegate().setMinP(minP);
        return this;
    }

    @Override
    public BatteryAdder setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }
}
