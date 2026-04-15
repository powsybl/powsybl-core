/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class LoadConverter extends AbstractConverter {

    LoadConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmLod) {
        NodeRef nodeRef = checkNodes(elmLod, 1).getFirst();
        LoadModel loadModel = LoadModel.create(elmLod);

        VoltageLevel vl = getNetwork().getVoltageLevel(nodeRef.voltageLevelId);
        vl.newLoad()
            .setId(elmLod.getLocName())
            .setEnsureIdUnicity(true)
            .setNode(nodeRef.node)
            .setP0(loadModel.p0)
            .setQ0(loadModel.q0)
            .add();

        // ElmLodmv can have also a generator
        if ("ElmLodmv".equals(elmLod.getDataClassName())) {
            createGeneratorFromLoad(elmLod, vl, nodeRef);
        }
    }

    private static void createGeneratorFromLoad(DataObject elmLod, VoltageLevel vl, NodeRef nodeRef) {
        GeneratorModel generatorModel = GeneratorModel.create(elmLod);
        if (generatorModel.isDefined()) {
            int newNode = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
            vl.getNodeBreakerView().newInternalConnection().setNode1(nodeRef.node).setNode2(newNode).add();
            Generator generator = vl.newGenerator()
                    .setId(elmLod.getLocName() + "-G")
                    .setEnsureIdUnicity(true)
                    .setNode(newNode)
                    .setTargetP(-generatorModel.p0)
                    .setTargetQ(-generatorModel.q0)
                    .setVoltageRegulatorOn(false)
                    .setMaxP(-generatorModel.p0)
                    .setMinP(-generatorModel.p0)
                    .add();
            generator.newMinMaxReactiveLimits()
                    .setMinQ(-generatorModel.q0)
                    .setMaxQ(-generatorModel.q0)
                    .add();
        }
    }

    // Only constant power load is considered. Powerfactory supports ZIP load model
    private record LoadModel(double p0, double q0) {

        private static LoadModel create(DataObject elmLod) {

            PQ pq = calculateLoadPQ(elmLod);
            PQ sign = calculatePQSign(elmLod, "plini", "qlini");

            return new LoadModel(pq.p * sign.p, pq.q * sign.q);
        }

        private static PQ calculateLoadPQ(DataObject elmLod) {

            String modeInp = elmLod.findStringAttributeValue("mode_inp").orElse(null);
            Double plini = float2Double(elmLod.findFloatAttributeValue("plini").orElse(null));
            Double qlini = float2Double(elmLod.findFloatAttributeValue("qlini").orElse(null));
            Double slini = float2Double(elmLod.findFloatAttributeValue("slini").orElse(null));
            Double coslini = float2Double(elmLod.findFloatAttributeValue("coslini").orElse(null));

            return calculate(modeInp, plini, qlini, slini, coslini);
        }
    }

    private record GeneratorModel(double p0, double q0) {

        private static GeneratorModel create(DataObject elmLod) {

            PQ pq = calculateGeneratorPQ(elmLod);
            PQ sign = calculatePQSign(elmLod, "pgini", "qgini");

            return new GeneratorModel(-pq.p * sign.p, -pq.q * sign.q);
        }

        private boolean isDefined() {
            return !Double.isNaN(this.p0) && !Double.isNaN(this.q0);
        }

        private static PQ calculateGeneratorPQ(DataObject elmLod) {

            Double pgini = float2Double(elmLod.findFloatAttributeValue("pgini").orElse(null));
            Double qgini = float2Double(elmLod.findFloatAttributeValue("qgini").orElse(null));
            Double sgini = float2Double(elmLod.findFloatAttributeValue("sgini").orElse(null));
            Double cosgini = float2Double(elmLod.findFloatAttributeValue("cosgini").orElse(null));

            return calculate(null, pgini, qgini, sgini, cosgini);
        }
    }

}
