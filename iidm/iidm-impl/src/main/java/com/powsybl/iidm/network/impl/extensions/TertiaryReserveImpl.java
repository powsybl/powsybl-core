/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.TertiaryReserve;

/**
 * @author Jacques Borsenberger {literal <jacques.borsenberger at rte-france.com}
 */
public class TertiaryReserveImpl extends AbstractExtension<Generator> implements TertiaryReserve {

    private boolean participate;

    public TertiaryReserveImpl(Generator generator, boolean participate) {
        super(generator);
        this.participate = participate;
    }

    @Override
    public boolean isParticipate() {
        return participate;
    }

    @Override
    public TertiaryReserve setParticipate(boolean participate) {
        this.participate = participate;
        return this;
    }

}
