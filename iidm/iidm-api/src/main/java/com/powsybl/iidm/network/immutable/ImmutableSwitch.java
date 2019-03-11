/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableSwitch extends AbstractImmutableIdentifiable<Switch> implements Switch {

    private final ImmutableCacheIndex cache;

    ImmutableSwitch(Switch identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return cache.getVoltageLevel(identifiable.getVoltageLevel());
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
