/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Loadflow result API.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface LoadFlowResult {

    /**
     * Result for one slack bus
     */
    interface SlackBusResult {
        /**
         * Get the slack bus id.
         * @return the slack bus id
         */
        String getId();

        /**
         * Get slack bus active power mismatch in MW.
         * @return the slack bus active power mismatch in MW
         */
        double getActivePowerMismatch();
    }

    /**
     * Result for one component
     */
    interface ComponentResult {

        /**
         * Calculation status
         */
        enum Status {
            /**
             * Loadflow converged
             */
            CONVERGED,
            /**
             * Non-convergence by iteration limit
             */
            MAX_ITERATION_REACHED,
            /**
             * Non-convergence by solver failure
             */
            SOLVER_FAILED,
            /**
             * Non-convergence
             */
            FAILED,
            /**
             * Component was not calculated, e.g. de-energized component without any voltage support
             */
            NO_CALCULATION
        }

        /**
         * Get connected component number.
         * @return connected component number
         */
        int getConnectedComponentNum();

        /**
         * Get synchronous component number.
         * @return component number
         */
        int getSynchronousComponentNum();

        /**
         * Get detailed status of the computation on this component.
         * @return the detailed status of the computation on this component
         */
        Status getStatus();

        /**
         * Get status text of the computation on this component. Status text is implementation specific
         * and can be used to provide additional information about e.g. failed reason.
         * @return the status text of the computation on this component
         */
        String getStatusText();

        /**
         * Get metrics. Metrics are generic key/value pairs and are specific to a loadflow implementation.
         * @return the metrics for this component
         */
        Map<String, String> getMetrics();

        /**
         * Get iteration count.
         * @return the iteration count
         */
        int getIterationCount();

        /**
         * Get the reference bus id (angle reference)
         * @return the reference bus id
         */
        String getReferenceBusId();

        /**
         * get slack results of all slack buses
         * @return list of slack results
         */
        List<SlackBusResult> getSlackBusResults();

        /**
         * Get the slack bus id.
         * @return the slack bus id
         * @deprecated use {@link #getSlackBusResults()} instead
         */
        @Deprecated(since = "6.1.0")
        String getSlackBusId();

        /**
         * Get slack bus active power mismatch in MW.
         * @return the slack bus active power mismatch in MW
         * @deprecated use {@link #getSlackBusResults()} instead
         */
        @Deprecated(since = "6.1.0")
        double getSlackBusActivePowerMismatch();

        /**
         * If distributed slack is activated {@link LoadFlowParameters#isDistributedSlack()}, the active power in MW
         * that has been distributed from slack bus to generators/loads (depending of {@link LoadFlowParameters#getBalanceType()} value)
         * @return the active power in MW that has been distributed
         */
        double getDistributedActivePower();
    }

    /**
     * Get the global status. Must be set to true/ok if <b>at least one</b> component has converged.
     * Note that when computing multiple components (islands), it is preferable to evaluate
     * the individual component status using {@link ComponentResult#getStatus()}.
     * @return the global status
     */
    boolean isOk();

    /**
     * Get metrics. Metrics are generic key/value pairs and are specific to a loadflow implementation.
     * @return the metrics
     */
    Map<String, String> getMetrics();

    /**
     * Get logs.
     * @return logs
     */
    String getLogs();

    /**
     * Get per component results.
     * @return per component results
     */
    default List<ComponentResult> getComponentResults() {
        return Collections.emptyList();
    }
}
