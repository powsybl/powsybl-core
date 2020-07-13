/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Iterator;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackTerminalBusBreakerImpl extends AbstractExtension<VoltageLevel> implements SlackTerminal {

    private final VoltageLevel voltageLevel;
    private final Bus bus;

    SlackTerminalBusBreakerImpl(String busId, VoltageLevel voltageLevel) {
        super(voltageLevel);
        this.voltageLevel = voltageLevel;
        this.bus = voltageLevel.getBusBreakerView().getBus(busId);
    }

    @Override
    public Terminal getTerminal() {
        Iterator<? extends Terminal> terminalsIter = bus.getConnectedTerminals().iterator();
        if (!terminalsIter.hasNext()) {
            throw new PowsyblException("The given slackTerminal is defined by a bus which lacks a terminal");
        }
        return terminalsIter.next();
    }
}
