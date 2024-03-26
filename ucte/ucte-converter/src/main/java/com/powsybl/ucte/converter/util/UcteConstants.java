/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter.util;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class UcteConstants {

    private UcteConstants() {
        throw new IllegalStateException("Should not be constructed");
    }

    public static final String CURRENT_LIMIT_PROPERTY_KEY = "currentLimit";
    public static final String ELEMENT_NAME_PROPERTY_KEY = "elementName";
    public static final String GEOGRAPHICAL_NAME_PROPERTY_KEY = "geographicalName";
    public static final String NOMINAL_POWER_KEY = "nomimalPower";
    public static final String STATUS_PROPERTY_KEY = "status";
    public static final String IS_COUPLER_PROPERTY_KEY = "isCoupler";
    public static final String NOT_POSSIBLE_TO_IMPORT = "It's not possible to import this network";
    public static final String ORDER_CODE = "orderCode";
    public static final String POWER_PLANT_TYPE_PROPERTY_KEY = "powerPlantType";
    public static final int DEFAULT_POWER_LIMIT = 9999;
}
