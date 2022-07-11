/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class CouplingDeviceDiagramData<T extends Identifiable<T>> extends AbstractExtension<T> {

    static final String NAME = "coupling-device-diagram-data";

    public class CouplingDeviceDiagramDetails {
        private final DiagramPoint point;
        private final double rotation;
        private List<DiagramPoint> terminal1Points = new ArrayList<>();
        private List<DiagramPoint> terminal2Points = new ArrayList<>();

        public CouplingDeviceDiagramDetails(DiagramPoint point, double rotation) {
            this.point = Objects.requireNonNull(point);
            this.rotation = Objects.requireNonNull(rotation);
        }

        public DiagramPoint getPoint() {
            return point;
        }

        public double getRotation() {
            return rotation;
        }

        public List<DiagramPoint> getTerminalPoints(DiagramTerminal terminal) {
            Objects.requireNonNull(terminal);
            switch (terminal) {
                case TERMINAL1:
                    return terminal1Points.stream().sorted().collect(Collectors.toList());
                case TERMINAL2:
                    return terminal2Points.stream().sorted().collect(Collectors.toList());
                default:
                    throw new AssertionError("Unexpected terminal: " + terminal);
            }
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
                default:
                    throw new AssertionError("Unexpected terminal: " + terminal);
            }
        }
    }

    private Map<String, CouplingDeviceDiagramDetails> diagramsDetails = new HashMap<>();

    private CouplingDeviceDiagramData(T extendable) {
        super(extendable);
    }

    public CouplingDeviceDiagramData(Switch sw) {
        this((T) sw);
    }

    public CouplingDeviceDiagramData(TwoWindingsTransformer transformer) {
        this((T) transformer);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void addData(String diagramName, CouplingDeviceDiagramDetails data) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(data);
        diagramsDetails.put(diagramName, data);
    }

    CouplingDeviceDiagramDetails getData(String diagramName) {
        Objects.requireNonNull(diagramName);
        return diagramsDetails.get(diagramName);
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }
}
