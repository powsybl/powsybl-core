/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * HVDC converter station. This is the base class for VSC and LCC.
 * AC side of the converter is connected inside a substation.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface HvdcConverterStation<T extends HvdcConverterStation<T>> extends Injection<T> {

    /**
     * HDVC type: VSC or LCC
     */
    enum HvdcType {
        VSC,
        LCC
    }

    /**
     * Get HVDC type.
     * @return HVDC type
     */
    HvdcType getHvdcType();

    /**
     * Get the loss factor.
     * @return the loss factor.
     */
    float getLossFactor();

    /**
     * Set the loss factor
     * @param lossFactor the loss factor
     * @return this station
     */
    T setLossFactor(float lossFactor);
}
