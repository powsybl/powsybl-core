/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable {@link StateEstimationResult}. Every collection handed in is copied.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class StateEstimationResultImpl extends AbstractExtendable<StateEstimationResult>
        implements StateEstimationResult {

    private final Status status;
    private final Map<String, String> metrics;
    private final String logs;
    private final List<ComponentResult> componentResults;
    private final List<BusResult> busResults;
    private final List<MeasurementResult> measurementResults;
    private final ObservabilityResult observabilityResult;

    public StateEstimationResultImpl(Status status, Map<String, String> metrics, String logs,
                                     List<ComponentResult> componentResults, List<BusResult> busResults,
                                     List<MeasurementResult> measurementResults, ObservabilityResult observabilityResult) {
        this.status = Objects.requireNonNull(status);
        this.metrics = Map.copyOf(Objects.requireNonNull(metrics));
        this.logs = logs;
        this.componentResults = List.copyOf(Objects.requireNonNull(componentResults));
        this.busResults = List.copyOf(Objects.requireNonNull(busResults));
        this.measurementResults = List.copyOf(Objects.requireNonNull(measurementResults));
        this.observabilityResult = Objects.requireNonNull(observabilityResult);
    }

    /** A failed estimation: no state, no diagnostics, nothing observable. */
    public static StateEstimationResultImpl failed(String logs) {
        return new StateEstimationResultImpl(Status.FAILED, Map.of(), logs, List.of(), List.of(), List.of(),
                new ObservabilityResultImpl(List.of(), List.of()));
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
        return componentResults;
    }

    @Override
    public List<BusResult> getBusResults() {
        return busResults;
    }

    @Override
    public List<MeasurementResult> getMeasurementResults() {
        return measurementResults;
    }

    @Override
    public ObservabilityResult getObservabilityResult() {
        return observabilityResult;
    }

    public static class ComponentResultImpl implements ComponentResult {

        private final int connectedComponentNum;
        private final int synchronousComponentNum;
        private final Status status;
        private final String statusText;
        private final int iterationCount;
        private final String referenceBusId;
        private final double objectiveFunctionValue;
        private final Map<String, String> metrics;

        public ComponentResultImpl(int connectedComponentNum, int synchronousComponentNum, Status status, String statusText,
                                   int iterationCount, String referenceBusId, double objectiveFunctionValue,
                                   Map<String, String> metrics) {
            this.connectedComponentNum = connectedComponentNum;
            this.synchronousComponentNum = synchronousComponentNum;
            this.status = Objects.requireNonNull(status);
            this.statusText = statusText;
            this.iterationCount = iterationCount;
            this.referenceBusId = referenceBusId;
            this.objectiveFunctionValue = objectiveFunctionValue;
            this.metrics = Map.copyOf(Objects.requireNonNullElse(metrics, Collections.emptyMap()));
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
        public int getIterationCount() {
            return iterationCount;
        }

        @Override
        public String getReferenceBusId() {
            return referenceBusId;
        }

        @Override
        public double getObjectiveFunctionValue() {
            return objectiveFunctionValue;
        }

        @Override
        public Map<String, String> getMetrics() {
            return metrics;
        }
    }

    public static class BusResultImpl implements BusResult {

        private final String busId;
        private final double v;
        private final double angle;

        public BusResultImpl(String busId, double v, double angle) {
            this.busId = Objects.requireNonNull(busId);
            this.v = v;
            this.angle = angle;
        }

        @Override
        public String getBusId() {
            return busId;
        }

        @Override
        public double getV() {
            return v;
        }

        @Override
        public double getAngle() {
            return angle;
        }
    }

    public static class MeasurementResultImpl implements MeasurementResult {

        private final String measurementId;
        private final double measuredValue;
        private final double estimatedValue;
        private final double normalizedResidual;
        private final boolean flagged;

        public MeasurementResultImpl(String measurementId, double measuredValue, double estimatedValue,
                                     double normalizedResidual, boolean flagged) {
            this.measurementId = Objects.requireNonNull(measurementId);
            this.measuredValue = measuredValue;
            this.estimatedValue = estimatedValue;
            this.normalizedResidual = normalizedResidual;
            this.flagged = flagged;
        }

        @Override
        public String getMeasurementId() {
            return measurementId;
        }

        @Override
        public double getMeasuredValue() {
            return measuredValue;
        }

        @Override
        public double getEstimatedValue() {
            return estimatedValue;
        }

        @Override
        public double getResidual() {
            return measuredValue - estimatedValue;
        }

        @Override
        public double getNormalizedResidual() {
            return normalizedResidual;
        }

        @Override
        public boolean isFlagged() {
            return flagged;
        }
    }

    public static class ObservabilityResultImpl implements ObservabilityResult {

        private final List<Area> areas;
        private final List<ElementObservability> elementObservabilities;

        public ObservabilityResultImpl(List<Area> areas, List<ElementObservability> elementObservabilities) {
            this.areas = List.copyOf(Objects.requireNonNull(areas));
            this.elementObservabilities = List.copyOf(Objects.requireNonNull(elementObservabilities));
        }

        @Override
        public List<Area> getAreas() {
            return areas;
        }

        @Override
        public Optional<Area> getArea(String busId) {
            Objects.requireNonNull(busId);
            return areas.stream().filter(a -> a.getBusIds().contains(busId)).findFirst();
        }

        @Override
        public List<ElementObservability> getElementObservabilities() {
            return elementObservabilities;
        }

        @Override
        public Optional<ElementObservability> getElementObservability(String elementId) {
            Objects.requireNonNull(elementId);
            return elementObservabilities.stream().filter(e -> e.getElementId().equals(elementId)).findFirst();
        }
    }

    public static class AreaImpl implements ObservabilityResult.Area {

        private final int areaNumber;
        private final ObservabilityResult.AreaStatus status;
        private final Set<String> busIds;

        public AreaImpl(int areaNumber, ObservabilityResult.AreaStatus status, Set<String> busIds) {
            this.areaNumber = areaNumber;
            this.status = Objects.requireNonNull(status);
            this.busIds = Set.copyOf(Objects.requireNonNull(busIds));
        }

        @Override
        public int getAreaNumber() {
            return areaNumber;
        }

        @Override
        public ObservabilityResult.AreaStatus getStatus() {
            return status;
        }

        @Override
        public Set<String> getBusIds() {
            return busIds;
        }
    }

    public static class ElementObservabilityImpl implements ObservabilityResult.ElementObservability {

        private final String elementId;
        private final boolean observable;
        private final List<ObservabilityResult.Quality> qualities;

        public ElementObservabilityImpl(String elementId, boolean observable, List<ObservabilityResult.Quality> qualities) {
            this.elementId = Objects.requireNonNull(elementId);
            this.observable = observable;
            this.qualities = List.copyOf(Objects.requireNonNull(qualities));
        }

        @Override
        public String getElementId() {
            return elementId;
        }

        @Override
        public boolean isObservable() {
            return observable;
        }

        @Override
        public List<ObservabilityResult.Quality> getQualities() {
            return qualities;
        }
    }

    public static class QualityImpl implements ObservabilityResult.Quality {

        private final ObservabilityResult.Quantity quantity;
        private final ThreeSides side;
        private final double standardDeviation;
        private final Boolean redundant;

        public QualityImpl(ObservabilityResult.Quantity quantity, ThreeSides side, double standardDeviation, Boolean redundant) {
            this.quantity = Objects.requireNonNull(quantity);
            this.side = side;
            this.standardDeviation = standardDeviation;
            this.redundant = redundant;
        }

        @Override
        public ObservabilityResult.Quantity getQuantity() {
            return quantity;
        }

        @Override
        public Optional<ThreeSides> getSide() {
            return Optional.ofNullable(side);
        }

        @Override
        public double getStandardDeviation() {
            return standardDeviation;
        }

        @Override
        public Optional<Boolean> isRedundant() {
            return Optional.ofNullable(redundant);
        }
    }
}
