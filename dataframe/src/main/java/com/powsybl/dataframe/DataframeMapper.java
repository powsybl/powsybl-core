/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import com.powsybl.dataframe.update.UpdatingDataframe;

import java.util.List;

/**
 * Provides methods to map an object's data to/from dataframe representation.
 * A dataframe is basically a table of values, where columns have a given type and name,
 * and rows correspond to one item.
 * <p>
 * The dataframe data can be read by a {@link DataframeHandler},
 * and provided by variants of "indexed series".
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface DataframeMapper<T> {

    /**
     * Provides dataframe data to the handler, which is responsible to
     * format it as needed.
     */
    void createDataframe(T object, DataframeHandler dataframeHandler, DataframeFilter dataframeFilter);

    List<SeriesMetadata> getSeriesMetadata();

    SeriesMetadata getSeriesMetadata(String seriesName);

    /**
     * Updates object data with the provided series.
     */
    void updateSeries(T object, UpdatingDataframe updatingDataframe);

    boolean isSeriesMetaDataExists(String seriesName);
}
