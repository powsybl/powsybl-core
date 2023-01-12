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
import com.powsybl.powerfactory.model.PowerFactoryException;

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

            float p0 = elmLod.getFloatAttributeValue("plini");
            Optional<Float> q0 = elmLod.findFloatAttributeValue("qlini");
            if (q0.isPresent()) {
                return new LoadModel(p0, q0.get());
            }

            float s0 = elmLod.getFloatAttributeValue("slini");
            double q;
            double q2 = s0 * s0 - p0 * p0;
            if (q2 >= 0) {
                q = Math.signum(p0) * Math.sqrt(q2);
            } else {
                throw new PowerFactoryException(String.format("Unexpected apparent power Mva %.2f Mw %.2f", s0, p0));
            }
            return new LoadModel(p0, q);
        }
    }
}
