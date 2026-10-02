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
public final class UcteConverterConstants {

    private UcteConverterConstants() {
        throw new IllegalStateException("Should not be constructed");
    }

    public static final String CURRENT_LIMIT_PROPERTY_KEY = "currentLimit";
    public static final String ELEMENT_NAME_PROPERTY_KEY = "elementName";
    public static final String GEOGRAPHICAL_NAME_PROPERTY_KEY = "geographicalName";
    /**
     * {@code "nomimalPower"} is the property name formerly used to let transformers carry nominal power. Note that this
     * property name has a typo and should have been {@code "nominalPower"}. This typo must be kept as-is: it may remain
     * present with this exact wording in IIDM networks that were created with an UCTE-DEF import before the deprecation.
     * To enforce retro-compatibility, do not try to fix the typo.
     * @deprecated use the new field {@code ratedS} instead.
     */
    @Deprecated(since = "7.5.0")
    public static final String NOMINAL_POWER_KEY = "nomimalPower";
    public static final String STATUS_PROPERTY_KEY = "status";
    public static final String IS_COUPLER_PROPERTY_KEY = "isCoupler";
    public static final String NOT_POSSIBLE_TO_IMPORT = "It's not possible to import this network";
    public static final String ORDER_CODE = "orderCode";
    public static final String POWER_PLANT_TYPE_PROPERTY_KEY = "powerPlantType";
    public static final int DEFAULT_POWER_LIMIT = 9999;
    public static final String NO_UCTE_CODE_ERROR = "No UCTE code found for id: ";
}
