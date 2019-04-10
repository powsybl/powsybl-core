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

/**
 * An immutable {@link Switch}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableSwitch extends AbstractImmutableIdentifiable<Switch> implements Switch {

    ImmutableSwitch(Switch identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableVoltageLevel}
     */
    @Override
    public VoltageLevel getVoltageLevel() {
        return cache.getVoltageLevel(identifiable.getVoltageLevel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SwitchKind getKind() {
        return identifiable.getKind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        return identifiable.isOpen();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void setOpen(boolean open) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRetained() {
        return identifiable.isRetained();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void setRetained(boolean retained) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFictitious() {
        return identifiable.isFictitious();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void setFictitious(boolean fictitious) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
