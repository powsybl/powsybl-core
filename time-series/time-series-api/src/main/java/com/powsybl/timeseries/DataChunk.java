/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface DataChunk<P extends AbstractPoint, A extends DataChunk<P, A>> {

    class Split<P extends AbstractPoint, A extends DataChunk<P, A>> {

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
     * Get data chunk offset.
     *
     * @return data chunk offset
     */
    int getOffset();

    /**
     * Get data chunk length
     *
     * @return data chunk length
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
     * Append the chunk with the one given in argument, and return the result. "This" dataChunk and the one in argument remain unchanged.
     * The two chunks have to be successive, i.e : this.getOffset() + this.length() = otherChunk.getOffset()
     * @param otherChunk the chunk to append with this object. It has to be the same implementation as this object.
     */
    A append(A otherChunk);

    /**
     * Serialize this data chunk to json.
     *
     * @param generator a json generator (jackson)
     */
    void writeJson(JsonGenerator generator);

    static DoubleDataChunk create(int offset, double[] values) {
        return new UncompressedDoubleDataChunk(offset, values);
    }

    static DoubleDataChunk create(double... values) {
        return new UncompressedDoubleDataChunk(0, values);
    }

    static StringDataChunk create(int offset, String[] values) {
        return new UncompressedStringDataChunk(offset, values);
    }

    static StringDataChunk create(String... values) {
        return new UncompressedStringDataChunk(0, values);
    }

    /**
     * Serialize a chunk list to json
     *
     * @param generator a json generator (jackson)
     * @param chunks    the chunk list
     */
    static void writeJson(JsonGenerator generator, List<? extends DataChunk> chunks) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartArray();
            for (DataChunk chunk : chunks) {
                chunk.writeJson(generator);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String toJson();

    class JsonParsingContext {
        JsonParsingContext(List<DoubleDataChunk> doubleChunks, List<StringDataChunk> stringChunks) {
            this.doubleChunks = Objects.requireNonNull(doubleChunks);
            this.stringChunks = Objects.requireNonNull(stringChunks);
        }

        private int offset = -1;
        private final List<DoubleDataChunk> doubleChunks;
        private final List<StringDataChunk> stringChunks;
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
            case "offset" -> {
                context.offset = parser.nextIntValue(-1);
                context.doubleValues = null;
                context.stringValues = null;
            }
            case "uncompressedLength" -> context.uncompressedLength = parser.nextIntValue(-1);
            case "stepLengths" -> {
                context.stepLengths = new TIntArrayList();
                context.valuesOrLengthArray = true;
            }
            case "values", "stepValues" -> context.valuesOrLengthArray = true;
            default -> {
                // Do nothing
            }
        }
    }

    static void addUncompressedChunk(JsonParsingContext context) {
        if (context.doubleValues != null && context.stringValues == null) {
            context.doubleChunks.add(new UncompressedDoubleDataChunk(context.offset, context.doubleValues.toArray()));
        } else if (context.stringValues != null && context.doubleValues == null) {
            context.stringChunks.add(new UncompressedStringDataChunk(context.offset, context.stringValues.toArray(new String[0])));
        } else if (context.stringValues != null) {
            throw new IllegalStateException("doubleValues and stringValues are not expected to be non null at the same time");
        } else {
            throw new IllegalStateException("doubleValues and stringValues are not expected to be null at the same time");
        }
    }

    static void addCompressedChunk(JsonParsingContext context) {
        if (context.doubleValues != null && context.stringValues == null) {
            context.doubleChunks.add(new CompressedDoubleDataChunk(context.offset, context.uncompressedLength,
                    context.doubleValues.toArray(), context.stepLengths.toArray()));
            context.doubleValues = null;
            context.stepLengths = null;
            context.uncompressedLength = -1;
        } else if (context.stringValues != null && context.doubleValues == null) {
            context.stringChunks.add(new CompressedStringDataChunk(context.offset, context.uncompressedLength,
                    context.stringValues.toArray(new String[0]),
                    context.stepLengths.toArray()));
            context.stringValues = null;
            context.stepLengths = null;
            context.uncompressedLength = -1;
        } else if (context.stringValues != null) {
            throw new IllegalStateException("doubleValues and stringValues are not expected to be non null at the same time");
        } else {
            throw new IllegalStateException("doubleValues and stringValues are not expected to be null at the same time");
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

    static void parseJson(JsonParser parser, List<DoubleDataChunk> doubleChunks,
                          List<StringDataChunk> stringChunks) {
        parseJson(parser, doubleChunks, stringChunks, false);
    }

    static void parseJson(JsonParser parser, List<DoubleDataChunk> doubleChunks,
                          List<StringDataChunk> stringChunks, boolean single) {
        Objects.requireNonNull(parser);
        try {
            JsonParsingContext context = new JsonParsingContext(doubleChunks, stringChunks);
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME -> parseFieldName(parser, context);
                    case END_OBJECT -> {
                        parseEndObject(context);
                        if (single) {
                            return;
                        }
                    }
                    case END_ARRAY -> {
                        if (context.valuesOrLengthArray) {
                            context.valuesOrLengthArray = false;
                        } else {
                            return; // end of chunk parsing
                        }
                    }
                    case VALUE_NUMBER_FLOAT -> context.addDoubleValue(parser.getDoubleValue());
                    case VALUE_NUMBER_INT -> parseValueNumberInt(parser, context);
                    case VALUE_STRING -> context.addStringValue(parser.getValueAsString());
                    case VALUE_NULL -> context.addStringValue(null);
                    default -> {
                        // Do nothing
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
