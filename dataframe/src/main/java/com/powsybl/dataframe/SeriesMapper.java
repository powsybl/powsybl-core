/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import java.util.List;

/**
 * Defines a mapping between objects and a series view.
 * It defines a name for the series, a way to retrieve data from the underlying objects
 * as a series, and a way to write data to the objects from a series input.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface SeriesMapper<T> {

    SeriesMetadata getMetadata();

    void createSeries(List<T> items, DataframeHandler factory);

    default void updateInt(T object, int value) {
        throw new UnsupportedOperationException("Cannot update series with int: " + getMetadata().getName());
    }

    default void updateDouble(T object, double value) {
        throw new UnsupportedOperationException("Cannot update series with double: " + getMetadata().getName());
    }

    default void updateString(T object, String value) {
        throw new UnsupportedOperationException("Cannot update series with string: " + getMetadata().getName());
    }
}
