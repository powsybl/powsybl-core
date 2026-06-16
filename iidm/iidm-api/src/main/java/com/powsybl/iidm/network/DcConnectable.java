/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.List;

/**
 * Equipment that is part of DC topology.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcConnectable<I extends DcConnectable<I>> extends Identifiable<I> {

    /**
     * @return the DC Terminals of the DC connectable
     */
    List<DcTerminal> getDcTerminals();

    /**
     * Remove the DC connectable from the network.
     */
    void remove();

    /**
     * Try to connect the DC connectable
     * @return true if the connection by this operation succeeded, false otherwise (the DC connectable was already connected)
     */
    boolean connectDc();

    /**
     * Try to disconnect the DC connectable
     * @return true if the disconnection by this operation succeeded, false otherwise (the DC connectable was already disconnected)
     */
    boolean disconnectDc();

    /**
     * Removes all loadflow output values of this DC connectable (e.g., P and I on DC terminals).
     */
    default void unsetSolvedValues() {
        getDcTerminals().forEach(DcTerminal::unsetSolvedValues);
    }
}
