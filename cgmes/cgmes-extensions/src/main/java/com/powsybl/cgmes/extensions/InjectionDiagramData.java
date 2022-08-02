/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;

import java.util.List;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public interface InjectionDiagramData<I extends Injection<I>> extends Extension<I> {

    String NAME = "injection-diagram-data";

    @Override
    default String getName() {
        return NAME;
    }

    interface InjectionDiagramDetails {

        void addTerminalPoint(DiagramPoint point);

        DiagramPoint getPoint();

        double getRotation();

        List<DiagramPoint> getTerminalPoints();
    }

    void addData(String diagramName, InjectionDiagramDetails diagramData);

    InjectionDiagramDetails getData(String diagramName);

    List<String> getDiagramsNames();

}
