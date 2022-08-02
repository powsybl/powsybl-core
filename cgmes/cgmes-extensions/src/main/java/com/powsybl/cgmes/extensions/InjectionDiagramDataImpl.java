/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class InjectionDiagramDataImpl<I extends Injection<I>> extends AbstractExtension<I>
        implements InjectionDiagramData<I> {

    static class InjectionDiagramDetailsImpl implements InjectionDiagramDetails {
        private final DiagramPoint point;
        private final double rotation;
        private final List<DiagramPoint> terminalPoints = new ArrayList<>();

        public InjectionDiagramDetailsImpl(DiagramPoint point, double rotation) {
            this.point = Objects.requireNonNull(point);
            this.rotation = rotation;
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

    private final Map<String, InjectionDiagramDetails> diagramsDetails = new HashMap<>();

    //TODO : Made it public?
    public InjectionDiagramDataImpl(I injection) {
        super(injection);
    }

    public InjectionDiagramDataImpl(Generator generator) {
        this((I) generator);
    }

    public InjectionDiagramDataImpl(Load load) {
        this((I) load);
    }

    public InjectionDiagramDataImpl(ShuntCompensator shunt) {
        this((I) shunt);
    }

    public InjectionDiagramDataImpl(StaticVarCompensator svc) {
        this((I) svc);
    }

    @Override
    public void addData(String diagramName, InjectionDiagramDetails diagramData) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(diagramData);
        diagramsDetails.put(diagramName, diagramData);
    }

    @Override
    public InjectionDiagramDetails getData(String diagramName) {
        Objects.requireNonNull(diagramName);
        return diagramsDetails.get(diagramName);
    }

    @Override
    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }


}
