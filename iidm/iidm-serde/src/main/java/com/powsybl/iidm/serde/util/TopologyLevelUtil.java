/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.List;
import java.util.Objects;

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
     * <p>Validates and possibly adjusts the topology level to use when exporting a voltage level.</p>
     *
     * <p>If the requested topologyLevel is TopologyLevel.BUS_BRANCH,
     * this method checks whether the voltage level can be exported in this topology level without leading to an invalid IIDM.</p>
     *
     * <p>The exported file can be invalid when an equipment references a bus that is not exported. It may happen
     * with connectables that are not connected, if their connectable bus has nothing connected on it.</p>
     *
     * <p>When a potential problem is detected, the behavior is driven by the export options
     * available in the provided context:</p>
     * <li>If the configured BusBranchVoltageLevelIncompatibilityBehavior is THROW_EXCEPTION, a PowsyblException is thrown.</li>
     * <li>Otherwise, the method falls back to the voltage level's own topology kind.</li>
     *
     * <p>When the requested topologyLevel is not BUS_BRANCH, the topology level passed as a parameter is returned unchanged.</p>
     *
     * @param vl the voltage level being exported
     * @param context the serialization context
     * @param topologyLevel the requested topology level for export
     * @return the topology level that should be used for export
     * @throws PowsyblException When the topology to check is BUS_BRANCH, an export problem is detected, and the configured behavior is THROW_EXCEPTION.
     */
    private static TopologyLevel checkVoltageLevelExportTopology(VoltageLevel vl, NetworkSerializerContext context, TopologyLevel topologyLevel) {
        if (topologyLevel == TopologyLevel.BUS_BRANCH
                && vl.getConnectableStream()
                    .map(Connectable::getTerminals)
                    .flatMap(List<Terminal>::stream)
                    .anyMatch(t -> t.getBusView().getConnectableBus() == null)) {
            if (context.getOptions()
                    .getBusBranchVoltageLevelIncompatibilityBehavior() == ExportOptions.BusBranchVoltageLevelIncompatibilityBehavior.THROW_EXCEPTION) {
                throw new PowsyblException("Cannot export voltage level \"" + vl.getId() + "\" in BUS_BRANCH topology: this would lead to an invalid IIDM.");
            }
            return TopologyLevel.valueOf(vl.getTopologyKind().name());
        }
        return topologyLevel;
    }
}

