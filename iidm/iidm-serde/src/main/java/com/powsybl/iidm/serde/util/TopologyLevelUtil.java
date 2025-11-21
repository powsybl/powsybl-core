/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.util;

import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class TopologyLevelUtil {

    private TopologyLevelUtil() {
    }

    public static TopologyLevel determineTopologyLevel(VoltageLevel vl, NetworkSerializerContext context) {
        TopologyLevel configTopologyLevel = Objects.requireNonNullElse(context.getOptions().getVoltageLevelTopologyLevel(vl.getId()), context.getOptions().getTopologyLevel());
        return TopologyLevel.min(vl.getTopologyKind(), configTopologyLevel);
    }
}

