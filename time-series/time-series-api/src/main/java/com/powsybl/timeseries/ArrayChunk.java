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
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ArrayChunk<P extends AbstractPoint, A extends ArrayChunk<P, A>> {

    class Split<P extends AbstractPoint, A extends ArrayChunk<P, A>> {

        private final A chunk1;

        private final A chunk2;

        Split(A chunk1, A chunk2) {
            this.chunk1 = Objects.requireNonNull(chunk1);
            this.chunk2 = Objects.requireNonNull(chunk2);
        }

        A getChunk1() {
            return chunk1;
        }

        A getChunk2() {
            return chunk2;
        }
    }

    /**
     * Get array chunk offset.
     *
     * @return array chunk offset
     */
    int getOffset();

    /**
     * Get array chunk length
     *
     * @return array chunk length
     */
    int getLength();

    /**
     * Get estimated size in bytes.
     *
     * @return estimated size in bytes
     */
    int getEstimatedSize();

    /**
     * Get compression factor. 1 means no compression.
     *
     * @return the compression factor
     */
    double getCompressionFactor();

    /**
     * Check if chunk is in compressed form.
     *
     * @return true if chunk is in compressed form, false otherwise
     */
    boolean isCompressed();

    /**
     * Get data type.
     *
     * @return the data type
     */
    TimeSeriesDataType getDataType();

    /**
     * Get a point stream.
     *
     * @param index the time series index
     * @return a point stream
     */
    Stream<P> stream(TimeSeriesIndex index);

    /**
     * Get a point iterator.
     *
     * @param index the time series index
     * @return a point iterator
     */
    Iterator<P> iterator(TimeSeriesIndex index);

    /**
     * Try to compress the chunk.
     *
     * @return the compressed chunk or itself if compression is not efficient enough
     */
    A tryToCompress();

    /**
     * Split the chunk in two parts.
     *
     * @param splitIndex the split index
     * @return both chunks
     */
    Split<P, A> splitAt(int splitIndex);

    /**
     * Serialize this array chunk to json.
     *
     * @param generator a json generator (jackson)
     * @throws IOException in case of json writing error
     */
    void writeJson(JsonGenerator generator);

    /**
     * Serialize a chunk list to json
     *
     * @param generator a json generator (jackson)
     * @param chunks    the chunk list
     */
    static void writeJson(JsonGenerator generator, List<? extends ArrayChunk> chunks) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartArray();
            for (ArrayChunk chunk : chunks) {
                chunk.writeJson(generator);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    class JsonParsingContext {
        JsonParsingContext(List<DoubleArrayChunk> doubleChunks, List<StringArrayChunk> stringChunks) {
            this.doubleChunks = Objects.requireNonNull(doubleChunks);
            this.stringChunks = Objects.requireNonNull(stringChunks);
        }
        private int offset = -1;
        private List<DoubleArrayChunk> doubleChunks;
        private List<StringArrayChunk> stringChunks;
        private TDoubleArrayList doubleValues;
        private List<String> stringValues;
        private TIntArrayList stepLengths;
        private int uncompressedLength = -1;
        private boolean valuesOrLengthArray = false;

        void addDoubleValue(double value) {
            if (doubleValues == null) {
                doubleValues = new TDoubleArrayList();
            }
            doubleValues.add(value);
        }

        void addStringValue(String value) {
            if (stringValues == null) {
                stringValues = new ArrayList<>();
            }
            stringValues.add(value);
        }
    }

    static void parseFieldName(JsonParser parser, JsonParsingContext context) throws IOException {
        String fieldName = parser.getCurrentName();
        switch (fieldName) {
            case "offset":
                context.offset = parser.nextIntValue(-1);
                context.doubleValues = null;
                context.stringValues = null;
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
                context.valuesOrLengthArray = true;
                break;
            default:
                break;
        }
    }

    static void addUncompressedChunk(JsonParsingContext context) {
        if (context.doubleValues != null && context.stringValues == null) {
            context.doubleChunks.add(new UncompressedDoubleArrayChunk(context.offset, context.doubleValues.toArray()));
        } else if (context.stringValues != null && context.doubleValues == null) {
            context.stringChunks.add(new UncompressedStringArrayChunk(context.offset, context.stringValues.toArray(new String[context.stringValues.size()])));
        } else if (context.stringValues != null && context.doubleValues != null) {
            throw new AssertionError("doubleValues and stringValues are not expected to be non null at the same time");
        } else {
            throw new AssertionError("doubleValues and stringValues are not expected to be null at the same time");
        }
    }

    static void addCompressedChunk(JsonParsingContext context) {
        if (context.doubleValues != null && context.stringValues == null) {
            context.doubleChunks.add(new CompressedDoubleArrayChunk(context.offset, context.uncompressedLength,
                    context.doubleValues.toArray(), context.stepLengths.toArray()));
            context.doubleValues = null;
            context.stepLengths = null;
            context.uncompressedLength = -1;
        } else if (context.stringValues != null && context.doubleValues == null) {
            context.stringChunks.add(new CompressedStringArrayChunk(context.offset, context.uncompressedLength,
                    context.stringValues.toArray(new String[context.stringValues.size()]),
                    context.stepLengths.toArray()));
            context.stringValues = null;
            context.stepLengths = null;
            context.uncompressedLength = -1;
        } else if (context.stringValues != null && context.doubleValues != null) {
            throw new AssertionError("doubleValues and stringValues are not expected to be non null at the same time");
        } else {
            throw new AssertionError("doubleValues and stringValues are not expected to be null at the same time");
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

    static void parseValueNumberInt(JsonParser parser, JsonParsingContext context) throws IOException {
        if (context.stepLengths != null) {
            context.stepLengths.add(parser.getIntValue());
        } else {
            context.addDoubleValue(parser.getIntValue());
        }
    }

    static void parseJson(JsonParser parser, List<DoubleArrayChunk> doubleChunks,
                          List<StringArrayChunk> stringChunks) {
        parseJson(parser, doubleChunks, stringChunks, false);
    }

    static void parseJson(JsonParser parser, List<DoubleArrayChunk> doubleChunks,
                          List<StringArrayChunk> stringChunks, boolean single) {
        Objects.requireNonNull(parser);
        try {
            JsonParsingContext context = new JsonParsingContext(doubleChunks, stringChunks);
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME:
                        parseFieldName(parser, context);
                        break;
                    case END_OBJECT:
                        parseEndObject(context);
                        if (single) {
                            return;
                        } else {
                            break;
                        }
                    case END_ARRAY:
                        if (context.valuesOrLengthArray) {
                            context.valuesOrLengthArray = false;
                        } else {
                            return; // end of chunk parsing
                        }
                        break;
                    case VALUE_NUMBER_FLOAT:
                        context.addDoubleValue(parser.getDoubleValue());
                        break;
                    case VALUE_NUMBER_INT:
                        parseValueNumberInt(parser, context);
                        break;
                    case VALUE_STRING:
                        context.addStringValue(parser.getValueAsString());
                        break;
                    case VALUE_NULL:
                        context.addStringValue(null);
                        break;

                    default:
                        break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
