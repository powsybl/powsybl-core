/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.google.common.base.Stopwatch;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class TimeSeriesTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesTable.class);

    public class Correlation {

        private final String timeSeriesName1;
        private final String timeSeriesName2;
        private final double coefficient;

        public Correlation(String timeSeriesName1, String timeSeriesName2, double coefficient) {
            this.timeSeriesName1 = Objects.requireNonNull(timeSeriesName1);
            this.timeSeriesName2 = Objects.requireNonNull(timeSeriesName2);
            this.coefficient = coefficient;
        }

        public String getTimeSeriesName1() {
            return timeSeriesName1;
        }

        public String getTimeSeriesName2() {
            return timeSeriesName2;
        }

        public double getCoefficient() {
            return coefficient;
        }

        @Override
        public String toString() {
            return "Correlation(timeSeriesName1=" + timeSeriesName1 + ", timeSeriesName2=" + timeSeriesName2 +
                    ", coefficient=" + coefficient + ")";
        }
    }

    private class TimeSeriesNameMap {

        private final List<String> indexToName = new ArrayList<>();

        private final TObjectIntMap<String> nameToIndex = new TObjectIntHashMap<>();

        int add(String name) {
            int i = indexToName.size();
            indexToName.add(name);
            nameToIndex.put(name, i);
            return i;
        }

        String getName(int index) {
            if (index < 0 || index >= indexToName.size()) {
                throw new IllegalArgumentException("Time series at index " + index + " not found");
            }
            return indexToName.get(index);
        }

        int getIndex(String name) {
            Objects.requireNonNull(name);
            if (!nameToIndex.containsKey(name)) {
                throw new IllegalArgumentException("Time series '" + name + "' not found");
            }
            return nameToIndex.get(name);
        }

        int size() {
            return indexToName.size();
        }
    }

    private int fromVersion;

    private int toVersion;

    private List<TimeSeriesMetadata> timeSeriesMetadata;

    private TimeSeriesIndex tableIndex;

    private final TIntArrayList timeSeriesIndexDoubleOrString = new TIntArrayList(); // global index to typed index

    private final TimeSeriesNameMap doubleTimeSeriesNames = new TimeSeriesNameMap();

    private final TimeSeriesNameMap stringTimeSeriesNames = new TimeSeriesNameMap();

    private DoubleBuffer doubleBuffer;

    private CompactStringBuffer stringBuffer;

    // statistics
    private double[] means;

    private double[] stdDevs;

    public TimeSeriesTable(int fromVersion, int toVersion) {
        TimeSeriesIndex.checkVersion(fromVersion);
        TimeSeriesIndex.checkVersion(toVersion);
        if (toVersion < fromVersion) {
            throw new TimeSeriesException("To version is expected to be greater tha from version");
        }
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
    }

    private void checkIndex(TimeSeries timeSeries) {
        if (timeSeries.getMetadata().getIndex() != InfiniteTimeSeriesIndex.INSTANCE) {
            if (tableIndex == null) {
                tableIndex = timeSeries.getMetadata().getIndex();
            } else {
                if (!tableIndex.equals(timeSeries.getMetadata().getIndex())) {
                    throw new TimeSeriesException("All time series must be synchronized");
                }
            }
        }
    }

    private void initTable(List<DoubleTimeSeries> doubleTimeSeries, List<StringTimeSeries> stringTimeSeries) {
        if (timeSeriesMetadata != null) {
            return; // already initialized
        }

        timeSeriesMetadata = new ArrayList<>(doubleTimeSeries.size() + stringTimeSeries.size());

        for (DoubleTimeSeries timeSeries : doubleTimeSeries) {
            checkIndex(timeSeries);
            timeSeriesMetadata.add(timeSeries.getMetadata());
            int i = doubleTimeSeriesNames.add(timeSeries.getMetadata().getName());
            timeSeriesIndexDoubleOrString.add(i);
        }
        for (StringTimeSeries timeSeries : stringTimeSeries) {
            checkIndex(timeSeries);
            timeSeriesMetadata.add(timeSeries.getMetadata());
            int i = stringTimeSeriesNames.add(timeSeries.getMetadata().getName());
            timeSeriesIndexDoubleOrString.add(i);
        }

        if (tableIndex == null) {
            throw new TimeSeriesException("None of the time series have a finite index");
        }

        int versionCount = toVersion - fromVersion + 1;

        // allocate double buffer
        int doubleBufferSize = versionCount * doubleTimeSeriesNames.size() * tableIndex.getPointCount();
        doubleBuffer = createDoubleBuffer(doubleBufferSize);
        for (int i = 0; i < doubleBufferSize; i++) {
            doubleBuffer.put(Double.NaN);
        }

        // allocate string buffer
        int stringBufferSize = versionCount * stringTimeSeriesNames.size() * tableIndex.getPointCount();
        stringBuffer = new CompactStringBuffer(stringBufferSize);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Allocation of {} for time series table",
                    FileUtils.byteCountToDisplaySize((long) doubleBuffer.capacity() * Double.BYTES + stringBuffer.capacity() * Integer.BYTES));
        }

        // allocate statistics buffer
        means = new double[doubleTimeSeriesNames.size() * versionCount];
        Arrays.fill(means, Double.NaN);
        stdDevs = new double[doubleTimeSeriesNames.size() * versionCount];
        Arrays.fill(stdDevs, Double.NaN);
    }

    private static DoubleBuffer createDoubleBuffer(int size) {
        return ByteBuffer.allocateDirect(size * Double.BYTES).asDoubleBuffer();
    }

    private int getTimeSeriesOffset(int version, int timeSeriesNum) {
        return timeSeriesNum * tableIndex.getPointCount() * (toVersion - fromVersion + 1) + (version - fromVersion) * tableIndex.getPointCount();
    }

    private int getStatisticsIndex(int version, int timeSeriesNum) {
        return (version - fromVersion) * doubleTimeSeriesNames.size() + timeSeriesNum;
    }

    private void checkVersion(int version) {
        if (version < fromVersion || version > toVersion) {
            throw new IllegalArgumentException("Version is out of range [" + fromVersion + ", " + toVersion + "]");
        }
    }

    private int checkTimeSeriesNum(int timeSeriesNum) {
        if (timeSeriesNum < 0 || timeSeriesNum >= timeSeriesIndexDoubleOrString.size()) {
            throw new IllegalArgumentException("Time series number is out of range [0, " + (timeSeriesIndexDoubleOrString.size() - 1) + "]");
        }
        return timeSeriesIndexDoubleOrString.get(timeSeriesNum);
    }

    private void checkPoint(int point) {
        if (point < 0 || point >= tableIndex.getPointCount()) {
            throw new IllegalArgumentException("Point is out of range [0, " + (tableIndex.getPointCount() - 1) + "]");
        }
    }

    private void loadDouble(int version, DoubleTimeSeries timeSeries) {
        // check time series exists in the table
        int timeSeriesNum = doubleTimeSeriesNames.getIndex(timeSeries.getMetadata().getName());

        // check time series index is the same than table one
        checkIndex(timeSeries);

        // copy data
        int timeSeriesOffset = getTimeSeriesOffset(version, timeSeriesNum);
        timeSeries.fillBuffer(doubleBuffer, timeSeriesOffset);

        // invalidate statistics
        invalidateStatistics(version, timeSeriesNum);
    }

    private void loadString(int version, StringTimeSeries timeSeries) {
        // check time series exists in the table
        int timeSeriesNum = stringTimeSeriesNames.getIndex(timeSeries.getMetadata().getName());

        // check time series index is the same than table one
        checkIndex(timeSeries);

        // copy data
        int timeSeriesOffset = getTimeSeriesOffset(version, timeSeriesNum);
        timeSeries.fillBuffer(stringBuffer, timeSeriesOffset);
    }

    @SafeVarargs
    public final void load(int version, List<? extends TimeSeries>... timeSeries) {
        load(version, Arrays.stream(timeSeries).flatMap(List::stream).collect(Collectors.toList()));
    }

    public void load(int version, List<TimeSeries> timeSeriesList) {
        checkVersion(version);
        Objects.requireNonNull(timeSeriesList);

        if (timeSeriesList.isEmpty()) {
            throw new TimeSeriesException("Empty time series list");
        }

        Stopwatch stopWatch = Stopwatch.createStarted();

        List<DoubleTimeSeries> doubleTimeSeries = new ArrayList<>();
        List<StringTimeSeries> stringTimeSeries = new ArrayList<>();
        for (TimeSeries timeSeries : timeSeriesList) {
            Objects.requireNonNull(timeSeries);
            if (timeSeries instanceof DoubleTimeSeries) {
                doubleTimeSeries.add((DoubleTimeSeries) timeSeries);
            } else if (timeSeries instanceof StringTimeSeries) {
                stringTimeSeries.add((StringTimeSeries) timeSeries);
            } else {
                throw new AssertionError("Unsupported time series type " + timeSeries.getClass());
            }
        }

        initTable(doubleTimeSeries, stringTimeSeries);

        for (DoubleTimeSeries timeSeries : doubleTimeSeries) {
            loadDouble(version, timeSeries);
        }
        for (StringTimeSeries timeSeries : stringTimeSeries) {
            loadString(version, timeSeries);
        }

        LOGGER.info("{} time series (version={}) loaded in {} ms", timeSeriesList.size(), version,
                stopWatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public List<String> getTimeSeriesNames() {
        return timeSeriesMetadata.stream().map(TimeSeriesMetadata::getName).collect(Collectors.toList());
    }

    public double getDoubleValue(int version, int timeSeriesNum, int point) {
        checkVersion(version);
        int doubleTimeSeriesNum = checkTimeSeriesNum(timeSeriesNum);
        checkPoint(point);
        int timeSeriesOffset = getTimeSeriesOffset(version, doubleTimeSeriesNum);
        return doubleBuffer.get(timeSeriesOffset + point);
    }

    public String getStringValue(int version, int timeSeriesNum, int point) {
        checkVersion(version);
        int stringTimeSeriesNum = checkTimeSeriesNum(timeSeriesNum);
        checkPoint(point);
        int timeSeriesOffset = getTimeSeriesOffset(version, stringTimeSeriesNum);
        return stringBuffer.getString(timeSeriesOffset + point);
    }

    private void invalidateStatistics(int version, int timeSeriesNum) {
        int statisticsIndex = getStatisticsIndex(version, timeSeriesNum);
        means[statisticsIndex] = Double.NaN;
        stdDevs[statisticsIndex] = Double.NaN;
    }

    private void updateStatistics(int version, int timeSeriesNum) {
        int statisticsIndex = getStatisticsIndex(version, timeSeriesNum);
        if (Double.isNaN(means[statisticsIndex]) || Double.isNaN(stdDevs[statisticsIndex])) {
            int timeSeriesOffset = getTimeSeriesOffset(version, timeSeriesNum);

            double sum = 0;
            for (int point = 0; point < tableIndex.getPointCount(); point++) {
                double value = doubleBuffer.get(timeSeriesOffset + point);
                if (!Double.isNaN(value)) {
                    sum += value;
                }
            }
            double mean = sum / tableIndex.getPointCount();
            means[statisticsIndex] = mean;

            double stdDev = 0;
            for (int point = 0; point < tableIndex.getPointCount(); point++) {
                double value = doubleBuffer.get(timeSeriesOffset + point);
                if (!Double.isNaN(value)) {
                    stdDev += (value - mean) * (value - mean);
                }
            }
            stdDev = Math.sqrt(stdDev / (tableIndex.getPointCount() - 1));
            stdDevs[statisticsIndex] = stdDev;
        }
    }

    private void updateStatistics(int version) {
        for (int timeSeriesNum = 0; timeSeriesNum < doubleTimeSeriesNames.size(); timeSeriesNum++) {
            updateStatistics(version, timeSeriesNum);
        }
    }

    private double getStatistics(int version, int timeSeriesNum, double[] stats) {
        checkVersion(version);
        int doubleTimeSeriesNum = checkTimeSeriesNum(timeSeriesNum);
        int statisticsIndex = getStatisticsIndex(version, doubleTimeSeriesNum);
        updateStatistics(version, timeSeriesNum);
        return stats[statisticsIndex];
    }

    public double getMean(int version, int timeSeriesNum) {
        return getStatistics(version, timeSeriesNum, means);
    }

    public double getStdDev(int version, int timeSeriesNum) {
        return getStatistics(version, timeSeriesNum, stdDevs);
    }

    public List<Correlation> findMostCorrelatedTimeSeries(String timeSeriesName, int version) {
        return findMostCorrelatedTimeSeries(timeSeriesName, version, 10);
    }

    public List<Correlation> findMostCorrelatedTimeSeries(String timeSeriesName, int version, int maxSize) {
        double[] ppmcc = computePpmcc(timeSeriesName, version);
        return IntStream.range(0, ppmcc.length)
                .mapToObj(i -> new Correlation(timeSeriesName, doubleTimeSeriesNames.getName(i), Math.abs(ppmcc[i])))
                .filter(correlation -> !correlation.getTimeSeriesName2().equals(timeSeriesName))
                .sorted(Comparator.comparingDouble(Correlation::getCoefficient).reversed())
                .limit(maxSize)
                .collect(Collectors.toList());
    }

    public double[] computePpmcc(String timeSeriesName, int version) {
        int timeSeriesNum1 = doubleTimeSeriesNames.getIndex(timeSeriesName);
        checkVersion(version);

        Stopwatch stopWatch = Stopwatch.createStarted();

        updateStatistics(version);

        int statisticsIndex1 = getStatisticsIndex(version, timeSeriesNum1);
        double stdDev1 = stdDevs[statisticsIndex1];

        double[] r = new double[doubleTimeSeriesNames.size()];

        if (stdDev1 == 0) { // constant time series
            for (int timeSeriesNum2 = 0; timeSeriesNum2 < doubleTimeSeriesNames.size(); timeSeriesNum2++) {
                int statisticsIndex2 = getStatisticsIndex(version, timeSeriesNum2);
                double stdDev2 = stdDevs[statisticsIndex2];

                // time series 1 is correlated to other constant time series
                r[timeSeriesNum2] = stdDev2 == 0 ? 1 : 0;
            }
        } else {
            double mean1 = means[statisticsIndex1];

            for (int timeSeriesNum2 = 0; timeSeriesNum2 < doubleTimeSeriesNames.size(); timeSeriesNum2++) {
                if (timeSeriesNum2 == timeSeriesNum1) {
                    r[timeSeriesNum2] = 1;
                } else {
                    int statisticsIndex2 = getStatisticsIndex(version, timeSeriesNum2);
                    double stdDev2 = stdDevs[statisticsIndex2];

                    r[timeSeriesNum2] = 0;

                    if (stdDev2 != 0) {
                        double mean2 = means[statisticsIndex2];

                        int timeSeriesOffset1 = getTimeSeriesOffset(version, timeSeriesNum1);
                        int timeSeriesOffset2 = getTimeSeriesOffset(version, timeSeriesNum2);

                        for (int point = 0; point < tableIndex.getPointCount(); point++) {
                            double value1 = doubleBuffer.get(timeSeriesOffset1 + point);
                            double value2 = doubleBuffer.get(timeSeriesOffset2 + point);
                            r[timeSeriesNum2] += (value1 - mean1) / stdDev1 * (value2 - mean2) / stdDev2;
                        }
                        r[timeSeriesNum2] /= tableIndex.getPointCount() - 1;
                    }
                }
            }
        }

        LOGGER.info("PPMCC computed in {} ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));

        return r;
    }

    private static BufferedWriter createWriter(Path file) throws IOException {
        if (file.getFileName().toString().endsWith(".gz")) {
            return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(file)), StandardCharsets.UTF_8));
        } else {
            return Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        }
    }

    public void writeCsv(Path file) {
        try (BufferedWriter writer = createWriter(file)) {
            writeCsv(writer, TimeSeries.DEFAULT_SEPARATOR, ZoneId.systemDefault());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toCsvString(char separator, ZoneId zoneId) {
        try (StringWriter writer = new StringWriter()) {
            writeCsv(writer, separator, zoneId);
            writer.flush();
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeCsv(Writer writer, char separator, ZoneId zoneId) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(zoneId);

        Stopwatch stopWatch = Stopwatch.createStarted();

        try {
            // write header
            writer.write("Time");
            writer.write(separator);
            writer.write("Version");
            for (TimeSeriesMetadata metadata : timeSeriesMetadata) {
                writer.write(separator);
                writer.write(metadata.getName());
            }
            writer.write(System.lineSeparator());

            // read time series in the doubleBuffer per 10 points chunk to avoid cache missed and improve performances
            int cacheSize = 10;
            double[] doubleCache = new double[cacheSize * doubleTimeSeriesNames.size()];
            String[] stringCache = new String[cacheSize * stringTimeSeriesNames.size()];

            // write data
            for (int version = fromVersion; version <= toVersion; version++) {
                for (int point = 0; point < tableIndex.getPointCount(); point += cacheSize) {

                    int cachedPoints = Math.min(cacheSize, tableIndex.getPointCount() - point);

                    // copy from doubleBuffer to cache
                    for (int i = 0; i < timeSeriesMetadata.size(); i++) {
                        TimeSeriesMetadata metadata = timeSeriesMetadata.get(i);
                        int timeSeriesNum = timeSeriesIndexDoubleOrString.get(i);
                        int timeSeriesOffset = getTimeSeriesOffset(version, timeSeriesNum);
                        if (metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                            for (int cachedPoint = 0; cachedPoint < cachedPoints; cachedPoint++) {
                                doubleCache[cachedPoint * doubleTimeSeriesNames.size() + timeSeriesNum] = doubleBuffer.get(timeSeriesOffset + point + cachedPoint);
                            }
                        } else if (metadata.getDataType() == TimeSeriesDataType.STRING) {
                            for (int cachedPoint = 0; cachedPoint < cachedPoints; cachedPoint++) {
                                stringCache[cachedPoint * stringTimeSeriesNames.size() + timeSeriesNum] = stringBuffer.getString(timeSeriesOffset + point + cachedPoint);
                            }
                        } else {
                            throw new AssertionError("Unexpected data type " + metadata.getDataType());
                        }
                    }

                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId);

                    // then write cache to CSV
                    for (int cachedPoint = 0; cachedPoint < cachedPoints; cachedPoint++) {
                        ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tableIndex.getTimeAt(point + cachedPoint)), ZoneId.systemDefault());
                        writer.write(dateTime.format(dateTimeFormatter));
                        writer.write(separator);
                        writer.write(Integer.toString(version));
                        for (int i = 0; i < timeSeriesMetadata.size(); i++) {
                            TimeSeriesMetadata metadata = timeSeriesMetadata.get(i);
                            int timeSeriesNum = timeSeriesIndexDoubleOrString.get(i);
                            writer.write(separator);
                            if (metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                                double value = doubleCache[cachedPoint * doubleTimeSeriesNames.size() + timeSeriesNum];
                                if (!Double.isNaN(value)) {
                                    writer.write(Double.toString(value));
                                }
                            } else if (metadata.getDataType() == TimeSeriesDataType.STRING) {
                                String value = stringCache[cachedPoint * stringTimeSeriesNames.size() + timeSeriesNum];
                                if (value != null) {
                                    writer.write(value);
                                }
                            } else {
                                throw new AssertionError("Unexpected data type " + metadata.getDataType());
                            }
                        }
                        writer.write(System.lineSeparator());
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("Csv written in {} ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));
    }
}
