/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadFlowResult {

    /**
     * Result for one component
     */
    interface ComponentResult {

        enum Status {
            CONVERGED,
            MAX_ITERATION_REACHED,
            SOLVER_FAILED,
            FAILED
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
         * Get iteration count.
         * @return the iteration count
         */
        int getIterationCount();

        /**
         * Get the slack bus id.
         * @return the slack bus id
         */
        String getSlackBusId();

        /**
         * Get slack bus active power mismatch in MW.
         * @return the slack bus active power mismatch in MW
         */
        double getSlackBusActivePowerMismatch();
    }

    /**
     * Get the global status. It is expected to be ok if at least one component has converged.
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
