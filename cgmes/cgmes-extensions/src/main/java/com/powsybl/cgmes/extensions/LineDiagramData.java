/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;

import java.util.List;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public interface LineDiagramData<I extends Identifiable<I>> extends Extension<I> {

    String NAME = "line-diagram-data";

    @Override
    default String getName() {
        return NAME;
    }

    void addPoint(String diagramName, DiagramPoint point);

    List<DiagramPoint> getPoints(String diagramName);

    DiagramPoint getFirstPoint(String diagramName);

    DiagramPoint getLastPoint(String diagramName);

    DiagramPoint getFirstPoint(String diagramName, double offset);

    DiagramPoint getLastPoint(String diagramName, double offset);

    List<String> getDiagramsNames();
}
