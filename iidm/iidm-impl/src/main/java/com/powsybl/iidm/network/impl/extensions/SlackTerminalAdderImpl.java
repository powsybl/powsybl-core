/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.extensions.TerminalWithPriority;
import com.powsybl.iidm.network.extensions.TerminalWithPriorityImpl;

import java.util.ArrayList;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackTerminalAdderImpl extends AbstractExtensionAdder<VoltageLevel, SlackTerminal>
        implements SlackTerminalAdder {

    private ArrayList<TerminalWithPriority> terminals;

    public SlackTerminalAdderImpl(VoltageLevel voltageLevel) {
        super(voltageLevel);
        this.terminals = new ArrayList<>();
    }

    @Override
    public SlackTerminalAdder withTerminal(Terminal terminal, int priority) {
        return withTerminal(new TerminalWithPriorityImpl(terminal, priority));
    }

    @Override
    public SlackTerminalAdder withTerminal(TerminalWithPriority terminal) {
        this.terminals.add(terminal);
        return this;
    }

    @Override
    public SlackTerminal createExtension(VoltageLevel voltageLevel) {
        if (terminals.isEmpty()) {
            throw new PowsyblException("Terminal needs to be set to create a SlackTerminal extension");
        }
        for (var terminal : terminals) {
            if (!terminal.getVoltageLevel().equals(voltageLevel)) {
                throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                        + terminal.getVoltageLevel().getId() + " instead of " + voltageLevel.getId() + ")");
            }
        }
        return new SlackTerminalImpl(voltageLevel, terminals);
    }
}
