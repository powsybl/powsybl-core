/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.commons.json.JsonUtil;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TimeSeries<P extends AbstractPoint, C extends ArrayChunk<P>> extends Iterable<P> {

    TimeSeriesMetadata getMetadata();

    List<C> getChunks();

    Stream<P> stream();

    Iterator<P> iterator();

    void writeJson(JsonGenerator generator);

    static void writeJson(JsonGenerator generator, List<TimeSeries> timeSeriesList) {
        for (TimeSeries timeSeries : timeSeriesList) {
            timeSeries.writeJson(generator);
        }
    }

    static void writeJson(Writer writer, List<TimeSeries> timeSeriesList) {
        JsonUtil.writeJson(writer, generator -> writeJson(generator, timeSeriesList));
    }

    static void writeJson(Path file, List<TimeSeries> timeSeriesList) {
        JsonUtil.writeJson(file, generator -> writeJson(generator, timeSeriesList));
    }

    static List<TimeSeries> parseJson(JsonParser parser) {
        List<TimeSeries> timeSeriesList = new ArrayList<>();
        try {
            TimeSeriesMetadata metadata = null;
            int offset = -1;
            List<DoubleArrayChunk> doubleChunks = null;
            List<StringArrayChunk> stringChunks = null;
            TDoubleArrayList doubleValues = null;
            List<String> stringValues = null;
            TIntArrayList stepLengths = null;
            int uncompressedLength = -1;
            boolean valuesOrLengthArray = false;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME:
                        String fieldName = parser.getCurrentName();
                        switch (fieldName) {
                            case "metadata":
                                metadata = TimeSeriesMetadata.parseJson(parser);
                                break;
                            case "offset":
                                offset = parser.nextIntValue(-1);
                                break;
                            case "uncompressedLength":
                                uncompressedLength = parser.nextIntValue(-1);
                                break;
                            case "stepLengths":
                                stepLengths = new TIntArrayList();
                                valuesOrLengthArray = true;
                                break;
                            case "values":
                            case "stepValues":
                                if (metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                                    doubleValues = new TDoubleArrayList();
                                } else if (metadata.getDataType() == TimeSeriesDataType.STRING) {
                                    stringValues = new ArrayList<>();
                                } else {
                                    throw new AssertionError();
                                }
                                valuesOrLengthArray = true;
                                break;
                        }
                        break;
                    case END_OBJECT:
                        if (stepLengths == null) {
                            if (doubleValues != null) {
                                if (doubleChunks == null) {
                                    doubleChunks = new ArrayList<>();
                                }
                                doubleChunks.add(new UncompressedDoubleArrayChunk(offset, doubleValues.toArray()));
                            } else if (stringValues != null) {
                                if (stringChunks == null) {
                                    stringChunks = new ArrayList<>();
                                }
                                stringChunks.add(new UncompressedStringArrayChunk(offset, stringValues.toArray(new String[stringValues.size()])));
                            }
                        } else {
                            if (doubleValues != null) {
                                if (doubleChunks == null) {
                                    doubleChunks = new ArrayList<>();
                                }
                                doubleChunks.add(new CompressedDoubleArrayChunk(offset, uncompressedLength,
                                                                                doubleValues.toArray(), stepLengths.toArray()));
                                doubleValues = null;
                                stepLengths = null;
                                uncompressedLength = -1;
                            } else if (stringValues != null) {
                                if (stringChunks == null) {
                                    stringChunks = new ArrayList<>();
                                }
                                stringChunks.add(new CompressedStringArrayChunk(offset, uncompressedLength,
                                                                                stringValues.toArray(new String[stringValues.size()]),
                                                                                stepLengths.toArray()));
                                stringValues = null;
                                stepLengths = null;
                                uncompressedLength = -1;
                            }
                        }
                        offset = -1;
                        break;
                    case END_ARRAY:
                        if (valuesOrLengthArray) {
                            valuesOrLengthArray = false;
                        } else {
                            if (metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                                timeSeriesList.add(new StoredDoubleTimeSeries(metadata, doubleChunks));
                                doubleChunks = null;
                            } else if (metadata.getDataType() == TimeSeriesDataType.STRING) {
                                timeSeriesList.add(new StringTimeSeries(metadata, stringChunks));
                                stringChunks = null;
                            } else {
                                throw new AssertionError();
                            }
                            metadata = null;
                        }
                        break;
                    case VALUE_NUMBER_FLOAT:
                        doubleValues.add(parser.getDoubleValue());
                        break;
                    case VALUE_NUMBER_INT:
                        if (stepLengths != null) {
                            stepLengths.add(parser.getIntValue());
                        } else if (doubleValues != null) {
                            doubleValues.add(parser.getIntValue());
                        } else {
                            throw new IllegalStateException("Should not happen");
                        }
                        break;
                    case VALUE_STRING:
                        stringValues.add(parser.getValueAsString());
                        break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return timeSeriesList;
    }

    static List<TimeSeries> parseJson(String json) {
        return JsonUtil.parseJson(json, parser -> parseJson(parser));
    }

    static List<TimeSeries> parseJson(Path file) {
        return JsonUtil.parseJson(file, parser -> parseJson(parser));
    }
}
