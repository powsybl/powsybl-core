/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.powsybl.commons.json.JsonUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractTimeSeries<P extends AbstractPoint, C extends DataChunk<P, C>, T extends TimeSeries<P, T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTimeSeries.class);

    protected final TimeSeriesMetadata metadata;

    protected final List<C> chunks;

    protected AbstractTimeSeries(TimeSeriesMetadata metadata, C... chunks) {
        this(metadata, Arrays.asList(chunks));
    }

    protected AbstractTimeSeries(TimeSeriesMetadata metadata, List<C> chunks) {
        this.metadata = Objects.requireNonNull(metadata);
        this.chunks = Objects.requireNonNull(chunks);
    }

    public void synchronize(TimeSeriesIndex newIndex) {
        Objects.requireNonNull(newIndex);
        if (!metadata.getIndex().equals(newIndex)) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public void addChunk(C chunk) {
        Objects.requireNonNull(chunk);
        chunks.add(chunk);
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

    public Stream<P> uncompressedStream() {
        return getCheckedChunks(true).stream().flatMap(chunk -> chunk.uncompressedStream(metadata.getIndex()));
    }

    public Stream<P> compressedStream() {
        return getCheckedChunks(true).stream().flatMap(chunk -> chunk.compressedStream(metadata.getIndex()));
    }

    public Iterator<P> uncompressedIterator() {
        return Iterators.concat(getCheckedChunks(true).stream().map(c -> c.uncompressedIterator(metadata.getIndex())).collect(Collectors.toList()).iterator());
    }

    public Iterator<P> compressedIterator() {
        return Iterators.concat(getCheckedChunks(true).stream().map(c -> c.compressedIterator(metadata.getIndex())).collect(Collectors.toList()).iterator());
    }

    protected abstract T createTimeSeries(C chunk);

    /**
     * <p>Splits a chunk into sub-chunks based on the specified first and last indices.</p>
     *
     * @param chunkToSplit The chunk to be split. Must not be null.
     * @param splitChunks The list to which the resulting sub-chunks will be added.
     * @param firstIndex The starting index (inclusive) for the split.
     * @param lastIndex The ending index (inclusive) for the split.
     */
    private void splitByFirstAndLastIndex(@NonNull C chunkToSplit, List<C> splitChunks, int firstIndex, int lastIndex) {
        traceSplitChunk(firstIndex, lastIndex);
        C newChunk;
        // chunkToSplit = [x0, y0] -> newChunk = [x0=firstIndex, y0]
        if (firstIndex == chunkToSplit.getOffset()) {
            newChunk = chunkToSplit;
        } else {
            // chunkToSplit = [x0, y0] -> newChunk = [firstIndex, y0]
            DataChunk.Split<P, C> split = chunkToSplit.splitAt(firstIndex);
            newChunk = split.getChunk2();
        }
        // chunkToSplit = [x0, y0] -> newChunk = [firstIndex, y0=lastIndex]
        if (lastIndex == chunkToSplit.getOffset() + chunkToSplit.getLength() - 1) {
            splitChunks.add(newChunk);
        } else {
            // chunkToSplit = [x0, y0] -> newChunk = [firstIndex, lastIndex]
            DataChunk.Split<P, C> split = newChunk.splitAt(lastIndex + 1);
            splitChunks.add(split.getChunk1());
        }
    }

    private static void traceSplitChunk(int firstIndex, int lastIndex) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Split chunk [{}, {}]", firstIndex, lastIndex);
        }
    }

    private void split(C chunkToSplit, List<C> splitChunks, int newChunkSize) {
        traceSplitChunk(chunkToSplit.getOffset(), chunkToSplit.getOffset() + chunkToSplit.getLength() - 1);

        boolean usePreviousChunk = false;
        C previousChunk = splitChunks.isEmpty() ? null : splitChunks.get(splitChunks.size() - 1);
        int previousChunkSize = 0;
        //We can complete the previous chunk if 1) it is uncomplete and 2) the current offset is not a multiple of newChunkSize
        if (previousChunk != null && previousChunk.getLength() < newChunkSize && chunkToSplit.getOffset() % newChunkSize != 0) {
            usePreviousChunk = true;
            previousChunkSize = previousChunk.getLength();
        }
        int correctedChunkSize = newChunkSize - previousChunkSize;

        if (usePreviousChunk) {
            LOGGER.trace("Previous output chunk's size is {} ; {} elements can be added.", previousChunkSize, correctedChunkSize);
        } else {
            LOGGER.trace("The previous chunk was complete (or there was no previous chunk). Starting a new one.");
        }

        if (chunkToSplit.getLength() > correctedChunkSize) {
            splitChunk(chunkToSplit, previousChunk, splitChunks, newChunkSize, correctedChunkSize, usePreviousChunk);
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("   Too small...");
            }
            if (usePreviousChunk) {
                C mergedChunk = previousChunk.append(chunkToSplit);
                splitChunks.remove(splitChunks.size() - 1);
                splitChunks.add(mergedChunk);
            } else {
                splitChunks.add(chunkToSplit);
            }
        }
    }

    private void splitChunk(C chunkToSplit, C previousChunk, List<C> splitChunks, int newChunkSize, int correctedChunkSize, boolean usePreviousChunk) {
        // compute lower intersection index with new chunk size
        int newChunkLowIndex = (int) Math.round(0.5f + (double) chunkToSplit.getOffset() / correctedChunkSize) * correctedChunkSize;
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("   At index {}", newChunkLowIndex);
        }
        DataChunk.Split<P, C> split = chunkToSplit.splitAt(newChunkLowIndex);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("   Adding chunk [{}, {}]", split.getChunk1().getOffset(), split.getChunk1().getOffset() + split.getChunk1().getLength() - 1);
        }

        if (usePreviousChunk) {
            C mergedChunk = previousChunk.append(split.getChunk1());
            splitChunks.remove(splitChunks.size() - 1);
            splitChunks.add(mergedChunk);
        } else {
            splitChunks.add(split.getChunk1());
        }
        split(split.getChunk2(), splitChunks, newChunkSize);
    }

    public List<T> split(int newChunkSize) {
        List<C> splitNewChunks = new ArrayList<>();
        for (C chunkToSplit : getCheckedChunks(false)) {
            split(chunkToSplit, splitNewChunks, newChunkSize);
        }
        return splitNewChunks.stream().map(this::createTimeSeries).collect(Collectors.toList());
    }

    /**
     * <p>Splits a list of chunks into sublists based on the specified ranges.</p>
     *
     * This method takes a list of ranges and a list of chunks, then splits the chunks
     * based on the provided ranges. The chunks are first sorted and checked to ensure
     * they cover the specified ranges. Then, each chunk is split based on the ranges,
     * and the resulting new chunks are collected into a new list.
     *
     * @param ranges The list of ranges to use for splitting the chunks. Each range is defined
     *               by a pair of integers representing the start and end indices (inclusive).
     * @return A list of new chunks created based on the specified ranges.
     * @throws IllegalArgumentException If the ranges or chunks are null or empty, or if the ranges are not valid.
     * @throws TimeSeriesException If at least a chunk is out of the TimeSeries index range or if two chunks overlap.
     */
    public List<T> splitByRanges(List<Range<@NonNull Integer>> ranges) {
        // Sort the ranges and the chunks
        List<Range<@NonNull Integer>> sortedRanges = checkAndSortRanges(ranges);
        List<C> sortedChunks = getCheckedChunks(false);
        checkForChunksInRanges(ranges, sortedChunks);

        // Split the chunks based on the ranges
        List<C> splitNewChunks = new ArrayList<>();
        int chunkIndex = 0;
        int numChunks = sortedChunks.size();
        for (Range<@NonNull Integer> range : sortedRanges) {
            chunkIndex = splitOnARange(range, chunkIndex, numChunks, sortedChunks, splitNewChunks);
        }

        return splitNewChunks.stream().map(this::createTimeSeries).collect(Collectors.toList());
    }

    private int splitOnARange(Range<@NonNull Integer> range, int chunkIndex, int numChunks,
                               List<C> sortedChunks, List<C> splitNewChunks) {
        int rangeStart = range.lowerEndpoint();
        int rangeEnd = range.upperEndpoint();
        int localChunkIndex = chunkIndex;

        // Skip chunks that end before this range starts, starting from the given index to avoid double pass on the list
        while (localChunkIndex < numChunks) {
            C chunk = sortedChunks.get(localChunkIndex);
            if (chunk.getOffset() + chunk.getLength() > rangeStart) {
                break;
            }
            localChunkIndex++;
        }

        // Collect and merge chunks that intersect with this range
        C merged = null;
        int currentIndex = localChunkIndex;
        while (currentIndex < numChunks) {
            C chunk = sortedChunks.get(currentIndex);
            if (chunk.getOffset() > rangeEnd) {
                // This chunk starts after the range ends, so we reset the index to the previous chunk
                localChunkIndex = Math.max(0, currentIndex - 1);
                break;
            }
            merged = (merged == null) ? chunk : merged.append(chunk);
            currentIndex++;
        }

        if (merged == null) {
            LOGGER.warn("No chunks found for range [{}-{}]", rangeStart, rangeEnd);
            return localChunkIndex;
        }

        // Extract the slice
        int actualStart = Math.max(rangeStart, merged.getOffset());
        int actualEnd = Math.min(rangeEnd, merged.getOffset() + merged.getLength() - 1);

        if (merged.getOffset() > rangeStart) {
            LOGGER.warn("Incomplete Range [{}-{}], first index found in chunk was {}", rangeStart, rangeEnd, merged.getOffset());
        }
        if (actualEnd < rangeEnd) {
            LOGGER.warn("Incomplete Range [{}-{}], last index found in chunk was {}", rangeStart, rangeEnd, actualEnd);
        }

        splitByFirstAndLastIndex(merged, splitNewChunks, actualStart, actualEnd);

        return localChunkIndex;
    }

    private void checkForChunksInRanges(List<Range<@NonNull Integer>> sortRanges, List<C> sortedChunks) {
        if (sortRanges.isEmpty() || sortedChunks.isEmpty()) {
            throw new IllegalArgumentException("Ranges or chunks are empty");
        }
        int minRangeIndex = sortRanges.getFirst().lowerEndpoint();
        int maxRangeIndex = sortRanges.getLast().upperEndpoint();
        int minChunkIndex = sortedChunks.getFirst().getOffset();
        C lastChunk = sortedChunks.getLast();
        int maxChunkIndex = lastChunk.getOffset() + lastChunk.getLength() - 1;
        if (maxRangeIndex < minChunkIndex || minRangeIndex > maxChunkIndex) {
            throw new IllegalArgumentException(String.format("No chunk found for ranges %s", sortRanges));
        }
    }

    private List<Range<@NonNull Integer>> checkAndSortRanges(List<Range<@NonNull Integer>> ranges) {
        // Sort the ranges
        List<Range<Integer>> sorted = new ArrayList<>(ranges);
        sorted.sort(Comparator.comparing(Range::lowerEndpoint));

        // Check the first range
        Range<Integer> prev = sorted.getFirst();

        // Check the other ranges and the overlaps
        int nbSorted = sorted.size();
        for (int i = 1; i < nbSorted; i++) {
            Range<Integer> curr = sorted.get(i);
            if (prev.isConnected(curr)) {
                throw new IllegalArgumentException(prev + " overlaps with range " + curr);
            }
            prev = curr;
        }
        return sorted;
    }

    public void writeJson(JsonGenerator generator) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            generator.writeFieldName("metadata");
            metadata.writeJson(generator);
            generator.writeFieldName("chunks");
            DataChunk.writeJson(generator, chunks);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }

    public void setTimeSeriesNameResolver(TimeSeriesNameResolver ignored) {
        // nothing to do
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, chunks);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractTimeSeries<?, ?, ?> other) {
            return metadata.equals(other.metadata) && chunks.equals(other.chunks);
        }
        return false;
    }
}
