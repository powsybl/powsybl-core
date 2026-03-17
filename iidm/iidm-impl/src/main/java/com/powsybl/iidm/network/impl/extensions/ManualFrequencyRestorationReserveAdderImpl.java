/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ManualFrequencyRestorationReserve;
import com.powsybl.iidm.network.extensions.ManualFrequencyRestorationReserveAdder;

/**
 * @author Jacques Borsenberger {@literal <jacques.borsenberger at rte-france.com>}
 */
public class ManualFrequencyRestorationReserveAdderImpl<I extends Injection<I>>
        extends AbstractExtensionAdder<I, ManualFrequencyRestorationReserve<I>>
        implements ManualFrequencyRestorationReserveAdder<I> {

    private boolean participate = false;

    protected ManualFrequencyRestorationReserveAdderImpl(I injection) {
        super(injection);
    }

    @Override
    protected ManualFrequencyRestorationReserve<I> createExtension(I injection) {
        return new ManualFrequencyRestorationReserveImpl<>(injection, participate);
    }

    @Override
    public ManualFrequencyRestorationReserveAdder<I> withParticipate(boolean participate) {
        this.participate = participate;
        return this;
    }
}
