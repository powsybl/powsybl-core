/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class TopologyLevelUtil {

    private TopologyLevelUtil() {
    }

    public static TopologyLevel determineTopologyLevel(VoltageLevel vl, NetworkSerializerContext context) {
        TopologyLevel exportTopologyLevel = context.getVoltageLevelExportTopologyLevel(vl.getId());
        if (exportTopologyLevel == null) {
            TopologyLevel configTopologyLevel = Objects.requireNonNullElse(context.getOptions().getVoltageLevelTopologyLevel(vl.getId()), context.getOptions().getTopologyLevel());
            exportTopologyLevel = checkVoltageLevelExportTopology(vl, context, TopologyLevel.min(vl.getTopologyKind(), configTopologyLevel));
            context.addVoltageLevelExportTopologyLevel(vl.getId(), exportTopologyLevel);
        }
        return exportTopologyLevel;
    }

    /**
     * Validates and possibly adjusts the topology level to use when exporting a voltage level.
     *
     * If the requested topologyLevel is TopologyLevel.BUS_BRANCH and the provided VoltageLevel has switches,
     * this method checks whether all switches are open. If all switches are open this is considered
     * incompatible with BUS_BRANCH export. In that situation the behavior is driven by the export options
     * available in the provided context:
     * - If the configured BusBranchVoltageLevelIncompatibilityBehavior is THROW_EXCEPTION, a PowsyblException is thrown.
     * - Otherwise, the method falls back to the voltage level's own topology kind.
     *
     * If the requested topologyLevel is not BUS_BRANCH or the voltage level does not contain only-open switches,
     * the original topologyLevel is returned unchanged.
     *
     * @param vl the voltage level being exported
     * @param context the serialization context
     * @param topologyLevel the requested topology level for export
     * @return the topology level that should be used for export
     * @throws PowsyblException if exporting a BUS_BRANCH topology while all switches are open and the configured behavior is THROW_EXCEPTION
     */
    private static TopologyLevel checkVoltageLevelExportTopology(VoltageLevel vl, NetworkSerializerContext context, TopologyLevel topologyLevel) {

        if (topologyLevel == TopologyLevel.BUS_BRANCH &&
                StreamSupport
                        .stream(vl.getSwitches().spliterator(), false).anyMatch(sw -> sw.getKind().equals(SwitchKind.BREAKER)) &&
                StreamSupport
                        .stream(vl.getSwitches().spliterator(), false)
                        .filter(sw -> sw.getKind().equals(SwitchKind.BREAKER)).count()
                        == StreamSupport
                        .stream(vl.getSwitches().spliterator(), false)
                        .filter(sw -> sw.getKind().equals(SwitchKind.BREAKER) && sw.isOpen()).count()) {
            if (context.getOptions()
                    .getBusBranchVoltageLevelIncompatibilityBehavior() == ExportOptions.BusBranchVoltageLevelIncompatibilityBehavior.THROW_EXCEPTION) {
                throw new PowsyblException("Cannot export voltage level '" + vl.getId() + "' with all its BREAKER switches open in BUS_BRANCH topology");
            }
            return TopologyLevel.valueOf(vl.getTopologyKind().name());
        }
        return topologyLevel;
    }
}

