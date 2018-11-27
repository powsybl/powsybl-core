/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

import com.powsybl.timeseries.AbstractPoint;
import com.powsybl.timeseries.DataChunk;
import com.powsybl.timeseries.TimeSeriesVersions;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTimeSeriesChunksAddition<P extends AbstractPoint, T extends DataChunk<P, T>> extends AbstractStorageChange {

    protected int version;

    protected String timeSeriesName;

    protected List<T> chunks;

    protected AbstractTimeSeriesChunksAddition(String nodeId, int version, String timeSeriesName, List<T> chunks) {
        super(nodeId);
        this.version = TimeSeriesVersions.check(version);
        this.timeSeriesName = Objects.requireNonNull(timeSeriesName);
        this.chunks = checkChunks(chunks);
    }

    @Override
    public long getEstimatedSize() {
        return chunks.stream().mapToLong(DataChunk::getEstimatedSize).sum();
    }

    private List<T> checkChunks(List<T> chunks) {
        Objects.requireNonNull(chunks);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Empty chunk list");
        }
        return chunks;
    }

    public int getVersion() {
        return version;
    }

    public String getTimeSeriesName() {
        return timeSeriesName;
    }

    public List<T> getChunks() {
        return chunks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, version, timeSeriesName, chunks);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractTimeSeriesChunksAddition) {
            AbstractTimeSeriesChunksAddition other = (AbstractTimeSeriesChunksAddition) obj;
            return nodeId.equals(other.nodeId)
                    && version == other.version
                    && timeSeriesName.equals(other.timeSeriesName)
                    && chunks.equals(other.chunks);
        }
        return false;
    }
}
