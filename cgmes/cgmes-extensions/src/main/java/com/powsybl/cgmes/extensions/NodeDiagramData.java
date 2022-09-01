/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Identifiable;

import java.util.List;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public interface NodeDiagramData<T extends Identifiable<T>> extends Extension<T> {

    String NAME = "node-diagram-data";

    interface NodeDiagramDataDetails {

        DiagramPoint getPoint1();

        void setPoint1(DiagramPoint point1);

        DiagramPoint getPoint2();

        void setPoint2(DiagramPoint point2);

    }

    void addData(String diagramName, NodeDiagramDataDetails nodeDetails);

    NodeDiagramDataDetails getData(String diagramName);

    List<String> getDiagramsNames();

    static NodeDiagramData<Bus> getOrCreateDiagramData(Bus bus) {
        NodeDiagramData<Bus> nodeDiagramData = bus.getExtension(NodeDiagramData.class);
        if (nodeDiagramData == null) {
            nodeDiagramData = new NodeDiagramDataImpl<>(bus);
        }
        return nodeDiagramData;
    }

    public static NodeDiagramData<BusbarSection> getOrCreateDiagramData(BusbarSection busbar) {
        NodeDiagramData<BusbarSection> nodeDiagramData = busbar.getExtension(NodeDiagramData.class);
        if (nodeDiagramData == null) {
            nodeDiagramData = new NodeDiagramDataImpl<>(busbar);
        }
        return nodeDiagramData;
    }

    @Override
    default String getName() {
        return NAME;
    }
}
