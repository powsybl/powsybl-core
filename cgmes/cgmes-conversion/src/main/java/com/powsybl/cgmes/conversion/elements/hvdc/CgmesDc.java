/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import com.powsybl.cgmes.model.CgmesModel;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesDc {
    private final CgmesModel cgmesModel;

    // En cada isla guardar
    // DcTopologicalNodes
    // DcLineSegments
    // AcDcConverters (AcNodes, DcTopologicalNodes)
    // AcNodes
    // Transformers
    // DcGrounds (No se considera incluirlo)
    public CgmesDc(CgmesModel cgmes) {
        this.cgmesModel = cgmes;

        Adjacency adjacency = new Adjacency(cgmesModel);
        adjacency.print();

        TPnodeEquipments tpNodeEquipments = new TPnodeEquipments(cgmesModel, adjacency);
        tpNodeEquipments.print();

        Islands islands = new Islands(adjacency);
        islands.print();

        IslandsEnds islandsEnds = new IslandsEnds();
        islands.islandsNodes.forEach(listNodes -> islandsEnds.add(adjacency, listNodes));
        islandsEnds.print();

        Hvdc hvdc = new Hvdc();
        islandsEnds.islandsEndsNodes.forEach(ine -> hvdc.add(adjacency, tpNodeEquipments,
            ine.commonTopologicalNode1, ine.topologicalNodes1, ine.commonTopologicalNode2,
            ine.topologicalNodes2));
        hvdc.print();
    }
}
