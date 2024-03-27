/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public interface RecordGroupIdentification {

    String getDataName();

    default String getUniqueName() {
        String recordGroupName = getJsonNodeName() != null ? getJsonNodeName() : getLegacyTextName();
        return getDataName() + "." + recordGroupName;
    }

    String getJsonNodeName();

    String getLegacyTextName();

    JsonObjectType getJsonObjectType();

    // Psse Json files consist of two types of data objects: Parameter Sets and Data Tables.
    // "caseid" in Power Flow data is a Parameter Set
    // "bus" in Power Flow data is an Data Table (which has multiple data rows, one for each bus record)

    enum JsonObjectType {
        PARAMETER_SET,
        DATA_TABLE;
    }
}
