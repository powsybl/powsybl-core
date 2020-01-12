/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class ConvertedT2xModel {

    private final double r;
    private final double x;
    private final TapChangerConversion.ConvertedEnd1 end1;
    private final ConvertedEnd2 end2;

    /**
     * ratioTapChanger and phaseTapChanger of end2 are moved to end1 and then
     * combined with the tapChangers initially defined at end1. If the structural
     * ratio is defined at end2 is moved to end1. The rest of attributes are directly
     * mapped
     */
    public ConvertedT2xModel(InterpretedT2xModel interpretedT2xModel, Context context) {

        TapChangerConversion tcc = new TapChangerConversion(context);

        TapChanger nRatioTapChanger2 = TapChangerConversion.moveTapChangerFrom2To1(interpretedT2xModel.getEnd2().getRatioTapChanger());
        TapChanger nPhaseTapChanger2 = TapChangerConversion.moveTapChangerFrom2To1(interpretedT2xModel.getEnd2().getPhaseTapChanger());

        TapChanger ratioTapChanger = tcc.combineTapChangers(interpretedT2xModel.getEnd1().getRatioTapChanger(), nRatioTapChanger2);
        TapChanger phaseTapChanger = tcc.combineTapChangers(interpretedT2xModel.getEnd1().getPhaseTapChanger(), nPhaseTapChanger2);

        TapChangerConversion.RatioConversion rc0;
        if (interpretedT2xModel.isStructuralRatioAtEnd2()) {
            double a0 = interpretedT2xModel.getEnd2().getRatedU() / interpretedT2xModel.getEnd1().getRatedU();
            rc0 = TapChangerConversion.moveRatioFrom2To1(a0, 0.0, interpretedT2xModel.getR(), interpretedT2xModel.getX(),
                interpretedT2xModel.getEnd1().getG(), interpretedT2xModel.getEnd1().getB(),
                interpretedT2xModel.getEnd2().getG(), interpretedT2xModel.getEnd2().getB());
        } else {
            rc0 = TapChangerConversion.identityRatioConversion(interpretedT2xModel.getR(), interpretedT2xModel.getX(),
                interpretedT2xModel.getEnd1().getG(), interpretedT2xModel.getEnd1().getB(),
                interpretedT2xModel.getEnd2().getG(), interpretedT2xModel.getEnd2().getB());
        }

        this.r = rc0.r;
        this.x = rc0.x;
        this.end1 = new TapChangerConversion.ConvertedEnd1(rc0.g1 + rc0.g2, rc0.b1 + rc0.b2, ratioTapChanger, phaseTapChanger,
            interpretedT2xModel.getEnd1().getRatedU(), interpretedT2xModel.getEnd1().getTerminal());
        this.end2 = new ConvertedEnd2(interpretedT2xModel.getEnd2().getRatedU(), interpretedT2xModel.getEnd2().getTerminal());

    }

    public double getR() {
        return this.r;
    }

    public double getX() {
        return this.x;
    }

    public TapChangerConversion.ConvertedEnd1 getEnd1() {
        return this.end1;
    }

    public ConvertedEnd2 getEnd2() {
        return this.end2;
    }

    static class ConvertedEnd2 {
        double ratedU;
        String terminal;

        ConvertedEnd2(double ratedU, String terminal) {
            this.ratedU = ratedU;
            this.terminal = terminal;
        }

        public double getRatedU() {
            return this.ratedU;
        }

        public String getTerminal() {
            return this.terminal;
        }
    }
}
