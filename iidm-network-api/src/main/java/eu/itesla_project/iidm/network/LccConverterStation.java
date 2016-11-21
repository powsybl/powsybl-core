/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 * LCC converter station.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface LccConverterStation extends HvdcConverterStation<LccConverterStation> {

    /**
     * Get power factor (ratio of the active power and the apparent power)
     * @return the power factor.
     */
    float getPowerFactor();

    /**
     * Set the power factor. Has to be greater that zero.
     * @param powerFactor the new power factor
     * @return the converter itself to allow method chaining
     */
    LccConverterStation setPowerFactor(float powerFactor);

    /**
     * Get a builder to create a new filter.
     * @return a builder to create a new filter
     */
    LccFilterAdder newFilter();

    /**
     * Get filter count.
     * @return filter count
     */
    int getFilterCount();

    /**
     * Remove a filter based on its index.
     * @param index index of the filter to remove
     */
    void removeFilterAt(int index);

    /**
     * Get a filter based on its index.
     * @param index index of the filter to get
     * @return the filter corresponding to the index
     */
    LccFilter getFilterAt(int index);

    /**
     * Return all the filters.
     * @return all the filters.
     */
    Iterable<LccFilter> getFilters();
}
