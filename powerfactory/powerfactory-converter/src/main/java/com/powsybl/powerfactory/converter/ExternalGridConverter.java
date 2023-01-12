/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

class ExternalGridConverter extends AbstractConverter {

    private enum BusType {
        PQ, PV, SLACK
    }

    ExternalGridConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmXnet) {
        NodeRef nodeRef = checkNodes(elmXnet, 1).get(0);
        ExternalGridModel externalGridModel = ExternalGridModel.create(elmXnet);

        VoltageLevel vl = getNetwork().getVoltageLevel(nodeRef.voltageLevelId);
        Generator g = vl.newGenerator()
                .setId(elmXnet.getLocName())
                .setEnsureIdUnicity(true)
                .setNode(nodeRef.node)
                .setTargetP(externalGridModel.p)
                .setTargetQ(externalGridModel.q)
                .setTargetV(externalGridModel.voltageSetpointpu * vl.getNominalV())
                .setVoltageRegulatorOn(obtainVoltageRegulatorOn(externalGridModel.type))
                .setMinP(externalGridModel.minP)
                .setMaxP(externalGridModel.maxP)
                .add();

        g.newMinMaxReactiveLimits()
            .setMinQ(externalGridModel.minQ)
            .setMaxQ(externalGridModel.maxQ)
            .add();
    }

    static boolean isSlack(DataObject elmXnet) {
        return false;
    }

    private static boolean obtainVoltageRegulatorOn(BusType type) {
        boolean voltageRegulatorOn = false;
        switch (type) {
            case PQ:
                break;
            case PV:
            case SLACK:
                voltageRegulatorOn = true;
                break;
        }
        return voltageRegulatorOn;
    }

    private static final class ExternalGridModel {
        private final BusType type;
        private final double p;
        private final double q;
        private final double voltageSetpointpu;
        private final double referenceAngle;
        private final double minP;
        private final double maxP;
        private final double minQ;
        private final double maxQ;

        private ExternalGridModel(BusType type, double p, double q, double voltageSetpointpu, double referenceAngle,
            double minP, double maxP, double minQ, double maxQ) {
            this.type = type;
            this.p = p;
            this.q = q;
            this.voltageSetpointpu = voltageSetpointpu;
            this.referenceAngle = referenceAngle;
            this.minP = minP;
            this.maxP = maxP;
            this.minQ = minQ;
            this.maxQ = maxQ;
        }

        private static ExternalGridModel create(DataObject elmXnet) {

            float p = elmXnet.findFloatAttributeValue("pgini_a").orElse(elmXnet.getFloatAttributeValue("pgini"));
            float q = elmXnet.findFloatAttributeValue("qgini_a").orElse(elmXnet.getFloatAttributeValue("qgini"));
            double usetp = elmXnet.getFloatAttributeValue("usetp");
            double minP = 0.0;
            double maxP = 100.0;

            return new ExternalGridModel(BusType.PQ, p, q, usetp, 0.0, minP, maxP, -100.0, 100.0);
        }
    }
}
