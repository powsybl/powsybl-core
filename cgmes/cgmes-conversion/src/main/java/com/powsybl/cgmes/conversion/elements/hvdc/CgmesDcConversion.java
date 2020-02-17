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
public class CgmesDcConversion {
    private final CgmesModel cgmesModel;

    // En cada isla guardar
    // DcTopologicalNodes
    // DcLineSegments
    // AcDcConverters (AcNodes, DcTopologicalNodes)
    // AcNodes
    // Transformers
    // DcGrounds (No se considera incluirlo)
    public CgmesDcConversion(CgmesModel cgmes) {
        this.cgmesModel = cgmes;

        Adjacency adjacency = new Adjacency(cgmesModel);
        //adjacency.print();

        TPnodeEquipments tpNodeEquipments = new TPnodeEquipments(cgmesModel, adjacency);
        //tpNodeEquipments.print();

        Islands islands = new Islands(adjacency);
        //islands.print();

        IslandsEnds islandsEnds = new IslandsEnds();
        islands.islandsNodes.forEach(listNodes -> islandsEnds.add(adjacency, listNodes));
        //islandsEnds.print();

        Hvdc hvdc = new Hvdc();
        islandsEnds.islandsEndsNodes.forEach(ien -> {
            IslandEndHvdc islandEndHvdc1 = new IslandEndHvdc();
            islandEndHvdc1.add(adjacency, tpNodeEquipments, ien.topologicalNodes1);
            // islandEndHvdc1.print();

            IslandEndHvdc islandEndHvdc2 = new IslandEndHvdc();
            islandEndHvdc2.add(adjacency, tpNodeEquipments, ien.topologicalNodes2);
            // islandEndHvdc2.print();

            hvdc.add(tpNodeEquipments, islandEndHvdc1, islandEndHvdc2);
        });
        hvdc.print();
    }
}
