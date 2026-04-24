/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides information about observability areas for buses in this voltage level.
 *
 * <p>The observability areas are associated to the topology as it was
 * when the computation was run. That topology can be inconsistent
 * with the current topology of the voltage level. The extension provides
 * different access methods to ease the management of that issue.
 *
 * <ul>
 *     <li>{@link AreaCharacteristics} expose that "snapshot" of the topology at the time of computation,
 *     for one bus.</li>
 *     <li>the {@link BusView} allows to retrieve the area corresponding to buses of
 *     the current topology, for buses which have not changed since the "snapshot".</li>
 *     <li>the {@link BusBreakerView} allows to retrieve the area corresponding to buses of
 *     the current bus-breaker topology, for buses which have not changed since the "snapshot".</li>
 * </ul>
 *
 * <p>This extension should not handle any logic to invalidate observability results after topological changes.
 * Indeed, observability results are tightly coupled to the topology, so any topological change could invalidate them.
 * It is rather challenging to evaluate the impact of a topological change on observability results.
 * Therefore, it is the responsibility of the user to manage the observability areas when topological changes occur.
 *
 * <p>To guarantee a minimum of consistency, the extension provides a method to check if the observability
 * areas are still valid, according to the current topology. It should return false if some
 * topological elements (buses, nodes,...) associated with the observability areas are missing.
 *
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface ObservabilityArea extends Extension<VoltageLevel> {

    String NAME = "observabilityArea";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * A bus can be either:
     * <ul>
     *     <li>observable, and inside an observable area</li>
     *     <li>observable, but at the border of an observable area.
     *     The distinction with the plain observable status is important because the injection at the border
     *     should be considered unobservable.</li>
     *     <li>non observable</li>
     * </ul>
     */
    enum ObservabilityStatus {
        OBSERVABLE,
        NON_OBSERVABLE,
        BORDER
    }

    /**
     * Provides information about the observable or unobservable area for
     * one bus, as identified at the time of computation, which we will call
     * a "snapshot" bus.
     */
    interface AreaCharacteristics {

        /**
         * Provides data specific to node-breaker topology.
         */
        interface NodeBreakerData {
            /**
             * The list of nodes that compose that snapshot bus.
             */
            Set<Integer> getNodes();
        }

        /**
         * Provides data specific to bus-breaker topology.
         */
        interface BusBreakerData {
            /**
             * The list of "bus-breaker" buses that compose that snapshot bus.
             */
            Set<String> getBusIds();
        }

        /**
         * The observable or unobservable area number, depending on the observability status of that snapshot bus.
         */
        int getAreaNumber();

        /**
         * The observability status for this snapshot bus.
         */
        ObservabilityStatus getStatus();

        /**
         * The terminals which are part of this snapshot bus.
         */
        Set<Terminal> getTerminals();

        /**
         * Provides information, for this snapshot bus, specific to node-breaker topology level.
         */
        NodeBreakerData getNodeBreakerData();

        /**
         * Provides data, for this snapshot bus, specific to bus-breaker topology level.
         */
        BusBreakerData getBusBreakerData();
    }

    /**
     * Provides access to observable areas for nodes of the node-breaker topology.
     */
    interface NodeBreakerView {
        Map<Integer, AreaCharacteristics> getObservabilityAreaByNode();

        AreaCharacteristics getObservabilityArea(int node);
    }

    /**
     * Provides access to observable areas for buses of the current bus-breaker topology.
     *
     * <p>If a bus does not map correctly to a "snapshot bus" as they were identified at the time of computation,
     * it may throw an exception or return a {@code null} object.
     */
    interface BusBreakerView extends BusView {
    }

    /**
     * Provides access to observable areas for buses of the current topology.
     *
     * <p>If a bus does not map correctly to a "snapshot bus" as they were identified at the time of computation,
     * it may throw an exception or return a {@code null} object.
     */
    interface BusView {
        Map<String, AreaCharacteristics> getObservabilityAreaByBus();

        Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException);

        AreaCharacteristics getObservabilityArea(String busId);

        AreaCharacteristics getObservabilityArea(String busId, boolean throwException);
    }

    /**
     * Provides access to observable areas for nodes of the node-breaker topology.
     */
    NodeBreakerView getNodeBreakerView();

    /**
     * Provides access to observable areas for buses of the current bus-breaker topology.
     *
     * <p>If a bus does not map correctly to a "snapshot bus" as they were identified at the time of computation,
     * it may throw an exception or return a {@code null} object.
     */
    BusBreakerView getBusBreakerView();

    /**
     * Provides access to observable areas for buses of the current topology.
     *
     * <p>If a bus does not map correctly to a "snapshot bus" as they were identified at the time of computation,
     * it may throw an exception or return a {@code null} object.
     */
    BusView getBusView();

    /**
     * The area for one terminal.
     *
     * <p>Note that unlike methods of the bus view or the bus-breaker view,
     * this method still allows to retrieve the area even when the topology has changed.
     */
    AreaCharacteristics getObservabilityArea(Terminal terminal);

    Collection<AreaCharacteristics> getObservabilityAreas();

    /**
     * {@code true} if snapshot buses are consistent with the current topology buses.
     */
    boolean isConsistentWithTopology();
}
