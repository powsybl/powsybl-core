/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * An equipment connection point in a substation topology.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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

        /**
         * Move the connectable end on the given side to the given node of the given voltage level.
         * If the given voltage level's topology is not NODE-BREAKER, a runtime exception is thrown.
         */
        void moveConnectable(int node, String voltageLevelId);

    }

    /**
     * A bus/breaker view of the terminal.
     */
    public static interface BusBreakerView {

        /**
         * Get the connection bus of this terminal in the bus/breaker topology.
         * <p>Depends on the working variant.
         * @return the connection bus in the bus/breaker topology or null if not connected
         * @see VariantManager
         */
        Bus getBus();

        /**
         * Get a bus that can be used to connected the terminal in the
         * bus/breaker topology.
         */
        Bus getConnectableBus();

        void setConnectableBus(String busId);

        /**
         * Move the connectable end on the given side to the given connectable bus with the given connection status.
         * If the given bus' voltage level topology is not BUS-BREAKER, a runtime exception is thrown.
         */
        void moveConnectable(String busId, boolean connected);
    }

    /**
     * A bus view of the terminal.
     */
    public static interface BusView {

        /**
         * Get the connection bus of this terminal in the bus only topology.
         * <p>Depends on the working variant.
         * @return the connection bus in the bus only topology or null if not connected
         * @see VariantManager
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
     * Get the active power in MW injected at the terminal.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getP();

    /**
     * Set the active power in MW injected at the terminal.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    Terminal setP(double p);

    /**
     * Get the reactive power in MVAR injected at the terminal.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getQ();

    /**
     * Set the reactive power in MVAR injected at the terminal.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    Terminal setQ(double q);

    /**
     * Get the current in A at the terminal.<br/>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getI();

    /**
     * Try to connect the terminal.<br/>
     * Depends on the working variant.
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    default boolean connect() {
        return connect(false);
    }

    /**
     * Try to connect the terminal.<br/>
     * Depends on the working variant.
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    boolean connect(boolean dryRun);

    /**
     * Try to connect the terminal.<br/>
     * Depends on the working variant.
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    default boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        return connect(isTypeSwitchToOperate, false);
    }

    /**
     * Try to connect the terminal.<br/>
     * Depends on the working variant.
     * @return true if terminal has been connected, false otherwise
     * @see VariantManager
     */
    boolean connect(Predicate<Switch> isTypeSwitchToOperate, boolean dryRun);

    /**
     * Disconnect the terminal.<br/>
     * Depends on the working variant.
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    default boolean disconnect() {
        return disconnect(false);
    }

    /**
     * Disconnect the terminal.<br/>
     * Depends on the working variant.
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    boolean disconnect(boolean dryRun);

    /**
     * Disconnect the terminal.<br/>
     * Depends on the working variant.
     * @param isSwitchOpenable predicate telling if a switch is considered openable
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    default boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        return disconnect(isSwitchOpenable, false);
    }

    /**
     * Disconnect the terminal.<br/>
     * Depends on the working variant.
     * @param isSwitchOpenable predicate telling if a switch is considered openable
     * @return true if terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    boolean disconnect(Predicate<Switch> isSwitchOpenable, boolean dryRun);

    /**
     * Test if the terminal is connected.
     * @return true if the terminal is connected, false otherwise
     */
    boolean isConnected();

    /**
     * Traverse the full network topology graph.
     * @param traverser traversal handler
     */
    void traverse(TopologyTraverser traverser);

    /**
     * Traverse the full network topology graph.
     * @param traverser traversal handler
     * @param traversalType traversal type
     */
    void traverse(TopologyTraverser traverser, TraversalType traversalType);

    /**
     * Topology traversal handler
     */
    interface TopologyTraverser {

        /**
         * Called when a terminal is encountered.
         *
         * @param terminal  the encountered terminal
         * @param connected in bus/breaker topology, give the terminal connection status
         * @return {@link TraverseResult#CONTINUE} to continue traversal, {@link TraverseResult#TERMINATE_PATH}
         * to stop the current traversal path, {@link TraverseResult#TERMINATE_TRAVERSER} to stop all the traversal paths
         */
        TraverseResult traverse(Terminal terminal, boolean connected);

        /**
         * Called when a switch is encountered
         *
         * @param aSwitch the encountered switch
         * @return {@link TraverseResult#CONTINUE} to continue traversal, {@link TraverseResult#TERMINATE_PATH}
         * to stop the current traversal path, {@link TraverseResult#TERMINATE_TRAVERSER} to stop all the traversal paths
         */
        TraverseResult traverse(Switch aSwitch);

    }

    static Optional<ThreeSides> getConnectableSide(Terminal terminal) {
        Connectable<?> c = terminal.getConnectable();
        if (c instanceof Injection) {
            return Optional.empty();
        } else if (c instanceof Branch<?> branch) {
            return Optional.of(branch.getSide(terminal).toThreeSides());
        } else if (c instanceof ThreeWindingsTransformer transformer) {
            return Optional.of(transformer.getSide(terminal));
        } else {
            throw new IllegalStateException("Unexpected Connectable instance: " + c.getClass());
        }
    }

    static Terminal getTerminal(Identifiable<?> identifiable, ThreeSides side) {
        if (identifiable instanceof Injection<?> injection) {
            return injection.getTerminal();
        } else if (identifiable instanceof Branch<?> branch) {
            return branch.getTerminal(side.toTwoSides());
        } else if (identifiable instanceof ThreeWindingsTransformer transformer) {
            return transformer.getTerminal(side);
        } else {
            throw new PowsyblException("Unexpected terminal reference identifiable instance: " + identifiable.getClass());
        }
    }

    ThreeSides getSide();
}
