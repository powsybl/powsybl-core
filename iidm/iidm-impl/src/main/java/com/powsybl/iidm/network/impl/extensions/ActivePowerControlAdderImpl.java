/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;

public class ActivePowerControlAdderImpl<I extends Injection<I>>
        extends AbstractExtensionAdder<I, ActivePowerControl<I>>
        implements ActivePowerControlAdder<I> {

    private boolean participate;

    private double droop = Double.NaN;
    private double participationFactor = Double.NaN;
    private double minPOverride = Double.NaN;
    private double maxPOverride = Double.NaN;

    protected ActivePowerControlAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected ActivePowerControlImpl<I> createExtension(I extendable) {
        return new ActivePowerControlImpl<>(extendable, participate, droop, participationFactor, minPOverride, maxPOverride);
    }

    @Override
    public ActivePowerControlAdder<I> withParticipate(boolean participate) {
        this.participate = participate;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withDroop(double droop) {
        this.droop = droop;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withParticipationFactor(double participationFactor) {
        this.participationFactor = participationFactor;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withMinPOverridde(double minPOverridde) {
        this.minPOverride = minPOverridde;
        return this;
    }

    @Override
    public ActivePowerControlAdder<I> withMaxPOverride(double maxPOverride) {
        this.maxPOverride = maxPOverride;
        return this;
    }

}
