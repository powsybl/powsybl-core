/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ConvertedT2xModel {

    final double r;
    final double x;
    final TapChangerConversion.ConvertedEnd1 end1;
    final ConvertedEnd2 end2;
    final Double ratedS;

    /**
     * ratioTapChanger and phaseTapChanger of end2 are moved to end1 and then
     * combined with the tapChangers initially defined at end1. If the structural
     * ratio is defined at end2 is moved to end1. The rest of attributes are directly
     * mapped
     */
    public ConvertedT2xModel(InterpretedT2xModel interpretedT2xModel, Context context) {

        TapChangerConversion tcc = new TapChangerConversion(context);

        TapChanger nRatioTapChanger2 = TapChangerConversion.moveTapChangerFrom2To1(interpretedT2xModel.end2.ratioTapChanger);
        TapChanger nPhaseTapChanger2 = TapChangerConversion.moveTapChangerFrom2To1(interpretedT2xModel.end2.phaseTapChanger);

        TapChanger ratioTapChanger = tcc.combineTapChangers(interpretedT2xModel.end1.ratioTapChanger, nRatioTapChanger2);
        TapChanger phaseTapChanger = tcc.combineTapChangers(interpretedT2xModel.end1.phaseTapChanger, nPhaseTapChanger2);

        TapChangerConversion.RatioConversion rc0;
        if (interpretedT2xModel.structuralRatioAtEnd2) {
            double a0 = interpretedT2xModel.end2.ratedU / interpretedT2xModel.end1.ratedU;
            rc0 = TapChangerConversion.moveRatioFrom2To1(a0, 0.0, interpretedT2xModel.r, interpretedT2xModel.x,
                interpretedT2xModel.end1.g, interpretedT2xModel.end1.b,
                interpretedT2xModel.end2.g, interpretedT2xModel.end2.b);
        } else {
            rc0 = TapChangerConversion.identityRatioConversion(interpretedT2xModel.r, interpretedT2xModel.x,
                interpretedT2xModel.end1.g, interpretedT2xModel.end1.b,
                interpretedT2xModel.end2.g, interpretedT2xModel.end2.b);
        }

        this.r = rc0.r;
        this.x = rc0.x;
        this.end1 = new TapChangerConversion.ConvertedEnd1(rc0.g1 + rc0.g2, rc0.b1 + rc0.b2, ratioTapChanger, phaseTapChanger,
            interpretedT2xModel.end1.ratedU, interpretedT2xModel.end1.terminal);
        this.end2 = new ConvertedEnd2(interpretedT2xModel.end2.ratedU, interpretedT2xModel.end2.terminal);
        this.ratedS = interpretedT2xModel.ratedS;
    }

    static class ConvertedEnd2 {
        final double ratedU;
        final String terminal;

        ConvertedEnd2(double ratedU, String terminal) {
            this.ratedU = ratedU;
            this.terminal = terminal;
        }
    }
}
