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
    }

    private CgmesT3xModel load() {
        CgmesT3xModel cgmesT3xModel = new CgmesT3xModel();

        // ends = ps
        loadWinding(ps.get(0), cgmesT3xModel.winding1);
        loadWinding(ps.get(1), cgmesT3xModel.winding2);
        loadWinding(ps.get(2), cgmesT3xModel.winding3);

        return cgmesT3xModel;
    }

    private void loadWinding(PropertyBag winding, CgmesWinding cgmesWinding) {
        PropertyBag rtc = getTransformerTapChanger(winding, STRING_RATIO_TAP_CHANGER,
            powerTransformerRatioTapChanger);
        PropertyBag ptc = getTransformerTapChanger(winding, STRING_PHASE_TAP_CHANGER,
            powerTransformerPhaseTapChanger);

        String terminal = winding.getId(CgmesNames.TERMINAL);
        double ratedU = winding.asDouble(STRING_RATEDU);
        double x = winding.asDouble(STRING_X);

        TapChangerConversion ratioTapChanger = getRatioTapChanger(rtc);
        TapChangerConversion phaseTapChanger = getPhaseTapChanger(ptc, x);

        cgmesWinding.r = winding.asDouble(STRING_R);
        cgmesWinding.x = x;
        cgmesWinding.g = winding.asDouble(STRING_G, 0);
        cgmesWinding.b = winding.asDouble(STRING_B);
        cgmesWinding.ratioTapChanger = ratioTapChanger;
        cgmesWinding.phaseTapChanger = phaseTapChanger;
        cgmesWinding.ratedU = ratedU;
        cgmesWinding.phaseAngleClock = winding.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesWinding.terminal = terminal;
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

    private void interpretWinding(CgmesWinding cgmesWinding, Conversion.Config alternative, InterpretedWinding interpretedWinding) {

        AllTapChanger windingInterpretedTapChanger = ratioPhaseAlternative(cgmesWinding, alternative);
        AllShunt windingInterpretedShunt = shuntAlternative(cgmesWinding, alternative);
        int windingInterpretedClock = phaseAngleClockAlternative(cgmesWinding, alternative);
        boolean windingRatio0AtEnd2 = ratio0Alternative(cgmesWinding, alternative);

        interpretedWinding.r = cgmesWinding.r;
        interpretedWinding.x = cgmesWinding.x;
        interpretedWinding.end1.g = windingInterpretedShunt.g1;
        interpretedWinding.end1.b = windingInterpretedShunt.b1;
        interpretedWinding.end1.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger1;
        interpretedWinding.end1.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger1;
        interpretedWinding.end1.phaseAngleClock = windingInterpretedClock;
        interpretedWinding.end1.ratedU = cgmesWinding.ratedU;
        interpretedWinding.end1.terminal = cgmesWinding.terminal;

        interpretedWinding.end2.g = windingInterpretedShunt.g2;
        interpretedWinding.end2.b = windingInterpretedShunt.b2;
        interpretedWinding.end2.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger2;
        interpretedWinding.end2.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger2;

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

    private int phaseAngleClockAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        int phaseAngleClock = 0;

        if (alternative.isXfmr3PhaseAngleClockOn()) {
            phaseAngleClock = cgmesWinding.phaseAngleClock;
        }

        return phaseAngleClock;
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
    }

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
