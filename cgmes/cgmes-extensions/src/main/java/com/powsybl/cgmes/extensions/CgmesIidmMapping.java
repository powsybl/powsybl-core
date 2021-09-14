/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;

import java.util.Map;
import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * <p>
 * WARNING: this class is still in a beta version, it will change in the future
 */
public interface CgmesIidmMapping extends Extension<Network> {

    @Override
    default String getName() {
        return "cgmesIidmMapping";
    }

    Set<String> getTopologicalNodes(String busId);

    String getTopologicalNode(String equipmentId, int side);

    boolean isTopologicalNodeMapped(String busId);

    boolean isTopologicalNodeEmpty();

    CgmesIidmMapping putTopologicalNode(String equipmentId, int side, String topologicalNodeId);

    CgmesIidmMapping putTopologicalNode(String busId, String topologicalNodeId);

    Map<String, Set<String>> topologicalNodesByBusViewBusMap();

    Set<String> getUnmappedTopologicalNodes();

    Map<Double, String> getBaseVoltages();

    String getBaseVoltage(double nominalVoltage);

    boolean isBaseVoltageMapped(double nominalVoltage);

    boolean isBaseVoltageEmpty();

    CgmesIidmMapping putBaseVoltage(double nominalVoltage, String baseVoltage);

    CgmesIidmMapping addBaseVoltage(double nominalVoltage, String baseVoltage);

    Map<Double, String> baseVoltagesByNominalVoltageMap();

    Set<String> getUnmappedBaseVoltages();
}
