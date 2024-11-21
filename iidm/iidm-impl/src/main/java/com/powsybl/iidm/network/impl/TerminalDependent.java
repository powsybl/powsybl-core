/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TerminalDependent {

    /**
     * Register a terminal so that {@link TerminalDependent#onReferencedTerminalRemoval(Terminal)} is called when
     * the connectable is removed from the network.
     *
     * @param terminal The terminal to register.
     */
    void registerReferencedTerminal(Terminal terminal);

    /**
     * Unregisters a previously registered terminal. This ensures that
     * {@link TerminalDependent#onReferencedTerminalRemoval(Terminal)} won't be called anymore if the connectable
     * is removed from the network.
     *
     * @param terminal The terminal to unregister.
     */
    void unregisterReferencedTerminal(Terminal terminal);

    /**
     * Called when a referenced terminal is removed because of a connectable removal.
     * Implementations of this method should handle any required cleanup or updates
     * necessary when the terminal is no longer part of the network.
     *
     * @param terminal The terminal that has been removed from the network.
     */
    void onReferencedTerminalRemoval(Terminal terminal);
}
