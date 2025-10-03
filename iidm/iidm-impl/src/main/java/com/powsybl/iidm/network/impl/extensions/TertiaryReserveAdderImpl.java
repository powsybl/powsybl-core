/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.TertiaryReserve;
import com.powsybl.iidm.network.extensions.TertiaryReserveAdder;

/**
 * @author Jacques Borsenberger {literal <jacques.borsenberger at rte-france.com}
 */
public class TertiaryReserveAdderImpl extends AbstractExtensionAdder<Generator, TertiaryReserve> implements TertiaryReserveAdder {

    private boolean participate = false;

    protected TertiaryReserveAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected TertiaryReserve createExtension(Generator generator) {
        return new TertiaryReserveImpl(generator, participate);
    }

    @Override
    public TertiaryReserveAdder withParticipate(boolean participate) {
        this.participate = participate;
        return this;
    }
}
