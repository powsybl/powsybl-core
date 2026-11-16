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
     * Create an uncompressed chunk covering {@code [offset, offset + length[} and filled with the values of the given
     * pieces. Indexes not covered by any piece are filled with {@code NaN} (double) or {@code null} (string), as when
     * converting the time series to an array.
     *
     * @param offset the offset of the chunk to create
     * @param length the length of the chunk to create
     * @param pieces sorted and non-overlapping chunks, all included in {@code [offset, offset + length[}
     * @return the merged uncompressed chunk
     */
    protected abstract C mergeToUncompressed(int offset, int length, List<C> pieces);

    /**
     * Merge successive compressed chunks into a single compressed chunk. The default implementation appends the
     * chunks one by one, which is quadratic in the number of chunks: implementations should override it to merge in a
     * single pass.
     *
     * @param pieces at least two successive compressed chunks, sorted by offset
     * @return the merged compressed chunk
     */
    protected C mergeCompressed(List<C> pieces) {
        C merged = pieces.getFirst();
        for (int i = 1; i < pieces.size(); i++) {
            merged = merged.append(pieces.get(i));
        }
        return merged;
    }

    /**
     * <p>Split the time series into time series of at most {@code newChunkSize} points.</p>
     *
     * <p>The index range is cut into windows aligned on the absolute grid {@code [j * newChunkSize, (j + 1) * newChunkSize[}.
     * One time series, holding a single chunk, is created for each window containing data; windows without data are
     * skipped. Within a window:</p>
     * <ul>
     *     <li>the chunk starts at the first and ends at the last index covered by the data (indexes without data at the
     *     edges of the window are not filled);</li>
     *     <li>the kind of the input chunks (compressed or not) is preserved when they are successive and of the same kind,
     *     otherwise an uncompressed chunk is created, with the indexes without data filled with {@code NaN} (double) or
     *     {@code null} (string).</li>
     * </ul>
     * <p>As a consequence, the resulting time series are not necessarily successive: consumers must rely on the chunk
     * offsets.</p>
     *
     * @param newChunkSize the maximum number of points of each resulting time series
     * @return the list of resulting time series, sorted by offset
     * @throws IllegalArgumentException if {@code newChunkSize < 1}
     * @throws TimeSeriesException if a chunk is out of the index range or if two chunks overlap
     */
    public List<T> split(int newChunkSize) {
        if (newChunkSize < 1) {
            throw new IllegalArgumentException("Invalid chunk size: " + newChunkSize);
        }
        List<T> splitTimeSeries = new ArrayList<>();
        // pieces of the input chunks lying in the current window [currentWindow * newChunkSize, (currentWindow + 1) * newChunkSize[
        List<C> windowPieces = new ArrayList<>();
        int currentWindow = -1;
        for (C chunk : getCheckedChunks(false)) {
            int end = chunk.getOffset() + chunk.getLength();
            int from = chunk.getOffset();
            while (from < end) {
                int window = from / newChunkSize;
                int to = (int) Math.min(end, (window + 1L) * newChunkSize);
                if (window != currentWindow && !windowPieces.isEmpty()) {
                    // merge the pieces of the previous window and create a time series, then start a new window
                    splitTimeSeries.add(createTimeSeries(mergePieces(windowPieces)));
                    windowPieces.clear();
                }
                currentWindow = window;
                windowPieces.add(chunk.slice(from, to));
                from = to;
            }
        }
        if (!windowPieces.isEmpty()) {
            splitTimeSeries.add(createTimeSeries(mergePieces(windowPieces)));
        }
        return splitTimeSeries;
    }

    private C mergePieces(List<C> pieces) {
        if (pieces.size() == 1) {
            return pieces.getFirst();
        }
        if (areCompressedAndSuccessive(pieces)) {
            return mergeCompressed(pieces);
        }
        C first = pieces.getFirst();
        C last = pieces.getLast();
        return mergeToUncompressed(first.getOffset(), last.getOffset() + last.getLength() - first.getOffset(), pieces);
    }

    private static boolean areCompressedAndSuccessive(List<? extends DataChunk<?, ?>> pieces) {
        DataChunk<?, ?> previous = null;
        for (DataChunk<?, ?> piece : pieces) {
            if (!piece.isCompressed()) {
                return false;
            }
            if (previous != null && previous.getOffset() + previous.getLength() != piece.getOffset()) {
                return false;
            }
            previous = piece;
        }
        return true;
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
        checkForChunksInRanges(sortedRanges, sortedChunks);

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

        splitNewChunks.add(merged.slice(actualStart, actualEnd + 1));

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

    public int getMinOffset() {
        return chunks.stream().mapToInt(C::getOffset).min().orElse(0);
    }

    public int getMaxChunkEnd() {
        return chunks.stream().mapToInt(chunk -> chunk.getOffset() + chunk.getLength()).max().orElse(0);
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
