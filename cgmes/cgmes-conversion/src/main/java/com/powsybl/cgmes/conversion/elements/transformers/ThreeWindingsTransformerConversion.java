/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.conversion.elements.OperationalLimitConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * ThreeWindingsTransformer Cgmes Conversion
 * <p>
 * Cgmes conversion for transformers (two and three windings) is divided into four stages: load, interpret, convert and set.
 * <p>
 * Load <br>
 * Native CGMES data is loaded from the triple store query and is put in the CGMES model object (CgmesT3xModel).
 * <p>
 * Interpret <br>
 * CgmesT3xModel data is mapped to a more general three windings transformer model (InterpretedT3xModel)
 * according to a predefined configured alternative. It is an elemental process as the only objective is to put
 * Cgmes data in the fields of the general three windings transformer model.
 * All possible alternatives and the default one are defined in conversion class. See {@link Conversion} <br>
 * InterpretedT3xModel supports ratioTapChanger and phaseTapChanger at each end of any leg. Shunt admittances
 * can also be defined at both ends of each leg and allows to specify the end of the structural ratio by leg.
 * <p>
 * Convert <br>
 * Converts the interpreted model (InterpretedT3xModel) to the converted model object (ConvertedT3xModel). <br>
 * The ConvertedT3xModel only allows to define ratioTapChanger and phaseTapChanger at the network side of any leg.
 * Shunt admittances and structural ratio must be also at the network side. <br>
 * To do this process the following methods are applied to each leg: <br>
 * moveTapChangerFrom2To1: To move a tapChanger from star bus side to network side <br>
 * combineTapChanger: To reduce two tapChangers to one <br>
 * moveRatioFrom2To1: To move structural ratio from star bus side to network side <br>
 * Finally shunt admittance of both ends of the leg are added to network side. This step is an approximation and only
 * will be possible to reproduce the exact case result if Cgmes shunts are defined at network side or
 * are split and the LoadflowParameter splitShuntAdmittance option is selected. <br>
 * See {@link TapChangerConversion}
 * <p>
 * Set <br>
 * A direct map from ConvertedT3xModel to IIDM model
 * <p>
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ThreeWindingsTransformerConversion extends AbstractTransformerConversion {

    public ThreeWindingsTransformerConversion(PropertyBags ends, Context context) {
        super(CgmesNames.POWER_TRANSFORMER, ends, context);
    }

    @Override
    public void convert() {
        CgmesT3xModel cgmesT3xModel = new CgmesT3xModel(ps, context);
        InterpretedT3xModel interpretedT3xModel = new InterpretedT3xModel(cgmesT3xModel, context.config());
        ConvertedT3xModel convertedT3xModel = new ConvertedT3xModel(interpretedT3xModel, context);

        setToIidm(convertedT3xModel);
    }

    public static void calculateVoltageAndAngleInStarBus(ThreeWindingsTransformer twt) {
        ThreeWindingsTransformerPhaseAngleClock phaseAngleClock = twt.getExtensionByName("threeWindingsTransformerPhaseAngleClock");
        int phaseAngleClock2 = 0;
        int phaseAngleClock3 = 0;
        if (phaseAngleClock != null) {
            phaseAngleClock2 = phaseAngleClock.getPhaseAngleClockLeg2();
            phaseAngleClock3 = phaseAngleClock.getPhaseAngleClockLeg3();
        }
        boolean splitShuntAdmittance = false;
        TwtData twtData = new TwtData(twt, phaseAngleClock2, phaseAngleClock3, 0.0, false, splitShuntAdmittance);

        double starBusV = twtData.getStarU();
        double starBusTheta = Math.toDegrees(twtData.getStarTheta());

        if (!Double.isNaN(starBusV) && !Double.isNaN(starBusTheta)) {
            twt.setProperty("v", Double.toString(starBusV));
            twt.setProperty("angle", Double.toString(starBusTheta));
        }
    }

    private void setToIidm(ConvertedT3xModel convertedT3xModel) {
        ThreeWindingsTransformerAdder txadder = substation()
                .map(Substation::newThreeWindingsTransformer)
                .orElseThrow(() -> new PowsyblException("Substation null! Transformer must be within a substation"))
                .setRatedU0(convertedT3xModel.ratedU0);
        identify(txadder);

        LegAdder l1adder = txadder.newLeg1();
        setToIidmWindingAdder(convertedT3xModel.winding1, l1adder);
        connectWithOnlyEq(l1adder, 1);
        l1adder.add();

        LegAdder l2adder = txadder.newLeg2();
        setToIidmWindingAdder(convertedT3xModel.winding2, l2adder);
        connectWithOnlyEq(l2adder, 2);
        l2adder.add();

        LegAdder l3adder = txadder.newLeg3();
        setToIidmWindingAdder(convertedT3xModel.winding3, l3adder);
        connectWithOnlyEq(l3adder, 3);
        l3adder.add();

        ThreeWindingsTransformer tx = txadder.add();
        addAliasesAndProperties(tx);
        convertedTerminalsWithOnlyEq(
            tx.getLeg1().getTerminal(),
            tx.getLeg2().getTerminal(),
            tx.getLeg3().getTerminal());

        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding1, tx, context);
        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding2, tx, context);
        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding3, tx, context);

        setRegulatingControlContext(convertedT3xModel, tx);
        addCgmesReferences(tx, convertedT3xModel.winding1.end1.ratioTapChanger);
        addCgmesReferences(tx, convertedT3xModel.winding1.end1.phaseTapChanger);
        addCgmesReferences(tx, convertedT3xModel.winding2.end1.ratioTapChanger);
        addCgmesReferences(tx, convertedT3xModel.winding2.end1.phaseTapChanger);
        addCgmesReferences(tx, convertedT3xModel.winding3.end1.ratioTapChanger);
        addCgmesReferences(tx, convertedT3xModel.winding3.end1.phaseTapChanger);
    }

    private static void setToIidmWindingAdder(ConvertedT3xModel.ConvertedWinding convertedModelWinding, LegAdder ladder) {
        ladder.setR(convertedModelWinding.r)
            .setX(convertedModelWinding.x)
            .setG(convertedModelWinding.end1.g)
            .setB(convertedModelWinding.end1.b)
            .setRatedU(convertedModelWinding.end1.ratedU);
        if (convertedModelWinding.ratedS != null) {
            ladder.setRatedS(convertedModelWinding.ratedS);
        }
    }

    private static void setToIidmWindingTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedT3xModel.ConvertedWinding convertedModelWinding,
        ThreeWindingsTransformer tx, Context context) {
        setToIidmRatioTapChanger(convertedT3xModel, convertedModelWinding, tx);
        setToIidmPhaseTapChanger(convertedT3xModel, convertedModelWinding, tx, context);
    }

    private static void setToIidmRatioTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedT3xModel.ConvertedWinding convertedWinding, ThreeWindingsTransformer tx) {
        TapChanger rtc = convertedWinding.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(convertedT3xModel, tx, convertedWinding.end1.terminal);
        if (rtca != null) {
            setToIidmRatioTapChanger(rtc, rtca);
        }
    }

    private static void setToIidmPhaseTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedT3xModel.ConvertedWinding convertedWinding, ThreeWindingsTransformer tx, Context context) {
        TapChanger ptc = convertedWinding.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(convertedT3xModel, tx, convertedWinding.end1.terminal);
        if (ptca != null) {
            setToIidmPhaseTapChanger(ptc, ptca, context);
        }
    }

    private static RatioTapChangerAdder newRatioTapChanger(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx,
        String terminal) {
        if (convertedT3xModel.winding1.end1.terminal.equals(terminal)) {
            return tx.getLeg1().newRatioTapChanger();
        } else if (convertedT3xModel.winding2.end1.terminal.equals(terminal)) {
            return tx.getLeg2().newRatioTapChanger();
        } else if (convertedT3xModel.winding3.end1.terminal.equals(terminal)) {
            return tx.getLeg3().newRatioTapChanger();
        }
        return null;
    }

    private static PhaseTapChangerAdder newPhaseTapChanger(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx,
        String terminal) {
        if (convertedT3xModel.winding1.end1.terminal.equals(terminal)) {
            return tx.getLeg1().newPhaseTapChanger();
        } else if (convertedT3xModel.winding2.end1.terminal.equals(terminal)) {
            return tx.getLeg2().newPhaseTapChanger();
        } else if (convertedT3xModel.winding3.end1.terminal.equals(terminal)) {
            return tx.getLeg3().newPhaseTapChanger();
        }
        return null;
    }

    private void setRegulatingControlContext(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx) {
        CgmesRegulatingControlRatio rcRtc1 = setContextRegulatingDataRatio(convertedT3xModel.winding1.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc1 = setContextRegulatingDataPhase(convertedT3xModel.winding1.end1.phaseTapChanger);
        CgmesRegulatingControlRatio rcRtc2 = setContextRegulatingDataRatio(convertedT3xModel.winding2.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc2 = setContextRegulatingDataPhase(convertedT3xModel.winding2.end1.phaseTapChanger);
        CgmesRegulatingControlRatio rcRtc3 = setContextRegulatingDataRatio(convertedT3xModel.winding3.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc3 = setContextRegulatingDataPhase(convertedT3xModel.winding3.end1.phaseTapChanger);

        context.regulatingControlMapping().forTransformers().add(tx.getId(), rcRtc1, rcPtc1, rcRtc2, rcPtc2, rcRtc3, rcPtc3);
    }

    public static void update(ThreeWindingsTransformer t3w, Context context) {
        updateTerminals(t3w, context, t3w.getLeg1().getTerminal(), t3w.getLeg2().getTerminal(), t3w.getLeg3().getTerminal());

        boolean isAllowedToRegulatePtc1 = true;
        t3w.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> updatePhaseTapChanger(t3w, ptc, "1", context, isAllowedToRegulatePtc1));
        boolean isAllowedToRegulateRtc1 = checkOnlyOneEnabled(isAllowedToRegulatePtc1, t3w.getLeg1().getOptionalPhaseTapChanger().map(com.powsybl.iidm.network.TapChanger::isRegulating).orElse(false));
        t3w.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> updateRatioTapChanger(t3w, rtc, "1", context, isAllowedToRegulateRtc1));

        boolean isAllowedToRegulatePtc2 = checkOnlyOneEnabled(isAllowedToRegulateRtc1, t3w.getLeg1().getOptionalRatioTapChanger().map(com.powsybl.iidm.network.TapChanger::isRegulating).orElse(false));
        t3w.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> updatePhaseTapChanger(t3w, ptc, "2", context, isAllowedToRegulatePtc2));
        boolean isAllowedToRegulateRtc2 = checkOnlyOneEnabled(isAllowedToRegulatePtc2, t3w.getLeg2().getOptionalPhaseTapChanger().map(com.powsybl.iidm.network.TapChanger::isRegulating).orElse(false));
        t3w.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> updateRatioTapChanger(t3w, rtc, "2", context, isAllowedToRegulateRtc2));

        boolean isAllowedToRegulatePtc3 = checkOnlyOneEnabled(isAllowedToRegulateRtc2, t3w.getLeg2().getOptionalRatioTapChanger().map(com.powsybl.iidm.network.TapChanger::isRegulating).orElse(false));
        t3w.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> updatePhaseTapChanger(t3w, ptc, "3", context, isAllowedToRegulatePtc3));
        boolean isAllowedToRegulateRtc3 = checkOnlyOneEnabled(isAllowedToRegulatePtc3, t3w.getLeg3().getOptionalPhaseTapChanger().map(com.powsybl.iidm.network.TapChanger::isRegulating).orElse(false));
        t3w.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> updateRatioTapChanger(t3w, rtc, "3", context, isAllowedToRegulateRtc3));

        t3w.getLeg1().getOperationalLimitsGroups().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(operationalLimitsGroup, "1", t3w, context));
        t3w.getLeg2().getOperationalLimitsGroups().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(operationalLimitsGroup, "2", t3w, context));
        t3w.getLeg3().getOperationalLimitsGroups().forEach(operationalLimitsGroup -> OperationalLimitConversion.update(operationalLimitsGroup, "3", t3w, context));
    }
}
