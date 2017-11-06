/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Iterators;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTimeSeries<P extends AbstractPoint, C extends ArrayChunk<P>> {

    protected final TimeSeriesMetadata metadata;

    protected final List<C> chunks;

    public AbstractTimeSeries(TimeSeriesMetadata metadata, C... chunks) {
        this(metadata, Arrays.asList(chunks));
    }

    public AbstractTimeSeries(TimeSeriesMetadata metadata, List<C> chunks) {
        this.metadata = Objects.requireNonNull(metadata);
        this.chunks = Objects.requireNonNull(chunks);
    }

    public List<C> getChunks() {
        return chunks;
    }

    public TimeSeriesMetadata getMetadata() {
        return metadata;
    }

    protected abstract C createGapFillingChunk(int i, int length);

    private List<C> fillGap() {
        // sort chunks by offset
        List<C> sortedChunks = chunks.stream()
                .sorted(Comparator.comparing(C::getOffset))
                .collect(Collectors.toList());
        int pointCount = metadata.getIndex().getPointCount();
        int i = 0;
        List<C> repairedChunks = new ArrayList<>(sortedChunks.size());
        for (C chunk : sortedChunks) {
            // check chunk offset is included in index range
            if (chunk.getOffset() > pointCount - 1) {
                throw new TimeSeriesException("Chunk offset " + chunk.getOffset() + " is out of index range [" + (pointCount - 1) +
                        ", " + (i + chunk.getLength()) + "]");
            }

            // check chunk overlap
            if (chunk.getOffset() < i) {
                throw new TimeSeriesException("Chunk at offset " + chunk.getOffset() + " overlap with previous one");
            }

            // check all values are included in index range
            if (i + chunk.getLength() > pointCount) {
                throw new TimeSeriesException("Chunk value at " + (i + chunk.getLength()) + " is out of index range [" +
                        (pointCount - 1) + ", " + (i + chunk.getLength()) + "]");
            }

            // fill with NaN if there is a gap with previous chunk
            if (chunk.getOffset() > i) {
                repairedChunks.add(createGapFillingChunk(i, chunk.getOffset() - i));
                i = chunk.getOffset();
            }
            repairedChunks.add(chunk);

            i += chunk.getLength();
        }
        if (i < pointCount) {
            repairedChunks.add(createGapFillingChunk(i, pointCount - i));
        }
        return repairedChunks;
    }

    public Stream<P> stream() {
        return fillGap().stream().flatMap(chunk -> chunk.stream(metadata.getIndex()));
    }

    public Iterator<P> iterator() {
        return Iterators.concat(fillGap().stream().map(c -> c.iterator(metadata.getIndex())).collect(Collectors.toList()).iterator());
    }

    public void writeJson(JsonGenerator generator) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            generator.writeFieldName("metadata");
            metadata.writeJson(generator);
            generator.writeFieldName("chunks");
            ArrayChunk.writeJson(generator, chunks);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
