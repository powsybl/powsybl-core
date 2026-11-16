/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * HVDC converter station. This is the base class for VSC and LCC.
 * AC side of the converter is connected inside a substation.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
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
     * Return the HVDC line attached to this station.
     * @return the HVDC line attached to this station or null.
     */
    default HvdcLine getHvdcLine() {
        throw new UnsupportedOperationException();
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

    /**
     * Get the converter station at the other side of the hvdc line.
     * @return the other converter station
     */
    default Optional<? extends HvdcConverterStation<?>> getOtherConverterStation() {
        if (getHvdcLine() != null) {
            HvdcLine hvdcLine = getHvdcLine();
            if (this == hvdcLine.getConverterStation1()) {
                return Optional.ofNullable(hvdcLine.getConverterStation2());
            }
            return Optional.ofNullable(hvdcLine.getConverterStation1());
        }
        return Optional.empty();
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.HVDC_CONVERTER_STATION;
    }
}
