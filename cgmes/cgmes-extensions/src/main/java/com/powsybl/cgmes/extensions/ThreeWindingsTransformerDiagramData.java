/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ThreeWindingsTransformerDiagramData extends AbstractExtension<ThreeWindingsTransformer> {

    static final String NAME = "three-windings-transformer-diagram-data";

    public class ThreeWindingsTransformerDiagramDataDetails {
        private final DiagramPoint point;
        private final double rotation;
        private List<DiagramPoint> terminal1Points = new ArrayList<>();
        private List<DiagramPoint> terminal2Points = new ArrayList<>();
        private List<DiagramPoint> terminal3Points = new ArrayList<>();

        public ThreeWindingsTransformerDiagramDataDetails(DiagramPoint point, double rotation) {
            this.point = Objects.requireNonNull(point);
            this.rotation = Objects.requireNonNull(rotation);
        }

        public void addTerminalPoint(DiagramTerminal terminal, DiagramPoint point) {
            Objects.requireNonNull(terminal);
            Objects.requireNonNull(point);
            switch (terminal) {
                case TERMINAL1:
                    terminal1Points.add(point);
                    break;
                case TERMINAL2:
                    terminal2Points.add(point);
                    break;
                case TERMINAL3:
                    terminal3Points.add(point);
                    break;
                default:
                    throw new AssertionError("Unexpected terminal: " + terminal);
            }
        }

        public List<DiagramPoint> getTerminalPoints(DiagramTerminal terminal) {
            Objects.requireNonNull(terminal);
            switch (terminal) {
                case TERMINAL1:
                    return terminal1Points.stream().sorted().collect(Collectors.toList());
                case TERMINAL2:
                    return terminal2Points.stream().sorted().collect(Collectors.toList());
                case TERMINAL3:
                    return terminal3Points.stream().sorted().collect(Collectors.toList());
                default:
                    throw new AssertionError("Unexpected terminal: " + terminal);
            }
        }

        public DiagramPoint getPoint() {
            return point;
        }

        public double getRotation() {
            return rotation;
        }
    }

    private Map<String, ThreeWindingsTransformerDiagramDataDetails> diagramsDetails = new HashMap<>();

    public ThreeWindingsTransformerDiagramData(ThreeWindingsTransformer transformer) {
        super(transformer);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void addData(String diagramName, ThreeWindingsTransformerDiagramDataDetails nodeDetails) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(nodeDetails);
        diagramsDetails.put(diagramName, nodeDetails);
    }

    public ThreeWindingsTransformerDiagramDataDetails getData(String diagramName) {
        Objects.requireNonNull(diagramName);
        return diagramsDetails.get(diagramName);
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }

}
