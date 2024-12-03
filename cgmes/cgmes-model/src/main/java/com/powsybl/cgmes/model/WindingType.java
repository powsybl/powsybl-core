/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public enum WindingType {

    UNKNOWN, PRIMARY, SECONDARY, TERTIARY;

    /**
     * Retrieve the WindingType for the given transformer winding/end.
     * @param end A PropertyBag with the transformer winding/end properties.
     * @return The WindingType (PRIMARY/SECONDARY/TERTIARY) corresponding to the given transformer winding/end.
     */
    public static WindingType windingType(PropertyBag end) {
        if (end.containsKey("windingType")) {
            // For CIM14 (CIM ENTSOE Profile1) primary is determined by TransformerWinding.windingType
            return switch (end.getLocal("windingType")) {
                case "WindingType.primary" -> WindingType.PRIMARY;
                case "WindingType.secondary" -> WindingType.SECONDARY;
                case "WindingType.tertiary" -> WindingType.TERTIARY;
                default -> WindingType.UNKNOWN;
            };
        } else if (end.containsKey("endNumber")) {
            // For CIM16 (CGMES 2.4.15) primary is defined by TransformerEnd.endNumber
            try {
                return WindingType.values()[end.asInt("endNumber")];
            } catch (Exception e) {
                return WindingType.UNKNOWN;
            }
        }
        return WindingType.UNKNOWN;
    }

    /**
     * Retrieve the endNumber for the given transformer winding/end.
     * @param end A PropertyBag with the transformer winding/end properties.
     * @return The endNumber value (1/2/3) corresponding to the given transformer winding/end.
     */
    public static int endNumber(PropertyBag end) {
        return windingType(end).ordinal();
    }
}
