/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import java.util.Optional;
import java.util.OptionalInt;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.NodeRef;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

class GeneratorConverter extends AbstractConverter {

    GeneratorConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(DataObject elmSym) {
        NodeRef nodeRef = checkNodes(elmSym, 1).get(0);
        GeneratorModel generatorModel = GeneratorModel.create(elmSym);

        VoltageLevel vl = getNetwork().getVoltageLevel(nodeRef.voltageLevelId);
        Generator g = vl.newGenerator()
            .setId(elmSym.getLocName())
            .setEnsureIdUnicity(true)
            .setNode(nodeRef.node)
            .setTargetP(generatorModel.targetP)
            .setTargetQ(generatorModel.targetQ)
            .setTargetV(generatorModel.targetVpu * vl.getNominalV())
            .setVoltageRegulatorOn(generatorModel.voltageRegulatorOn)
            .setMinP(generatorModel.minP)
            .setMaxP(generatorModel.maxP)
            .add();
        if (generatorModel.isDisconnected) {
            g.getTerminal().disconnect();
        }
        Optional<ReactiveLimits> reactiveLimits = ReactiveLimits.create(elmSym);
        if (reactiveLimits.isPresent()) {
            g.newMinMaxReactiveLimits()
                .setMinQ(reactiveLimits.get().minQ)
                .setMaxQ(reactiveLimits.get().maxQ)
                .add();
        }
    }

    static class GeneratorModel {
        private final boolean isDisconnected;
        private final double targetP;
        private final double targetQ;
        private final double targetVpu;
        private final boolean voltageRegulatorOn;
        private final double minP;
        private final double maxP;

        GeneratorModel(boolean isDisconnected, double targetP, double targetQ, double targetVpu, boolean voltageRegulatorOn, double minP, double maxP) {
            this.isDisconnected = isDisconnected;
            this.targetP = targetP;
            this.targetQ = targetQ;
            this.targetVpu = targetVpu;
            this.voltageRegulatorOn = voltageRegulatorOn;
            this.minP = minP;
            this.maxP = maxP;
        }

        static GeneratorModel create(DataObject elmSym) {
            boolean isDisconnected = disconnected(elmSym);
            boolean voltageRegulatorOn = voltageRegulatorOn(elmSym);

            float pgini = elmSym.getFloatAttributeValue("pgini");
            float qgini = elmSym.getFloatAttributeValue("qgini");
            double usetp = elmSym.getFloatAttributeValue("usetp");
            double pMinUc = minP(elmSym, pgini);
            double pMaxUc = maxP(elmSym, pgini);

            return new GeneratorModel(isDisconnected, pgini, qgini, usetp, voltageRegulatorOn, pMinUc, pMaxUc);
        }

        private static boolean disconnected(DataObject elmSym) {
            OptionalInt outserv = elmSym.findIntAttributeValue("outserv");
            if (outserv.isPresent()) {
                return outserv.getAsInt() == 1;
            }
            return false;
        }

        private static boolean voltageRegulatorOn(DataObject elmSym) {
            OptionalInt ivMode = elmSym.findIntAttributeValue("iv_mode");
            if (ivMode.isPresent()) {
                return ivMode.getAsInt() == 1;
            }
            Optional<String> avMode = elmSym.findStringAttributeValue("av_mode");
            if (avMode.isPresent()) {
                return avMode.get().equals("constv");
            }
            return false;
        }

        private static double minP(DataObject elmSym, double p) {
            Optional<Float> pMinUc = elmSym.findFloatAttributeValue("Pmin_uc");
            if (pMinUc.isPresent()) {
                return pMinUc.get();
            }
            if (p < 0.0) {
                return p;
            }
            return 0.0;
        }

        private static double maxP(DataObject elmSym, double p) {
            Optional<Float> pMaxUc = elmSym.findFloatAttributeValue("Pmax_uc");
            if (pMaxUc.isPresent()) {
                return pMaxUc.get();
            }
            if (p > 0.0) {
                return p;
            }
            return 0.0;
        }
    }

    static class ReactiveLimits {
        private final double minQ;
        private final double maxQ;

        ReactiveLimits(double minQ, double maxQ) {
            this.minQ = minQ;
            this.maxQ = maxQ;
        }

        static Optional<ReactiveLimits> create(DataObject elmSym) {
            Optional<DataObject> pQlimType = elmSym.findObjectAttributeValue("pQlimType").flatMap(DataObjectRef::resolve);
            if (pQlimType.isPresent()) {
                throw new PowsyblException("Reactive capability curve not supported: '" + elmSym + "'");
            }
            Optional<DataObject> typSym = elmSym.findObjectAttributeValue(TYP_ID).flatMap(DataObjectRef::resolve);
            if (typSym.isPresent()) {
                return create(elmSym, typSym.get());
            } else {
                return Optional.empty();
            }
        }

        private static Optional<ReactiveLimits> create(DataObject elmSym, DataObject typSym) {

            OptionalInt iqtype = elmSym.findIntAttributeValue("iqtype");
            Optional<Float> qMinPuElm = elmSym.findFloatAttributeValue("q_min");
            Optional<Float> qMaxPuElm = elmSym.findFloatAttributeValue("q_max");
            Optional<Float> qMinPuTyp = typSym.findFloatAttributeValue("q_min");
            Optional<Float> qMaxPuTyp = typSym.findFloatAttributeValue("q_max");
            Optional<Float> sgn = typSym.findFloatAttributeValue("sgn");
            Optional<Float> qMinMvar = typSym.findFloatAttributeValue("Q_min");
            Optional<Float> qMaxMvar = typSym.findFloatAttributeValue("Q_max");

            // Reactive limits form Elm
            double qMinElm = Double.NaN;
            double qMaxElm = Double.NaN;
            if (qMinPuElm.isPresent() && qMaxPuElm.isPresent() && sgn.isPresent()) {
                qMinElm = qMinPuElm.get() * sgn.get();
                qMaxElm = qMaxPuElm.get() * sgn.get();
            }
            // Reactive limits from Typ
            double qMinTyp = Double.NaN;
            double qMaxTyp = Double.NaN;
            if (qMinMvar.isPresent() && qMaxMvar.isPresent()) {
                qMinTyp = qMinMvar.get();
                qMaxTyp = qMaxMvar.get();
            } else if (qMinPuTyp.isPresent() && qMaxPuTyp.isPresent() && sgn.isPresent()) {
                qMinTyp = qMinPuTyp.get() * sgn.get();
                qMaxTyp = qMaxPuTyp.get() * sgn.get();
            }

            if (iqtype.isPresent() && iqtype.getAsInt() == 0 && !Double.isNaN(qMinElm) && !Double.isNaN(qMaxElm)) {
                return Optional.of(new ReactiveLimits(qMinElm, qMaxElm));
            }
            if (iqtype.isPresent() && iqtype.getAsInt() != 0 && !Double.isNaN(qMinTyp) && !Double.isNaN(qMaxTyp)) {
                return Optional.of(new ReactiveLimits(qMinTyp, qMaxTyp));
            }
            if (!Double.isNaN(qMinElm) && !Double.isNaN(qMaxElm)) {
                return Optional.of(new ReactiveLimits(qMinElm, qMaxElm));
            }
            if (!Double.isNaN(qMinTyp) && !Double.isNaN(qMaxTyp)) {
                return Optional.of(new ReactiveLimits(qMinTyp, qMaxTyp));
            }
            return Optional.empty();
        }
    }
}
