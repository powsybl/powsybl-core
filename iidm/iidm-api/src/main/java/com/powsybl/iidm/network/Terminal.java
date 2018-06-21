/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * An equipment connection point in a substation topology.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Terminal {

    /**
     * A node/breaker view of the terminal.
     */
    public static interface NodeBreakerView {

        /**
         * Get the connection node of this terminal in a node/breaker topology.
         */
        int getNode();

    }

    /**
     * A bus/breaker view of the terminal.
     */
    public static interface BusBreakerView {

        /**
         * Get the connection bus of this terminal in the bus/breaker topology.
         * <p>Depends on the working state.
         * @return the connection bus in the bus/breaker topology or null if not connected
         * @see StateManager
         */
        Bus getBus();

        /**
         * Get a bus that can be used to connected the terminal in the
         * bus/breaker topology.
         */
        Bus getConnectableBus();

        void setConnectableBus(String busId);
    }

    /**
     * A bus view of the terminal.
     */
    public static interface BusView {

        /**
         * Get the connection bus of this terminal in the bus only topology.
         * <p>Depends on the working state.
         * @return the connection bus in the bus only topology or null if not connected
         * @see StateManager
         */
        Bus getBus();

        /**
         * Get a bus that can be used to connected the terminal in the
         * bus only topology.
         */
        Bus getConnectableBus();

    }

    /**
     * Get the substation to which the terminal belongs.
     */
    VoltageLevel getVoltageLevel();

    /**
     * Get a view to access to node/breaker topology informations at the terminal.
     */
    NodeBreakerView getNodeBreakerView();

    /**
     * Get a view to access to bus/breaker topology informations at the terminal.
     */
    BusBreakerView getBusBreakerView();

    /**
     * Get a view to access to bus topology informations at the terminal.
     */
    BusView getBusView();

    /**
     * Get the equipment that is connected to the terminal.
     */
    Connectable getConnectable();

    /**
     * Get the active power in MW injected at the terminal.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    double getP();

    /**
     * Set the active power in MW injected at the terminal.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Terminal setP(double p);

    /**
     * Get the reactive power in MVAR injected at the terminal.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    double getQ();

    /**
     * Set the reactive power in MVAR injected at the terminal.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Terminal setQ(double q);

    /**
     * Get the current in A at the terminal.
     * <p>Depends on the working state.
     * @see StateManager
     */
    double getI();

    /**
     * Try to connect the terminal.
     * <p>Depends on the working state.
     * @return true if terminal has been connected, false otherwise
     * @see StateManager
     */
    boolean connect();

    /**
     * Disconnect the terminal.
     * <p>Depends on the working state.
     * @return true if terminal has been disconnected, false otherwise
     * @see StateManager
     */
    boolean disconnect();

    /**
     * Test if the terminal is connected.
     * @return true if the terminal is connected, false otherwise
     */
    boolean isConnected();

    /**
     * Traverse the full network topology graph.
     * @param traverser traversal handler
     */
    void traverse(VoltageLevel.TopologyTraverser traverser);

}
