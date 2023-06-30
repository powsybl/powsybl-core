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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
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
        network.getVoltageLevels().forEach(vl -> reset(vl));
    }

    /**
     * @deprecated
     * Reset the slackTerminal extension to the given terminal (may be null)
     *
     * @param voltageLevel the voltageLevel to reset the slackTerminal extension from
     * @param terminal the terminal to reset the extension to (may be null)
     */
    @Deprecated
    static void reset(VoltageLevel voltageLevel, Terminal terminal) {
        Objects.requireNonNull(voltageLevel);

        SlackTerminal st = voltageLevel.getExtension(SlackTerminal.class);
        if (st == null && terminal != null) {
            voltageLevel.newExtension(SlackTerminalAdder.class).withTerminal(terminal).add();
        } else if (st != null) {
            st.setTerminal(terminal, true);
        }
    }

    /**
     * Reset the slackTerminal extension with no terminal
     *
     * @param voltageLevel the voltageLevel to reset the slackTerminal extension from
     */
    static void reset(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);

        SlackTerminal st = voltageLevel.getExtension(SlackTerminal.class);
        if (st != null) {
            st.setNoTerminalAndClean();
        }
    }

    /**
     * Reset the slackTerminal extension to the given terminal (may NOT be null)
     *
     * @param voltageLevel the voltageLevel to reset the slackTerminal extension from
     * @param terminal the terminals to reset the extension to
     */
    static void reset(VoltageLevel voltageLevel, List<? extends TerminalWithPriority> terminals) {
        Objects.requireNonNull(voltageLevel);
        Objects.requireNonNull(terminals);

        SlackTerminal st = voltageLevel.getExtension(SlackTerminal.class);
        if (st == null) {
            var extention = voltageLevel.newExtension(SlackTerminalAdder.class);
            for (var terminal : terminals) {
                extention = extention.withTerminal(terminal);
            }
            extention.add();
        } else {
            st.setTerminals(terminals);
        }
    }

    /**
     * Create a SlackTerminal extension attached to the voltage level of the given bus, using the default
     * {@link com.powsybl.iidm.network.util.TerminalFinder} strategy.
     */
    static void attach(Bus bus) {
        Objects.requireNonNull(bus);

        VoltageLevel vl = bus.getVoltageLevel();

        ArrayList<TerminalWithPriority> terminals = new ArrayList<>();

        for (var terminal : bus.getConnectedTerminals()) {
            terminals.add(new TerminalWithPriorityImpl(terminal, 1));
        }

        reset(vl, terminals);
    }

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the terminals pointed by the current SlackTerminal
     *
     * @return the corresponding terminal and its priority
     */
    List<TerminalWithPriority> getTerminals();

    /**
     * @deprecated Get the terminal pointed by the current SlackTerminal
     * @return the corresponding terminal
     */
    @Deprecated
    Terminal getTerminal();

    /**
     * Set the terminals pointed by the current SlackTerminal
     *
     * @param terminals the corresponding terminals with a priority (may NOT be null)
     * @return the current SlackTerminal
     */
    SlackTerminal setTerminals(List<? extends TerminalWithPriority> terminals);

    default SlackTerminal addTerminal(TerminalWithPriority terminal) {
        var terminals = getTerminals();
        terminals.add(terminal);
        setTerminals(terminals);
        return this;
    }

    /**
     * Reset the current SlackTerminal to point to no terminal
     *
     * @return the current SlackTerminal
     */
    SlackTerminal setNoTerminal();

    /**
     * Reset the current SlackTerminal to point to no terminal Remove the SlackTerminal extension from the corresponding
     * VoltageLevel if the SlackTerminal is empty
     *
     * @return the current SlackTerminal
     */
    default SlackTerminal setNoTerminalAndClean() {
        setNoTerminal();
        if (isEmpty()) {
            getExtendable().removeExtension(SlackTerminal.class);
        }
        return this;
    }

    /**
     * @deprecated Set the terminal pointed by the current SlackTerminal
     * @param terminal the corresponding terminal
     * @return the current SlackTerminal
     */
    @Deprecated
    SlackTerminal setTerminal(Terminal terminal);

    /**
     * @deprecated Set the terminal pointed by the current SlackTerminal
     * @param terminal the corresponding terminal (may be null)
     * @param cleanIfEmpty if true and if the slackTerminal is empty, removes the SlackTerminal extension from the
     *        corresponding VoltageLevel
     * @return the current SlackTerminal
     */
    @Deprecated
    default SlackTerminal setTerminal(Terminal terminal, boolean cleanIfEmpty) {
        setTerminal(terminal);
        if (cleanIfEmpty && terminal == null && isEmpty()) {
            getExtendable().removeExtension(SlackTerminal.class);
        }
        return this;
    }

    /**
     * Returns true if the current SlackTerminal is empty, meaning that this extension is unused
     */
    boolean isEmpty();

}
