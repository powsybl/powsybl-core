/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class LoadFlowResultImpl implements LoadFlowResult {

    public static class SlackBusResultImpl implements SlackBusResult {

        private final String id;
        private final double activePowerMismatch;

        public SlackBusResultImpl(String id, double activePowerMismatch) {
            this.id = Objects.requireNonNull(id);
            this.activePowerMismatch = activePowerMismatch;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public double getActivePowerMismatch() {
            return activePowerMismatch;
        }
    }

    public static class ComponentResultImpl implements ComponentResult {

        private final int connectedComponentNum;

        private final int synchronousComponentNum;

        private final Status status;

        private final String statusText;

        private final Map<String, String> metrics;

        private final int iterationCount;

        private final String referenceBusId;

        private final List<SlackBusResult> slackBusResults;

        private final double distributedActivePower;

        public ComponentResultImpl(int connectedComponentNum, int synchronousComponentNum, Status status, int iterationCount,
                                   String slackBusId, double slackBusActivePowerMismatch, double distributedActivePower) {
            this.connectedComponentNum = checkComponentNum(connectedComponentNum);
            this.synchronousComponentNum = checkComponentNum(synchronousComponentNum);
            this.status = Objects.requireNonNull(status);
            this.statusText = status.name();
            this.metrics = Collections.emptyMap();
            this.iterationCount = checkIterationCount(iterationCount);
            this.referenceBusId = slackBusId;
            this.slackBusResults = Collections.singletonList(new SlackBusResultImpl(slackBusId, slackBusActivePowerMismatch));
            this.distributedActivePower = distributedActivePower;
        }

        public ComponentResultImpl(int connectedComponentNum, int synchronousComponentNum, Status status,
                                   String statusText, Map<String, String> metrics, int iterationCount,
                                   String referenceBusId, List<SlackBusResult> slackBusResults,
                                   double distributedActivePower) {
            this.connectedComponentNum = checkComponentNum(connectedComponentNum);
            this.synchronousComponentNum = checkComponentNum(synchronousComponentNum);
            this.status = Objects.requireNonNull(status);
            this.statusText = Objects.requireNonNullElse(statusText, status.name());
            this.metrics = Objects.requireNonNull(metrics);
            this.iterationCount = checkIterationCount(iterationCount);
            this.referenceBusId = referenceBusId; // allowed to be null
            this.slackBusResults = Objects.requireNonNull(slackBusResults);
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
        public String getStatusText() {
            return statusText;
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
        public List<SlackBusResult> getSlackBusResults() {
            return Collections.unmodifiableList(slackBusResults);
        }

        @Override
        public String getSlackBusId() {
            if (slackBusResults.isEmpty()) {
                return "";
            } else if (slackBusResults.size() == 1) {
                return slackBusResults.get(0).getId();
            } else {
                throw new IllegalStateException("Deprecated method: cannot return a value in the case of multiple slack results. Please migrate to new API.");
            }
        }

        @Override
        public double getSlackBusActivePowerMismatch() {
            if (slackBusResults.isEmpty()) {
                return 0;
            } else if (slackBusResults.size() == 1) {
                return slackBusResults.get(0).getActivePowerMismatch();
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
    private final Status status;
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
        this.status = computeStatus(componentResults);
    }

    private Status computeStatus(List<ComponentResult> componentResults) {
        int convergedCount = 0;
        int maxIterOrFailedCount = 0;
        for (ComponentResult componentResult : componentResults) {
            ComponentResult.Status componentResultStatus = Objects.requireNonNull(componentResult.getStatus());
            if (componentResultStatus == ComponentResult.Status.CONVERGED) {
                convergedCount++;
            } else if (componentResultStatus == ComponentResult.Status.MAX_ITERATION_REACHED || componentResultStatus == ComponentResult.Status.FAILED) {
                maxIterOrFailedCount++;
            }
        }
        if (convergedCount == 0) {
            return Status.FAILED;
        } else if (maxIterOrFailedCount > 0) {
            return Status.PARTIALLY_CONVERGED;
        } else {
            return Status.FULLY_CONVERGED;
        }
    }

    @Override
    public boolean isOk() {
        return ok;
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
    public String getLogs() {
        return logs;
    }

    @Override
    public List<ComponentResult> getComponentResults() {
        return Collections.unmodifiableList(componentResults);
    }
}
