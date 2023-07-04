/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.modifications;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public enum DataframeNetworkModificationType {
    VOLTAGE_LEVEL_TOPOLOGY_CREATION,
    CREATE_COUPLING_DEVICE,
    CREATE_FEEDER_BAY,
    CREATE_LINE_FEEDER,
    CREATE_TWO_WINDINGS_TRANSFORMER_FEEDER,
    CREATE_LINE_ON_LINE,
    REVERT_CREATE_LINE_ON_LINE,
    CONNECT_VOLTAGE_LEVEL_ON_LINE,
    REVERT_CONNECT_VOLTAGE_LEVEL_ON_LINE,
    REPLACE_TEE_POINT_BY_VOLTAGE_LEVEL_ON_LINE,
}
