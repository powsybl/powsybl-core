/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class InterpretedT2xModel {

    final double r;
    final double x;
    final TapChangerConversion.InterpretedEnd end1;
    final TapChangerConversion.InterpretedEnd end2;
    final boolean structuralRatioAtEnd2;
    final Double ratedS;

    /**
     * Maps Cgmes ratioTapChangers, phaseTapChangers, shuntAdmittances and
     * structural ratio according to the alternative. The rest of the Cgmes data is
     * directly mapped.
     */
    public InterpretedT2xModel(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative, Context context) {

        TapChangerConversion tcc = new TapChangerConversion(context);

        TapChangerConversion.AllTapChanger interpretedTapChanger = ratioPhaseAlternative(cgmesT2xModel, alternative, tcc);
        TapChangerConversion.AllShunt interpretedShunt = shuntAlternative(cgmesT2xModel, alternative);

        this.r = cgmesT2xModel.r;
        this.x = cgmesT2xModel.x;
        this.end1 = new TapChangerConversion.InterpretedEnd(interpretedShunt.g1, interpretedShunt.b1,
            interpretedTapChanger.ratioTapChanger1, interpretedTapChanger.phaseTapChanger1,
            cgmesT2xModel.end1.ratedU, cgmesT2xModel.end1.terminal);
        this.end2 = new TapChangerConversion.InterpretedEnd(interpretedShunt.g2, interpretedShunt.b2,
            interpretedTapChanger.ratioTapChanger2, interpretedTapChanger.phaseTapChanger2,
            cgmesT2xModel.end2.ratedU, cgmesT2xModel.end2.terminal);
        this.structuralRatioAtEnd2 = structuralRatioAlternative(cgmesT2xModel, alternative);
        this.ratedS = cgmesT2xModel.ratedS;
    }

    /**
     * END1. All tapChangers of the Cgmes model are supposed to be at end1. The
     * interpreted model only supports one ratioTapChanger or phaseTapChanger at
     * each end so they should be combined to only one as two are possible.
     * END2. All tapChangers of the Cgmes model are supposed to be at end2. They should be
     * combined.
     * END1_END2. Tap changers are directly mapped at each end.
     * X. Tap changers are mapped at end1 or end2 depending on the xIsZero attribute.
     */
    private TapChangerConversion.AllTapChanger ratioPhaseAlternative(CgmesT2xModel cgmesT2xModel,
        Conversion.Config alternative, TapChangerConversion tcc) {
        TapChanger ratioTapChanger1 = null;
        TapChanger phaseTapChanger1 = null;
        TapChanger ratioTapChanger2 = null;
        TapChanger phaseTapChanger2 = null;

        switch (alternative.getXfmr2RatioPhase()) {
            case END1:
                ratioTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.end1.ratioTapChanger,
                    cgmesT2xModel.end2.ratioTapChanger);
                phaseTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.end1.phaseTapChanger,
                    cgmesT2xModel.end2.phaseTapChanger);
                break;
            case END2:
                ratioTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.end2.ratioTapChanger,
                    cgmesT2xModel.end1.ratioTapChanger);
                phaseTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.end2.phaseTapChanger,
                    cgmesT2xModel.end1.phaseTapChanger);
                break;
            case END1_END2:
                ratioTapChanger1 = cgmesT2xModel.end1.ratioTapChanger;
                phaseTapChanger1 = cgmesT2xModel.end1.phaseTapChanger;
                ratioTapChanger2 = cgmesT2xModel.end2.ratioTapChanger;
                phaseTapChanger2 = cgmesT2xModel.end2.phaseTapChanger;
                break;
            case X:
                if (cgmesT2xModel.x1IsZero) {
                    ratioTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.end1.ratioTapChanger,
                        cgmesT2xModel.end2.ratioTapChanger);
                    phaseTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.end1.phaseTapChanger,
                        cgmesT2xModel.end2.phaseTapChanger);
                } else {
                    ratioTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.end2.ratioTapChanger,
                        cgmesT2xModel.end1.ratioTapChanger);
                    phaseTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.end2.phaseTapChanger,
                        cgmesT2xModel.end1.phaseTapChanger);
                }
                break;
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
    private static TapChangerConversion.AllShunt shuntAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        double g1 = 0.0;
        double b1 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        switch (alternative.getXfmr2Shunt()) {
            case END1:
                g1 = cgmesT2xModel.end1.g + cgmesT2xModel.end2.g;
                b1 = cgmesT2xModel.end1.b + cgmesT2xModel.end2.b;
                break;
            case END2:
                g2 = cgmesT2xModel.end1.g + cgmesT2xModel.end2.g;
                b2 = cgmesT2xModel.end1.b + cgmesT2xModel.end2.b;
                break;
            case END1_END2:
                g1 = cgmesT2xModel.end1.g;
                b1 = cgmesT2xModel.end1.b;
                g2 = cgmesT2xModel.end2.g;
                b2 = cgmesT2xModel.end2.b;
                break;
            case SPLIT:
                g1 = (cgmesT2xModel.end1.g + cgmesT2xModel.end2.g) * 0.5;
                b1 = (cgmesT2xModel.end1.b + cgmesT2xModel.end2.b) * 0.5;
                g2 = (cgmesT2xModel.end1.g + cgmesT2xModel.end2.g) * 0.5;
                b2 = (cgmesT2xModel.end1.b + cgmesT2xModel.end2.b) * 0.5;
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
     * return true if the structural ratio is at end2
     */
    private static boolean structuralRatioAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        if (cgmesT2xModel.end1.ratedU == cgmesT2xModel.end2.ratedU) {
            return false;
        }
        switch (alternative.getXfmr2StructuralRatio()) {
            case END1:
                return false;
            case END2:
                return true;
            case X:
                return !cgmesT2xModel.x1IsZero;
        }
        return false;
    }
}
