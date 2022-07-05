/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.VoltageLevel;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class VoltageLevelDiagramData extends AbstractExtension<VoltageLevel> {

    static final String NAME = "voltage-level-diagram-data";

    private Map<String, Map<Integer, DiagramPoint>> internalNodesPoints = new HashMap<>();

    private VoltageLevelDiagramData() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static VoltageLevelDiagramData getVoltageLevelDiagramData(VoltageLevel voltageLevel) {
        VoltageLevelDiagramData voltageLevelDiagramData = voltageLevel.getExtension(VoltageLevelDiagramData.class);
        if (voltageLevelDiagramData == null) {
            voltageLevelDiagramData = new VoltageLevelDiagramData();
        }
        return voltageLevelDiagramData;
    }

    public static boolean checkDiagramData(VoltageLevel voltageLevel) {
        Objects.requireNonNull(voltageLevel);
        return voltageLevel.getExtension(VoltageLevelDiagramData.class) != null;
    }

    public List<String> getDiagramsNames() {
        return new ArrayList<>(internalNodesPoints.keySet());
    }

    public static void addInternalNodeDiagramPoint(VoltageLevel voltageLevel, String diagramName, int nodeNum, DiagramPoint point) {
        Objects.requireNonNull(voltageLevel);
        Objects.requireNonNull(diagramName);
        Objects.requireNonNull(point);
        VoltageLevelDiagramData voltageLevelDiagramData = getVoltageLevelDiagramData(voltageLevel);
        voltageLevelDiagramData.internalNodesPoints.putIfAbsent(diagramName, new HashMap<>());
        voltageLevelDiagramData.internalNodesPoints.get(diagramName).put(nodeNum, point);
        voltageLevel.addExtension(VoltageLevelDiagramData.class, voltageLevelDiagramData);
    }

    public static DiagramPoint getInternalNodeDiagramPoint(VoltageLevel voltageLevel, String diagramName, int nodeNum) {
        Objects.requireNonNull(voltageLevel);
        Objects.requireNonNull(diagramName);
        VoltageLevelDiagramData voltageLevelDiagramData = getVoltageLevelDiagramData(voltageLevel);
        if (voltageLevelDiagramData.internalNodesPoints.get(diagramName) != null) {
            return voltageLevelDiagramData.internalNodesPoints.get(diagramName).get(nodeNum);
        } else {
            return null;
        }
    }

    public static int[] getInternalNodeDiagramPoints(VoltageLevel voltageLevel, String diagramName) {
        Objects.requireNonNull(voltageLevel);
        Objects.requireNonNull(diagramName);
        VoltageLevelDiagramData voltageLevelDiagramData = getVoltageLevelDiagramData(voltageLevel);
        if (voltageLevelDiagramData.internalNodesPoints.get(diagramName) != null) {
            return voltageLevelDiagramData.internalNodesPoints.get(diagramName).keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
        } else {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
    }
}
