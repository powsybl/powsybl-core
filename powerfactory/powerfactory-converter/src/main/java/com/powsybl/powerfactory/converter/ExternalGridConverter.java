/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import java.util.Optional;

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
        return bustpToBusType(elmXnet.getStringAttributeValue("bustp")) == BusType.SLACK;
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

            BusType busType = bustpToBusType(elmXnet.getStringAttributeValue("bustp"));

            PQ target = calculateTargetPQ(elmXnet);
            PQ sign = calculateSignPQ(elmXnet);

            double usetpoint = elmXnet.findFloatAttributeValue("usetp").orElse(Float.NaN);
            double referenceAngle = elmXnet.findFloatAttributeValue("phini").orElse(Float.NaN);

            double minP = elmXnet.getFloatAttributeValue("Pmin_uc");
            double maxP = elmXnet.getFloatAttributeValue("Pmax_uc");
            double minQ = elmXnet.getFloatAttributeValue("q_min");
            double maxQ = elmXnet.getFloatAttributeValue("q_max");

            if (!valid(busType, target, usetpoint, referenceAngle)) {
                throw new PowerFactoryException("Unexpected target values '" + elmXnet.getLocName() + "'");
            }

            return new ExternalGridModel(busType, target.p * sign.p, target.q * sign.q, usetpoint, referenceAngle, minP, maxP, minQ, maxQ);
        }

        // In powerfactory a positive value for P is considered to be a generated active power and
        // a negative value is considered to be a consumed active power
        private static PQ calculateTargetPQ(DataObject elmXnet) {

            Optional<Float> pgini = elmXnet.findFloatAttributeValue("pgini");
            Optional<Float> qgini = elmXnet.findFloatAttributeValue("qgini");
            Optional<Float> sgini = elmXnet.findFloatAttributeValue("sgini");
            Optional<Float> cosgini = elmXnet.findFloatAttributeValue("cosgini");

            PQ target;
            if (pgini.isPresent() && qgini.isPresent()) {
                target = new PQ(pgini.get(), qgini.get());
            } else if (pgini.isPresent() && sgini.isPresent()) {
                target = calculatePQFromPandS(pgini.get(), sgini.get());
            } else if (qgini.isPresent() && sgini.isPresent()) {
                target = calculatePQFromQandS(qgini.get(), sgini.get());
            } else if (pgini.isPresent() && cosgini.isPresent()) {
                target = calculatePQFromPandPowerFactor(pgini.get(), cosgini.get());
            } else if (qgini.isPresent() && cosgini.isPresent()) {
                target = calculatePQFromQandPowerFactor(qgini.get(), cosgini.get());
            } else if (sgini.isPresent() && cosgini.isPresent()) {
                target = calculatePQFromSandPowerFactor(sgini.get(), cosgini.get());
            } else {
                target = new PQ(Double.NaN, Double.NaN);
            }

            return target;
        }

        private static PQ calculateSignPQ(DataObject elmXnet) {

            Optional<Float> pgini = elmXnet.findFloatAttributeValue("pgini");
            Optional<Float> qgini = elmXnet.findFloatAttributeValue("qgini");

            double signP = 1;
            if (pgini.isEmpty() && qgini.isPresent()) {
                signP = Math.signum(qgini.get());
            }
            double signQ = 1;
            if (qgini.isEmpty() && pgini.isPresent()) {
                signP = Math.signum(pgini.get());
            }

            return new PQ(signP, signQ);
        }

        private static boolean valid(BusType busType, PQ target, double targetV, double targetA) {
            switch (busType) {
                case PQ:
                case PV:
                    return !Double.isNaN(target.p) && !Double.isNaN(target.q) && !Double.isNaN(targetV);
                case SLACK:
                    return !Double.isNaN(target.p) && !Double.isNaN(target.q) && !Double.isNaN(targetV) && !Double.isNaN(targetA);
            }
            return false;
        }
    }

    private static BusType bustpToBusType(String bustp) {
        if (bustp.equals("PQ")) {
            return BusType.PQ;
        } else if (bustp.equals("PV")) {
            return BusType.PV;
        } else if (bustp.equals("SL")) {
            return BusType.SLACK;
        } else {
            throw new PowerFactoryException("Unexpected bustp '" + bustp + "'");
        }
    }
}
