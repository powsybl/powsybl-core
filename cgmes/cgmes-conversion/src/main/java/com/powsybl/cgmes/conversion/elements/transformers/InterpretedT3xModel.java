/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.transformers.CgmesT3xModel.CgmesEnd;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class InterpretedT3xModel {

    final InterpretedWinding winding1;
    final InterpretedWinding winding2;
    final InterpretedWinding winding3;
    final double ratedU0;

    /**
     * RatedU0 is selected according to the alternative. Each leg or winding is
     * interpreted.
     */
    public InterpretedT3xModel(CgmesT3xModel cgmesT3xModel, Conversion.Config alternative) {

        this.ratedU0 = ratedU0Alternative(cgmesT3xModel, alternative);

        this.winding1 = new InterpretedWinding(cgmesT3xModel.end1, alternative);
        this.winding2 = new InterpretedWinding(cgmesT3xModel.end2, alternative);
        this.winding3 = new InterpretedWinding(cgmesT3xModel.end3, alternative);
    }

    /**
     * return the ratedU0 (ratedU at the star bus side) If the structural ratio is
     * defined at the star bus side ratedU0 can be any value. selectRatedU0 selects it.
     * If the structural ratio is defined at the network side only four options
     * are considered, 1.0 kv, ratedU1, ratedU2 and ratedU3
     */
    private static double ratedU0Alternative(CgmesT3xModel cgmesT3xModel, Conversion.Config alternative) {
        switch (alternative.getXfmr3StructuralRatio()) {
            case NETWORK_SIDE:
                return 1.0;
            case STAR_BUS_SIDE:
                return selectRatedU0(cgmesT3xModel);
            case END1:
                return cgmesT3xModel.end1.ratedU;
            case END2:
                return cgmesT3xModel.end2.ratedU;
            case END3:
                return cgmesT3xModel.end3.ratedU;
        }
        return 1.0;
    }

    private static double selectRatedU0(CgmesT3xModel cgmesT3xModel) {
        return cgmesT3xModel.end1.ratedU;
    }

    static class InterpretedWinding {
        final double r;
        final double x;
        final TapChangerConversion.InterpretedEnd end1;
        final InterpretedEnd2 end2;
        final boolean structuralRatioAtEnd2;
        final Double ratedS;

        /**
         * Maps Cgmes ratioTapChangers, phaseTapChangers, shuntAdmittances and
         * structural ratio according to the alternative. The rest of the Cgmes data is
         * directly mapped.
         */
        InterpretedWinding(CgmesEnd cgmesEnd, Conversion.Config alternative) {

            TapChangerConversion.AllTapChanger windingInterpretedTapChanger = ratioPhaseAlternative(cgmesEnd, alternative);
            TapChangerConversion.AllShunt windingInterpretedShunt = shuntAlternative(cgmesEnd, alternative);
            boolean windingStructuralRatioAtEnd2 = structuralRatioAlternative(alternative);

            this.r = cgmesEnd.r;
            this.x = cgmesEnd.x;
            this.end1 = new TapChangerConversion.InterpretedEnd(windingInterpretedShunt.g1, windingInterpretedShunt.b1,
                    windingInterpretedTapChanger.ratioTapChanger1, windingInterpretedTapChanger.phaseTapChanger1,
                    cgmesEnd.ratedU, cgmesEnd.terminal);
            this.end2 = new InterpretedEnd2(windingInterpretedShunt.g2, windingInterpretedShunt.b2,
                    windingInterpretedTapChanger.ratioTapChanger2, windingInterpretedTapChanger.phaseTapChanger2);
            this.structuralRatioAtEnd2 = windingStructuralRatioAtEnd2;
            this.ratedS = cgmesEnd.ratedS;
        }

        /**
         * RatioTapChanger and PhaseTapChanger are assigned according the alternative
         * Network side is always the end1 of the leg and star bus side end2
         */
        private static TapChangerConversion.AllTapChanger ratioPhaseAlternative(CgmesEnd cgmesEnd, Conversion.Config alternative) {
            TapChanger ratioTapChanger1 = null;
            TapChanger phaseTapChanger1 = null;
            TapChanger ratioTapChanger2 = null;
            TapChanger phaseTapChanger2 = null;

            if (alternative.getXfmr3RatioPhase() == Conversion.Xfmr3RatioPhaseInterpretationAlternative.NETWORK_SIDE) {
                ratioTapChanger1 = cgmesEnd.ratioTapChanger;
                phaseTapChanger1 = cgmesEnd.phaseTapChanger;
            } else if (alternative.getXfmr3RatioPhase() == Conversion.Xfmr3RatioPhaseInterpretationAlternative.STAR_BUS_SIDE) {
                ratioTapChanger2 = cgmesEnd.ratioTapChanger;
                phaseTapChanger2 = cgmesEnd.phaseTapChanger;
            }

            TapChangerConversion.AllTapChanger allTapChanger = new TapChangerConversion.AllTapChanger();
            allTapChanger.ratioTapChanger1 = ratioTapChanger1;
            allTapChanger.phaseTapChanger1 = phaseTapChanger1;
            allTapChanger.ratioTapChanger2 = ratioTapChanger2;
            allTapChanger.phaseTapChanger2 = phaseTapChanger2;

            return allTapChanger;
        }

        /**
         * Shunt admittances are mapped according to alternative options
         */
        private static TapChangerConversion.AllShunt shuntAlternative(CgmesEnd cgmesEnd,
                                                                      Conversion.Config alternative) {
            double g1 = 0.0;
            double b1 = 0.0;
            double g2 = 0.0;
            double b2 = 0.0;

            switch (alternative.getXfmr3Shunt()) {
                case NETWORK_SIDE:
                    g1 = cgmesEnd.g;
                    b1 = cgmesEnd.b;
                    break;
                case STAR_BUS_SIDE:
                    g2 = cgmesEnd.g;
                    b2 = cgmesEnd.b;
                    break;
                case SPLIT:
                    g1 = cgmesEnd.g * 0.5;
                    b1 = cgmesEnd.b * 0.5;
                    g2 = cgmesEnd.g * 0.5;
                    b2 = cgmesEnd.b * 0.5;
                    break;
            }

            TapChangerConversion.AllShunt allShunt = new TapChangerConversion.AllShunt();
            allShunt.g1 = g1;
            allShunt.b1 = b1;
            allShunt.g2 = g2;
            allShunt.b2 = b2;

            return allShunt;
        }

        /**
         * True if the structural ratio is at end2 of the leg (star bus side)
         */
        private static boolean structuralRatioAlternative(Conversion.Config alternative) {
            switch (alternative.getXfmr3StructuralRatio()) {
                case NETWORK_SIDE:
                case END1:
                case END2:
                case END3:
                    return false;
                case STAR_BUS_SIDE:
                    return true;
            }
            return false;
        }
    }

    static class InterpretedEnd2 {
        final double g;
        final double b;
        final TapChanger ratioTapChanger;
        final TapChanger phaseTapChanger;

        InterpretedEnd2(double g, double b, TapChanger ratioTapChanger, TapChanger phaseTapChanger) {
            this.g = g;
            this.b = b;
            this.ratioTapChanger = ratioTapChanger;
            this.phaseTapChanger = phaseTapChanger;
        }
    }
}
