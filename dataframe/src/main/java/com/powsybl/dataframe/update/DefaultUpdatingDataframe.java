/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.update;

import com.powsybl.dataframe.SeriesDataType;
import com.powsybl.dataframe.SeriesMetadata;

import java.util.*;

/**
 * Default implementation for the dataframe, the behaviour will rely on the provided series implementations.
 *
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class DefaultUpdatingDataframe implements UpdatingDataframe {
    private final int rowCount;
    private final Map<String, SeriesMetadata> seriesMetadata = new LinkedHashMap<>();
    private final Map<String, IntSeries> intSeries = new HashMap<>();
    private final Map<String, DoubleSeries> doubleSeries = new HashMap<>();
    private final Map<String, StringSeries> stringSeries = new HashMap<>();

    public DefaultUpdatingDataframe(int rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public List<SeriesMetadata> getSeriesMetadata() {
        return new ArrayList<>(seriesMetadata.values());
    }

    @Override
    public DoubleSeries getDoubles(String column) {
        return doubleSeries.get(column);
    }

    @Override
    public IntSeries getInts(String column) {
        return intSeries.get(column);
    }

    @Override
    public StringSeries getStrings(String column) {
        return stringSeries.get(column);
    }

    public void addSeries(String name, boolean index, IntSeries data) {
        this.seriesMetadata.put(name, new SeriesMetadata(index, name, true, SeriesDataType.INT, true));
        this.intSeries.put(name, data);
    }

    public void addSeries(String name, boolean index, DoubleSeries series) {
        this.seriesMetadata.put(name, new SeriesMetadata(index, name, true, SeriesDataType.DOUBLE, true));
        this.doubleSeries.put(name, series);
    }

    public void addSeries(String name, boolean index, StringSeries series) {
        this.seriesMetadata.put(name, new SeriesMetadata(index, name, true, SeriesDataType.STRING, true));
        this.stringSeries.put(name, series);
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }
}
