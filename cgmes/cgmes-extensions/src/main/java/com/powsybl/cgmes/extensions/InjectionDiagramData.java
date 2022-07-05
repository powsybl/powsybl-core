/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class InjectionDiagramData<T extends Injection<T>> extends AbstractExtension<T> {

    static final String NAME = "injection-diagram-data";

    public class InjectionDiagramDetails {
        private final DiagramPoint point;
        private final double rotation;
        private List<DiagramPoint> terminalPoints = new ArrayList<>();

        public InjectionDiagramDetails(DiagramPoint point, double rotation) {
            this.point = Objects.requireNonNull(point);
            this.rotation = Objects.requireNonNull(rotation);
        }

        public void addTerminalPoint(DiagramPoint point) {
            Objects.requireNonNull(point);
            terminalPoints.add(point);
        }

        public DiagramPoint getPoint() {
            return point;
        }

        public double getRotation() {
            return rotation;
        }

        public List<DiagramPoint> getTerminalPoints() {
            return terminalPoints.stream().sorted().collect(Collectors.toList());
        }
    }

    private Map<String, InjectionDiagramDetails> diagramsDetails = new HashMap<>();

    private InjectionDiagramData(T injection) {
        super(injection);
    }

    public InjectionDiagramData(Generator generator) {
        this((T) generator);
    }

    public InjectionDiagramData(Load load) {
        this((T) load);
    }

    public InjectionDiagramData(ShuntCompensator shunt) {
        this((T) shunt);
    }

    public InjectionDiagramData(StaticVarCompensator svc) {
        this((T) svc);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void addData(String diagramName, InjectionDiagramDetails diagramData) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(diagramData);
        diagramsDetails.put(diagramName, diagramData);
    }

    public InjectionDiagramDetails getData(String diagramName) {
        Objects.requireNonNull(diagramName);
        return diagramsDetails.get(diagramName);
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }
}
