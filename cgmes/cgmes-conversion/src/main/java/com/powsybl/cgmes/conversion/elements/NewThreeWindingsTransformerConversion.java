/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class NewThreeWindingsTransformerConversion extends AbstractTransformerConversion {

    public NewThreeWindingsTransformerConversion(PropertyBags ends,
        Map<String, PropertyBag> powerTransformerRatioTapChanger,
        Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super(STRING_POWER_TRANSFORMER, ends, context);
        this.powerTransformerRatioTapChanger = powerTransformerRatioTapChanger;
        this.powerTransformerPhaseTapChanger = powerTransformerPhaseTapChanger;
    }

    @Override
    public void convert() {
        CgmesT3xModel cgmesT3xModel = load();
        InterpretedT3xModel interpretedT3xModel = interpret(cgmesT3xModel, context.config());
        ConvertedT3xModel convertedT3xModel = convertToIidm(interpretedT3xModel);
    }

    private CgmesT3xModel load() {
        CgmesT3xModel cgmesT3xModel = new CgmesT3xModel();

        // ends = ps
        loadWinding(ps.get(0), cgmesT3xModel.winding1);
        loadWinding(ps.get(1), cgmesT3xModel.winding2);
        loadWinding(ps.get(2), cgmesT3xModel.winding3);

        return cgmesT3xModel;
    }

    private void loadWinding(PropertyBag winding, CgmesWinding cgmesWindingModel) {
        PropertyBag rtc = getTransformerTapChanger(winding, STRING_RATIO_TAP_CHANGER,
            powerTransformerRatioTapChanger);
        PropertyBag ptc = getTransformerTapChanger(winding, STRING_PHASE_TAP_CHANGER,
            powerTransformerPhaseTapChanger);

        String terminal = winding.getId(CgmesNames.TERMINAL);
        double ratedU = winding.asDouble(STRING_RATEDU);
        double x = winding.asDouble(STRING_X);

        TapChangerConversion ratioTapChanger = getRatioTapChanger(rtc);
        TapChangerConversion phaseTapChanger = getPhaseTapChanger(ptc, x);

        cgmesWindingModel.r = winding.asDouble(STRING_R);
        cgmesWindingModel.x = x;
        cgmesWindingModel.g = winding.asDouble(STRING_G, 0);
        cgmesWindingModel.b = winding.asDouble(STRING_B);
        cgmesWindingModel.ratioTapChanger = ratioTapChanger;
        cgmesWindingModel.phaseTapChanger = phaseTapChanger;
        cgmesWindingModel.ratedU = ratedU;
        cgmesWindingModel.phaseAngleClock = winding.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesWindingModel.terminal = terminal;
    }

    private InterpretedT3xModel interpret(CgmesT3xModel cgmesT3xModel, Conversion.Config alternative) {

        InterpretedT3xModel interpretedT3xModel = new InterpretedT3xModel();
        double ratedUf = ratedUfAlternative(cgmesT3xModel, alternative);
        interpretedT3xModel.ratedUf = ratedUf;

        interpretWinding(cgmesT3xModel.winding1, alternative, interpretedT3xModel.winding1);
        interpretWinding(cgmesT3xModel.winding2, alternative, interpretedT3xModel.winding2);
        interpretWinding(cgmesT3xModel.winding3, alternative, interpretedT3xModel.winding3);

        return interpretedT3xModel;
    }

    private void interpretWinding(CgmesWinding cgmesWindingModel, Conversion.Config alternative, InterpretedWinding interpretedWinding) {

        AllTapChanger windingInterpretedTapChanger = ratioPhaseAlternative(cgmesWindingModel, alternative);
        AllShunt windingInterpretedShunt = shuntAlternative(cgmesWindingModel, alternative);
        AllPhaseAngleClock windingInterpretedClock = phaseAngleClockAlternative(cgmesWindingModel, alternative);
        boolean windingRatio0AtEnd2 = ratio0Alternative(cgmesWindingModel, alternative);

        interpretedWinding.r = cgmesWindingModel.r;
        interpretedWinding.x = cgmesWindingModel.x;
        interpretedWinding.end1.g = windingInterpretedShunt.g1;
        interpretedWinding.end1.b = windingInterpretedShunt.b1;
        interpretedWinding.end1.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger1;
        interpretedWinding.end1.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger1;
        interpretedWinding.end1.phaseAngleClock = windingInterpretedClock.phaseAngleClock1;
        interpretedWinding.end1.ratedU = cgmesWindingModel.ratedU;
        interpretedWinding.end1.terminal = cgmesWindingModel.terminal;

        interpretedWinding.end2.g = windingInterpretedShunt.g2;
        interpretedWinding.end2.b = windingInterpretedShunt.b2;
        interpretedWinding.end2.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger2;
        interpretedWinding.end2.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger2;
        interpretedWinding.end2.phaseAngleClock = windingInterpretedClock.phaseAngleClock2;

        interpretedWinding.ratio0AtEnd2 = windingRatio0AtEnd2;
    }

    private AllTapChanger ratioPhaseAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        TapChangerConversion ratioTapChanger1 = null;
        TapChangerConversion phaseTapChanger1 = null;
        TapChangerConversion ratioTapChanger2 = null;
        TapChangerConversion phaseTapChanger2 = null;

        if (alternative.isXfmr3RatioPhaseNetworkSide()) {
            ratioTapChanger1 = cgmesWinding.ratioTapChanger;
            phaseTapChanger1 = cgmesWinding.phaseTapChanger;
        } else {
            ratioTapChanger2 = cgmesWinding.ratioTapChanger;
            phaseTapChanger2 = cgmesWinding.phaseTapChanger;
        }

        AllTapChanger allTapChanger = new AllTapChanger();
        allTapChanger.ratioTapChanger1 = ratioTapChanger1;
        allTapChanger.phaseTapChanger1 = phaseTapChanger1;
        allTapChanger.ratioTapChanger2 = ratioTapChanger2;
        allTapChanger.phaseTapChanger2 = phaseTapChanger2;

        return allTapChanger;
    }

    private AllShunt shuntAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        double g1 = 0.0;
        double b1 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        if (alternative.isXfmr3ShuntNetworkSide()) {
            g1 = cgmesWinding.g;
            b1 = cgmesWinding.b;
        } else if (alternative.isXfmr3ShuntStarBusSide()) {
            g2 = cgmesWinding.g;
            b2 = cgmesWinding.b;
        } else {
            g1 = cgmesWinding.g * 0.5;
            b1 = cgmesWinding.b * 0.5;
            g2 = cgmesWinding.g * 0.5;
            b2 = cgmesWinding.b * 0.5;
        }

        AllShunt allShunt = new AllShunt();
        allShunt.g1 = g1;
        allShunt.b1 = b1;
        allShunt.g2 = g2;
        allShunt.b2 = b2;

        return allShunt;
    }

    private AllPhaseAngleClock phaseAngleClockAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        int phaseAngleClock1 = 0;
        int phaseAngleClock2 = 0;

        if (cgmesWinding.phaseAngleClock != 0) {
            if (alternative.isXfmr3PhaseAngleClockNetworkSide()) {
                phaseAngleClock1 = cgmesWinding.phaseAngleClock;
            } else if (alternative.isXfmr3PhaseAngleClockStarBusSide()) {
                phaseAngleClock2 = cgmesWinding.phaseAngleClock;
            }
        }

        AllPhaseAngleClock allPhaseAngleClock = new AllPhaseAngleClock();
        allPhaseAngleClock.phaseAngleClock1 = phaseAngleClock1;
        allPhaseAngleClock.phaseAngleClock2 = phaseAngleClock2;

        return allPhaseAngleClock;
    }

    private boolean ratio0Alternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        boolean ratio0AtEnd2;
        if (alternative.isXfmr3Ratio0StarBusSide()) {
            ratio0AtEnd2 = true;
        } else {
            ratio0AtEnd2 = false;
        }
        return ratio0AtEnd2;
    }

    private double ratedUfAlternative(CgmesT3xModel cgmesT3xModel, Conversion.Config alternative) {

        double ratedUf = 1.0;
        if (alternative.isXfmr3Ratio0StarBusSide()) {
            ratedUf = selectRatedUf(cgmesT3xModel);
        } else if (alternative.isXfmr3Ratio0NetworkSide()) {
            ratedUf = 1.0;
        } else if (alternative.isXfmr3Ratio0End1()) {
            ratedUf = cgmesT3xModel.winding1.ratedU;
        } else if (alternative.isXfmr3Ratio0End2()) {
            ratedUf = cgmesT3xModel.winding2.ratedU;
        } else if (alternative.isXfmr3Ratio0End3()) {
            ratedUf = cgmesT3xModel.winding3.ratedU;
        }
        return ratedUf;
    }

    // Select the ratedUf voltage
    private double selectRatedUf(CgmesT3xModel cgmesT3xModel) {
        return cgmesT3xModel.winding1.ratedU;
    }

    private ConvertedT3xModel convertToIidm(InterpretedT3xModel interpretedT3xModel) {

        double ratedUf = interpretedT3xModel.ratedUf;
        ConvertedT3xModel convertedModel = new ConvertedT3xModel();

        convertToIidmWinding(interpretedT3xModel.winding1, convertedModel.winding1, ratedUf);
        convertToIidmWinding(interpretedT3xModel.winding2, convertedModel.winding2, ratedUf);
        convertToIidmWinding(interpretedT3xModel.winding3, convertedModel.winding3, ratedUf);

        convertedModel.ratedUf = ratedUf;

        return convertedModel;
    }

    private void convertToIidmWinding(InterpretedWinding interpretedWinding, ConvertedWinding convertedWinding, double ratedUf) {
        TapChangerWinding windingTapChanger = moveCombineTapChangerWinding(interpretedWinding);

        RatioConversion windingRc0 = rc0Winding(interpretedWinding, ratedUf);

        convertedWinding.r = windingRc0.r;
        convertedWinding.x = windingRc0.x;
        convertedWinding.end1.g = windingRc0.g1;
        convertedWinding.end1.b = windingRc0.b1;
        convertedWinding.end1.ratioTapChanger = windingTapChanger.ratioTapChanger;
        convertedWinding.end1.phaseTapChanger = windingTapChanger.phaseTapChanger;
        convertedWinding.end1.phaseAngleClock = interpretedWinding.end1.phaseAngleClock;
        convertedWinding.end1.ratedU = interpretedWinding.end1.ratedU;
        convertedWinding.end1.terminal = interpretedWinding.end1.terminal;
        convertedWinding.end2.g = windingRc0.g2;
        convertedWinding.end2.b = windingRc0.b2;
        convertedWinding.end2.phaseAngleClock = interpretedWinding.end2.phaseAngleClock;
    }

    private RatioConversion rc0Winding(InterpretedWinding interpretedWinding, double ratedUf) {
        RatioConversion rc0;
        // IIDM: Structural ratio always at network side
        if (interpretedWinding.ratio0AtEnd2) {
            double a0 = ratedUf / interpretedWinding.end1.ratedU;
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedWinding.r, interpretedWinding.x,
                interpretedWinding.end1.g, interpretedWinding.end1.b,
                interpretedWinding.end2.g, interpretedWinding.end2.b);
        } else {
            rc0 = identityRatioConversion(interpretedWinding.r, interpretedWinding.x,
                interpretedWinding.end1.g, interpretedWinding.end1.b,
                interpretedWinding.end2.g, interpretedWinding.end2.b);
        }

        return rc0;
    }

    private TapChangerWinding moveCombineTapChangerWinding(InterpretedWinding interpretedWinding) {

        TapChangerConversion nRatioTapChanger = moveTapChangerFrom2To1(interpretedWinding.end2.ratioTapChanger);
        TapChangerConversion nPhaseTapChanger = moveTapChangerFrom2To1(interpretedWinding.end2.phaseTapChanger);

        TapChangerConversion cRatioTapChanger = combineTapChangers(interpretedWinding.end1.ratioTapChanger, nRatioTapChanger);
        TapChangerConversion cPhaseTapChanger = combineTapChangers(interpretedWinding.end1.phaseTapChanger, nPhaseTapChanger);

        TapChangerWinding tapChangerWinding = new TapChangerWinding();
        tapChangerWinding.ratioTapChanger = cRatioTapChanger;
        tapChangerWinding.phaseTapChanger = cPhaseTapChanger;
        return tapChangerWinding;
    }

    static class CgmesT3xModel {
        CgmesWinding winding1 = new CgmesWinding();
        CgmesWinding winding2 = new CgmesWinding();
        CgmesWinding winding3 = new CgmesWinding();
    }

    static class CgmesWinding {
        double r;
        double x;
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        int phaseAngleClock;
        String terminal;
    }

    static class InterpretedT3xModel {
        InterpretedWinding winding1 = new InterpretedWinding();
        InterpretedWinding winding2 = new InterpretedWinding();
        InterpretedWinding winding3 = new InterpretedWinding();
        double ratedUf;
    }

    // 1 network side, 2 start bus side
    static class InterpretedWinding {
        double r;
        double x;
        InterpretedEnd1 end1 = new InterpretedEnd1();
        InterpretedEnd2 end2 = new InterpretedEnd2();
        boolean ratio0AtEnd2;
    }

    static class InterpretedEnd1 {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    static class InterpretedEnd2 {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        int phaseAngleClock;
    }

    static class ConvertedT3xModel {
        ConvertedWinding winding1 = new ConvertedWinding();
        ConvertedWinding winding2 = new ConvertedWinding();
        ConvertedWinding winding3 = new ConvertedWinding();
        double ratedUf;
    }

    // 1 network side, 2 start bus side
    static class ConvertedWinding {
        double r;
        double x;
        ConvertedEnd1 end1 = new ConvertedEnd1();
        ConvertedEnd2 end2 = new ConvertedEnd2();
    }

    static class ConvertedEnd2 {
        double g;
        double b;
        int phaseAngleClock;
    }

    static class TapChangerWinding {
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
    }

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
