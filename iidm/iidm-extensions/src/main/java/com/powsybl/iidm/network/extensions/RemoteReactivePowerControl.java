/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public interface RemoteReactivePowerControl extends Extension<Generator> {

    String NAME = "generatorRemoteReactivePowerControl";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the reactive power target in MVar, at the remote regulating terminal. It is not the local target of the generator.
     */
    double getTargetQ();

    /**
     * Get the regulating terminal where the reactive power should be controlled. The regulating terminal should be the
     * terminal of a branch or the terminal of a dangling line that will be merged later. It is not the regulating terminal
     * of the generator used for voltage control that is less restrictive.
     */
    Terminal getRegulatingTerminal();

    /**
     * Says if the control is active or not.
     */
    boolean isEnabled();

    RemoteReactivePowerControl setTargetQ(double targetQ);

    RemoteReactivePowerControl setEnabled(boolean enabled);

    RemoteReactivePowerControl setRegulatingTerminal(Terminal regulatingTerminals);
}
