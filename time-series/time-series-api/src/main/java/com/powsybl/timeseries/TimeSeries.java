/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Doubles;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.ast.NodeCalc;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import gnu.trove.list.array.TDoubleArrayList;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TimeSeries<P extends AbstractPoint, T extends TimeSeries<P, T>> extends Iterable<P> {

    enum TimeFormat {
        DATE_TIME,
        MILLIS,
        FRACTIONS_OF_SECOND;
    }

    TimeSeriesMetadata getMetadata();

    void synchronize(TimeSeriesIndex newIndex);

    Stream<P> stream();

    Iterator<P> iterator();

    List<T> split(int newChunkSize);

    void setTimeSeriesNameResolver(TimeSeriesNameResolver resolver);

    static StoredDoubleTimeSeries createDouble(String name, TimeSeriesIndex index) {
        return createDouble(name, index, new double[0]);
    }

    static StoredDoubleTimeSeries createDouble(String name, TimeSeriesIndex index, double... values) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(index);
        Objects.requireNonNull(values);
        List<DoubleDataChunk> chunks = new ArrayList<>();
        if (values.length > 0) {
            if (index.getPointCount() != values.length) {
                throw new IllegalArgumentException("Bad number of values " + values.length + ", expected " + index.getPointCount());
            }
            chunks.add(new UncompressedDoubleDataChunk(0, values));
        }
        return new StoredDoubleTimeSeries(new TimeSeriesMetadata(name, TimeSeriesDataType.DOUBLE, index), chunks);
    }

    static StringTimeSeries createString(String name, TimeSeriesIndex index) {
        return createString(name, index, new String[0]);
    }

    static int computeChunkCount(TimeSeriesIndex index, int newChunkSize) {
        return (int) Math.ceil((double) index.getPointCount() / newChunkSize);
    }

    static StringTimeSeries createString(String name, TimeSeriesIndex index, String... values) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(index);
        Objects.requireNonNull(values);
        List<StringDataChunk> chunks = new ArrayList<>();
        if (values.length > 0) {
            if (index.getPointCount() != values.length) {
                throw new IllegalArgumentException("Bad number of values " + values.length + ", expected " + index.getPointCount());
            }
            chunks.add(new UncompressedStringDataChunk(0, values));
        }
        return new StringTimeSeries(new TimeSeriesMetadata(name, TimeSeriesDataType.STRING, index), chunks);
    }

    static <P extends AbstractPoint, T extends TimeSeries<P, T>> List<List<T>> split(List<T> timeSeriesList, int newChunkSize) {
        Objects.requireNonNull(timeSeriesList);
        if (timeSeriesList.isEmpty()) {
            throw new IllegalArgumentException("Time series list is empty");
        }
        if (newChunkSize < 1) {
            throw new IllegalArgumentException("Invalid chunk size: " + newChunkSize);
        }
        Set<TimeSeriesIndex> indexes = timeSeriesList.stream()
                .map(ts -> ts.getMetadata().getIndex())
                .filter(index -> !(index instanceof InfiniteTimeSeriesIndex))
                .collect(Collectors.toSet());
        if (indexes.isEmpty()) {
            throw new IllegalArgumentException("Cannot split a list of time series with only infinite index");
        }
        if (indexes.size() != 1) {
            throw new IllegalArgumentException("Cannot split a list of time series with different time indexes: " + indexes);
        }
        TimeSeriesIndex index = indexes.iterator().next();
        if (newChunkSize > index.getPointCount()) {
            throw new IllegalArgumentException("New chunk size " + newChunkSize + " is greater than point count "
                    + index.getPointCount());
        }
        int chunkCount = computeChunkCount(index, newChunkSize);
        List<List<T>> splitList = new ArrayList<>(chunkCount);
        for (int i = 0; i < chunkCount; i++) {
            splitList.add(new ArrayList<>(timeSeriesList.size()));
        }
        for (T timeSeries : timeSeriesList) {
            List<T> split = timeSeries.split(newChunkSize);
            for (int i = 0; i < chunkCount; i++) {
                splitList.get(i).add(split.get(i));
            }
        }
        return splitList;
    }

    static Map<Integer, List<TimeSeries>> parseCsv(Path file) {
        return parseCsv(file, new TimeSeriesCsvConfig());
    }

    static Map<Integer, List<TimeSeries>> parseCsv(String csv) {
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            return parseCsv(reader, new TimeSeriesCsvConfig());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Map<Integer, List<TimeSeries>> parseCsv(String csv, TimeSeriesCsvConfig timeSeriesCsvConfig) {
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            return parseCsv(reader, timeSeriesCsvConfig);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Map<Integer, List<TimeSeries>> parseCsv(Path file, TimeSeriesCsvConfig timeSeriesCsvConfig) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseCsv(reader, timeSeriesCsvConfig);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static double parseDouble(String token) {
        return token.isEmpty() ? Double.NaN : Double.parseDouble(token);
    }

    static String checkString(String token) {
        return token.isEmpty() ? null : token;
    }

    class CsvParsingContext {
        private final List<String> names;
        private final TimeSeriesCsvConfig timeSeriesCsvConfig;
        private final int fixedColumns;

        private final TimeSeriesDataType[] dataTypes;
        private final Object[] values;

        private final List<Long> times = new ArrayList<>();

        private TimeSeriesIndex refIndex;

        CsvParsingContext(List<String> names) {
            this(names, new TimeSeriesCsvConfig());
        }

        CsvParsingContext(List<String> names, TimeSeriesCsvConfig timeSeriesCsvConfig) {
            this.names = names;
            this.timeSeriesCsvConfig = timeSeriesCsvConfig;
            this.fixedColumns = timeSeriesCsvConfig.versioned() ? 2 : 1;
            dataTypes = new TimeSeriesDataType[names.size()];
            values = new Object[names.size()];
        }

        private static TimeSeriesException assertDataType(TimeSeriesDataType dataType) {
            return new TimeSeriesException("Unexpected data type " + dataType);
        }

        private TDoubleArrayList createDoubleValues() {
            TDoubleArrayList doubleValues = new TDoubleArrayList();
            if (!times.isEmpty()) {
                doubleValues.fill(0, times.size(), Double.NaN);
            }
            return doubleValues;
        }

        private List<String> createStringValues() {
            List<String> stringValues = new ArrayList<>();
            if (!times.isEmpty()) {
                for (int j = 0; j < times.size(); j++) {
                    stringValues.add(null);
                }
            }
            return stringValues;
        }

        int getVersion(String[] tokens) {
            return timeSeriesCsvConfig.versioned() ? Integer.parseInt(tokens[1]) : 0;
        }

        int timesSize() {
            return times.size();
        }

        int expectedTokens() {
            return names.size() + fixedColumns;
        }

        void parseToken(int i, String token) {
            if (dataTypes[i - fixedColumns] == null) {
                // test double parsing, in case of error we consider it a string time series
                if (Doubles.tryParse(token) != null) {
                    dataTypes[i - fixedColumns] = TimeSeriesDataType.DOUBLE;
                    TDoubleArrayList doubleValues = createDoubleValues();
                    doubleValues.add(parseDouble(token));
                    values[i - fixedColumns] = doubleValues;
                } else {
                    dataTypes[i - fixedColumns] = TimeSeriesDataType.STRING;
                    List<String> stringValues = createStringValues();
                    stringValues.add(checkString(token));
                    values[i - fixedColumns] = stringValues;
                }
            } else {
                if (dataTypes[i - fixedColumns] == TimeSeriesDataType.DOUBLE) {
                    ((TDoubleArrayList) values[i - fixedColumns]).add(parseDouble(token));
                } else if (dataTypes[i - fixedColumns] == TimeSeriesDataType.STRING) {
                    ((List<String>) values[i - fixedColumns]).add(checkString(token));
                } else {
                    throw assertDataType(dataTypes[i - fixedColumns]);
                }
            }
        }

        void parseLine(String[] tokens) {
            for (int i = fixedColumns; i < tokens.length; i++) {
                String token = tokens[i] != null ? tokens[i].trim() : "";
                parseToken(i, token);
            }

            parseTokenTime(tokens);
        }

        void parseTokenTime(String[] tokens) {
            switch (timeSeriesCsvConfig.timeFormat()) {
                case DATE_TIME:
                    times.add(ZonedDateTime.parse(tokens[0]).toInstant().toEpochMilli());
                    break;
                case FRACTIONS_OF_SECOND:
                    Double time = Double.parseDouble(tokens[0]) * 1000;
                    times.add(time.longValue());
                    break;
                case MILLIS:
                    Double millis = Double.parseDouble(tokens[0]);
                    times.add(millis.longValue());
                    break;
                default:
                    throw new IllegalStateException("Unknown time format " + timeSeriesCsvConfig.timeFormat());
            }
        }

        void reInit() {
            // re-init
            times.clear();
            for (int i = 0; i < dataTypes.length; i++) {
                if (dataTypes[i] == TimeSeriesDataType.DOUBLE) {
                    ((TDoubleArrayList) values[i]).clear();
                } else if (dataTypes[i] == TimeSeriesDataType.STRING) {
                    ((List) values[i]).clear();
                } else {
                    throw assertDataType(dataTypes[i]);
                }
            }
        }

        List<TimeSeries> createTimeSeries() {
            // check time spacing is regular
            TimeSeriesIndex index = getTimeSeriesIndex();

            // check all data version have the same index
            if (this.refIndex != null && !index.equals(refIndex)) {
                throw new TimeSeriesException("All version of the data must have the same index: " + refIndex + " != " + index);
            } else {
                this.refIndex = index;
            }

            List<TimeSeries> timeSeriesList = new ArrayList<>(names.size());
            for (int i = 0; i < names.size(); i++) {
                if (Objects.isNull(names.get(i))) {
                    LoggerFactory.getLogger(TimeSeries.class).warn("Timeseries without name");
                    continue;
                }
                TimeSeriesMetadata metadata = new TimeSeriesMetadata(names.get(i), dataTypes[i], index);
                if (dataTypes[i] == TimeSeriesDataType.DOUBLE) {
                    TDoubleArrayList doubleValues = (TDoubleArrayList) values[i];
                    DoubleDataChunk chunk = new UncompressedDoubleDataChunk(0, doubleValues.toArray()).tryToCompress();
                    timeSeriesList.add(new StoredDoubleTimeSeries(metadata, chunk));
                } else if (dataTypes[i] == TimeSeriesDataType.STRING) {
                    List<String> stringValues = (List<String>) values[i];
                    StringDataChunk chunk = new UncompressedStringDataChunk(0, stringValues.toArray(new String[0])).tryToCompress();
                    timeSeriesList.add(new StringTimeSeries(metadata, chunk));
                } else {
                    throw assertDataType(dataTypes[i - fixedColumns]);
                }
            }
            return timeSeriesList;
        }

        private TimeSeriesIndex getTimeSeriesIndex() {
            Long spacing = checkRegularSpacing();
            if (spacing != Long.MIN_VALUE) {
                return new RegularTimeSeriesIndex(times.get(0), times.get(times.size() - 1), spacing);
            } else {
                return new IrregularTimeSeriesIndex(times.stream().mapToLong(l -> l).toArray());
            }
        }

        private long checkRegularSpacing() {
            if (times.size() < 2) {
                throw new TimeSeriesException("At least 2 rows are expected");
            }

            long spacing = Long.MIN_VALUE;
            for (int i = 1; i < times.size(); i++) {
                long duration = times.get(i) - times.get(i - 1);
                if (spacing == Long.MIN_VALUE) {
                    spacing = duration;
                } else {
                    if (duration != spacing) {
                        return Long.MIN_VALUE;
                    }
                }
            }

            return spacing;
        }
    }

    static void readCsvValues(ResultIterator<String[], ParsingContext> iterator, CsvParsingContext context,
                              Map<Integer, List<TimeSeries>> timeSeriesPerVersion) {
        int currentVersion = Integer.MIN_VALUE;
        while (iterator.hasNext()) {
            String[] tokens = iterator.next();

            if (tokens.length != context.expectedTokens()) {
                throw new TimeSeriesException("Columns of line " + context.timesSize() + " are inconsistent with header");
            }

            int version = context.getVersion(tokens);
            if (currentVersion == Integer.MIN_VALUE) {
                currentVersion = version;
            } else if (version != currentVersion) {
                timeSeriesPerVersion.put(currentVersion, context.createTimeSeries());
                context.reInit();
                currentVersion = version;
            }

            context.parseLine(tokens);
        }
        timeSeriesPerVersion.put(currentVersion, context.createTimeSeries());
    }

    static CsvParsingContext readCsvHeader(ResultIterator<String[], ParsingContext> iterator, TimeSeriesCsvConfig timeSeriesCsvConfig) {
        if (!iterator.hasNext()) {
            throw new TimeSeriesException("CSV header is missing");
        }
        String[] tokens = iterator.next();

        checkCsvHeader(timeSeriesCsvConfig, tokens);

        List<String> duplicates = new ArrayList<>();
        Set<String> namesWithoutDuplicates = new HashSet<>();
        for (String token : tokens) {
            if (!namesWithoutDuplicates.add(token)) {
                duplicates.add(token);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new TimeSeriesException("Bad CSV header, there are duplicates in time series names " + duplicates);
        }
        List<String> names = Arrays.asList(tokens).subList(timeSeriesCsvConfig.versioned() ? 2 : 1, tokens.length);
        return new CsvParsingContext(names, timeSeriesCsvConfig);
    }

    static void checkCsvHeader(TimeSeriesCsvConfig timeSeriesCsvConfig, String[] tokens) {
        String separatorStr = Character.toString(timeSeriesCsvConfig.separator());
        if (timeSeriesCsvConfig.versioned() && (tokens.length < 3 || !"time".equals(tokens[0].toLowerCase()) || !"version".equals(tokens[1].toLowerCase()))) {
            throw new TimeSeriesException("Bad CSV header, should be \ntime" + separatorStr + "version" + separatorStr + "...");
        } else if (tokens.length < 2 || !"time".equals(tokens[0].toLowerCase())) {
            throw new TimeSeriesException("Bad CSV header, should be \ntime" + separatorStr + "...");
        }
    }

    static Map<Integer, List<TimeSeries>> parseCsv(BufferedReader reader, TimeSeriesCsvConfig timeSeriesCsvConfig) {
        Objects.requireNonNull(reader);

        Stopwatch stopwatch = Stopwatch.createStarted();

        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = new HashMap<>();

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(timeSeriesCsvConfig.separator());
        settings.getFormat().setQuoteEscape('"');
        settings.getFormat().setLineSeparator(System.lineSeparator());
        settings.setMaxColumns(timeSeriesCsvConfig.getMaxColumns());
        CsvParser csvParser = new CsvParser(settings);
        ResultIterator<String[], ParsingContext> iterator = csvParser.iterate(reader).iterator();
        CsvParsingContext context = readCsvHeader(iterator, timeSeriesCsvConfig);
        readCsvValues(iterator, context, timeSeriesPerVersion);

        LoggerFactory.getLogger(TimeSeries.class)
                .info("{} time series loaded from CSV in {} ms",
                timeSeriesPerVersion.entrySet().stream().mapToInt(e -> e.getValue().size()).sum(),
                stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return timeSeriesPerVersion;
    }

    void writeJson(JsonGenerator generator);

    String toJson();

    static void writeJson(JsonGenerator generator, List<? extends TimeSeries> timeSeriesList) {
        Objects.requireNonNull(timeSeriesList);
        try {
            generator.writeStartArray();
            for (TimeSeries timeSeries : timeSeriesList) {
                timeSeries.writeJson(generator);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void writeJson(Writer writer, List<? extends TimeSeries> timeSeriesList) {
        JsonUtil.writeJson(writer, generator -> writeJson(generator, timeSeriesList));
    }

    static void writeJson(Path file, List<? extends TimeSeries> timeSeriesList) {
        JsonUtil.writeJson(file, generator -> writeJson(generator, timeSeriesList));
    }

    static String toJson(List<? extends TimeSeries> timeSeriesList) {
        return JsonUtil.toJson(generator -> writeJson(generator, timeSeriesList));
    }

    static void parseChunks(JsonParser parser, TimeSeriesMetadata metadata, List<TimeSeries> timeSeriesList) {
        Objects.requireNonNull(metadata);
        List<DoubleDataChunk> doubleChunks = new ArrayList<>();
        List<StringDataChunk> stringChunks = new ArrayList<>();
        DataChunk.parseJson(parser, doubleChunks, stringChunks);
        if (metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
            if (!stringChunks.isEmpty()) {
                throw new TimeSeriesException("String chunks found in a double time series");
            }
            timeSeriesList.add(new StoredDoubleTimeSeries(metadata, doubleChunks));
        } else if (metadata.getDataType() == TimeSeriesDataType.STRING) {
            if (!doubleChunks.isEmpty()) {
                throw new TimeSeriesException("Double chunks found in a string time series");
            }
            timeSeriesList.add(new StringTimeSeries(metadata, stringChunks));
        } else {
            throw new TimeSeriesException("Unexpected time series data type " + metadata.getDataType());
        }
    }

    static List<TimeSeries> parseJson(JsonParser parser) {
        return parseJson(parser, false);
    }

    static List<TimeSeries> parseJson(JsonParser parser, boolean single) {
        Objects.requireNonNull(parser);
        List<TimeSeries> timeSeriesList = new ArrayList<>();
        try {
            TimeSeriesMetadata metadata = null;
            String name = null;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    switch (fieldName) {
                        case "metadata":
                            metadata = TimeSeriesMetadata.parseJson(parser);
                            break;
                        case "chunks":
                            if (metadata == null) {
                                throw new TimeSeriesException("metadata is null");
                            }
                            parseChunks(parser, metadata, timeSeriesList);
                            metadata = null;
                            break;
                        case "name":
                            name = parser.nextTextValue();
                            break;
                        case "expr":
                            Objects.requireNonNull(name);
                            NodeCalc nodeCalc = NodeCalc.parseJson(parser);
                            timeSeriesList.add(new CalculatedTimeSeries(name, nodeCalc));
                            break;
                        default:
                            break;
                    }
                } else if (token == JsonToken.END_OBJECT && single) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return timeSeriesList;
    }

    static List<TimeSeries> parseJson(String json) {
        return JsonUtil.parseJson(json, TimeSeries::parseJson);
    }

    static List<TimeSeries> parseJson(Reader reader) {
        return JsonUtil.parseJson(reader, TimeSeries::parseJson);
    }

    static List<TimeSeries> parseJson(Path file) {
        return JsonUtil.parseJson(file, TimeSeries::parseJson);
    }
}
