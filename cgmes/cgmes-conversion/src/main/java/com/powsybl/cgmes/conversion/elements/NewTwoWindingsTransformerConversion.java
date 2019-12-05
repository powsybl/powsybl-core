/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class NewTwoWindingsTransformerConversion extends AbstractTransformerConversion {

    public NewTwoWindingsTransformerConversion(PropertyBags ends,
        Map<String, PropertyBag> powerTransformerRatioTapChanger,
        Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super(STRING_POWER_TRANSFORMER, ends, context);
        this.powerTransformerRatioTapChanger = powerTransformerRatioTapChanger;
        this.powerTransformerPhaseTapChanger = powerTransformerPhaseTapChanger;
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (context.boundary().containsNode(nodeId(1))
            || context.boundary().containsNode(nodeId(2))) {
            invalid("2 windings transformer end point at boundary is not supported");
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        CgmesT2xModel cgmesT2xModel = load();
        InterpretedT2xModel interpretedT2xModel = interpret(cgmesT2xModel, context.config());
        ConvertedT2xModel convertedT2xModel = convertToIidm(interpretedT2xModel);

        setToIidm(convertedT2xModel);
    }

    private CgmesT2xModel load() {
        // ends = ps
        PropertyBag end1 = ps.get(0);
        PropertyBag end2 = ps.get(1);

        double x1 = end1.asDouble(STRING_X);
        double x2 = end2.asDouble(STRING_X);
        double r = end1.asDouble(STRING_R) + end2.asDouble(STRING_R);
        double x = x1 + x2;

        String terminal1 = end1.getId(CgmesNames.TERMINAL);
        String terminal2 = end2.getId(CgmesNames.TERMINAL);

        PropertyBag rtc1 = getTransformerTapChanger(end1, STRING_RATIO_TAP_CHANGER, powerTransformerRatioTapChanger);
        PropertyBag ptc1 = getTransformerTapChanger(end1, STRING_PHASE_TAP_CHANGER, powerTransformerPhaseTapChanger);
        PropertyBag rtc2 = getTransformerTapChanger(end2, STRING_RATIO_TAP_CHANGER, powerTransformerRatioTapChanger);
        PropertyBag ptc2 = getTransformerTapChanger(end2, STRING_PHASE_TAP_CHANGER, powerTransformerPhaseTapChanger);

        double ratedU1 = end1.asDouble(STRING_RATEDU);
        double ratedU2 = end2.asDouble(STRING_RATEDU);

        TapChangerConversion ratioTapChanger1 = getRatioTapChanger(rtc1);
        TapChangerConversion ratioTapChanger2 = getRatioTapChanger(rtc2);
        TapChangerConversion phaseTapChanger1 = getPhaseTapChanger(ptc1, x);
        TapChangerConversion phaseTapChanger2 = getPhaseTapChanger(ptc2, x);

        CgmesT2xModel cgmesT2xModel = new CgmesT2xModel();
        cgmesT2xModel.end1.g = end1.asDouble(STRING_G, 0);
        cgmesT2xModel.end1.b = end1.asDouble(STRING_B);
        cgmesT2xModel.end1.ratioTapChanger = ratioTapChanger1;
        cgmesT2xModel.end1.phaseTapChanger = phaseTapChanger1;
        cgmesT2xModel.end1.ratedU = ratedU1;
        cgmesT2xModel.end1.phaseAngleClock = end1.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesT2xModel.end1.terminal = terminal1;

        if (x1 == 0.0) {
            cgmesT2xModel.end1.xIsZero = true;
        } else {
            cgmesT2xModel.end1.xIsZero = false;
        }

        cgmesT2xModel.end2.g = end2.asDouble(STRING_G, 0);
        cgmesT2xModel.end2.b = end2.asDouble(STRING_B);
        cgmesT2xModel.end2.ratioTapChanger = ratioTapChanger2;
        cgmesT2xModel.end2.phaseTapChanger = phaseTapChanger2;
        cgmesT2xModel.end2.ratedU = ratedU2;
        cgmesT2xModel.end2.phaseAngleClock = end2.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesT2xModel.end2.terminal = terminal2;

        if (x2 == 0.0) {
            cgmesT2xModel.end2.xIsZero = true;
        } else {
            cgmesT2xModel.end2.xIsZero = false;
        }

        cgmesT2xModel.r = r;
        cgmesT2xModel.x = x;

        return cgmesT2xModel;
    }

    private InterpretedT2xModel interpret(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {

        AllTapChanger interpretedTapChanger = ratioPhaseAlternative(cgmesT2xModel, alternative);
        AllShunt interpretedShunt = shuntAlternative(cgmesT2xModel, alternative);

        AllPhaseAngleClock interpretedClock = phaseAngleClockAlternative(cgmesT2xModel, alternative);
        boolean ratio0AtEnd2 = ratio0Alternative(cgmesT2xModel, alternative);

        InterpretedT2xModel interpretedT2xModel = new InterpretedT2xModel();
        interpretedT2xModel.r = cgmesT2xModel.r;
        interpretedT2xModel.x = cgmesT2xModel.x;

        interpretedT2xModel.end1.g = interpretedShunt.g1;
        interpretedT2xModel.end1.b = interpretedShunt.b1;
        interpretedT2xModel.end1.ratioTapChanger = interpretedTapChanger.ratioTapChanger1;
        interpretedT2xModel.end1.phaseTapChanger = interpretedTapChanger.phaseTapChanger1;
        interpretedT2xModel.end1.ratedU = cgmesT2xModel.end1.ratedU;
        interpretedT2xModel.end1.terminal = cgmesT2xModel.end1.terminal;
        interpretedT2xModel.end1.phaseAngleClock = interpretedClock.phaseAngleClock1;

        interpretedT2xModel.end2.g = interpretedShunt.g2;
        interpretedT2xModel.end2.b = interpretedShunt.b2;
        interpretedT2xModel.end2.ratioTapChanger = interpretedTapChanger.ratioTapChanger2;
        interpretedT2xModel.end2.phaseTapChanger = interpretedTapChanger.phaseTapChanger2;
        interpretedT2xModel.end2.ratedU = cgmesT2xModel.end2.ratedU;
        interpretedT2xModel.end2.terminal = cgmesT2xModel.end2.terminal;
        interpretedT2xModel.end2.phaseAngleClock = interpretedClock.phaseAngleClock2;

        interpretedT2xModel.ratio0AtEnd2 = ratio0AtEnd2;

        return interpretedT2xModel;
    }

    private AllTapChanger ratioPhaseAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        TapChangerConversion ratioTapChanger1 = null;
        TapChangerConversion phaseTapChanger1 = null;
        TapChangerConversion ratioTapChanger2 = null;
        TapChangerConversion phaseTapChanger2 = null;

        if (alternative.isXfmr2RatioPhaseEnd1()) {
            ratioTapChanger1 = combineTapChangers(cgmesT2xModel.end1.ratioTapChanger, cgmesT2xModel.end2.ratioTapChanger);
            phaseTapChanger1 = combineTapChangers(cgmesT2xModel.end1.phaseTapChanger, cgmesT2xModel.end2.phaseTapChanger);
        } else if (alternative.isXfmr2RatioPhaseEnd2()) {
            ratioTapChanger2 = combineTapChangers(cgmesT2xModel.end2.ratioTapChanger, cgmesT2xModel.end1.ratioTapChanger);
            phaseTapChanger2 = combineTapChangers(cgmesT2xModel.end2.phaseTapChanger, cgmesT2xModel.end1.phaseTapChanger);
        } else if (alternative.isXfmr2RatioPhaseEnd1End2()) {
            ratioTapChanger1 = cgmesT2xModel.end1.ratioTapChanger;
            phaseTapChanger1 = cgmesT2xModel.end1.phaseTapChanger;
            ratioTapChanger2 = cgmesT2xModel.end2.ratioTapChanger;
            phaseTapChanger2 = cgmesT2xModel.end2.phaseTapChanger;
        } else {
            if (cgmesT2xModel.end1.xIsZero) {
                ratioTapChanger1 = combineTapChangers(cgmesT2xModel.end1.ratioTapChanger, cgmesT2xModel.end2.ratioTapChanger);
                phaseTapChanger1 = combineTapChangers(cgmesT2xModel.end1.phaseTapChanger, cgmesT2xModel.end2.phaseTapChanger);
            } else {
                ratioTapChanger2 = combineTapChangers(cgmesT2xModel.end2.ratioTapChanger, cgmesT2xModel.end1.ratioTapChanger);
                phaseTapChanger2 = combineTapChangers(cgmesT2xModel.end2.phaseTapChanger, cgmesT2xModel.end1.phaseTapChanger);
            }
        }

        if (alternative.isXfmr2PhaseNegate()) {
            negatePhaseTapChanger(phaseTapChanger1);
            negatePhaseTapChanger(phaseTapChanger2);
        }

        AllTapChanger allTapChanger = new AllTapChanger();
        allTapChanger.ratioTapChanger1 = ratioTapChanger1;
        allTapChanger.phaseTapChanger1 = phaseTapChanger1;
        allTapChanger.ratioTapChanger2 = ratioTapChanger2;
        allTapChanger.phaseTapChanger2 = phaseTapChanger2;

        return allTapChanger;
    }

    private AllShunt shuntAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        double g1 = 0.0;
        double b1 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        if (alternative.isXfmr2ShuntEnd1()) {
            g1 = cgmesT2xModel.end1.g + cgmesT2xModel.end2.g;
            b1 = cgmesT2xModel.end1.b + cgmesT2xModel.end2.b;
        } else if (alternative.isXfmr2ShuntEnd2()) {
            g2 = cgmesT2xModel.end1.g + cgmesT2xModel.end2.g;
            b2 = cgmesT2xModel.end1.b + cgmesT2xModel.end2.b;
        } else if (alternative.isXfmr2ShuntEnd1End2()) {
            g1 = cgmesT2xModel.end1.g;
            b1 = cgmesT2xModel.end1.b;
            g2 = cgmesT2xModel.end2.g;
            b2 = cgmesT2xModel.end2.b;
        } else {
            g1 = (cgmesT2xModel.end1.g + cgmesT2xModel.end2.g) * 0.5;
            b1 = (cgmesT2xModel.end1.b + cgmesT2xModel.end2.b) * 0.5;
            g2 = (cgmesT2xModel.end1.g + cgmesT2xModel.end2.g) * 0.5;
            b2 = (cgmesT2xModel.end1.b + cgmesT2xModel.end2.b) * 0.5;
        }

        AllShunt allShunt = new AllShunt();
        allShunt.g1 = g1;
        allShunt.b1 = b1;
        allShunt.g2 = g2;
        allShunt.b2 = b2;

        return allShunt;
    }

    private AllPhaseAngleClock phaseAngleClockAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        int phaseAngleClock1 = 0;
        int phaseAngleClock2 = 0;

        if (alternative.isXfmr2PhaseAngleClockOn()) {
            phaseAngleClock1 = cgmesT2xModel.end1.phaseAngleClock;
            phaseAngleClock2 = cgmesT2xModel.end2.phaseAngleClock;
        }

        AllPhaseAngleClock allPhaseAngleClock = new AllPhaseAngleClock();
        allPhaseAngleClock.phaseAngleClock1 = phaseAngleClock1;
        allPhaseAngleClock.phaseAngleClock2 = phaseAngleClock2;

        return allPhaseAngleClock;
    }

    private boolean ratio0Alternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        if (cgmesT2xModel.end1.ratedU == cgmesT2xModel.end2.ratedU) {
            return false;
        }

        boolean ratio0AtEnd2 = false;
        if (alternative.isXfmr2Ratio0End1()) {
            ratio0AtEnd2 = false;
        } else if (alternative.isXfmr2Ratio0End2()) {
            ratio0AtEnd2 = true;
        } else {
            if (cgmesT2xModel.end1.xIsZero) {
                ratio0AtEnd2 = false;
            } else {
                ratio0AtEnd2 = true;
            }
        }
        return ratio0AtEnd2;
    }

    private ConvertedT2xModel convertToIidm(InterpretedT2xModel interpretedT2xModel) {

        TapChangerConversion nRatioTapChanger2 = moveTapChangerFrom2To1(interpretedT2xModel.end2.ratioTapChanger);
        TapChangerConversion nPhaseTapChanger2 = moveTapChangerFrom2To1(interpretedT2xModel.end2.phaseTapChanger);

        TapChangerConversion ratioTapChanger = combineTapChangers(interpretedT2xModel.end1.ratioTapChanger, nRatioTapChanger2);
        TapChangerConversion phaseTapChanger = combineTapChangers(interpretedT2xModel.end1.phaseTapChanger, nPhaseTapChanger2);

        RatioConversion rc0;
        if (interpretedT2xModel.ratio0AtEnd2) {
            double a0 = interpretedT2xModel.end2.ratedU / interpretedT2xModel.end1.ratedU;
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedT2xModel.r, interpretedT2xModel.x,
                interpretedT2xModel.end1.g, interpretedT2xModel.end1.b,
                interpretedT2xModel.end2.g, interpretedT2xModel.end2.b);
        } else {
            rc0 = identityRatioConversion(interpretedT2xModel.r, interpretedT2xModel.x,
                interpretedT2xModel.end1.g, interpretedT2xModel.end1.b,
                interpretedT2xModel.end2.g, interpretedT2xModel.end2.b);
        }
        ConvertedT2xModel convertedModel = new ConvertedT2xModel();

        convertedModel.r = rc0.r;
        convertedModel.x = rc0.x;

        convertedModel.end1.g = rc0.g1;
        convertedModel.end1.b = rc0.b1;
        convertedModel.end1.ratioTapChanger = ratioTapChanger;
        convertedModel.end1.phaseTapChanger = phaseTapChanger;
        convertedModel.end1.ratedU = interpretedT2xModel.end1.ratedU;
        convertedModel.end1.terminal = interpretedT2xModel.end1.terminal;
        convertedModel.end1.phaseAngleClock = interpretedT2xModel.end1.phaseAngleClock;

        convertedModel.end2.g = rc0.g2;
        convertedModel.end2.b = rc0.b2;
        convertedModel.end2.ratedU = interpretedT2xModel.end2.ratedU;
        convertedModel.end2.terminal = interpretedT2xModel.end2.terminal;
        convertedModel.end2.phaseAngleClock = interpretedT2xModel.end2.phaseAngleClock;

        return convertedModel;
    }

    private void setToIidm(ConvertedT2xModel convertedT2xModel) {
        TwoWindingsTransformerAdder adder = substation().newTwoWindingsTransformer()
            .setR(convertedT2xModel.r)
            .setX(convertedT2xModel.x)
            .setG(convertedT2xModel.end1.g + convertedT2xModel.end2.g)
            .setB(convertedT2xModel.end1.b + convertedT2xModel.end2.b)
            .setRatedU1(convertedT2xModel.end1.ratedU)
            .setRatedU2(convertedT2xModel.end2.ratedU);
        identify(adder);
        connect(adder);
        TwoWindingsTransformer tx = adder.add();
        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        setToIidmRatioTapChanger(convertedT2xModel, tx);
        setToIidmPhaseTapChanger(convertedT2xModel, tx);

        setToIidmPhaseAngleClock(convertedT2xModel, tx);
        setRegulatingControlContext(convertedT2xModel, tx);
    }

    private void setToIidmRatioTapChanger(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        TapChangerConversion rtc = convertedT2xModel.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(tx);
        setToIidmRatioTapChanger(rtc, rtca);
    }

    private void setToIidmPhaseTapChanger(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        TapChangerConversion ptc = convertedT2xModel.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(tx);
        setToIidmPhaseTapChanger(ptc, ptca);
    }

    protected RatioTapChangerAdder newRatioTapChanger(TwoWindingsTransformer tx) {
        return tx.newRatioTapChanger();
    }

    protected PhaseTapChangerAdder newPhaseTapChanger(TwoWindingsTransformer tx) {
        return tx.newPhaseTapChanger();
    }

    private void setToIidmPhaseAngleClock(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        // add phaseAngleClock as an extension, cgmes does not allow pac at end1
        if (convertedT2xModel.end1.phaseAngleClock != 0) {
            String reason = "Unsupported modelling: twoWindingsTransformer with phaseAngleClock at end1";
            ignored("phaseAngleClock end1", reason);
        }
        if (convertedT2xModel.end2.phaseAngleClock != 0) {
            TwoWindingsTransformerPhaseAngleClock phaseAngleClock = new TwoWindingsTransformerPhaseAngleClock(tx,
                convertedT2xModel.end2.phaseAngleClock);
            tx.addExtension(TwoWindingsTransformerPhaseAngleClock.class, phaseAngleClock);
        }
    }

    private void setRegulatingControlContext(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        CgmesRegulatingControlRatio rcRtc = setContextRegulatingDataRatio(convertedT2xModel.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc = setContextRegulatingDataPhase(convertedT2xModel.end1.phaseTapChanger);

        context.regulatingControlMapping().forTransformers().add(tx.getId(), rcRtc, rcPtc);
    }

    static class CgmesT2xModel {
        double r;
        double x;
        CgmesEnd end1 = new CgmesEnd();
        CgmesEnd end2 = new CgmesEnd();
    }

    static class CgmesEnd {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        int phaseAngleClock;
        String terminal;
        boolean xIsZero;
    }

    static class InterpretedT2xModel {
        double r;
        double x;
        InterpretedEnd end1 = new InterpretedEnd();
        InterpretedEnd end2 = new InterpretedEnd();
        boolean ratio0AtEnd2;
    }

    static class InterpretedEnd {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    static class ConvertedT2xModel {
        double r;
        double x;
        ConvertedEnd1 end1 = new ConvertedEnd1();
        ConvertedEnd2 end2 = new ConvertedEnd2();
    }

    static class ConvertedEnd2 {
        double g;
        double b;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
