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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
final class NcPowerTransferCorridorImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NcPowerTransferCorridorImporter.class);

    private static final String POWER_TRANSFER_CORRIDOR = "powerTransferCorridor";
    private static final String CORRIDOR_MRID = "corridorMrid";
    private static final String CORRIDOR_NAME = "corridorName";
    private static final String NORMAL_ENABLED = "normalEnabled";
    private static final String ENABLED = "enabled";
    private static final String OPERATIONAL_LIMIT_SET = "operationalLimitSet";
    private static final String OPERATIONAL_LIMIT_TYPE = "operationalLimitType";
    private static final String INFEED_LIMIT = "infeedLimit";
    private static final String INFEED_LIMIT_MRID = "infeedLimitMrid";
    private static final String INFEED_LIMIT_NAME = "infeedLimitName";
    private static final String NORMAL_VALUE_W = "normalValueW";
    private static final String NORMAL_VALUE_A = "normalValueA";
    private static final String VALUE_W = "valueW";
    private static final String VALUE_A = "valueA";
    private static final String IS_MINIMUM = "isMinimum";
    private static final String DIRECTION = "direction";
    private static final String TERMINAL = "terminal";
    private static final String INFEED_TERMINAL = "infeedTerminal";
    private static final String INFEED_TERMINAL_MRID = "infeedTerminalMrid";

    private final NcSystemConstraintsModel model;

    NcPowerTransferCorridorImporter(NcSystemConstraintsModel model) {
        this.model = Objects.requireNonNull(model);
    }

    Set<NcPowerTransferCorridor> importData() {
        Overrides overrides = readOverrides();
        Map<String, CorridorBuilder> corridors = readCorridors(overrides);
        Map<String, OperationalLimitSetData> operationalLimitSets = readOperationalLimitSets();
        Map<String, OperationalLimitTypeData> operationalLimitTypes = readOperationalLimitTypes();
        Map<String, Set<NcPowerTransferCorridor.InfeedTerminal>> terminalsByLimit = readInfeedTerminals();

        model.getInfeedLimits().forEach(row -> processInfeedLimit(row, corridors, operationalLimitSets,
            operationalLimitTypes, terminalsByLimit, overrides));

        Set<NcPowerTransferCorridor> result = new LinkedHashSet<>();
        corridors.values().forEach(corridor -> {
            if (corridor.isEmpty()) {
                LOGGER.warn("Power transfer corridor {} has no valid infeed limit and will be ignored", corridor.mrid);
            } else {
                result.add(corridor.build());
            }
        });
        return result;
    }

    private Map<String, CorridorBuilder> readCorridors(Overrides overrides) {
        Map<String, CorridorBuilder> corridors = new LinkedHashMap<>();
        model.getPowerTransferCorridors().forEach(row -> {
            String id = row.getId(POWER_TRANSFER_CORRIDOR);
            if (id == null || row.get(CORRIDOR_MRID) == null) {
                LOGGER.warn("Power transfer corridor without an identifier or mRID will be ignored");
                return;
            }
            corridors.put(id, new CorridorBuilder(row.get(CORRIDOR_MRID), row.get(CORRIDOR_NAME),
                Boolean.parseBoolean(overrides.enabled.getOrDefault(id, row.getOrDefault(NORMAL_ENABLED, "true")))));
        });
        return corridors;
    }

    private Map<String, OperationalLimitSetData> readOperationalLimitSets() {
        Map<String, OperationalLimitSetData> sets = new HashMap<>();
        model.getOperationalLimitSets().forEach(row -> {
            String id = row.getId(OPERATIONAL_LIMIT_SET);
            String corridorId = row.getId(POWER_TRANSFER_CORRIDOR);
            String terminalId = row.getId(TERMINAL);
            if (id != null && (corridorId != null || terminalId != null)) {
                sets.put(id, new OperationalLimitSetData(corridorId, terminalId));
            }
        });
        return sets;
    }

    private Map<String, OperationalLimitTypeData> readOperationalLimitTypes() {
        Map<String, OperationalLimitTypeData> types = new HashMap<>();
        model.getOperationalLimitTypes().forEach(row -> {
            String id = row.getId(OPERATIONAL_LIMIT_TYPE);
            if (id != null) {
                types.put(id, new OperationalLimitTypeData(
                    Boolean.parseBoolean(row.get(IS_MINIMUM)), row.getId(DIRECTION)));
            }
        });
        return types;
    }

    private Map<String, Set<NcPowerTransferCorridor.InfeedTerminal>> readInfeedTerminals() {
        Map<String, Set<NcPowerTransferCorridor.InfeedTerminal>> terminalsByLimit = new HashMap<>();
        model.getInfeedTerminals().forEach(row -> {
            String infeedLimitId = row.getId(INFEED_LIMIT);
            String terminalId = row.getId(TERMINAL);
            if (infeedLimitId == null || terminalId == null) {
                LOGGER.warn("Infeed terminal {} has no infeed limit or AC/DC terminal and will be ignored",
                    row.getId(INFEED_TERMINAL));
                return;
            }
            terminalsByLimit.computeIfAbsent(infeedLimitId, key -> new LinkedHashSet<>())
                .add(new NcPowerTransferCorridor.InfeedTerminal(
                    row.get(INFEED_TERMINAL_MRID), terminalId));
        });
        return terminalsByLimit;
    }

    private static void processInfeedLimit(PropertyBag row, Map<String, CorridorBuilder> corridors,
                                           Map<String, OperationalLimitSetData> operationalLimitSets,
                                           Map<String, OperationalLimitTypeData> operationalLimitTypes,
                                           Map<String, Set<NcPowerTransferCorridor.InfeedTerminal>> terminalsByLimit,
                                           Overrides overrides) {
        String id = row.getId(INFEED_LIMIT);
        OperationalLimitSetData limitSet = operationalLimitSets.get(row.getId(OPERATIONAL_LIMIT_SET));
        OperationalLimitTypeData limitType = operationalLimitTypes.get(row.getId(OPERATIONAL_LIMIT_TYPE));
        if (limitSet == null || limitSet.corridorId == null || limitSet.terminalId == null) {
            LOGGER.warn("Infeed limit {} has no complete operational limit set and will be ignored", id);
            return;
        }
        CorridorBuilder corridor = corridors.get(limitSet.corridorId);
        if (corridor == null) {
            LOGGER.warn("Infeed limit {} references unknown power transfer corridor {} and will be ignored",
                id, limitSet.corridorId);
            return;
        }
        if (limitType == null || limitType.direction == null) {
            LOGGER.warn("Infeed limit {} has no complete operational limit type and will be ignored", id);
            return;
        }
        String mrid = row.get(INFEED_LIMIT_MRID);
        if (id == null || mrid == null) {
            LOGGER.warn("Infeed limit without an identifier or mRID will be ignored");
            return;
        }
        corridor.add(new NcPowerTransferCorridor.InfeedLimit(
            mrid,
            row.get(INFEED_LIMIT_NAME),
            asDouble(overrides.valueW.getOrDefault(id, row.get(NORMAL_VALUE_W))),
            asDouble(overrides.valueA.getOrDefault(id, row.get(NORMAL_VALUE_A))),
            limitType.minimum,
            limitType.direction,
            limitSet.terminalId,
            terminalsByLimit.getOrDefault(id, Set.of())));
    }

    private Overrides readOverrides() {
        Map<String, String> enabled = new HashMap<>();
        Map<String, String> valueW = new HashMap<>();
        Map<String, String> valueA = new HashMap<>();
        model.getPowerTransferCorridorOverrides()
            .forEach(row -> putIfPresent(enabled, row.getId(POWER_TRANSFER_CORRIDOR), row.get(ENABLED)));
        model.getInfeedLimitOverrides().forEach(row -> {
            putIfPresent(valueW, row.getId(INFEED_LIMIT), row.get(VALUE_W));
            putIfPresent(valueA, row.getId(INFEED_LIMIT), row.get(VALUE_A));
        });
        return new Overrides(enabled, valueW, valueA);
    }

    private static void putIfPresent(Map<String, String> values, String key, String value) {
        if (key != null && value != null) {
            values.put(key, value);
        }
    }

    private static Double asDouble(String value) {
        return value == null ? null : Double.valueOf(value);
    }

    private record Overrides(Map<String, String> enabled, Map<String, String> valueW, Map<String, String> valueA) {
    }

    private record OperationalLimitSetData(String corridorId, String terminalId) {
    }

    private record OperationalLimitTypeData(boolean minimum, String direction) {
    }

    private static final class CorridorBuilder {
        private final String mrid;
        private final String name;
        private final boolean enabled;
        private final Set<NcPowerTransferCorridor.InfeedLimit> infeedLimits = new LinkedHashSet<>();

        private CorridorBuilder(String mrid, String name, boolean enabled) {
            this.mrid = mrid;
            this.name = name;
            this.enabled = enabled;
        }

        private void add(NcPowerTransferCorridor.InfeedLimit infeedLimit) {
            infeedLimits.add(infeedLimit);
        }

        private boolean isEmpty() {
            return infeedLimits.isEmpty();
        }

        private NcPowerTransferCorridor build() {
            return new NcPowerTransferCorridor(mrid, name, enabled, infeedLimits);
        }
    }
}
