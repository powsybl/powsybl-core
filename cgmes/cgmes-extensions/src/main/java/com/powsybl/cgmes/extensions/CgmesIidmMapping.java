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

    class CgmesTopologicalNode {
        private final String cgmesId;
        private final String name;
        private final Source source;

        public CgmesTopologicalNode(String cgmesId, String name, Source source) {
            this.cgmesId = Objects.requireNonNull(cgmesId);
            this.name = Objects.requireNonNull(name);
            this.source = Objects.requireNonNull(source);
        }

        public String getCgmesId() {
            return cgmesId;
        }

        public String getName() {
            return name;
        }

        public Source getSource() {
            return source;
        }

        @Override
        public int hashCode() {
            return cgmesId.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CgmesTopologicalNode)) {
                return false;
            }

            CgmesTopologicalNode node = (CgmesTopologicalNode) o;
            return cgmesId.equals(node.getCgmesId());
        }
    }

    String NAME = "cgmesIidmMapping";

    @Override
    default String getName() {
        return NAME;
    }

    Set<CgmesTopologicalNode> getTopologicalNodes(String busId);

    String getTopologicalNode(String equipmentId, int side);

    boolean isTopologicalNodeMapped(String busId);

    boolean isTopologicalNodeEmpty();

    CgmesIidmMapping putTopologicalNode(String equipmentId, int side, String topologicalNodeId);

    CgmesIidmMapping putTopologicalNode(String busId, String topologicalNodeId, String topologicalNodeName, Source source);

    CgmesIidmMapping putUnmappedTopologicalNode(String topologicalNodeId, String topologicalNodeName, Source source);

    Map<String, Set<CgmesTopologicalNode>> topologicalNodesByBusViewBusMap();

    Set<CgmesTopologicalNode> getUnmappedTopologicalNodes();

    void invalidateTopology();

    Map<Double, BaseVoltageSource> getBaseVoltages();

    BaseVoltageSource getBaseVoltage(double nominalVoltage);

    boolean isBaseVoltageMapped(double nominalVoltage);

    boolean isBaseVoltageEmpty();

    CgmesIidmMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source);

    Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap();

    void addTopologyListener();
}
