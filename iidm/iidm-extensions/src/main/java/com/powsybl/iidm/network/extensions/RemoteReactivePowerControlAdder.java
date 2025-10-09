/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public interface RemoteReactivePowerControlAdder extends ExtensionAdder<Generator, RemoteReactivePowerControl> {
    @Override
    default Class<RemoteReactivePowerControl> getExtensionClass() {
        return RemoteReactivePowerControl.class;
    }

    RemoteReactivePowerControlAdder withTargetQ(double targetQ);

    RemoteReactivePowerControlAdder withRegulatingTerminal(Terminal regulatingTerminal);

    RemoteReactivePowerControlAdder withEnabled(boolean enabled);
}
