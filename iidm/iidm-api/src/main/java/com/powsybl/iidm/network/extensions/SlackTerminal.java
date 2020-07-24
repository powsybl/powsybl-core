/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface SlackTerminal extends Extension<VoltageLevel> {

    /**
     * Remove all SlackTerminal extensions from given network
     * @param network the network to remove the slackTerminal extensions from
     */
    static void removeAllFrom(Network network) {
        network.getVoltageLevels().forEach(vl -> vl.removeExtension(SlackTerminal.class));
    }

    /**
     * Reset the slackTerminal extension to the given terminal (may be null)
     * @param voltageLevel the voltageLevel to reset the slackTerminal extension from
     * @param terminal the terminal to reset the extension to (may be null)
     */
    static void reset(VoltageLevel voltageLevel, Terminal terminal) {
        SlackTerminal st = voltageLevel.getExtension(SlackTerminal.class);
        if (st == null) {
            voltageLevel.newExtension(SlackTerminalAdder.class)
                .withTerminal(terminal)
                .add();
        } else {
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
        if (cleanIfCleanable) {
            if (setTerminal(terminal).isCleanable()) {
                getExtendable().removeExtension(SlackTerminal.class);
            }
        } else {
            setTerminal(terminal);
        }
        return this;
    }

    /**
     * Returns if the current SlackTerminal can be cleaned, that is, if the extension is unused
     */
    boolean isCleanable();

}
