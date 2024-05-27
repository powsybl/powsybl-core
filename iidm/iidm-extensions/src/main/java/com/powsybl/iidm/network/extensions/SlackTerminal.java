/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.TerminalFinder;

import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface SlackTerminal extends Extension<VoltageLevel> {

    String NAME = "slackTerminal";

    /**
     * Set the terminal of all SlackTerminal extensions from the given network to null. If the extension is empty,
     * meaning that for each variant the terminal is null, this method automatically remove the extension.
     *
     * @param network A network to cleanup
     */
    static void reset(Network network) {
        network.getVoltageLevels().forEach(vl -> reset(vl, null));
    }

    /**
     * Reset the slackTerminal extension to the given terminal (may be null)
     * @param voltageLevel the voltageLevel to reset the slackTerminal extension from
     * @param terminal the terminal to reset the extension to (may be null)
     */
    static void reset(VoltageLevel voltageLevel, Terminal terminal) {
        Objects.requireNonNull(voltageLevel);

        SlackTerminal st = voltageLevel.getExtension(SlackTerminal.class);
        if (st == null && terminal != null) {
            voltageLevel.newExtension(SlackTerminalAdder.class)
                    .withTerminal(terminal)
                    .add();
        } else if (st != null) {
            st.setTerminal(terminal, true);
        }
    }

    /**
     * Create a SlackTerminal extension attached to the voltage level of the given bus, using the default
     * {@link com.powsybl.iidm.network.util.TerminalFinder} strategy.
     */
    static void attach(Bus bus) {
        Objects.requireNonNull(bus);

        VoltageLevel vl = bus.getVoltageLevel();
        Terminal terminal = TerminalFinder.getDefault()
                .find(bus.getConnectedTerminals())
                .orElseThrow(() -> new PowsyblException("Unable to find a terminal in the bus " + bus.getId()));

        reset(vl, terminal);
    }

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the terminal pointed by the current SlackTerminal
     * @return the corresponding terminal
     */
    Terminal getTerminal();

    /**
     * Set the terminal pointed by the current SlackTerminal
     * @param terminal the corresponding terminal
     * @return the current SlackTerminal
     */
    SlackTerminal setTerminal(Terminal terminal);

    /**
     * Set the terminal pointed by the current SlackTerminal
     * @param terminal the corresponding terminal (may be null)
     * @param cleanIfEmpty if true and if the slackTerminal is empty, removes the SlackTerminal extension from
     *                     the corresponding VoltageLevel
     * @return the current SlackTerminal
     */
    default SlackTerminal setTerminal(Terminal terminal, boolean cleanIfEmpty) {
        setTerminal(terminal);
        if (cleanIfEmpty && terminal == null && isEmpty()) {
            getExtendable().removeExtension(SlackTerminal.class);
        }
        return this;
    }

    /**
     * <p>Returns <code>true</code> if the current SlackTerminal is empty, meaning that this extension is unused.</p>
     * <p>Note that this method returns <code>true</code> only when the terminal is <code>null</code> <b>for all the variants</b>.
     * Thus, when it returns <code>false</code>, the current variant's terminal may still be <code>null</code>.</p>
     */
    boolean isEmpty();

}
