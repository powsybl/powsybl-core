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

    enum ConnectionStatus {
        CONNECTED,
        CONNECTABLE
    }

    /**
     * A bus/breaker view of the terminal.
     */
    public static interface BusBreakerView {

        /**
         * Get the bus of this terminal in the bus/breaker topology.
         * <p>Depends on the working variant.
         * @return the bus in the bus/breaker topology or null if not connected and not connectable
         * @see VariantManager
         */
        Bus getBus();

        /**
         * Get the connection status of the bus.
         * @return the connection status or null if not connected and not connectable
         */
        default ConnectionStatus getConnectionStatus() {
            return null;
        }

        default void setBus(String busId) {
        }

        /**
         * Get a bus that can be used to connected the terminal in the
         * bus/breaker topology.
         * @deprecated bus and connectableBus are redundant, so we use bus and connection status
         */
        @Deprecated
        default Bus getConnectableBus() {
            return getBus();
        }

        @Deprecated
        default void setConnectableBus(String busId) {
            setBus(busId);
        }
    }

    /**
     * A bus view of the terminal.
     */
    public static interface BusView {

        /**
         * Get the bus of this terminal in the bus only topology.
         * <p>Depends on the working variant.
         * @return the bus in the bus only topology or null if not connected and not connectable
         * @see VariantManager
         */
        Bus getBus();

        /**
         * Get the connection status of the bus.
         * @return the connection status or null if not connected and not connectable
         */
        default ConnectionStatus getConnectionStatus() {
            return null;
        }

        /**
         * Get a bus that can be used to connected the terminal in the
         * bus only topology.
         * @deprecated bus and connectableBus are redundant, so we use bus and connection status
         */
        @Deprecated
        default Bus getConnectableBus() {
            return getBus();
        }

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
     * Depends on the working variant.
     * @see VariantManager
     */
    double getP();

    /**
     * Set the active power in MW injected at the terminal.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Terminal setP(double p);

    /**
     * Get the reactive power in MVAR injected at the terminal.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getQ();

    /**
     * Set the reactive power in MVAR injected at the terminal.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Terminal setQ(double q);

    /**
     * Get the current in A at the terminal.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getI();

    /**
     * Try to connect the terminal.
     * <p>Depends on the working variant.
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    boolean connect();

    /**
     * Disconnect the terminal.
     * <p>Depends on the working variant.
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
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
