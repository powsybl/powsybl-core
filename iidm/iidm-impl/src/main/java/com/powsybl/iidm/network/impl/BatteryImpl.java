/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryImpl extends AbstractConnectable<Battery> implements Battery, ReactiveLimitsOwner {

    private double p0;

    private double q0;

    private double minP;

    private double maxP;

    private ReactiveLimits reactiveLimits;

    BatteryImpl(Ref<? extends VariantManagerHolder> ref,
                String id, String name,
                double p0, double q0,
                double minP, double maxP) {
        super(id, name);
        this.p0 = p0;
        this.q0 = q0;
        this.minP = minP;
        this.maxP = maxP;
        reactiveLimits = new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.BATTERY;
    }

    @Override
    protected String getTypeDescription() {
        return "Battery";
    }


    @Override
    public double getP0() {
        return p0;
    }

    @Override
    public Battery setP0(double p0) {
        ValidationUtil.checkP0(this, p0);
        double oldValue = this.p0;
        this.p0 = p0;
        notifyUpdate("p0", oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return q0;
    }

    @Override
    public Battery setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0);
        double oldValue = this.q0;
        this.q0 = q0;
        notifyUpdate("q0", oldValue, q0);
        return this;
    }

    @Override
    public double getMinP() {
        return minP;
    }

    @Override
    public Battery setMinP(double minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        double oldValue = this.minP;
        this.minP = minP;
        notifyUpdate("minP", oldValue, minP);
        return this;
    }

    @Override
    public double getMaxP() {
        return maxP;
    }

    @Override
    public Battery setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        double oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits;
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits = reactiveLimits;
    }

    @Override
    public <RL extends ReactiveLimits> RL getReactiveLimits(Class<RL> type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(reactiveLimits)) {
            return type.cast(reactiveLimits);
        } else {
            throw new ValidationException(this, "incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }
}
