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
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
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
                .setId(getId(elmSym))
                .setEnsureIdUnicity(true)
                .setNode(nodeRef.node)
                .setTargetP(generatorModel.targetP)
                .setTargetQ(generatorModel.targetQ)
                .setTargetV(generatorModel.targetVpu * vl.getNominalV())
                .setVoltageRegulatorOn(generatorModel.voltageRegulatorOn)
                .setMinP(generatorModel.minP)
                .setMaxP(generatorModel.maxP)
                .add();

        Optional<List<CapabilityCurvePoint>> capabitlityCurve = CapabilityCurvePoint.create(elmSym);
        if (capabitlityCurve.isPresent()) {
            ReactiveCapabilityCurveAdder adder = g.newReactiveCapabilityCurve();
            capabitlityCurve.get().forEach(capabitlityCurvePoint -> adder.beginPoint()
                .setP(capabitlityCurvePoint.p)
                .setMinQ(capabitlityCurvePoint.qMin)
                .setMaxQ(capabitlityCurvePoint.qMax)
                .endPoint());
            adder.add();
        } else {
            ReactiveLimits.create(elmSym).ifPresent(reactiveLimits -> g.newMinMaxReactiveLimits()
                .setMinQ(reactiveLimits.minQ)
                .setMaxQ(reactiveLimits.maxQ)
                .add());
        }
    }

    static boolean isSlack(DataObject elmSym) {
        OptionalInt ipCtrl = elmSym.findIntAttributeValue("ip_ctrl");
        if (ipCtrl.isPresent() && ipCtrl.getAsInt() == 1) {
            return true;
        }
        Optional<String> bustp = elmSym.findStringAttributeValue("bustp");
        return bustp.isPresent() && bustp.get().equals("SL");
    }

    private static final class GeneratorModel {
        private final double targetP;
        private final double targetQ;
        private final double targetVpu;
        private final boolean voltageRegulatorOn;
        private final double minP;
        private final double maxP;

        private GeneratorModel(double targetP, double targetQ, double targetVpu, boolean voltageRegulatorOn, double minP, double maxP) {
            this.targetP = targetP;
            this.targetQ = targetQ;
            this.targetVpu = targetVpu;
            this.voltageRegulatorOn = voltageRegulatorOn;
            this.minP = minP;
            this.maxP = maxP;
        }

        private static GeneratorModel create(DataObject elmSym) {
            boolean voltageRegulatorOn = voltageRegulatorOn(elmSym);

            float pgini = elmSym.findFloatAttributeValue("pgini_a").orElse(elmSym.getFloatAttributeValue("pgini"));
            float qgini = elmSym.findFloatAttributeValue("qgini_a").orElse(elmSym.getFloatAttributeValue("qgini"));
            double usetp = elmSym.getFloatAttributeValue("usetp");
            double pMinUc = minP(elmSym, pgini);
            double pMaxUc = maxP(elmSym, pgini);

            return new GeneratorModel(pgini, qgini, usetp, voltageRegulatorOn, pMinUc, pMaxUc);
        }

        private static boolean voltageRegulatorOn(DataObject elmSym) {
            OptionalInt ivMode = elmSym.findIntAttributeValue("iv_mode");
            if (ivMode.isPresent()) {
                return ivMode.getAsInt() == 1;
            }
            return elmSym.findStringAttributeValue("av_mode").map(s -> s.equals("constv")).orElse(false);
        }

        private static double minP(DataObject elmSym, double p) {
            Optional<Float> pMinUc = elmSym.findFloatAttributeValue("Pmin_uc");
            if (pMinUc.isPresent()) {
                return pMinUc.get();
            }
            return Math.min(p, 0.0);
        }

        private static double maxP(DataObject elmSym, double p) {
            Optional<Float> pMaxUc = elmSym.findFloatAttributeValue("Pmax_uc");
            if (pMaxUc.isPresent()) {
                return pMaxUc.get();
            }
            return Math.max(p, 0.0);
        }
    }

    private static final class ReactiveLimits {
        private final double minQ;
        private final double maxQ;

        private ReactiveLimits(double minQ, double maxQ) {
            this.minQ = minQ;
            this.maxQ = maxQ;
        }

        private static Optional<ReactiveLimits> create(DataObject elmSym) {
            Optional<DataObject> pQlimType = elmSym.findObjectAttributeValue("pQlimType").flatMap(DataObjectRef::resolve);
            if (pQlimType.isPresent()) {
                return Optional.empty();
            }
            return elmSym
                    .findObjectAttributeValue(DataAttributeNames.TYP_ID)
                    .flatMap(DataObjectRef::resolve)
                    .flatMap(typSym -> create(elmSym, typSym));
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

    private static final class CapabilityCurvePoint {
        private final double p;
        private final double qMin;
        private final double qMax;

        private CapabilityCurvePoint(double p, double qMin, double qMax) {
            this.p = p;
            this.qMin = qMin;
            this.qMax = qMax;
        }

        private static Optional<List<CapabilityCurvePoint>> create(DataObject elmSym) {
            Optional<DataObject> pQlimType = elmSym.findObjectAttributeValue("pQlimType")
                    .flatMap(DataObjectRef::resolve);
            if (pQlimType.isPresent()) {
                Optional<List<Double>> capP = pQlimType.get().findDoubleVectorAttributeValue("cap_P");
                Optional<List<Double>> capQmn = pQlimType.get().findDoubleVectorAttributeValue("cap_Qmn");
                Optional<List<Double>> capQmx = pQlimType.get().findDoubleVectorAttributeValue("cap_Qmx");
                if (capP.isPresent() && capQmn.isPresent() && capQmx.isPresent()
                        && !capP.get().isEmpty()
                        && capP.get().size() == capQmn.get().size() && capP.get().size() == capQmx.get().size()) {
                    List<CapabilityCurvePoint> capabilityCurve = new ArrayList<>();
                    for (int i = 0; i < capP.get().size(); i++) {
                        capabilityCurve.add(new CapabilityCurvePoint(capP.get().get(i), capQmn.get().get(i), capQmx.get().get(i)));
                    }
                    return Optional.of(capabilityCurve);
                }
            }
            return Optional.empty();
        }
    }

    static String getId(DataObject elmSym) {
        return elmSym.getLocName();
    }
}
