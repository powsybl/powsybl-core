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
        if (chunkToSplit.getOffset() > firstIndex) {
            throw new IllegalArgumentException(String.format("Incomplete chunk, expected at least first offset to be %s, but we got %s", firstIndex, chunkToSplit.getOffset()));
        }
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
     * @throws IllegalArgumentException If the ranges or chunks are null, or if the ranges are not valid.
     * @throws IllegalStateException If the chunks do not cover the specified ranges.
     *
     */
    public List<T> splitByRanges(List<Range<@NonNull Integer>> ranges) {
        List<Range<@NonNull Integer>> sortedRanges = checkAndSortRanges(ranges);
        List<C> sortedChunks = getCheckedChunks(false);
        checkChunkIsPresent(ranges, sortedChunks);
        List<C> splitNewChunks = new ArrayList<>();
        int rangesIndex = 0;
        int chunkIndex = 0;
        int maxRangesIndex = ranges.size() - 1;
        int maxChunkIndex = sortedChunks.size() - 1;
        Optional<C> previousChunk = Optional.empty();
        boolean isSplitRunning = true;
        while (isSplitRunning) {
            Range<@NonNull Integer> currentRange = sortedRanges.get(rangesIndex);
            C currentChunk = sortedChunks.get(chunkIndex);
            int firstIndex = currentChunk.getOffset();
            int lastIndex = currentChunk.getOffset() + currentChunk.getLength() - 1;
            if (lastIndex < currentRange.lowerEndpoint()) {
                chunkIndex++;
            } else if (firstIndex > currentRange.upperEndpoint()) {
                rangesIndex++;
            } else if (currentRange.lowerEndpoint() >= firstIndex && currentRange.upperEndpoint() <= lastIndex) {
                splitByFirstAndLastIndex(currentChunk, splitNewChunks, currentRange.lowerEndpoint(), currentRange.upperEndpoint());
                rangesIndex++;
            } else if (currentRange.lowerEndpoint() >= firstIndex) {
                previousChunk = previousChunk.map(c -> c.append(currentChunk))
                    .or(() -> Optional.of(currentChunk));
                chunkIndex++;
            } else if (currentRange.upperEndpoint() <= lastIndex) {
                previousChunk = previousChunk.map(c -> c.append(currentChunk));
                splitByFirstAndLastIndex(previousChunk.orElse(currentChunk), splitNewChunks, currentRange.lowerEndpoint(), currentRange.upperEndpoint());
                rangesIndex++;
                previousChunk = Optional.empty();
            } else {
                previousChunk = previousChunk.map(c -> c.append(currentChunk))
                    .or(() -> Optional.of(currentChunk));
                chunkIndex++;
            }

            if (chunkIndex > maxChunkIndex) {
                if (previousChunk.isPresent()) {
                    LOGGER.warn("Incomplete Range [{}-{}], last index found in chunk was {}", currentRange.lowerEndpoint(), currentRange.upperEndpoint(), lastIndex);
                    splitByFirstAndLastIndex(previousChunk.get(), splitNewChunks, currentRange.lowerEndpoint(), lastIndex);
                }
                isSplitRunning = false;
            }

            if (rangesIndex > maxRangesIndex) {
                isSplitRunning = false;
            }
        }
        return splitNewChunks.stream().map(this::createTimeSeries).collect(Collectors.toList());
    }

    private void checkChunkIsPresent(List<Range<@NonNull Integer>> ranges, List<C> sortedChunks) {
        int minRangeIndex = ranges.stream().map(Range::lowerEndpoint).min(Integer::compareTo).orElse(0);
        int maxRangeIndex = ranges.stream().map(Range::upperEndpoint).max(Integer::compareTo).orElse(0);
        int minChunkindex = sortedChunks.stream().map(DataChunk::getOffset).min(Integer::compareTo).orElse(0);
        int maxChunkIndex = sortedChunks.stream().map(chunk -> chunk.getOffset() + chunk.getLength() - 1).max(Integer::compareTo).orElse(0);
        if (!(minChunkindex <= minRangeIndex && minRangeIndex <= maxChunkIndex || minChunkindex <= maxRangeIndex && maxRangeIndex <= maxChunkIndex)) {
            throw new IllegalArgumentException(String.format("No chunk found for ranges %s", ranges));
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
