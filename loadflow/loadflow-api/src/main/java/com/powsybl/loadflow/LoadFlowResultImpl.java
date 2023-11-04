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
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
public class LoadFlowResultImpl implements LoadFlowResult {

    public static class SlackResultImpl implements SlackResult {

        private final String busId;
        private final double activePowerMismatch;

        public SlackResultImpl(String busId, double activePowerMismatch) {
            this.busId = Objects.requireNonNull(busId);
            this.activePowerMismatch = activePowerMismatch;
        }

        @Override
        public String getBusId() {
            return busId;
        }

        @Override
        public double getBusActivePowerMismatch() {
            return activePowerMismatch;
        }
    }

    public static class ComponentResultImpl implements ComponentResult {

        private final int connectedComponentNum;

        private final int synchronousComponentNum;

        private final Status status;

        private final Map<String, String> metrics;

        private final int iterationCount;

        private final String referenceBusId;

        private final List<SlackResult> slackResults;

        private final double distributedActivePower;

        public ComponentResultImpl(int connectedComponentNum, int synchronousComponentNum, Status status, int iterationCount,
                                   String slackBusId, double slackBusActivePowerMismatch, double distributedActivePower) {
            this.connectedComponentNum = checkComponentNum(connectedComponentNum);
            this.synchronousComponentNum = checkComponentNum(synchronousComponentNum);
            this.status = Objects.requireNonNull(status);
            this.metrics = Collections.emptyMap();
            this.iterationCount = checkIterationCount(iterationCount);
            this.referenceBusId = slackBusId;
            this.slackResults = Collections.singletonList(new SlackResultImpl(slackBusId, slackBusActivePowerMismatch));
            this.distributedActivePower = distributedActivePower;
        }

        public ComponentResultImpl(int connectedComponentNum, int synchronousComponentNum, Status status,
                                   Map<String, String> metrics, int iterationCount, String referenceBusId,
                                   List<SlackResult> slackResults, double distributedActivePower) {
            this.connectedComponentNum = checkComponentNum(connectedComponentNum);
            this.synchronousComponentNum = checkComponentNum(synchronousComponentNum);
            this.status = Objects.requireNonNull(status);
            this.metrics = Objects.requireNonNull(metrics);
            this.iterationCount = checkIterationCount(iterationCount);
            this.referenceBusId = referenceBusId; // allowed to be null
            this.slackResults = Objects.requireNonNull(slackResults);
            this.distributedActivePower = distributedActivePower;
        }

        private static int checkComponentNum(int componentNum) {
            if (componentNum < 0) {
                throw new IllegalArgumentException("Invalid component number: " + componentNum);
            }
            return componentNum;
        }

        private static int checkIterationCount(int iterationCount) {
            if (iterationCount < 0) { // 0 is ok if not relevant for a particular implementation
                throw new IllegalArgumentException("Invalid iteration count: " + iterationCount);
            }
            return iterationCount;
        }

        @Override
        public int getConnectedComponentNum() {
            return connectedComponentNum;
        }

        @Override
        public int getSynchronousComponentNum() {
            return synchronousComponentNum;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public Map<String, String> getMetrics() {
            return metrics;
        }

        @Override
        public int getIterationCount() {
            return iterationCount;
        }

        @Override
        public String getReferenceBusId() {
            return referenceBusId;
        }

        @Override
        public List<SlackResult> getSlackResults() {
            return Collections.unmodifiableList(slackResults);
        }

        @Override
        public String getSlackBusId() {
            if (slackResults.isEmpty()) {
                return "";
            } else if (slackResults.size() == 1) {
                return slackResults.get(0).getBusId();
            } else {
                throw new IllegalStateException("Deprecated method: cannot return a value in the case of multiple slack results. Please migrate to new API.");
            }
        }

        @Override
        public double getSlackBusActivePowerMismatch() {
            if (slackResults.isEmpty()) {
                return 0;
            } else if (slackResults.size() == 1) {
                return slackResults.get(0).getBusActivePowerMismatch();
            } else {
                throw new IllegalStateException("Deprecated method: cannot return a value in the case of multiple slack results. Please migrate to new API.");
            }
        }

        @Override
        public double getDistributedActivePower() {
            return distributedActivePower;
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
