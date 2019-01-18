/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableSwitch extends AbstractImmutableIdentifiable<Switch> implements Switch {

    private static final Map<Switch, ImmutableSwitch> CACHE = new HashMap<>();

    private ImmutableSwitch(Switch identifiable) {
        super(identifiable);
    }

    static ImmutableSwitch ofNullable(Switch sw) {
        return null == sw ? null : CACHE.computeIfAbsent(sw, k -> new ImmutableSwitch(sw));
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return ImmutableVoltageLevel.ofNullable(identifiable.getVoltageLevel());
    }

    @Override
    public SwitchKind getKind() {
        return identifiable.getKind();
    }

    @Override
    public boolean isOpen() {
        return identifiable.isOpen();
    }

    @Override
    public void setOpen(boolean open) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean isRetained() {
        return identifiable.isRetained();
    }

    @Override
    public void setRetained(boolean retained) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean isFictitious() {
        return identifiable.isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
