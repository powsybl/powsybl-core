/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.NodeTopologyPoint;
import com.powsybl.iidm.network.TopologyKind;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeTopologyPointImpl implements NodeTopologyPoint {

    private final String voltageLevelId;

    private final int node;

    public NodeTopologyPointImpl(String voltageLevelId, int node) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.node = node;
    }

    @Override
    public TopologyKind getTopologyKind() {
        return TopologyKind.NODE_BREAKER;
    }

    @Override
    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public int getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "NodeTopologyPoint(" +
                "voltageLevelId='" + voltageLevelId + '\'' +
                ", node=" + node +
                ')';
    }
}
