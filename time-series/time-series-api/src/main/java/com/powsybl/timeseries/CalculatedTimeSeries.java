/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Iterators;
import com.powsybl.timeseries.ast.*;

import java.nio.DoubleBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CalculatedTimeSeries implements DoubleTimeSeries {

    private TimeSeriesMetadata metadata;

    private final NodeCalc nodeCalc;

    private final List<DoubleTimeSeries> timeSeriesList;

    private final NodeCalc resolvedNodeCalc;

    public CalculatedTimeSeries(String name, NodeCalc nodeCalc, ReadOnlyTimeSeriesStore timeSeriesStore, int version) {
        this.nodeCalc = Objects.requireNonNull(nodeCalc);
        Objects.requireNonNull(timeSeriesStore);
        metadata = getMetadata(name, nodeCalc, timeSeriesStore);

        Set<String> timeSeriesNames = TimeSeriesNames.list(nodeCalc);
        if (timeSeriesNames.isEmpty()) {
            timeSeriesList = Collections.emptyList();
        } else {
            timeSeriesList = timeSeriesStore.getDoubleTimeSeries(timeSeriesNames, version);
        }

        NodeCalc simplifiedNodeCalc = NodeCalcSimplifier.simplify(nodeCalc);

        Map<String, Integer> timeSeriesNums = IntStream.range(0, timeSeriesList.size())
                .boxed()
                .collect(Collectors.toMap(i -> timeSeriesList.get(i).getMetadata().getName(),
                                          Function.identity()));
        resolvedNodeCalc = NodeCalcResolver.resolve(simplifiedNodeCalc, timeSeriesNums);
    }

    public static TimeSeriesMetadata getMetadata(String name, NodeCalc nodeCalc, ReadOnlyTimeSeriesStore timeSeriesStore) {
        return new TimeSeriesMetadata(name, TimeSeriesDataType.DOUBLE, computeIndex(nodeCalc, timeSeriesStore));
    }

    private static TimeSeriesIndex computeIndex(NodeCalc nodeCalc, ReadOnlyTimeSeriesStore timeSeriesStore) {
        TimeSeriesIndex index;

        Set<String> timeSeriesNames = TimeSeriesNames.list(nodeCalc);
        if (timeSeriesNames.isEmpty()) {
            // no reference index to sync the calculated time series, use infinite one
            index = InfiniteTimeSeriesIndex.INSTANCE;
        } else {
            if (timeSeriesStore == null) {
                throw new AssertionError("Time series store is null");
            }

            // check all time series are already sync on the same index
            Set<TimeSeriesIndex> indexes = timeSeriesStore.getTimeSeriesMetadata(timeSeriesNames).stream()
                    .map(TimeSeriesMetadata::getIndex)
                    .collect(Collectors.toSet());
            if (indexes.size() > 1) {
                throw new TimeSeriesException("A calculated time series must depend on synchronized time series");
            }

            index = indexes.iterator().next();
        }

        return index;
    }

    @Override
    public void synchronize(TimeSeriesIndex newIndex) {
        Objects.requireNonNull(newIndex);
        if (metadata.getIndex() == InfiniteTimeSeriesIndex.INSTANCE) {
            metadata = new TimeSeriesMetadata(metadata.getName(), metadata.getDataType(), metadata.getTags(), newIndex);
        } else {
            if (!metadata.getIndex().equals(newIndex)) {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }
    }

    @Override
    public void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset) {
        if (metadata.getIndex() == InfiniteTimeSeriesIndex.INSTANCE) {
            throw new TimeSeriesException("Impossible to fill buffer because calculated time series has not been synchronized on a finite time index");
        }
        Iterator<DoublePoint> it = iterator();
        DoublePoint prevPoint = null;
        while (it.hasNext()) {
            DoublePoint point = it.next();
            if (prevPoint != null) {
                for (int i = prevPoint.getIndex(); i < point.getIndex(); i++) {
                    buffer.put(timeSeriesOffset + i, prevPoint.getValue());
                }
            }
            prevPoint = point;
        }
        if (prevPoint != null) {
            for (int i = prevPoint.getIndex(); i < metadata.getIndex().getPointCount(); i++) {
                buffer.put(timeSeriesOffset + i, prevPoint.getValue());
            }
        }
    }

    @Override
    public double[] toArray() {
        DoubleBuffer buffer = DoubleBuffer.allocate(metadata.getIndex().getPointCount());
        fillBuffer(buffer, 0);
        return buffer.array();
    }

    @Override
    public TimeSeriesMetadata getMetadata() {
        return metadata;
    }

    private DoublePoint evaluateMultiPoint(DoubleMultiPoint multiPoint) {
        double value = NodeCalcEvaluator.eval(resolvedNodeCalc, multiPoint);
        return new DoublePoint(multiPoint.getIndex(), multiPoint.getTime(), value);
    }

    private DoublePoint evaluate() {
        double value = NodeCalcEvaluator.eval(resolvedNodeCalc, null);
        return new DoublePoint(0, InfiniteTimeSeriesIndex.START_TIME, value);
    }

    @Override
    public Stream<DoublePoint> stream() {
        if (timeSeriesList.isEmpty()) {
            return Stream.of(evaluate());
        } else {
            return DoubleTimeSeries.stream(timeSeriesList).map(this::evaluateMultiPoint);
        }
    }

    @Override
    public Iterator<DoublePoint> iterator() {
        if (timeSeriesList.isEmpty()) {
            return Iterators.singletonIterator(evaluate());
        } else {
            return Iterators.transform(DoubleTimeSeries.iterator(timeSeriesList), this::evaluateMultiPoint);
        }
    }

    @Override
    public List<DoubleTimeSeries> split(int chunkCount) {
        return Collections.nCopies(chunkCount, this);
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        NodeCalc.writeJson(nodeCalc, generator);
    }
}
