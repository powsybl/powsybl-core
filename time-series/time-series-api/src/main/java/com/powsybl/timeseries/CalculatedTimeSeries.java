/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.ast.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.DoubleBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class CalculatedTimeSeries implements DoubleTimeSeries {

    public static final TimeSeriesNameResolver EMPTY_RESOLVER = new TimeSeriesNameResolver() {

        @Override
        public List<TimeSeriesMetadata> getTimeSeriesMetadata(Set<String> timeSeriesNames) {
            return Collections.emptyList();
        }

        @Override
        public Set<Integer> getTimeSeriesDataVersions(String timeSeriesName) {
            return Collections.emptySet();
        }

        @Override
        public List<DoubleTimeSeries> getDoubleTimeSeries(Set<String> timeSeriesNames) {
            return Collections.emptyList();
        }
    };

    private final String name;

    private final NodeCalc nodeCalc;

    private TimeSeriesNameResolver resolver;

    private final TimeSeriesMetadata metadata;

    private TimeSeriesIndex index;

    public CalculatedTimeSeries(String name, NodeCalc nodeCalc, TimeSeriesNameResolver resolver) {
        this.name = Objects.requireNonNull(name);
        this.nodeCalc = Objects.requireNonNull(nodeCalc);
        this.resolver = Objects.requireNonNull(resolver);
        metadata = new TimeSeriesMetadata(name, TimeSeriesDataType.DOUBLE, InfiniteTimeSeriesIndex.INSTANCE) {
            @Override
            public TimeSeriesIndex getIndex() {
                return CalculatedTimeSeries.this.getIndex();
            }
        };
    }

    public CalculatedTimeSeries(String name, NodeCalc nodeCalc) {
        this(name, nodeCalc, EMPTY_RESOLVER);
    }

    @Override
    public void setTimeSeriesNameResolver(TimeSeriesNameResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver);
    }

    private List<DoubleTimeSeries> loadData() {
        Set<String> timeSeriesNames = TimeSeriesNames.list(nodeCalc);
        return timeSeriesNames.isEmpty() ? Collections.emptyList() : resolver.getDoubleTimeSeries(timeSeriesNames);
    }

    private NodeCalc resolve(List<DoubleTimeSeries> timeSeriesList) {
        NodeCalc simplifiedNodeCalc = NodeCalcSimplifier.simplify(nodeCalc);

        Map<String, Integer> timeSeriesNums = IntStream.range(0, timeSeriesList.size())
                .boxed()
                .collect(Collectors.toMap(i -> timeSeriesList.get(i).getMetadata().getName(),
                        Function.identity()));
        return NodeCalcResolver.resolve(simplifiedNodeCalc, timeSeriesNums);
    }

    public static TimeSeriesIndex computeIndex(NodeCalc nodeCalc, TimeSeriesNameResolver resolver) {
        Objects.requireNonNull(nodeCalc);

        TimeSeriesIndex index;

        Set<String> timeSeriesNames = TimeSeriesNames.list(nodeCalc);
        if (timeSeriesNames.isEmpty()) {
            // no reference index to sync the calculated time series, use infinite one
            index = InfiniteTimeSeriesIndex.INSTANCE;
        } else {
            if (resolver == null) {
                throw new TimeSeriesException("Time series name resolver is null");
            }

            // check all time series are already sync on the same index
            Set<TimeSeriesIndex> indexes = resolver.getTimeSeriesMetadata(timeSeriesNames).stream()
                    .map(TimeSeriesMetadata::getIndex)
                    .collect(Collectors.toSet());
            if (indexes.size() > 1) {
                throw new TimeSeriesException("A calculated time series must depend on synchronized time series");
            }

            index = indexes.iterator().next();
        }

        return index;
    }

    public static Set<Integer> computeVersions(NodeCalc nodeCalc, TimeSeriesNameResolver resolver) {
        Objects.requireNonNull(nodeCalc);

        Set<String> timeSeriesNames = TimeSeriesNames.list(nodeCalc);
        if (timeSeriesNames.isEmpty()) {
            return Collections.emptySet();
        } else {
            if (resolver == null) {
                throw new TimeSeriesException("Time series name resolver is null");
            }

            Set<Integer> commonVersions = new HashSet<>();
            for (String timeSeriesName : timeSeriesNames) {
                Set<Integer> versions = resolver.getTimeSeriesDataVersions(timeSeriesName);
                if (commonVersions.isEmpty()) {
                    commonVersions = versions;
                } else {
                    commonVersions = Sets.intersection(commonVersions, versions);
                }
            }

            return commonVersions;
        }
    }

    public Set<Integer> getVersions() {
        return computeVersions(nodeCalc, resolver);
    }

    @Override
    public void synchronize(TimeSeriesIndex newIndex) {
        Objects.requireNonNull(newIndex);
        if (metadata.getIndex() == InfiniteTimeSeriesIndex.INSTANCE) {
            index = newIndex;
        } else {
            if (!metadata.getIndex().equals(newIndex)) {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        }
    }

    //To remove if we ever get it from somewhere else
    @FunctionalInterface private interface DoubleIntConsumer { void accept(double a, int b); }

    private void forEachMaterializedValueIndex(DoubleIntConsumer consumer) {
        if (metadata.getIndex() == InfiniteTimeSeriesIndex.INSTANCE) {
            throw new TimeSeriesException("Impossible to fill buffer because calculated time series has not been synchronized on a finite time index");
        }
        Iterator<DoublePoint> it = iterator();
        DoublePoint prevPoint = null;
        while (it.hasNext()) {
            DoublePoint point = it.next();
            if (prevPoint != null) {
                for (int i = prevPoint.getIndex(); i < point.getIndex(); i++) {
                    consumer.accept(prevPoint.getValue(), i);
                }
            }
            prevPoint = point;
        }
        if (prevPoint != null) {
            for (int i = prevPoint.getIndex(); i < metadata.getIndex().getPointCount(); i++) {
                consumer.accept(prevPoint.getValue(), i);
            }
        }
    }

    @Override
    public void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        forEachMaterializedValueIndex((v, i) -> buffer.put(i + timeSeriesOffset, v));
    }

    @Override
    public void fillBuffer(BigDoubleBuffer buffer, long timeSeriesOffset) {
        Objects.requireNonNull(buffer);
        forEachMaterializedValueIndex((v, i) -> buffer.put(i + timeSeriesOffset, v));
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

    public TimeSeriesIndex getIndex() {
        if (index == null) {
            index = computeIndex(nodeCalc, resolver);
        }
        return index;
    }

    private static DoublePoint evaluateMultiPoint(NodeCalc resolvedNodeCalc, DoubleMultiPoint multiPoint) {
        double value = NodeCalcEvaluator.eval(resolvedNodeCalc, multiPoint);
        return new DoublePoint(multiPoint.getIndex(), multiPoint.getInstant(), value);
    }

    private static DoublePoint evaluate(NodeCalc resolvedNodeCalc) {
        double value = NodeCalcEvaluator.eval(resolvedNodeCalc, null);
        return new DoublePoint(0, InfiniteTimeSeriesIndex.START_INSTANT, value);
    }

    @Override
    public Stream<DoublePoint> stream() {
        List<DoubleTimeSeries> timeSeriesList = loadData();
        NodeCalc resolvedNodeCalc = resolve(timeSeriesList);
        if (timeSeriesList.isEmpty()) {
            return Stream.of(evaluate(resolvedNodeCalc));
        } else {
            return DoubleTimeSeries.stream(timeSeriesList).map(multiPoint -> evaluateMultiPoint(resolvedNodeCalc, multiPoint));
        }
    }

    @Override
    public Iterator<DoublePoint> iterator() {
        List<DoubleTimeSeries> timeSeriesList = loadData();
        NodeCalc resolvedNodeCalc = resolve(timeSeriesList);
        if (timeSeriesList.isEmpty()) {
            return Iterators.singletonIterator(evaluate(resolvedNodeCalc));
        } else {
            return Iterators.transform(DoubleTimeSeries.iterator(timeSeriesList), multiPoint -> evaluateMultiPoint(resolvedNodeCalc, multiPoint));
        }
    }

    @Override
    public List<DoubleTimeSeries> split(int newChunkSize) {
        int chunkCount = TimeSeries.computeChunkCount(index, newChunkSize);
        return Collections.nCopies(chunkCount, this);
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        try {
            generator.writeStartObject();
            generator.writeStringField("name", name);
            generator.writeFieldName("expr");
            generator.writeStartObject();
            nodeCalc.writeJson(generator);
            generator.writeEndObject();
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, nodeCalc);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CalculatedTimeSeries other) {
            return name.equals(other.name) && nodeCalc.equals(other.nodeCalc);
        }
        return false;
    }
}
