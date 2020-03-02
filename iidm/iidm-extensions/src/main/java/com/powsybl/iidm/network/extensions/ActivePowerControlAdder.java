/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Injection;

public interface ActivePowerControlAdder<I extends Injection<I>>
        extends ExtensionAdder<I, ActivePowerControl<I>> {

    @Override
    default Class<ActivePowerControl> getExtensionClass() {
        return ActivePowerControl.class;
    }

    ActivePowerControlAdder<I> withParticipate(boolean participate);

    ActivePowerControlAdder<I> withDroop(float droop);
}
