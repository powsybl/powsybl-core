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

    class JsonParsingContext {
        private TimeSeriesMetadata metadata;
        private int offset = -1;
        private List<DoubleArrayChunk> doubleChunks;
        private List<StringArrayChunk> stringChunks;
        private TDoubleArrayList doubleValues;
        private List<String> stringValues;
        private TIntArrayList stepLengths;
        private int uncompressedLength = -1;
        private boolean valuesOrLengthArray = false;
    }

    static void parseFieldName(JsonParser parser, JsonParsingContext context) throws IOException {
        String fieldName = parser.getCurrentName();
        switch (fieldName) {
            case "metadata":
                context.metadata = TimeSeriesMetadata.parseJson(parser);
                break;
            case "offset":
                context.offset = parser.nextIntValue(-1);
                break;
            case "uncompressedLength":
                context.uncompressedLength = parser.nextIntValue(-1);
                break;
            case "stepLengths":
                context.stepLengths = new TIntArrayList();
                context.valuesOrLengthArray = true;
                break;
            case "values":
            case "stepValues":
                if (context.metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                    context.doubleValues = new TDoubleArrayList();
                } else if (context.metadata.getDataType() == TimeSeriesDataType.STRING) {
                    context.stringValues = new ArrayList<>();
                } else {
                    throw new AssertionError();
                }
                context.valuesOrLengthArray = true;
                break;
            default:
                break;
        }
    }

    static void addUncompressedChunk(JsonParsingContext context) {
        if (context.doubleValues != null) {
            if (context.doubleChunks == null) {
                context.doubleChunks = new ArrayList<>();
            }
            context.doubleChunks.add(new UncompressedDoubleArrayChunk(context.offset, context.doubleValues.toArray()));
        } else if (context.stringValues != null) {
            if (context.stringChunks == null) {
                context.stringChunks = new ArrayList<>();
            }
            context.stringChunks.add(new UncompressedStringArrayChunk(context.offset, context.stringValues.toArray(new String[context.stringValues.size()])));
        }
    }

    static void addCompressedChunk(JsonParsingContext context) {
        if (context.doubleValues != null) {
            if (context.doubleChunks == null) {
                context.doubleChunks = new ArrayList<>();
            }
            context.doubleChunks.add(new CompressedDoubleArrayChunk(context.offset, context.uncompressedLength,
                    context.doubleValues.toArray(), context.stepLengths.toArray()));
            context.doubleValues = null;
            context.stepLengths = null;
            context.uncompressedLength = -1;
        } else if (context.stringValues != null) {
            if (context.stringChunks == null) {
                context.stringChunks = new ArrayList<>();
            }
            context.stringChunks.add(new CompressedStringArrayChunk(context.offset, context.uncompressedLength,
                    context.stringValues.toArray(new String[context.stringValues.size()]),
                    context.stepLengths.toArray()));
            context.stringValues = null;
            context.stepLengths = null;
            context.uncompressedLength = -1;
        }
    }

    static void parseEndObject(JsonParsingContext context) {
        if (context.stepLengths == null) {
            addUncompressedChunk(context);
        } else {
            addCompressedChunk(context);
        }
        context.offset = -1;
    }

    static void parseEndArray(List<TimeSeries> timeSeriesList, JsonParsingContext context) {
        if (context.valuesOrLengthArray) {
            context.valuesOrLengthArray = false;
        } else {
            if (context.metadata.getDataType() == TimeSeriesDataType.DOUBLE) {
                timeSeriesList.add(new StoredDoubleTimeSeries(context.metadata, context.doubleChunks));
                context.doubleChunks = null;
            } else if (context.metadata.getDataType() == TimeSeriesDataType.STRING) {
                timeSeriesList.add(new StringTimeSeries(context.metadata, context.stringChunks));
                context.stringChunks = null;
            } else {
                throw new AssertionError();
            }
            context.metadata = null;
        }
    }

    static List<TimeSeries> parseJson(JsonParser parser) {
        List<TimeSeries> timeSeriesList = new ArrayList<>();
        try {
            JsonParsingContext context = new JsonParsingContext();
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME:
                        parseFieldName(parser, context);
                        break;
                    case END_OBJECT:
                        parseEndObject(context);
                        break;
                    case END_ARRAY:
                        parseEndArray(timeSeriesList, context);
                        break;
                    case VALUE_NUMBER_FLOAT:
                        context.doubleValues.add(parser.getDoubleValue());
                        break;
                    case VALUE_NUMBER_INT:
                        if (context.stepLengths != null) {
                            context.stepLengths.add(parser.getIntValue());
                        } else if (context.doubleValues != null) {
                            context.doubleValues.add(parser.getIntValue());
                        } else {
                            throw new IllegalStateException("Should not happen");
                        }
                        break;
                    case VALUE_STRING:
                        context.stringValues.add(parser.getValueAsString());
                        break;
                    default:
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

    static List<TimeSeries> parseJson(Path file) {
        return JsonUtil.parseJson(file, TimeSeries::parseJson);
    }
}
