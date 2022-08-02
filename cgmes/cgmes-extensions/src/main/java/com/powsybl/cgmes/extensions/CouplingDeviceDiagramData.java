/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

import java.util.List;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public interface CouplingDeviceDiagramData<I extends Identifiable<I>> extends Extension<I> {

    String NAME = "coupling-device-diagram-data";

    interface CouplingDeviceDiagramDetails {
        DiagramPoint getPoint();

        double getRotation();

        List<DiagramPoint> getTerminalPoints(DiagramTerminal terminal);

        void addTerminalPoint(DiagramTerminal terminal, DiagramPoint point);

    }

    void addData(String diagramName, CouplingDeviceDiagramDetails data);

    CouplingDeviceDiagramDetails getData(String diagramName);

    List<String> getDiagramsNames();

    @Override
    default String getName() {
        return NAME;
    }
}
