/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.google.common.base.Stopwatch;
import com.google.common.math.IntMath;
import gnu.trove.list.array.TIntArrayList;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class to load time series into a table and then:
 *
 * <ul>
 *   <li>Get direct access to all values</li>
 *   <li>Compute statistics like mean, standard deviation, and Pearson product-moment correlation coefficient</li>
 *   <li>Convert to CSV</li>
 * </ul>
 *
 * Some design considerations and limitations:
 * <ul>
 *     <li>Number of version loadable in the table has to be specified at creation</li>
 *     <li>Versions have to contiguous</li>
 *     <li>Once first batch of time series has been loaded, new time series cannot be added but data of existing one can be updated</li>
 *     <li>Concurrent load (i.e multi-thread) of data is supported (using same time series list)</li>
 *     <li>Concurrency between data loading and other operations (CSV writing, statistics computation) is NOT supported</li>
 * </ul>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesTable {

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

        private final BiList<String> names = new BiList<>();

        int add(String name) {
            return names.add(name);
        }

        String getName(int index) {
            if (index < 0 || index >= names.size()) {
                throw new IllegalArgumentException("Time series at index " + index + " not found");
            }
            return names.get(index);
        }

        int getIndex(String name) {
            Objects.requireNonNull(name);
            int index = names.indexOf(name);
            if (index == -1) {
                throw new IllegalArgumentException("Time series '" + name + "' not found");
            }
            return index;
        }

        int size() {
            return names.size();
        }

        void clear() {
            names.clear();
        }
    }

    private int fromVersion;

    private int toVersion;

    private List<TimeSeriesMetadata> timeSeriesMetadata;

    private final TimeSeriesIndex tableIndex;

    private final IntFunction<ByteBuffer> byteBufferAllocator;

    private final TIntArrayList timeSeriesIndexDoubleOrString = new TIntArrayList(); // global index to typed index

    private final TimeSeriesNameMap doubleTimeSeriesNames = new TimeSeriesNameMap();

    private final TimeSeriesNameMap stringTimeSeriesNames = new TimeSeriesNameMap();

    private DoubleBuffer doubleBuffer;

    private CompactStringBuffer stringBuffer;

    private final Lock initLock = new ReentrantLock();

    // statistics
    private double[] means;

    private double[] stdDevs;

    private final Lock statsLock = new ReentrantLock();

    public TimeSeriesTable(int fromVersion, int toVersion, TimeSeriesIndex tableIndex) {
        this(fromVersion, toVersion, tableIndex, ByteBuffer::allocateDirect);
    }

    public TimeSeriesTable(int fromVersion, int toVersion, TimeSeriesIndex tableIndex, IntFunction<ByteBuffer> byteBufferAllocator) {
        TimeSeriesIndex.checkVersion(fromVersion);
        TimeSeriesIndex.checkVersion(toVersion);
        if (toVersion < fromVersion) {
            throw new TimeSeriesException("toVersion (" + toVersion + ") is expected to be greater than fromVersion (" + fromVersion + ")");
        }
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.tableIndex = Objects.requireNonNull(tableIndex);
        this.byteBufferAllocator = Objects.requireNonNull(byteBufferAllocator);
    }

    public static TimeSeriesTable createDirectMem(int fromVersion, int toVersion, TimeSeriesIndex tableIndex) {
        return new TimeSeriesTable(fromVersion, toVersion, tableIndex);
    }

    public static TimeSeriesTable createMem(int fromVersion, int toVersion, TimeSeriesIndex tableIndex) {
        return new TimeSeriesTable(fromVersion, toVersion, tableIndex, ByteBuffer::allocate);
    }

    private void initTable(List<DoubleTimeSeries> doubleTimeSeries, List<StringTimeSeries> stringTimeSeries) {
        initLock.lock();
        try {
            if (timeSeriesMetadata != null) {
                return; // already initialized
            }

            timeSeriesMetadata = new ArrayList<>(doubleTimeSeries.size() + stringTimeSeries.size());

            for (DoubleTimeSeries timeSeries : doubleTimeSeries.stream()
                                                               .sorted(Comparator.comparing(ts -> ts.getMetadata().getName()))
                                                               .collect(Collectors.toList())) {
                timeSeriesMetadata.add(timeSeries.getMetadata());
                int i = doubleTimeSeriesNames.add(timeSeries.getMetadata().getName());
                timeSeriesIndexDoubleOrString.add(i);
            }
            for (StringTimeSeries timeSeries : stringTimeSeries.stream()
                                                               .sorted(Comparator.comparing(ts -> ts.getMetadata().getName()))
                                                               .collect(Collectors.toList())) {
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
            doubleBuffer = createDoubleBuffer(byteBufferAllocator, doubleBufferSize, Double.NaN);

            // allocate string buffer
            int stringBufferSize = versionCount * stringTimeSeriesNames.size() * tableIndex.getPointCount();
            stringBuffer = new CompactStringBuffer(byteBufferAllocator, stringBufferSize);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Allocation of {} for time series table",
                        FileUtils.byteCountToDisplaySize((long) doubleBuffer.capacity() * Double.BYTES + stringBuffer.capacity() * Integer.BYTES));
            }

            // allocate statistics buffer
            means = new double[doubleTimeSeriesNames.size() * versionCount];
            Arrays.fill(means, Double.NaN);

            stdDevs = new double[doubleTimeSeriesNames.size() * versionCount];
            Arrays.fill(stdDevs, Double.NaN);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            timeSeriesMetadata = null;
            doubleTimeSeriesNames.clear();
            stringTimeSeriesNames.clear();
            timeSeriesIndexDoubleOrString.clear();
            doubleBuffer = null;
            stringBuffer = null;
            means = null;
            stdDevs = null;
            throw e;
        } finally {
            initLock.unlock();
        }
    }

    public TimeSeriesIndex getTableIndex() {
        return tableIndex;
    }

    private static DoubleBuffer createDoubleBuffer(IntFunction<ByteBuffer> byteBufferAllocator, int size) {
        return byteBufferAllocator.apply(IntMath.checkedMultiply(size, Double.BYTES)).asDoubleBuffer();
    }

    private static DoubleBuffer createDoubleBuffer(IntFunction<ByteBuffer> byteBufferAllocator, int size, double initialValue) {
        DoubleBuffer doubleBuffer = createDoubleBuffer(byteBufferAllocator, size);
        for (int i = 0; i < size; i++) {
            doubleBuffer.put(initialValue);
        }
        return doubleBuffer;
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

        // copy data
        int timeSeriesOffset = getTimeSeriesOffset(version, timeSeriesNum);
        timeSeries.fillBuffer(doubleBuffer, timeSeriesOffset);

        // invalidate statistics
        invalidateStatistics(version, timeSeriesNum);
    }

    private void loadString(int version, StringTimeSeries timeSeries) {
        // check time series exists in the table
        int timeSeriesNum = stringTimeSeriesNames.getIndex(timeSeries.getMetadata().getName());

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
            timeSeries.synchronize(tableIndex);
            loadDouble(version, timeSeries);
        }
        for (StringTimeSeries timeSeries : stringTimeSeries) {
            timeSeries.synchronize(tableIndex);
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

    public int getDoubleTimeSeriesIndex(String timeSeriesName) {
        return doubleTimeSeriesNames.getIndex(timeSeriesName);
    }

    public int getStringTimeSeriesIndex(String timeSeriesName) {
        return stringTimeSeriesNames.getIndex(timeSeriesName);
    }

    private void invalidateStatistics(int version, int timeSeriesNum) {
        int statisticsIndex = getStatisticsIndex(version, timeSeriesNum);
        statsLock.lock();
        try {
            means[statisticsIndex] = Double.NaN;
            stdDevs[statisticsIndex] = Double.NaN;
        } finally {
            statsLock.unlock();
        }
    }

    private void updateStatistics(int version, int timeSeriesNum) {

        int statisticsIndex = getStatisticsIndex(version, timeSeriesNum);
        if (!Double.isNaN(means[statisticsIndex]) && !Double.isNaN(stdDevs[statisticsIndex])) {
            return;
        }
        int timeSeriesOffset = getTimeSeriesOffset(version, timeSeriesNum);

        double sum = 0;
        int nbPoints = 0;
        for (int point = 0; point < tableIndex.getPointCount(); point++) {
            double value = doubleBuffer.get(timeSeriesOffset + point);
            if (!Double.isNaN(value)) {
                sum += value;
                nbPoints++;
            }
        }
        double mean = nbPoints > 0 ? sum / nbPoints : 0;
        means[statisticsIndex] = mean;

        double stdDev = 0;
        for (int point = 0; point < tableIndex.getPointCount(); point++) {
            double value = doubleBuffer.get(timeSeriesOffset + point);
            if (!Double.isNaN(value)) {
                stdDev += (value - mean) * (value - mean);
            }
        }
        stdDev = nbPoints > 1 ? Math.sqrt(stdDev / (nbPoints - 1)) : 0;
        stdDevs[statisticsIndex] = stdDev;
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

        statsLock.lock();
        try {
            updateStatistics(version, timeSeriesNum);
            return stats[statisticsIndex];
        } finally {
            statsLock.unlock();
        }
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

    private void computeConstantTimeSeriesPpmcc(double[] r, int version) {
        for (int timeSeriesNum2 = 0; timeSeriesNum2 < doubleTimeSeriesNames.size(); timeSeriesNum2++) {
            int statisticsIndex2 = getStatisticsIndex(version, timeSeriesNum2);
            double stdDev2 = stdDevs[statisticsIndex2];

            // time series 1 is correlated to other constant time series
            r[timeSeriesNum2] = stdDev2 == 0 ? 1 : 0;
        }
    }

    private void computeVariableTimeSeriesPpmcc(double[] r, int timeSeriesNum1, int statisticsIndex1, double stdDev1,
                                                int version) {
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

    public double[] computePpmcc(String timeSeriesName, int version) {
        int timeSeriesNum1 = doubleTimeSeriesNames.getIndex(timeSeriesName);
        checkVersion(version);

        Stopwatch stopWatch = Stopwatch.createStarted();

        double[] r = new double[doubleTimeSeriesNames.size()];

        statsLock.lock();
        try {
            updateStatistics(version);

            int statisticsIndex1 = getStatisticsIndex(version, timeSeriesNum1);
            double stdDev1 = stdDevs[statisticsIndex1];

            if (stdDev1 == 0) { // constant time series
                computeConstantTimeSeriesPpmcc(r, version);
            } else {
                computeVariableTimeSeriesPpmcc(r, timeSeriesNum1, statisticsIndex1, stdDev1, version);
            }
        } finally {
            statsLock.unlock();
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
            writeCsv(writer, TimeSeriesConstants.DEFAULT_SEPARATOR, ZoneId.systemDefault());
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

    private void writeHeader(Writer writer, CsvConfig config) throws IOException {
        // write header
        writer.write("Time");
        writer.write(config.separator);
        writer.write("Version");
        if (timeSeriesMetadata != null) {
            for (TimeSeriesMetadata metadata : timeSeriesMetadata) {
                writer.write(config.separator);
                writer.write(metadata.getName());
            }
        }
        writer.write(System.lineSeparator());
    }

    private class CsvCache {

        static final int CACHE_SIZE = 10;

        final double[] doubleCache = new double[CACHE_SIZE * doubleTimeSeriesNames.size()];

        final String[] stringCache = new String[CACHE_SIZE * stringTimeSeriesNames.size()];
    }

    private static class CsvConfig {

        final char separator;

        final DateTimeFormatter dateTimeFormatter;

        public CsvConfig(char separator, DateTimeFormatter dateTimeFormatter) {
            this.separator = separator;
            this.dateTimeFormatter = dateTimeFormatter;
        }
    }

    private void fillCache(int point, CsvCache cache, int cachedPoints, int version) {
        for (int i = 0; i < timeSeriesMetadata.size(); i++) {
            TimeSeriesMetadata metadata = timeSeriesMetadata.get(i);
            int timeSeriesNum = timeSeriesIndexDoubleOrString.get(i);
            int timeSeriesOffset = getTimeSeriesOffset(version, timeSeriesNum);
            if (metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                for (int cachedPoint = 0; cachedPoint < cachedPoints; cachedPoint++) {
                    cache.doubleCache[cachedPoint * doubleTimeSeriesNames.size() + timeSeriesNum] = doubleBuffer.get(timeSeriesOffset + point + cachedPoint);
                }
            } else if (metadata.getDataType() == TimeSeriesDataType.STRING) {
                for (int cachedPoint = 0; cachedPoint < cachedPoints; cachedPoint++) {
                    cache.stringCache[cachedPoint * stringTimeSeriesNames.size() + timeSeriesNum] = stringBuffer.getString(timeSeriesOffset + point + cachedPoint);
                }
            } else {
                throw new AssertionError("Unexpected data type " + metadata.getDataType());
            }
        }
    }

    private static void writeDouble(Writer writer, double value) throws IOException {
        if (!Double.isNaN(value)) {
            writer.write(Double.toString(value));
        }
    }

    private static void writeString(Writer writer, String value) throws IOException {
        if (value != null) {
            writer.write(value);
        }
    }

    private void dumpCache(Writer writer, CsvConfig config, int point, CsvCache cache, int cachedPoints, int version) throws IOException {
        for (int cachedPoint = 0; cachedPoint < cachedPoints; cachedPoint++) {
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tableIndex.getTimeAt(point + cachedPoint)), ZoneId.systemDefault());
            writer.write(dateTime.format(config.dateTimeFormatter));
            writer.write(config.separator);
            writer.write(Integer.toString(version));
            for (int i = 0; i < timeSeriesMetadata.size(); i++) {
                TimeSeriesMetadata metadata = timeSeriesMetadata.get(i);
                int timeSeriesNum = timeSeriesIndexDoubleOrString.get(i);
                writer.write(config.separator);
                if (metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                    double value = cache.doubleCache[cachedPoint * doubleTimeSeriesNames.size() + timeSeriesNum];
                    writeDouble(writer, value);
                } else if (metadata.getDataType() == TimeSeriesDataType.STRING) {
                    String value = cache.stringCache[cachedPoint * stringTimeSeriesNames.size() + timeSeriesNum];
                    writeString(writer, value);
                } else {
                    throw new AssertionError("Unexpected data type " + metadata.getDataType());
                }
            }
            writer.write(System.lineSeparator());
        }
    }

    public void writeCsv(Writer writer, char separator, ZoneId zoneId) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(zoneId);

        Stopwatch stopWatch = Stopwatch.createStarted();

        try {
            CsvConfig config = new CsvConfig(separator, DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId));

            writeHeader(writer, config);

            if (timeSeriesMetadata != null) {
                // read time series in the doubleBuffer per 10 points chunk to avoid cache missed and improve performances
                CsvCache cache = new CsvCache();

                // write data
                for (int version = fromVersion; version <= toVersion; version++) {
                    for (int point = 0; point < tableIndex.getPointCount(); point += CsvCache.CACHE_SIZE) {

                        int cachedPoints = Math.min(CsvCache.CACHE_SIZE, tableIndex.getPointCount() - point);

                        // copy from doubleBuffer to cache
                        fillCache(point, cache, cachedPoints, version);

                        // then write cache to CSV
                        dumpCache(writer, config, point, cache, cachedPoints, version);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LOGGER.info("Csv written in {} ms", stopWatch.elapsed(TimeUnit.MILLISECONDS));
    }
}
