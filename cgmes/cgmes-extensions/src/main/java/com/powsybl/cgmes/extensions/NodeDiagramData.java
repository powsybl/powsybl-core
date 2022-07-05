/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Identifiable;

import java.util.*;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class NodeDiagramData<T extends Identifiable<T>> extends AbstractExtension<T> {

    static final String NAME = "node-diagram-data";

    public class NodeDiagramDataDetails {
        private DiagramPoint point1;
        private DiagramPoint point2;

        public DiagramPoint getPoint1() {
            return point1;
        }

        public void setPoint1(DiagramPoint point1) {
            this.point1 = Objects.requireNonNull(point1);
        }

        public DiagramPoint getPoint2() {
            return point2;
        }

        public void setPoint2(DiagramPoint point2) {
            this.point2 = Objects.requireNonNull(point2);
        }
    }

    private Map<String, NodeDiagramDataDetails> diagramsDetails = new HashMap<>();

    private NodeDiagramData(T identifiable) {
        super(identifiable);
    }

    public NodeDiagramData(BusbarSection busbar) {
        this((T) busbar);
    }

    public NodeDiagramData(Bus bus) {
        this((T) bus);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void addData(String diagramName, NodeDiagramDataDetails nodeDetails) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(nodeDetails);
        diagramsDetails.put(diagramName, nodeDetails);
    }

    public NodeDiagramDataDetails getData(String diagramName) {
        Objects.requireNonNull(diagramName);
        return diagramsDetails.get(diagramName);
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }

    public static NodeDiagramData<Bus> getOrCreateDiagramData(Bus bus) {
        NodeDiagramData<Bus> nodeDiagramData = bus.getExtension(NodeDiagramData.class);
        if (nodeDiagramData == null) {
            nodeDiagramData = new NodeDiagramData<>(bus);
        }
        return nodeDiagramData;
    }

    public static NodeDiagramData<BusbarSection> getOrCreateDiagramData(BusbarSection busbar) {
        NodeDiagramData<BusbarSection> nodeDiagramData = busbar.getExtension(NodeDiagramData.class);
        if (nodeDiagramData == null) {
            nodeDiagramData = new NodeDiagramData<>(busbar);
        }
        return nodeDiagramData;
    }

}
