/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public enum WindingType {

    PRIMARY, SECONDARY, TERTIARY;

    public static WindingType fromTransformerEnd(PropertyBag end) {
        // For CIM14 (CIM ENTSOE Profile1) primary is determined by windingType
        // For CIM16 (CGMES) primary is defined by the corresponding terminal sequence number:
        // "The Terminal.sequenceNumber distinguishes the terminals much as previously done by
        // TransformerWinding.windingType:WindingType"

        if (end.containsKey("windingType")) {
            String wtype = end.getLocal("windingType");
            if (wtype.endsWith("WindingType.primary")) {
                return WindingType.PRIMARY;
            } else if (wtype.endsWith("WindingType.secondary")) {
                return WindingType.SECONDARY;
            } else if (wtype.endsWith("WindingType.tertiary")) {
                return WindingType.TERTIARY;
            }
        } else if (end.containsKey("terminalSequenceNumber")) {
            // Terminal.sequenceNumber := 1, 2 ,3 ...
            return WindingType.values()[end.asInt("terminalSequenceNumber") - 1];
        }
        return WindingType.PRIMARY;
    }
}
