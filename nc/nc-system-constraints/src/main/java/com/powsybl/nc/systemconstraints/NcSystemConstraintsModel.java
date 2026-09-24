/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.systemconstraints;

import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.io.NcQueryContext;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;

import java.util.Objects;

/**
 * Triple-store view used by the system constraints importers.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class NcSystemConstraintsModel {
    static final String VOLTAGE_ANGLE_LIMIT = "voltageAngleLimit";
    static final String VOLTAGE_ANGLE_LIMIT_OVERRIDING = "voltageAngleLimitOverriding";
    static final String POWER_TRANSFER_CORRIDOR = "powerTransferCorridor";
    static final String POWER_TRANSFER_CORRIDOR_OVERRIDING = "powerTransferCorridorOverriding";
    static final String OPERATIONAL_LIMIT_SET = "operationalLimitSet";
    static final String OPERATIONAL_LIMIT_TYPE = "operationalLimitType";
    static final String INFEED_LIMIT = "infeedLimit";
    static final String INFEED_LIMIT_OVERRIDING = "infeedLimitOverriding";
    static final String INFEED_TERMINAL = "infeedTerminal";

    private final NcQueryContext queryContext;
    private final QueryCatalog queryCatalog;

    NcSystemConstraintsModel(NcQueryContext queryContext) {
        this(queryContext, new QueryCatalog("nc-system-constraints.sparql"));
    }

    NcSystemConstraintsModel(NcQueryContext queryContext, QueryCatalog queryCatalog) {
        this.queryContext = Objects.requireNonNull(queryContext);
        this.queryCatalog = Objects.requireNonNull(queryCatalog);
    }

    PropertyBags getVoltageAngleLimits() {
        return query(NcKeyword.EQUIPMENT_RELIABILITY, VOLTAGE_ANGLE_LIMIT);
    }

    PropertyBags getVoltageAngleLimitOverrides() {
        return query(NcKeyword.STEADY_STATE_INSTRUCTION, VOLTAGE_ANGLE_LIMIT_OVERRIDING);
    }

    PropertyBags getPowerTransferCorridors() {
        return query(NcKeyword.EQUIPMENT_RELIABILITY, POWER_TRANSFER_CORRIDOR);
    }

    PropertyBags getPowerTransferCorridorOverrides() {
        return query(NcKeyword.STEADY_STATE_INSTRUCTION, POWER_TRANSFER_CORRIDOR_OVERRIDING);
    }

    PropertyBags getOperationalLimitSets() {
        return query(NcKeyword.EQUIPMENT_RELIABILITY, OPERATIONAL_LIMIT_SET);
    }

    PropertyBags getOperationalLimitTypes() {
        return query(NcKeyword.EQUIPMENT_RELIABILITY, OPERATIONAL_LIMIT_TYPE);
    }

    PropertyBags getInfeedLimits() {
        return query(NcKeyword.EQUIPMENT_RELIABILITY, INFEED_LIMIT);
    }

    PropertyBags getInfeedLimitOverrides() {
        return query(NcKeyword.STEADY_STATE_INSTRUCTION, INFEED_LIMIT_OVERRIDING);
    }

    PropertyBags getInfeedTerminals() {
        return query(NcKeyword.EQUIPMENT_RELIABILITY, INFEED_TERMINAL);
    }

    private PropertyBags query(NcKeyword keyword, String queryName) {
        return queryContext.query(keyword, queryCatalog.get(queryName));
    }
}
