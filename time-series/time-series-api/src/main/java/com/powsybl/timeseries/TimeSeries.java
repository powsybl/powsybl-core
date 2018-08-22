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
import gnu.trove.list.array.TDoubleArrayList;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TimeSeries<P extends AbstractPoint, T extends TimeSeries<P, T>> extends Iterable<P> {

    TimeSeriesMetadata getMetadata();

    void synchronize(TimeSeriesIndex newIndex);

    Stream<P> stream();

    Iterator<P> iterator();

    List<T> split(int newChunkSize);

    static <P extends AbstractPoint, T extends TimeSeries<P, T>> List<List<T>> split(List<T> timeSeriesList, int newChunkSize) {
        Objects.requireNonNull(timeSeriesList);
        if (timeSeriesList.isEmpty()) {
            throw new IllegalArgumentException("Time series list is empty");
        }
        TimeSeriesIndex index = timeSeriesList.get(0).getMetadata().getIndex();
        for (int i = 1; i < timeSeriesList.size(); i++) {
            TimeSeriesMetadata metadata = timeSeriesList.get(i).getMetadata();
            if (!index.equals(metadata.getIndex())) {
                throw new IllegalArgumentException("Time series '" + metadata.getName() + "' has a different index: "
                        + metadata.getIndex() + " != " + index);
            }
        }
        if (newChunkSize > index.getPointCount()) {
            throw new IllegalArgumentException("New chunk size " + newChunkSize + " is greater than point count "
                    + index.getPointCount());
        }
        int chunkCount = (int) Math.ceil((double) index.getPointCount() / newChunkSize);
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
        return parseCsv(file, TimeSeriesConstants.DEFAULT_SEPARATOR);
    }

    static Map<Integer, List<TimeSeries>> parseCsv(String csv, char separator) {
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            return parseCsv(reader, separator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Map<Integer, List<TimeSeries>> parseCsv(Path file, char separator) {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseCsv(reader, separator);
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
        final List<String> names;

        final TimeSeriesDataType[] dataTypes;
        final Object[] values;

        final List<ZonedDateTime> times = new ArrayList<>();

        TimeSeriesIndex refIndex;

        CsvParsingContext(List<String> names) {
            this.names = names;
            dataTypes = new TimeSeriesDataType[names.size()];
            values = new Object[names.size()];
        }

        private static AssertionError assertDataType(TimeSeriesDataType dataType) {
            return new AssertionError("Unexpected data type " + dataType);
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

        void parseToken(int i, String token) {
            if (dataTypes[i - 2] == null) {
                // test double parsing, in case of error we consider it a string time series
                if (Doubles.tryParse(token) != null) {
                    dataTypes[i - 2] = TimeSeriesDataType.DOUBLE;
                    TDoubleArrayList doubleValues = createDoubleValues();
                    doubleValues.add(parseDouble(token));
                    values[i - 2] = doubleValues;
                } else {
                    dataTypes[i - 2] = TimeSeriesDataType.STRING;
                    List<String> stringValues = createStringValues();
                    stringValues.add(checkString(token));
                    values[i - 2] = stringValues;
                }
            } else {
                if (dataTypes[i - 2] == TimeSeriesDataType.DOUBLE) {
                    ((TDoubleArrayList) values[i - 2]).add(parseDouble(token));
                } else if (dataTypes[i - 2] == TimeSeriesDataType.STRING) {
                    ((List<String>) values[i - 2]).add(checkString(token));
                } else {
                    throw assertDataType(dataTypes[i - 2]);
                }
            }
        }

        void parseLine(String[] tokens) {
            for (int i = 2; i < tokens.length; i++) {
                String token = tokens[i].trim();
                parseToken(i, token);
            }
            // empty last cell case
            if (tokens.length == names.size() + 1) {
                parseToken(tokens.length, "");
            }

            times.add(ZonedDateTime.parse(tokens[0]));
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

        private Duration checkRegularSpacing() {
            if (times.size() < 2) {
                throw new TimeSeriesException("At least 2 rows are expected");
            }

            Duration spacing = null;
            for (int i = 1; i < times.size(); i++) {
                Duration duration = Duration.between(times.get(i - 1), times.get(i));
                if (spacing == null) {
                    spacing = duration;
                } else {
                    if (!duration.equals(spacing)) {
                        throw new TimeSeriesException("Time spacing has to be regular");
                    }
                }
            }

            return spacing;
        }

        List<TimeSeries> createTimeSeries() {
            // check time spacing is regular
            Duration spacing = checkRegularSpacing();

            Interval interval = Interval.of(times.get(0).toInstant(), times.get(times.size() - 1).toInstant());
            TimeSeriesIndex index = RegularTimeSeriesIndex.create(interval, spacing);

            // check all data version have the same index
            if (this.refIndex != null && !index.equals(refIndex)) {
                throw new TimeSeriesException("All version of the data must have the same index: " + refIndex + " != " + index);
            } else {
                this.refIndex = index;
            }

            List<TimeSeries> timeSeriesList = new ArrayList<>(names.size());
            for (int i = 0; i < names.size(); i++) {
                TimeSeriesMetadata metadata = new TimeSeriesMetadata(names.get(i), dataTypes[i], index);
                if (dataTypes[i] == TimeSeriesDataType.DOUBLE) {
                    TDoubleArrayList doubleValues = (TDoubleArrayList) values[i];
                    DoubleArrayChunk chunk = new UncompressedDoubleArrayChunk(0, doubleValues.toArray()).tryToCompress();
                    timeSeriesList.add(new StoredDoubleTimeSeries(metadata, chunk));
                } else if (dataTypes[i] == TimeSeriesDataType.STRING) {
                    List<String> stringValues = (List<String>) values[i];
                    StringArrayChunk chunk = new UncompressedStringArrayChunk(0, stringValues.toArray(new String[0])).tryToCompress();
                    timeSeriesList.add(new StringTimeSeries(metadata, chunk));
                } else {
                    throw assertDataType(dataTypes[i - 2]);
                }
            }
            return timeSeriesList;
        }
    }

    static void readCsvValues(BufferedReader reader, String separatorStr, CsvParsingContext context,
                              Map<Integer, List<TimeSeries>> timeSeriesPerVersion) throws IOException {
        String line;
        int currentVersion = Integer.MIN_VALUE;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(separatorStr);

            if (tokens.length != context.names.size() + 2 && tokens.length != context.names.size() + 1) {
                throw new TimeSeriesException("Columns of line " + context.times.size() + " are inconsistent with header");
            }

            int version = Integer.parseInt(tokens[1]);
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

    static CsvParsingContext readCsvHeader(BufferedReader reader, String separatorStr) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new TimeSeriesException("CSV header is missing");
        }
        String[] tokens = line.split(separatorStr);
        if (tokens.length < 2 || !"Time".equals(tokens[0]) || !"Version".equals(tokens[1])) {
            throw new TimeSeriesException("Bad CSV header, should be \nTime" + separatorStr + "Version" + separatorStr + "...");
        }
        List<String> duplicates = new ArrayList<>();
        Set<String> namesWithoutDuplicates = new HashSet<>();
        for (int i = 0; i < tokens.length; i++) {
            if (!namesWithoutDuplicates.add(tokens[i])) {
                duplicates.add(tokens[i]);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new TimeSeriesException("Bad CSV header, there are duplicates in time series names " + duplicates);
        }
        List<String> names = Arrays.asList(tokens).subList(2, tokens.length);
        return new CsvParsingContext(names);
    }

    static Map<Integer, List<TimeSeries>> parseCsv(BufferedReader reader, char separator) {
        Objects.requireNonNull(reader);

        Stopwatch stopwatch = Stopwatch.createStarted();

        Map<Integer, List<TimeSeries>> timeSeriesPerVersion = new HashMap<>();
        String separatorStr = Character.toString(separator);
        try {
            CsvParsingContext context = readCsvHeader(reader, separatorStr);
            readCsvValues(reader, separatorStr, context, timeSeriesPerVersion);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        LoggerFactory.getLogger(TimeSeries.class)
                .info("{} time series loaded from CSV in {} ms",
                timeSeriesPerVersion.entrySet().stream().mapToInt(e -> e.getValue().size()).sum(),
                stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return timeSeriesPerVersion;
    }

    void writeJson(JsonGenerator generator);

    static void writeJson(JsonGenerator generator, List<? extends TimeSeries> timeSeriesList) {
        Objects.requireNonNull(timeSeriesList);
        for (TimeSeries timeSeries : timeSeriesList) {
            timeSeries.writeJson(generator);
        }
    }

    static void writeJson(Writer writer, List<? extends TimeSeries> timeSeriesList) {
        JsonUtil.writeJson(writer, generator -> writeJson(generator, timeSeriesList));
    }

    static void writeJson(Path file, List<? extends TimeSeries> timeSeriesList) {
        JsonUtil.writeJson(file, generator -> writeJson(generator, timeSeriesList));
    }

    static void parseChunks(JsonParser parser, TimeSeriesMetadata metadata, List<TimeSeries> timeSeriesList) {
        Objects.requireNonNull(metadata);
        List<DoubleArrayChunk> doubleChunks = new ArrayList<>();
        List<StringArrayChunk> stringChunks = new ArrayList<>();
        ArrayChunk.parseJson(parser, doubleChunks, stringChunks);
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
            throw new AssertionError("Unexpected time series data type " + metadata.getDataType());
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
                                throw new AssertionError("metadata is null");
                            }
                            parseChunks(parser, metadata, timeSeriesList);
                            metadata = null;
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
