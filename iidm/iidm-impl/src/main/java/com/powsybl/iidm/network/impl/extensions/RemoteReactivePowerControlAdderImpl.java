/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class RemoteReactivePowerControlAdderImpl extends AbstractExtensionAdder<Generator, RemoteReactivePowerControl> implements RemoteReactivePowerControlAdder {

    private double targetQ = Double.NaN;

    private Terminal regulatingTerminal;

    private boolean enabled = true;

    protected RemoteReactivePowerControlAdderImpl(final Generator extendable) {
        super(extendable);
    }

    @Override
    protected RemoteReactivePowerControl createExtension(final Generator extendable) {
        if (Double.isNaN(targetQ)) {
            throw new PowsyblException("Reactive power target must be set");
        }
        if (regulatingTerminal == null) {
            throw new PowsyblException("Regulating terminal must be set");
        }
        return new RemoteReactivePowerControlImpl(extendable, targetQ, regulatingTerminal, enabled);
    }

    @Override
    public RemoteReactivePowerControlAdder withTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public RemoteReactivePowerControlAdder withRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public RemoteReactivePowerControlAdder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
