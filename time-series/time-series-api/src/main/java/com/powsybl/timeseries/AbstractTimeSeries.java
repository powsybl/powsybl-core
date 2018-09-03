/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTimeSeries<P extends AbstractPoint, C extends ArrayChunk<P, C>, T extends TimeSeries<P, T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTimeSeries.class);

    protected final TimeSeriesMetadata metadata;

    protected final List<C> chunks;

    public AbstractTimeSeries(TimeSeriesMetadata metadata, C... chunks) {
        this(metadata, Arrays.asList(chunks));
    }

    public AbstractTimeSeries(TimeSeriesMetadata metadata, List<C> chunks) {
        this.metadata = Objects.requireNonNull(metadata);
        this.chunks = Objects.requireNonNull(chunks);
    }

    public void synchronize(TimeSeriesIndex newIndex) {
        Objects.requireNonNull(newIndex);
        if (!metadata.getIndex().equals(newIndex)) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public List<C> getChunks() {
        return chunks;
    }

    public TimeSeriesMetadata getMetadata() {
        return metadata;
    }

    protected abstract C createGapFillingChunk(int i, int length);

    private List<C> getSortedChunks() {
        return chunks.stream()
                .sorted(Comparator.comparing(C::getOffset))
                .collect(Collectors.toList());
    }

    private List<C> getCheckedChunks(boolean fillGap) {
        // sort chunks by offset
        List<C> sortedChunks = getSortedChunks();
        int pointCount = metadata.getIndex().getPointCount();
        int i = 0;
        List<C> checkedChunks = new ArrayList<>(sortedChunks.size());
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
                if (fillGap) {
                    checkedChunks.add(createGapFillingChunk(i, chunk.getOffset() - i));
                }
                i = chunk.getOffset();
            }
            checkedChunks.add(chunk);

            i += chunk.getLength();
        }
        if (fillGap && i < pointCount) {
            checkedChunks.add(createGapFillingChunk(i, pointCount - i));
        }
        return checkedChunks;
    }

    public Stream<P> stream() {
        return getCheckedChunks(true).stream().flatMap(chunk -> chunk.stream(metadata.getIndex()));
    }

    public Iterator<P> iterator() {
        return Iterators.concat(getCheckedChunks(true).stream().map(c -> c.iterator(metadata.getIndex())).collect(Collectors.toList()).iterator());
    }

    protected abstract T createTimeSeries(C chunk);

    private void split(C chunk, List<T> splitList, int newChunkSize) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Split chunk [{}, {}]", chunk.getOffset(), chunk.getOffset() + chunk.getLength() - 1);
        }
        if (chunk.getLength() > newChunkSize) {
            // compute lower intersection index with new chunk size
            int newChunkLowIndex = Math.round(0.5f + (float) chunk.getOffset() / newChunkSize) * newChunkSize;
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("   At index {}", newChunkLowIndex);
            }
            ArrayChunk.Split<P, C> split = chunk.splitAt(newChunkLowIndex);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("   Adding chunk [{}, {}]", split.getChunk1().getOffset(), split.getChunk1().getOffset() + split.getChunk1().getLength() - 1);
            }
            splitList.add(createTimeSeries(split.getChunk1()));
            split(split.getChunk2(), splitList, newChunkSize);
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("   Too small...");
            }
            splitList.add(createTimeSeries(chunk));
        }
    }

    public List<T> split(int newChunkSize) {
        List<T> splitList = new ArrayList<>();
        for (C chunk : getCheckedChunks(false)) {
            split(chunk, splitList, newChunkSize);
        }
        return splitList;
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

    @Override
    public int hashCode() {
        return Objects.hash(metadata, chunks);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractTimeSeries) {
            AbstractTimeSeries other = (AbstractTimeSeries) obj;
            return metadata.equals(other.metadata) && chunks.equals(other.chunks);
        }
        return false;
    }
}
