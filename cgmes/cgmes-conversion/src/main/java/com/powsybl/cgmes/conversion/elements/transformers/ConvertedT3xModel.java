/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.transformers.InterpretedT3xModel.InterpretedWinding;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ConvertedT3xModel {

    final ConvertedWinding winding1;
    final ConvertedWinding winding2;
    final ConvertedWinding winding3;
    final double ratedU0;

    public ConvertedT3xModel(InterpretedT3xModel interpretedT3xModel, Context context) {

        TapChangerConversion tcc = new TapChangerConversion(context);

        this.winding1 = new ConvertedWinding(interpretedT3xModel.winding1, interpretedT3xModel.ratedU0, tcc);
        this.winding2 = new ConvertedWinding(interpretedT3xModel.winding2, interpretedT3xModel.ratedU0, tcc);
        this.winding3 = new ConvertedWinding(interpretedT3xModel.winding3, interpretedT3xModel.ratedU0, tcc);
        this.ratedU0 = interpretedT3xModel.ratedU0;
    }

    static class ConvertedWinding {
        final double r;
        final double x;
        final TapChangerConversion.ConvertedEnd1 end1;
        final Double ratedS;

        /**
         * At each winding or leg:
         * TapChanger are moved from star bus side (end2) to network side (end1) then are combined with tapChangers
         * initially defined at the network side.
         * Structural ratio is moved from star bus side to network side if it is necessary
         * The rest of attributes are directly mapped
         */
        ConvertedWinding(InterpretedWinding interpretedWinding, double ratedU0, TapChangerConversion tcc) {

            TapChangerWinding windingTapChanger = moveCombineTapChangerWinding(interpretedWinding, tcc);
            TapChangerConversion.RatioConversion windingRc0 = moveStructuralRatioWinding(interpretedWinding, ratedU0);

            this.r = windingRc0.r;
            this.x = windingRc0.x;
            this.end1 = new TapChangerConversion.ConvertedEnd1(windingRc0.g1 + windingRc0.g2,
                windingRc0.b1 + windingRc0.b2,
                windingTapChanger.ratioTapChanger,
                windingTapChanger.phaseTapChanger,
                interpretedWinding.end1.ratedU,
                interpretedWinding.end1.terminal);
            this.ratedS = interpretedWinding.ratedS;
        }

        private TapChangerWinding moveCombineTapChangerWinding(InterpretedWinding interpretedWinding, TapChangerConversion tcc) {

            TapChanger nRatioTapChanger = TapChangerConversion.moveTapChangerFrom2To1(interpretedWinding.end2.ratioTapChanger);
            TapChanger nPhaseTapChanger = TapChangerConversion.moveTapChangerFrom2To1(interpretedWinding.end2.phaseTapChanger);

            TapChanger cRatioTapChanger = tcc.combineTapChangers(interpretedWinding.end1.ratioTapChanger, nRatioTapChanger);
            TapChanger cPhaseTapChanger = tcc.combineTapChangers(interpretedWinding.end1.phaseTapChanger, nPhaseTapChanger);

            TapChangerWinding tapChangerWinding = new TapChangerWinding();
            tapChangerWinding.ratioTapChanger = cRatioTapChanger;
            tapChangerWinding.phaseTapChanger = cPhaseTapChanger;
            return tapChangerWinding;
        }

        private static TapChangerConversion.RatioConversion moveStructuralRatioWinding(InterpretedWinding interpretedWinding, double ratedU0) {
            TapChangerConversion.RatioConversion rc0;
            // IIDM: Structural ratio always at network side of the leg (end1)
            if (interpretedWinding.structuralRatioAtEnd2) {
                double a0 = ratedU0 / interpretedWinding.end1.ratedU;
                rc0 = TapChangerConversion.moveRatioFrom2To1(a0, 0.0, interpretedWinding.r, interpretedWinding.x,
                    interpretedWinding.end1.g, interpretedWinding.end1.b,
                    interpretedWinding.end2.g, interpretedWinding.end2.b);
            } else {
                rc0 = TapChangerConversion.identityRatioConversion(interpretedWinding.r, interpretedWinding.x,
                    interpretedWinding.end1.g, interpretedWinding.end1.b,
                    interpretedWinding.end2.g, interpretedWinding.end2.b);
            }

            return rc0;
        }

        static class TapChangerWinding {
            TapChanger ratioTapChanger;
            TapChanger phaseTapChanger;
        }
    }
}
