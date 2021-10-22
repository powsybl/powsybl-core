/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;

import java.util.Objects;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class RemoteReactivePowerControlImpl extends AbstractExtension<Generator> implements RemoteReactivePowerControl {

    private final double targetQ;

    private final Terminal regulatingTerminal;

    private final boolean enabled;

    public RemoteReactivePowerControlImpl(double targetQ, Terminal regulatingTerminal, boolean enabled) {
        this.targetQ = targetQ;
        this.regulatingTerminal = Objects.requireNonNull(regulatingTerminal);
        this.enabled = enabled;
    }

    @Override
    public double getTargetQ() {
        return targetQ;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
