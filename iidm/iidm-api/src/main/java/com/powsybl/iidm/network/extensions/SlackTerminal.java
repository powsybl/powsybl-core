/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.TerminalFinder;

import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface SlackTerminal extends Extension<VoltageLevel> {

    /**
     * Set the terminal of all SlackTerminal extensions from the given network to null. If the extension is cleanable,
     * this method automatically remove the extension.
     *
     * @param network A network to cleanup
     */
    static void reset(Network network) {
        network.getVoltageLevels().forEach(vl -> reset(vl, null));
    }

    /**
     * Create a SlackTerminal extension attached to the voltage level of the given bus, using the default
     * {@link com.powsybl.iidm.network.util.TerminalFinder} strategy.
     */
    static void attach(Bus bus) {
        VoltageLevel vl = bus.getVoltageLevel();
        Terminal terminal = TerminalFinder.getDefault().find(bus.getConnectedTerminals());

        vl.newExtension(SlackTerminalAdder.class)
                .withTerminal(terminal)
                .add();
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

    @Override
    default String getName() {
        return "slackTerminal";
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
     * @param cleanIfCleanable if true and if the slackTerminal is cleanable, removes the SlackTerminal extension from
     *                         the corresponding VoltageLevel
     * @return the current SlackTerminal
     */
    default SlackTerminal setTerminal(Terminal terminal, boolean cleanIfCleanable) {
        setTerminal(terminal);
        if (cleanIfCleanable && terminal == null && isCleanable()) {
            getExtendable().removeExtension(SlackTerminal.class);
        }
        return this;
    }

    /**
     * Returns if the current SlackTerminal can be cleaned, that is, if the extension is unused
     */
    boolean isCleanable();

}
