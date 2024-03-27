/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.nio.DoubleBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface DoubleTimeSeries extends TimeSeries<DoublePoint, DoubleTimeSeries> {

    void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset);

    void fillBuffer(BigDoubleBuffer buffer, long timeSeriesOffset);

    double[] toArray();

    static Iterator<DoubleMultiPoint> iterator(List<DoubleTimeSeries> timeSeriesList) {
        Objects.requireNonNull(timeSeriesList);

        if (timeSeriesList.isEmpty()) {
            return Collections.emptyIterator();
        }

        // check index unicity
        long indexCount = timeSeriesList.stream().map(DoubleTimeSeries::getMetadata)
                                                 .map(TimeSeriesMetadata::getIndex)
                                                 .distinct()
                                                 .count();
        if (indexCount > 1) {
            throw new TimeSeriesException("Time series must have the same index");
        }

        class DoublePointExt {

            private final DoublePoint point;

            private final int timeSeriesNum;

            DoublePointExt(DoublePoint point, int timeSeriesNum) {
                this.point = point;
                this.timeSeriesNum = timeSeriesNum;
            }

            public DoublePoint getPoint() {
                return point;
            }

            public int getTimeSeriesNum() {
                return timeSeriesNum;
            }
        }

        Map<Integer, List<DoublePointExt>> points = new HashMap<>();
        for (int timeSeriesNum = 0; timeSeriesNum < timeSeriesList.size(); timeSeriesNum++) {
            DoubleTimeSeries timeSeries = timeSeriesList.get(timeSeriesNum);
            for (DoublePoint point : timeSeries) {
                points.computeIfAbsent(point.getIndex(), key -> new ArrayList<>())
                        .add(new DoublePointExt(point, timeSeriesNum));
            }
        }

        Iterator<Map.Entry<Integer, List<DoublePointExt>>> it = points.entrySet().stream()
            .sorted(Comparator.comparingInt(Map.Entry::getKey))
            .iterator();

        return new Iterator<DoubleMultiPoint>() {

            private final double[] values = new double[timeSeriesList.size()];

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public DoubleMultiPoint next() {
                Map.Entry<Integer, List<DoublePointExt>> e = it.next();

                // update values
                for (DoublePointExt point : e.getValue()) {
                    values[point.getTimeSeriesNum()] = point.getPoint().getValue();
                }

                return new DoubleMultiPoint() {
                    @Override
                    public int getIndex() {
                        return e.getKey();
                    }

                    @Override
                    public long getTime() {
                        return e.getValue().get(0).getPoint().getTime();
                    }

                    @Override
                    public double getValue(int timeSeriesNum) {
                        return values[timeSeriesNum];
                    }
                };
            }
        };
    }

    static Stream<DoubleMultiPoint> stream(List<DoubleTimeSeries> timeSeriesList) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator(timeSeriesList),
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    class Builder {

        private final List<DoubleTimeSeries> list;

        Builder(List<DoubleTimeSeries> list) {
            this.list = Objects.requireNonNull(list);
        }

        private static List<DoubleTimeSeries> build(List<DoubleTimeSeries> list, String... scriptLines) {
            String script = Arrays.stream(scriptLines).collect(Collectors.joining(System.lineSeparator()));
            ReadOnlyTimeSeriesStore store = new ReadOnlyTimeSeriesStoreCache(list);
            return CalculatedTimeSeriesDslLoader.find()
                    .load(script, store)
                    .entrySet()
                    .stream()
                    .map(e -> new CalculatedTimeSeries(e.getKey(), e.getValue(), new FromStoreTimeSeriesNameResolver(store, -1)))
                    .collect(Collectors.toList());
        }

        public List<DoubleTimeSeries> build(String... scriptLines) {
            return build(list, scriptLines);
        }
    }

    static List<DoubleTimeSeries> build(String... scriptLines) {
        return Builder.build(Collections.emptyList(), scriptLines);
    }

    static Builder fromTimeSeries(List<DoubleTimeSeries> list) {
        return new Builder(list);
    }

    static Builder fromTimeSeries(DoubleTimeSeries... list) {
        return fromTimeSeries(Arrays.asList(list));
    }
}
