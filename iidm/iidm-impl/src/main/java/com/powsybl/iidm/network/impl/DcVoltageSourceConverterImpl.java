/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.DcVoltageSourceConverter;
import com.powsybl.iidm.network.ReactiveLimits;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcVoltageSourceConverterImpl extends AbstractDcConverter<DcVoltageSourceConverter> implements DcVoltageSourceConverter, ReactiveLimitsOwner {

    private final ReactiveLimitsHolderImpl reactiveLimits;

    DcVoltageSourceConverterImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                                 double idleLoss, double switchingLoss, double resistiveLoss,
                                 TerminalExt pccTerminal, ControlMode controlMode, double targetP, double targetVdc) {
        super(ref, id, name, fictitious, idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc);
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Override
    protected String getTypeDescription() {
        return "DC Voltage Source Converter";
    }

    @Override
    public ReactiveCapabilityCurveAdderImpl newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdderImpl newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits.setReactiveLimits(reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits.getReactiveLimits();
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        return reactiveLimits.getReactiveLimits(type);
    }
}
