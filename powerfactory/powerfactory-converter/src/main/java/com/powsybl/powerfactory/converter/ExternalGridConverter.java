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
import com.powsybl.powerfactory.model.PowerFactoryException;

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
        NodeRef nodeRef = checkNodes(elmXnet, 1).getFirst();
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
        return bustpToBusType(elmXnet.getStringAttributeValue("bustp")) == BusType.SLACK;
    }

    private static boolean obtainVoltageRegulatorOn(BusType type) {
        boolean voltageRegulatorOn = false;
        switch (type) {
            case PQ:
                break;
            case PV, SLACK:
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
        private final double minP;
        private final double maxP;
        private final double minQ;
        private final double maxQ;

        private ExternalGridModel(BusType type, double p, double q, double voltageSetpointpu,
                                  double minP, double maxP, double minQ, double maxQ) {
            this.type = type;
            this.p = p;
            this.q = q;
            this.voltageSetpointpu = voltageSetpointpu;
            this.minP = minP;
            this.maxP = maxP;
            this.minQ = minQ;
            this.maxQ = maxQ;
        }

        private static ExternalGridModel create(DataObject elmXnet) {

            BusType busType = bustpToBusType(elmXnet.getStringAttributeValue("bustp"));

            PQ target = calculateTargetPQ(elmXnet);
            PQ sign = calculatePQSign(elmXnet, "pgini", "qgini");

            double usetpoint = elmXnet.findFloatAttributeValue("usetp").orElse(Float.NaN);
            double referenceAngle = elmXnet.findFloatAttributeValue("phiini").orElse(Float.NaN);

            double minP = elmXnet.findFloatAttributeValue("Pmin_uc").orElse(-Float.MAX_VALUE);
            double maxP = elmXnet.findFloatAttributeValue("MaxS").orElse(Float.MAX_VALUE);
            double minQ = elmXnet.findFloatAttributeValue("cQ_min").orElse(-Float.MAX_VALUE);
            double maxQ = elmXnet.findFloatAttributeValue("cQ_max").orElse(Float.MAX_VALUE);

            if (!valid(busType, target, usetpoint, referenceAngle)) {
                throw new PowerFactoryException("Unexpected target values '" + elmXnet.getLocName() + "'");
            }

            return new ExternalGridModel(busType, target.p * sign.p, target.q * sign.q, usetpoint, minP, maxP, minQ, maxQ);
        }

        // In powerfactory a positive value for P is considered to be a generated active power and
        // a negative value is considered to be a consumed active power
        private static PQ calculateTargetPQ(DataObject elmXnet) {

            String modeInp = elmXnet.findStringAttributeValue("mode_inp").orElse(null);
            Double pgini = float2Double(elmXnet.findFloatAttributeValue("pgini").orElse(null));
            Double qgini = float2Double(elmXnet.findFloatAttributeValue("qgini").orElse(null));
            Double sgini = float2Double(elmXnet.findFloatAttributeValue("sgini").orElse(null));
            Double cosgini = float2Double(elmXnet.findFloatAttributeValue("cosgini").orElse(null));

            return calculate(modeInp, pgini, qgini, sgini, cosgini);
        }

        private static boolean valid(BusType busType, PQ target, double targetV, double targetA) {
            return switch (busType) {
                case PQ, PV -> !Double.isNaN(target.p) && !Double.isNaN(target.q) && !Double.isNaN(targetV);
                case SLACK -> !Double.isNaN(target.p) && !Double.isNaN(target.q) && !Double.isNaN(targetV) && !Double.isNaN(targetA);
            };
        }
    }

    private static BusType bustpToBusType(String bustp) {
        return switch (bustp) {
            case "PQ" -> BusType.PQ;
            case "PV" -> BusType.PV;
            case "SL" -> BusType.SLACK;
            default -> throw new PowerFactoryException("Unexpected bustp '" + bustp + "'");
        };
    }
}
