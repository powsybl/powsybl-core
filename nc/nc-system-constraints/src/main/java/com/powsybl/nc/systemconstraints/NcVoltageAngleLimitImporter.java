/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.systemconstraints;

import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class NcVoltageAngleLimitImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NcVoltageAngleLimitImporter.class);

    private static final String VOLTAGE_ANGLE_LIMIT = "voltageAngleLimit";
    private static final String NORMAL_VALUE = "normalValue";
    private static final String VALUE = "value";
    private static final String TERMINAL_1 = "terminal1";
    private static final String TERMINAL = "terminal";
    private static final String OPERATIONAL_LIMIT_SET = "operationalLimitSet";
    private static final String OPERATIONAL_LIMIT_TYPE = "operationalLimitType";
    private static final String DIRECTION = "direction";
    private static final String IS_FLOW_TO_REF_TERMINAL = "isFlowToRefTerminal";

    private final NcSystemConstraintsModel model;

    NcVoltageAngleLimitImporter(NcSystemConstraintsModel model) {
        this.model = Objects.requireNonNull(model);
    }

    Set<NcVoltageAngleLimit> importData() {
        Map<String, String> overridingValues = readOverridingValues();
        Map<String, String> terminalsByLimitSet = readLimitSetTerminals();
        Map<String, String> directionsByLimitType = readLimitTypeDirections();
        Set<NcVoltageAngleLimit> result = new LinkedHashSet<>();
        model.getVoltageAngleLimits().forEach(row -> process(
            row, overridingValues, terminalsByLimitSet, directionsByLimitType, result));
        return result;
    }

    private static void process(PropertyBag row, Map<String, String> overridingValues,
                                Map<String, String> terminalsByLimitSet,
                                Map<String, String> directionsByLimitType,
                                Set<NcVoltageAngleLimit> result) {
        String id = row.getId(VOLTAGE_ANGLE_LIMIT);
        String terminal1 = row.getId(TERMINAL_1);
        String terminal2 = terminalsByLimitSet.get(row.getId(OPERATIONAL_LIMIT_SET));
        String direction = directionsByLimitType.get(row.getId(OPERATIONAL_LIMIT_TYPE));
        String value = overridingValues.getOrDefault(id, row.get(NORMAL_VALUE));
        if (id == null || terminal1 == null || terminal2 == null || direction == null || value == null) {
            LOGGER.warn("Voltage angle limit {} has incomplete terminal, type or value references and will be ignored", id);
            return;
        }
        result.add(new NcVoltageAngleLimit(id, Double.parseDouble(value), terminal1, terminal2, direction,
            row.asBoolean(IS_FLOW_TO_REF_TERMINAL).orElse(null)));
    }

    private Map<String, String> readOverridingValues() {
        Map<String, String> values = new HashMap<>();
        model.getVoltageAngleLimitOverrides().forEach(row -> {
            String id = row.getId(VOLTAGE_ANGLE_LIMIT);
            if (id != null && row.get(VALUE) != null) {
                values.put(id, row.get(VALUE));
            }
        });
        return values;
    }

    private Map<String, String> readLimitSetTerminals() {
        Map<String, String> terminals = new HashMap<>();
        model.getOperationalLimitSets().forEach(row -> {
            String id = row.getId(OPERATIONAL_LIMIT_SET);
            String terminal = row.getId(TERMINAL);
            if (id != null && terminal != null) {
                terminals.put(id, terminal);
            }
        });
        return terminals;
    }

    private Map<String, String> readLimitTypeDirections() {
        Map<String, String> directions = new HashMap<>();
        model.getOperationalLimitTypes().forEach(row -> {
            String id = row.getId(OPERATIONAL_LIMIT_TYPE);
            String direction = row.getId(DIRECTION);
            if (id != null && direction != null) {
                directions.put(id, direction);
            }
        });
        return directions;
    }
}
