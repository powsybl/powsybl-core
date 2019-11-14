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
        CgmesT3xModel cgmesT2xModel = load();
    }

    private CgmesT3xModel load() {
        CgmesT3xModel cgmesT2xModel = new CgmesT3xModel();

        // ends = ps
        loadWinding(ps.get(0), cgmesT2xModel.winding1);
        loadWinding(ps.get(1), cgmesT2xModel.winding2);
        loadWinding(ps.get(2), cgmesT2xModel.winding3);

        return cgmesT2xModel;
    }

    private void loadWinding(PropertyBag winding, CgmesWinding cgmesModelWinding) {
        PropertyBag rtc = getTransformerTapChanger(winding, STRING_RATIO_TAP_CHANGER,
            powerTransformerRatioTapChanger);
        PropertyBag ptc = getTransformerTapChanger(winding, STRING_PHASE_TAP_CHANGER,
            powerTransformerPhaseTapChanger);

        String terminal = winding.getId(CgmesNames.TERMINAL);
        double ratedU = winding.asDouble(STRING_RATEDU);
        double x = winding.asDouble(STRING_X);

        TapChangerConversion ratioTapChanger = getRatioTapChanger(rtc);
        TapChangerConversion phaseTapChanger = getPhaseTapChanger(ptc, x);

        cgmesModelWinding.r = winding.asDouble(STRING_R);
        cgmesModelWinding.x = x;
        cgmesModelWinding.g = winding.asDouble(STRING_G, 0);
        cgmesModelWinding.b = winding.asDouble(STRING_B);
        cgmesModelWinding.ratioTapChanger = ratioTapChanger;
        cgmesModelWinding.phaseTapChanger = phaseTapChanger;
        cgmesModelWinding.ratedU = ratedU;
        cgmesModelWinding.phaseAngleClock = winding.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesModelWinding.terminal = terminal;
    }

    private InterpretedModel interpret(CgmesT3xModel cgmesModel, Conversion.Config alternative) {

        InterpretedModel interpretedModel = new InterpretedModel();
        double ratedUf = ratedUfAlternative(cgmesModel, alternative);
        interpretedModel.ratedUf = ratedUf;

        interpretWinding(cgmesModel.winding1, alternative, interpretedModel.winding1);
        interpretWinding(cgmesModel.winding2, alternative, interpretedModel.winding2);
        interpretWinding(cgmesModel.winding3, alternative, interpretedModel.winding3);

        return interpretedModel;
    }

    private void interpretWinding(CgmesWinding cgmesModelWinding, Conversion.Config alternative, InterpretedWinding interpretedModelWinding) {

        TapChangerAll windingInterpretedTapChanger = ratioPhaseAlternative(cgmesModelWinding, alternative);
        ShuntAll windingInterpretedShunt = shuntAlternative(cgmesModelWinding, alternative);
        PhaseAngleClockAll windingInterpretedClock = phaseAngleClockAlternative(cgmesModelWinding, alternative);
        boolean windingRatio0AtEnd2 = ratio0Alternative(cgmesModelWinding, alternative);

        interpretedModelWinding.r = cgmesModelWinding.r;
        interpretedModelWinding.x = cgmesModelWinding.x;
        interpretedModelWinding.end1.g = windingInterpretedShunt.g1;
        interpretedModelWinding.end1.b = windingInterpretedShunt.b1;
        interpretedModelWinding.end1.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger1;
        interpretedModelWinding.end1.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger1;
        interpretedModelWinding.end1.phaseAngleClock = windingInterpretedClock.phaseAngleClock1;
        interpretedModelWinding.end1.ratedU = cgmesModelWinding.ratedU;
        interpretedModelWinding.end1.terminal = cgmesModelWinding.terminal;

        interpretedModelWinding.end2.g = windingInterpretedShunt.g2;
        interpretedModelWinding.end2.b = windingInterpretedShunt.b2;
        interpretedModelWinding.end2.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger2;
        interpretedModelWinding.end2.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger2;
        interpretedModelWinding.end2.phaseAngleClock = windingInterpretedClock.phaseAngleClock2;

        interpretedModelWinding.ratio0AtEnd2 = windingRatio0AtEnd2;
    }

    private TapChangerAll ratioPhaseAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
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

        TapChangerAll tapChanger22 = new TapChangerAll();
        tapChanger22.ratioTapChanger1 = ratioTapChanger1;
        tapChanger22.phaseTapChanger1 = phaseTapChanger1;
        tapChanger22.ratioTapChanger2 = ratioTapChanger2;
        tapChanger22.phaseTapChanger2 = phaseTapChanger2;

        return tapChanger22;
    }

    private ShuntAll shuntAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
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

        ShuntAll shunt22 = new ShuntAll();
        shunt22.g1 = g1;
        shunt22.b1 = b1;
        shunt22.g2 = g2;
        shunt22.b2 = b2;

        return shunt22;
    }

    private PhaseAngleClockAll phaseAngleClockAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        int phaseAngleClock1 = 0;
        int phaseAngleClock2 = 0;

        if (cgmesWinding.phaseAngleClock != 0) {
            if (alternative.isXfmr3PhaseAngleClockNetworkSide()) {
                phaseAngleClock1 = cgmesWinding.phaseAngleClock;
            } else if (alternative.isXfmr3PhaseAngleClockStarBusSide()) {
                phaseAngleClock2 = cgmesWinding.phaseAngleClock;
            }
        }

        PhaseAngleClockAll phaseAngleClock02 = new PhaseAngleClockAll();
        phaseAngleClock02.phaseAngleClock1 = phaseAngleClock1;
        phaseAngleClock02.phaseAngleClock2 = phaseAngleClock2;

        return phaseAngleClock02;
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

    private double ratedUfAlternative(CgmesT3xModel cgmesModel, Conversion.Config alternative) {

        double ratedUf = 1.0;
        if (alternative.isXfmr3Ratio0StarBusSide()) {
            ratedUf = selectRatedUf(cgmesModel);
        } else if (alternative.isXfmr3Ratio0NetworkSide()) {
            ratedUf = 1.0;
        } else if (alternative.isXfmr3Ratio0End1()) {
            ratedUf = cgmesModel.winding1.ratedU;
        } else if (alternative.isXfmr3Ratio0End2()) {
            ratedUf = cgmesModel.winding2.ratedU;
        } else if (alternative.isXfmr3Ratio0End3()) {
            ratedUf = cgmesModel.winding3.ratedU;
        }
        return ratedUf;
    }

    // Select the ratedUf voltage
    private double selectRatedUf(CgmesT3xModel cgmesModel) {
        return cgmesModel.winding1.ratedU;
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

    static class InterpretedModel {
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

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
