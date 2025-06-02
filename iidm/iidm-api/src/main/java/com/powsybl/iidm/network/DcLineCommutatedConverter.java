/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcLineCommutatedConverter extends DcConverter<DcLineCommutatedConverter> {

    enum ReactiveModel {
        FIXED_POWER_FACTOR,
        CALCULATED_POWER_FACTOR,
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_LINE_COMMUTATED_CONVERTER;
    }

    ReactiveModel getReactiveModel();

    DcLineCommutatedConverter setReactiveModel(ReactiveModel reactiveModel);

    /**
     * Get power factor (ratio of the active power and the apparent power)
     * @return the power factor.
     */
    double getPowerFactor();

    /**
     * Set the power factor. Has to be greater that zero.
     * @param powerFactor the new power factor
     * @return the converter itself to allow method chaining
     */
    DcLineCommutatedConverter setPowerFactor(double powerFactor);
}
