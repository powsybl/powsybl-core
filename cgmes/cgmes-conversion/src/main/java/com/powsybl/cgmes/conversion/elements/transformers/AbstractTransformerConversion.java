/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.extensions.CgmesTapChangersAdder;
import com.powsybl.cgmes.model.WindingType;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import static com.powsybl.cgmes.conversion.Conversion.CGMES_PREFIX_ALIAS_PROPERTIES;
import static com.powsybl.cgmes.model.CgmesNames.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
abstract class AbstractTransformerConversion extends AbstractConductingEquipmentConversion {

    AbstractTransformerConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

    protected static void setToIidmRatioTapChanger(TapChanger rtc, RatioTapChangerAdder rtca) {
        boolean isLtcFlag = rtc.isLtcFlag();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        rtca.setLoadTapChangingCapabilities(isLtcFlag).setLowTapPosition(lowStep).setTapPosition(position);

        rtc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double r = step.getR();
            double x = step.getX();
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            rtca.beginStep()
                    .setRho(1 / ratio)
                    .setR(r)
                    .setX(x)
                    .setB(b1)
                    .setG(g1)
                    .endStep();
        });
        rtca.add();
    }

    protected static void setToIidmPhaseTapChanger(TapChanger ptc, PhaseTapChangerAdder ptca, Context context) {
        int lowStep = ptc.getLowTapPosition();
        int position = ptc.getTapPosition();
        ptca.setLowTapPosition(lowStep).setTapPosition(position);

        ptc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            if (Double.isNaN(x)) {
                context.fixed("ptc.step.x", "ptc.step.x is undefined", x, 0.0);
                x = 0.0;
            }
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            ptca.beginStep()
                    .setRho(1 / ratio)
                    .setAlpha(-angle)
                    .setR(r)
                    .setX(x)
                    .setB(b1)
                    .setG(g1)
                    .endStep();
        });
        ptca.add();
    }

    protected CgmesRegulatingControlRatio setContextRegulatingDataRatio(TapChanger tc) {
        CgmesRegulatingControlRatio rcRtc = null;
        if (tc != null) {
            rcRtc = context.regulatingControlMapping().forTransformers().buildRegulatingControlRatio(tc.getId(),
                    tc.getRegulatingControlId(), tc.getTculControlMode(), tc.isTapChangerControlEnabled());
        }
        return rcRtc;
    }

    protected CgmesRegulatingControlPhase setContextRegulatingDataPhase(TapChanger tc) {
        CgmesRegulatingControlPhase rcPtc = null;
        if (tc != null) {
            return context.regulatingControlMapping().forTransformers().buildRegulatingControlPhase(
                    tc.getId(), tc.getRegulatingControlId(), tc.isTapChangerControlEnabled(), tc.isLtcFlag());
        }
        return rcPtc;
    }

    @Override
    protected void addAliasesAndProperties(Identifiable<?> identifiable) {
        // Add PowerTransformer aliases
        super.addAliasesAndProperties(identifiable);

        // Add PowerTransformerEnds aliases
        String alias;
        String aliasType;
        for (PropertyBag end : ps) {
            alias = end.getId("TransformerEnd");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + TRANSFORMER_END + WindingType.endNumber(end);
            identifiable.addAlias(alias, aliasType);
        }

        // Add RatioTapChangers aliases
        for (PropertyBag rtc : context.ratioTapChangers(identifiable.getId())) {
            alias = rtc.getId("RatioTapChanger");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + RATIO_TAP_CHANGER + WindingType.endNumber(rtc);
            identifiable.addAlias(alias, aliasType, context.config().isEnsureIdAliasUnicity());
        }

        // Add PhaseTapChangers aliases
        for (PropertyBag ptc : context.phaseTapChangers(identifiable.getId())) {
            alias = ptc.getId("PhaseTapChanger");
            aliasType = CGMES_PREFIX_ALIAS_PROPERTIES + PHASE_TAP_CHANGER + WindingType.endNumber(ptc);
            identifiable.addAlias(alias, aliasType, context.config().isEnsureIdAliasUnicity());
        }
    }

    protected static <C extends Connectable<C>> void addCgmesReferences(C transformer, TapChanger tc) {
        if (tc == null || tc.getId() == null) {
            return;
        }
        TapChanger tch = tc.getHiddenCombinedTapChanger();
        if (tc.getRegulatingControlId() != null || tc.getType() != null || tch != null) {
            CgmesTapChangers<C> tapChangers = transformer.getExtension(CgmesTapChangers.class);
            if (tapChangers == null) {
                transformer.newExtension(CgmesTapChangersAdder.class).add();
                tapChangers = transformer.getExtension(CgmesTapChangers.class);
            }
            if (tc.getRegulatingControlId() != null || tc.getType() != null) {
                tapChangers.newTapChanger()
                        .setId(tc.getId())
                        .setType(tc.getType())
                        .setControlId(tc.getRegulatingControlId())
                        .add();
            }
            if (tch != null) {
                tapChangers.newTapChanger()
                        .setId(tch.getId())
                        .setCombinedTapChangerId(tc.getId())
                        .setHiddenStatus(true)
                        .setStep(tch.getTapPosition())
                        .setType(tch.getType())
                        .add();
            }
        }
    }
}
