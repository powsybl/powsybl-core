/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class UcteConstants {

    private UcteConstants() {
        throw new AssertionError("Should not be constructed");
    }

    static final String CURRENT_LIMIT_PROPERTY_KEY = "currentLimit";
    static final String ELEMENT_NAME_PROPERTY_KEY = "elementName";
    static final String GEOGRAPHICAL_NAME_PROPERTY_KEY = "geographicalName";
    static final String ORDER_CODE = "orderCode";

    static final int DEFAULT_MAX_CURRENT = 999999;

}
