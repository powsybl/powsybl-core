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
import java.util.Objects;
import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * <p>
 * WARNING: this class is still in a beta version, it will change in the future
 */
public interface CgmesIidmMapping extends Extension<Network> {

    enum Source {
        BOUNDARY, IGM
    }

    class BaseVoltageSource {
        private final String cgmesId;
        private final double nominalV;
        private final Source source;

        public BaseVoltageSource(String cgmesId, double nominalV, Source source) {
            this.cgmesId = Objects.requireNonNull(cgmesId);
            this.nominalV = nominalV;
            this.source = Objects.requireNonNull(source);
        }

        public String getCgmesId() {
            return cgmesId;
        }

        public double getNominalV() {
            return nominalV;
        }

        public Source getSource() {
            return source;
        }
    }

    String NAME = "cgmesIidmMapping";

    @Override
    default String getName() {
        return NAME;
    }

    Set<String> getTopologicalNodes(String busId);

    String getTopologicalNode(String equipmentId, int side);

    boolean isTopologicalNodeMapped(String busId);

    boolean isTopologicalNodeEmpty();

    CgmesIidmMapping putTopologicalNode(String equipmentId, int side, String topologicalNodeId);

    CgmesIidmMapping putTopologicalNode(String busId, String topologicalNodeId);

    Map<String, Set<String>> topologicalNodesByBusViewBusMap();

    Set<String> getUnmappedTopologicalNodes();

    Map<Double, BaseVoltageSource> getBaseVoltages();

    BaseVoltageSource getBaseVoltage(double nominalVoltage);

    boolean isBaseVoltageMapped(double nominalVoltage);

    boolean isBaseVoltageEmpty();

    CgmesIidmMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source);

    Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap();
}
