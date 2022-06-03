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
public class BatteryAdapter extends AbstractInjectionAdapter<Battery> implements Battery {

    BatteryAdapter(final Battery delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ReactiveLimits getReactiveLimits() {
        return getDelegate().getReactiveLimits();
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(final Class<L> type) {
        return getDelegate().getReactiveLimits(type);
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return getDelegate().newReactiveCapabilityCurve();
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return getDelegate().newMinMaxReactiveLimits();
    }

    @Override
    public double getTargetP() {
        return getDelegate().getTargetP();
    }

    @Override
    public BatteryAdapter setTargetP(final double targetP) {
        getDelegate().setTargetP(targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return getDelegate().getTargetQ();
    }

    @Override
    public BatteryAdapter setTargetQ(final double targetQ) {
        getDelegate().setTargetQ(targetQ);
        return this;
    }

    @Override
    public double getMinP() {
        return getDelegate().getMinP();
    }

    @Override
    public BatteryAdapter setMinP(final double minP) {
        getDelegate().setMinP(minP);
        return this;
    }

    @Override
    public double getMaxP() {
        return getDelegate().getMaxP();
    }

    @Override
    public BatteryAdapter setMaxP(final double maxP) {
        getDelegate().setMaxP(maxP);
        return this;
    }
}
