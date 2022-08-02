/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class LineDiagramDataImpl <I extends Identifiable<I>> extends AbstractExtension<I>
        implements LineDiagramData<I>  {

    private Map<String, List<DiagramPoint>> diagramsDetails = new HashMap<>();

    public LineDiagramDataImpl(I line) {
        super(line);
    }

    public LineDiagramDataImpl(Line line) {
        this((I) line);
    }

    public LineDiagramDataImpl(DanglingLine danglingLine) {
        this((I) danglingLine);
    }

    public LineDiagramDataImpl(HvdcLine hvdcLine) {
        this((I) hvdcLine);
    }

    @Override
    public void addPoint(String diagramName, DiagramPoint point) {
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(point);
        List<DiagramPoint> points = diagramsDetails.getOrDefault(diagramName, new ArrayList<>());
        points.add(point);
        diagramsDetails.put(diagramName, points);
    }

    @Override
    public List<DiagramPoint> getPoints(String diagramName) {
        return diagramsDetails.getOrDefault(diagramName, Collections.emptyList()).stream().sorted().collect(Collectors.toList());
    }

    @Override
    public DiagramPoint getFirstPoint(String diagramName) {
        return diagramsDetails.getOrDefault(diagramName, Collections.emptyList()).stream().sorted().findFirst().orElse(new DiagramPoint(0, 0, 0));
    }

    @Override
    public DiagramPoint getLastPoint(String diagramName) {
        return diagramsDetails.getOrDefault(diagramName, Collections.emptyList()).stream().sorted(Comparator.reverseOrder()).findFirst().orElse(new DiagramPoint(0, 0, 0));
    }

    @Override
    public DiagramPoint getFirstPoint(String diagramName, double offset) {
        List<DiagramPoint> points = diagramsDetails.getOrDefault(diagramName, Collections.emptyList());
        if (points.size() < 2) {
            return getFirstPoint(diagramName);
        }
        DiagramPoint firstPoint = points.stream().sorted().findFirst().orElseThrow(AssertionError::new);
        DiagramPoint secondPoint = points.stream().sorted().skip(1).findFirst().orElseThrow(AssertionError::new);
        return shiftPoint(firstPoint, secondPoint, offset);
    }

    @Override
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

    @Override
    public List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsDetails.keySet());
    }

    public static LineDiagramData<Line> getOrCreateDiagramData(Line line) {
        LineDiagramData<Line> lineDiagramData = line.getExtension(LineDiagramData.class);
        if (lineDiagramData == null) {
            lineDiagramData = new LineDiagramDataImpl<>(line);
        }
        return lineDiagramData;
    }

    public static LineDiagramData<DanglingLine> getOrCreateDiagramData(DanglingLine danglingLine) {
        LineDiagramData<DanglingLine> danglingLineData = danglingLine.getExtension(LineDiagramData.class);
        if (danglingLineData == null) {
            danglingLineData = new LineDiagramDataImpl<>(danglingLine);
        }
        return danglingLineData;
    }
}
