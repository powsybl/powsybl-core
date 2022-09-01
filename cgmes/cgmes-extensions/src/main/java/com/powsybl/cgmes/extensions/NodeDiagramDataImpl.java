/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class NodeDiagramDataImpl<T extends Identifiable<T>> extends AbstractExtension<T> implements NodeDiagramData<T> {

    public static class NodeDiagramDataDetailsImpl implements NodeDiagramDataDetails {
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

    public NodeDiagramDataImpl(T identifiable) {
        super(identifiable);
    }

    public NodeDiagramDataImpl(BusbarSection busbar) {
        this((T) busbar);
    }

    public NodeDiagramDataImpl(Bus bus) {
        this((T) bus);
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

}
