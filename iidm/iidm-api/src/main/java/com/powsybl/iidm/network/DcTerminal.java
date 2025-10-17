/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;

import java.util.Set;
/**
 * A DC equipment connection point in a DC system.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */

public interface DcTerminal {

    /**
     * @return the DC equipment the DC terminal belongs to
     */
    DcConnectable getDcConnectable();

    /**
     * @return the DC equipment side
     */
    TwoSides getSide();

    /**
     * @return the DC equipment terminal number
     */
    TerminalNumber getTerminalNumber();

    /**
     * @return the DC node the DC terminal connects to
     */
    DcNode getDcNode();

    /**
     * @return true if the DC Terminal is connected to the DC node.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    boolean isConnected();

    /**
     * @param connected new connected status.<br/>
     *                  Depends on the working variant.
     * @return self for method chaining
     * @see VariantManager
     */
    DcTerminal setConnected(boolean connected);

    /**
     * Get the DC connection bus of this DC terminal.
     * <p>Depends on the working variant.
     * @return the DC connection bus or null if not connected
     * @see VariantManager
     */
    DcBus getDcBus();

    /**
     * @return the active power in MW injected at the DC terminal.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getP();

    /**
     * @param p new active power in MW injected at the DC terminal.<br/>
     * Depends on the working variant.
     * @return self for method chaining
     * @see VariantManager
     */
    DcTerminal setP(double p);

    /**
     * @return the current in A at the DC terminal.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getI();

    /**
     * @param i new current in A injected at the DC terminal.<br/>
     * Depends on the working variant.
     * @return self for method chaining
     * @see VariantManager
     */
    DcTerminal setI(double i);

    void traverse(DcTerminal.TopologyTraverser traverser);

    void traverse(DcTerminal.TopologyTraverser traverser, TraversalType traversalType);

    interface TopologyTraverser {

        /**
         * Called when a DC terminal is encountered.
         *
         * @param terminal  the encountered DC terminal
         * @param connected in bus/breaker topology, give the DC terminal connection status
         * @return {@link TraverseResult#CONTINUE} to continue traversal, {@link TraverseResult#TERMINATE_PATH}
         * to stop the current traversal path, {@link TraverseResult#TERMINATE_TRAVERSER} to stop all the traversal paths
         */

        TraverseResult traverse(DcTerminal terminal, boolean connected);
    }

    boolean traverse(DcTerminal.TopologyTraverser traverser, Set<DcTerminal> visitedDcTerminals, TraversalType traversalType);

    /**
     * Disconnect the DC terminal.<br/>
     * Depends on the working variant.
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    boolean disconnect();
}
