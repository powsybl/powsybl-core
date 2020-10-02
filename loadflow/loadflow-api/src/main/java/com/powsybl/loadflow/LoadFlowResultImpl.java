/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class LoadFlowResultImpl implements LoadFlowResult {

    public static class ComponentResultImpl implements ComponentResult {

        private final String status;

        private final int iterationCount;

        private final String slackBusId;

        private final double slackBusActivePowerMismatch;

        public ComponentResultImpl(String status, int iterationCount, String slackBusId, double slackBusActivePowerMismatch) {
            this.status = Objects.requireNonNull(status);
            this.iterationCount = checkIterationCount(iterationCount);
            this.slackBusId = Objects.requireNonNull(slackBusId);
            this.slackBusActivePowerMismatch = slackBusActivePowerMismatch;
        }

        private static int checkIterationCount(int iterationCount) {
            if (iterationCount < 0) { // 0 is ok if not relevant for a particular implementation
                throw new IllegalArgumentException("Invalid iteration count: " + iterationCount);
            }
            return iterationCount;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public int getIterationCount() {
            return iterationCount;
        }

        @Override
        public String getSlackBusId() {
            return slackBusId;
        }

        @Override
        public double getSlackBusActivePowerMismatch() {
            return slackBusActivePowerMismatch;
        }
    }

    private final boolean ok;
    private final Map<String, String> metrics;
    private final String logs;
    private final List<ComponentResult> componentResults;

    public LoadFlowResultImpl(boolean ok, Map<String, String> metrics, String logs) {
        this(ok, metrics, logs, Collections.emptyList());
    }

    public LoadFlowResultImpl(boolean ok, Map<String, String> metrics, String logs, List<ComponentResult> componentResults) {
        this.ok = ok;
        this.metrics = Objects.requireNonNull(metrics);
        this.logs = logs;
        this.componentResults = Objects.requireNonNull(componentResults);
    }

    @Override
    public boolean isOk() {
        return ok;
    }

    @Override
    public Map<String, String> getMetrics() {
        return metrics;
    }

    @Override
    public String getLogs() {
        return logs;
    }

    @Override
    public List<ComponentResult> getComponentResults() {
        return Collections.unmodifiableList(componentResults);
    }
}
