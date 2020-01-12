/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.transformers.InterpretedT3xModel.InterpretedWinding;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class ConvertedT3xModel {

    private final ConvertedWinding winding1;
    private final ConvertedWinding winding2;
    private final ConvertedWinding winding3;
    private final double ratedU0;

    public ConvertedT3xModel(InterpretedT3xModel interpretedT3xModel, Context context) {

        TapChangerConversion tcc = new TapChangerConversion(context);

        double ratedU0 = interpretedT3xModel.getRatedU0();
        this.winding1 = new ConvertedWinding(interpretedT3xModel.getWinding1(), ratedU0, tcc);
        this.winding2 = new ConvertedWinding(interpretedT3xModel.getWinding2(), ratedU0, tcc);
        this.winding3 = new ConvertedWinding(interpretedT3xModel.getWinding3(), ratedU0, tcc);
        this.ratedU0 = ratedU0;
    }

    public ConvertedWinding getWinding1() {
        return this.winding1;
    }

    public ConvertedWinding getWinding2() {
        return this.winding2;
    }

    public ConvertedWinding getWinding3() {
        return this.winding3;
    }

    public double getRatedU0() {
        return this.ratedU0;
    }

    static class ConvertedWinding {
        private final double r;
        private final double x;
        private final TapChangerConversion.ConvertedEnd1 end1;

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
                interpretedWinding.getEnd1().getRatedU(),
                interpretedWinding.getEnd1().getTerminal());
        }

        private TapChangerWinding moveCombineTapChangerWinding(InterpretedWinding interpretedWinding, TapChangerConversion tcc) {

            TapChanger nRatioTapChanger = TapChangerConversion.moveTapChangerFrom2To1(interpretedWinding.getEnd2().getRatioTapChanger());
            TapChanger nPhaseTapChanger = TapChangerConversion.moveTapChangerFrom2To1(interpretedWinding.getEnd2().getPhaseTapChanger());

            TapChanger cRatioTapChanger = tcc.combineTapChangers(interpretedWinding.getEnd1().getRatioTapChanger(), nRatioTapChanger);
            TapChanger cPhaseTapChanger = tcc.combineTapChangers(interpretedWinding.getEnd1().getPhaseTapChanger(), nPhaseTapChanger);

            TapChangerWinding tapChangerWinding = new TapChangerWinding();
            tapChangerWinding.ratioTapChanger = cRatioTapChanger;
            tapChangerWinding.phaseTapChanger = cPhaseTapChanger;
            return tapChangerWinding;
        }

        private static TapChangerConversion.RatioConversion moveStructuralRatioWinding(InterpretedWinding interpretedWinding, double ratedU0) {
            TapChangerConversion.RatioConversion rc0;
            // IIDM: Structural ratio always at network side of the leg (end1)
            if (interpretedWinding.isStructuralRatioAtEnd2()) {
                double a0 = ratedU0 / interpretedWinding.getEnd1().getRatedU();
                rc0 = TapChangerConversion.moveRatioFrom2To1(a0, 0.0, interpretedWinding.getR(), interpretedWinding.getX(),
                    interpretedWinding.getEnd1().getG(), interpretedWinding.getEnd1().getB(),
                    interpretedWinding.getEnd2().getG(), interpretedWinding.getEnd2().getB());
            } else {
                rc0 = TapChangerConversion.identityRatioConversion(interpretedWinding.getR(), interpretedWinding.getX(),
                    interpretedWinding.getEnd1().getG(), interpretedWinding.getEnd1().getB(),
                    interpretedWinding.getEnd2().getG(), interpretedWinding.getEnd2().getB());
            }

            return rc0;
        }

        static class TapChangerWinding {
            TapChanger ratioTapChanger;
            TapChanger phaseTapChanger;
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
    }
}
