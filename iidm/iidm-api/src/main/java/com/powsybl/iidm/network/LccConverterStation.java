/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * LCC converter station.
 *
 * **Characteristics**
 *
 * | Attribute | Type | Unit | Required | Default value | Description |
 * | --------- | ---- | ---- | -------- | ------------- | ----------- |
 * | Id | string | - | yes | - | Unique identifier of the LCC converter station |
 * | Name | string | - | yes | - | Human-readable name of the LCC converter station |
 * | PowerFactor | float | - | yes | - | The power factor |
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
}
