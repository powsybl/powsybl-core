/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import java.util.Optional;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

class LoadConverter extends AbstractConverter {

    LoadConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmLod) {
        NodeRef nodeRef = checkNodes(elmLod, 1).get(0);
        LoadModel loadModel = LoadModel.create(elmLod);

        VoltageLevel vl = getNetwork().getVoltageLevel(nodeRef.voltageLevelId);
        vl.newLoad()
            .setId(elmLod.getLocName())
            .setEnsureIdUnicity(true)
            .setNode(nodeRef.node)
            .setP0(loadModel.p0)
            .setQ0(loadModel.q0)
            .add();
    }

    // Only constant power load is considered. Powerfactory supports ZIP load model
    private static final class LoadModel {
        private final double p0;
        private final double q0;

        private LoadModel(double p0, double q0) {
            this.p0 = p0;
            this.q0 = q0;
        }

        private static LoadModel create(DataObject elmLod) {

            PQ pq = calculatePQ(elmLod);
            PQ sign = calculateSignPQ(elmLod);

            return new LoadModel(pq.p * sign.p, pq.q * sign.q);
        }

        private static PQ calculatePQ(DataObject elmLod) {

            Optional<Float> plini = elmLod.findFloatAttributeValue("plini");
            Optional<Float> qlini = elmLod.findFloatAttributeValue("qlini");
            Optional<Float> slini = elmLod.findFloatAttributeValue("slini");
            Optional<Float> coslini = elmLod.findFloatAttributeValue("coslini");

            PQ target;
            if (plini.isPresent() && qlini.isPresent()) {
                target = new PQ(plini.get(), qlini.get());
            } else if (plini.isPresent() && slini.isPresent()) {
                target = calculatePQFromPandS(plini.get(), slini.get());
            } else if (qlini.isPresent() && slini.isPresent()) {
                target = calculatePQFromQandS(qlini.get(), slini.get());
            } else if (plini.isPresent() && coslini.isPresent()) {
                target = calculatePQFromPandPowerFactor(plini.get(), coslini.get());
            } else if (qlini.isPresent() && coslini.isPresent()) {
                target = calculatePQFromQandPowerFactor(qlini.get(), coslini.get());
            } else if (slini.isPresent() && coslini.isPresent()) {
                target = calculatePQFromSandPowerFactor(slini.get(), coslini.get());
            } else {
                target = new PQ(Double.NaN, Double.NaN);
            }

            return target;
        }

        private static PQ calculateSignPQ(DataObject elmLod) {

            Optional<Float> plini = elmLod.findFloatAttributeValue("plini");
            Optional<Float> qlini = elmLod.findFloatAttributeValue("qlini");

            double signP = 1;
            if (plini.isEmpty() && qlini.isPresent()) {
                signP = Math.signum(qlini.get());
            }
            double signQ = 1;
            if (qlini.isEmpty() && plini.isPresent()) {
                signP = Math.signum(plini.get());
            }

            return new PQ(signP, signQ);
        }
    }
}
