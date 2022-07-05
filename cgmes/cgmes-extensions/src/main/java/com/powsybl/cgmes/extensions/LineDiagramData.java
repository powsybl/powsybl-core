/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class LineDiagramData<T extends Identifiable<T>> extends AbstractExtension<T> {

    static final String NAME = "line-diagram-data";

    private Map<String, List<DiagramPoint>> diagramsDetails = new HashMap<>();

    private LineDiagramData(T line) {
        super(line);
    }

    public LineDiagramData(Line line) {
        this((T) line);
    }

    public LineDiagramData(DanglingLine danglingLine) {
        this((T) danglingLine);
    }

    public LineDiagramData(HvdcLine hvdcLine) {
        this((T) hvdcLine);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void addPoint(String diagramName, DiagramPoint point) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(point);
        List<DiagramPoint> points = diagramsDetails.getOrDefault(diagramName, new ArrayList<>());
        points.add(point);
        diagramsDetails.put(diagramName, points);
    }

    public List<DiagramPoint> getPoints(String diagramName) {
        return diagramsDetails.getOrDefault(diagramName, Collections.emptyList()).stream().sorted().collect(Collectors.toList());
    }

    public DiagramPoint getFirstPoint(String diagramName) {
        return diagramsDetails.getOrDefault(diagramName, Collections.emptyList()).stream().sorted().findFirst().orElse(new DiagramPoint(0, 0, 0));
    }

    public DiagramPoint getLastPoint(String diagramName) {
        return diagramsDetails.getOrDefault(diagramName, Collections.emptyList()).stream().sorted(Comparator.reverseOrder()).findFirst().orElse(new DiagramPoint(0, 0, 0));
    }

    public DiagramPoint getFirstPoint(String diagramName, double offset) {
        List<DiagramPoint> points = diagramsDetails.getOrDefault(diagramName, Collections.emptyList());
        if (points.size() < 2) {
            return getFirstPoint(diagramName);
        }
        DiagramPoint firstPoint = points.stream().sorted().findFirst().orElseThrow(AssertionError::new);
        DiagramPoint secondPoint = points.stream().sorted().skip(1).findFirst().orElseThrow(AssertionError::new);
        return shiftPoint(firstPoint, secondPoint, offset);
    }

    public DiagramPoint getLastPoint(String diagramName, double offset) {
        List<DiagramPoint> points = diagramsDetails.getOrDefault(diagramName, Collections.emptyList());
        if (points.size() < 2) {
            return getLastPoint(diagramName);
        }
        DiagramPoint lastPoint = points.stream().sorted(Comparator.reverseOrder()).findFirst().orElseThrow(AssertionError::new);
        DiagramPoint secondLastPoint = points.stream().sorted(Comparator.reverseOrder()).skip(1).findFirst().orElseThrow(AssertionError::new);
        return shiftPoint(lastPoint, secondLastPoint, offset);
    }

    private DiagramPoint shiftPoint(DiagramPoint point, DiagramPoint otherPoint, double offset) {
        Vector2D pointVector = new Vector2D(point.getX(), point.getY());
        Vector2D otherPointVector = new Vector2D(otherPoint.getX(), otherPoint.getY());
        Vector2D shiftedPointVector = pointVector.add(otherPointVector.subtract(pointVector).normalize().scalarMultiply(offset));
        return new DiagramPoint(shiftedPointVector.getX(), shiftedPointVector.getY(), point.getSeq());
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }

    public static LineDiagramData<Line> getOrCreateDiagramData(Line line) {
        LineDiagramData<Line> lineDiagramData = line.getExtension(LineDiagramData.class);
        if (lineDiagramData == null) {
            lineDiagramData = new LineDiagramData<>(line);
        }
        return lineDiagramData;
    }

    public static LineDiagramData<DanglingLine> getOrCreateDiagramData(DanglingLine danglingLine) {
        LineDiagramData<DanglingLine> danglingLineData = danglingLine.getExtension(LineDiagramData.class);
        if (danglingLineData == null) {
            danglingLineData = new LineDiagramData<>(danglingLine);
        }
        return danglingLineData;
    }

}
