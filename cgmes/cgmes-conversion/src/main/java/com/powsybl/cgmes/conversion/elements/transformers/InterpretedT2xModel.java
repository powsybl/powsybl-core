/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class InterpretedT2xModel {

    private final double r;
    private final double x;
    private final InterpretedEnd end1;
    private final InterpretedEnd end2;
    private final boolean structuralRatioAtEnd2;

    /**
     * Maps Cgmes ratioTapChangers, phaseTapChangers, shuntAdmittances and
     * structural ratio according to the alternative. The rest of the Cgmes data is
     * directly mapped.
     */
    public InterpretedT2xModel(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative, Context context) {

        TapChangerConversion tcc = new TapChangerConversion(context);

        TapChangerConversion.AllTapChanger interpretedTapChanger = ratioPhaseAlternative(cgmesT2xModel, alternative, tcc);
        TapChangerConversion.AllShunt interpretedShunt = shuntAlternative(cgmesT2xModel, alternative);
        boolean structuralRatioAtEnd2 = structuralRatioAlternative(cgmesT2xModel, alternative);

        this.r = cgmesT2xModel.getR();
        this.x = cgmesT2xModel.getX();
        this.end1 = new InterpretedEnd(interpretedShunt.g1, interpretedShunt.b1, interpretedTapChanger.ratioTapChanger1,
            interpretedTapChanger.phaseTapChanger1, cgmesT2xModel.getEnd1().getRatedU(),
            cgmesT2xModel.getEnd1().getTerminal());
        this.end2 = new InterpretedEnd(interpretedShunt.g2, interpretedShunt.b2, interpretedTapChanger.ratioTapChanger2,
            interpretedTapChanger.phaseTapChanger2, cgmesT2xModel.getEnd2().getRatedU(),
            cgmesT2xModel.getEnd2().getTerminal());
        this.structuralRatioAtEnd2 = structuralRatioAtEnd2;
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
                ratioTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.getEnd1().getRatioTapChanger(),
                    cgmesT2xModel.getEnd2().getRatioTapChanger());
                phaseTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.getEnd1().getPhaseTapChanger(),
                    cgmesT2xModel.getEnd2().getPhaseTapChanger());
                break;
            case END2:
                ratioTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.getEnd2().getRatioTapChanger(),
                    cgmesT2xModel.getEnd1().getRatioTapChanger());
                phaseTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.getEnd2().getPhaseTapChanger(),
                    cgmesT2xModel.getEnd1().getPhaseTapChanger());
                break;
            case END1_END2:
                ratioTapChanger1 = cgmesT2xModel.getEnd1().getRatioTapChanger();
                phaseTapChanger1 = cgmesT2xModel.getEnd1().getPhaseTapChanger();
                ratioTapChanger2 = cgmesT2xModel.getEnd2().getRatioTapChanger();
                phaseTapChanger2 = cgmesT2xModel.getEnd2().getPhaseTapChanger();
                break;
            case X:
                if (cgmesT2xModel.getEnd1().isXisZero()) {
                    ratioTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.getEnd1().getRatioTapChanger(),
                        cgmesT2xModel.getEnd2().getRatioTapChanger());
                    phaseTapChanger1 = tcc.combineTapChangers(cgmesT2xModel.getEnd1().getPhaseTapChanger(),
                        cgmesT2xModel.getEnd2().getPhaseTapChanger());
                } else {
                    ratioTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.getEnd2().getRatioTapChanger(),
                        cgmesT2xModel.getEnd1().getRatioTapChanger());
                    phaseTapChanger2 = tcc.combineTapChangers(cgmesT2xModel.getEnd2().getPhaseTapChanger(),
                        cgmesT2xModel.getEnd1().getPhaseTapChanger());
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
                g1 = cgmesT2xModel.getEnd1().getG() + cgmesT2xModel.getEnd2().getG();
                b1 = cgmesT2xModel.getEnd1().getB() + cgmesT2xModel.getEnd2().getB();
                break;
            case END2:
                g2 = cgmesT2xModel.getEnd1().getG() + cgmesT2xModel.getEnd2().getG();
                b2 = cgmesT2xModel.getEnd1().getB() + cgmesT2xModel.getEnd2().getB();
                break;
            case END1_END2:
                g1 = cgmesT2xModel.getEnd1().getG();
                b1 = cgmesT2xModel.getEnd1().getB();
                g2 = cgmesT2xModel.getEnd2().getG();
                b2 = cgmesT2xModel.getEnd2().getB();
                break;
            case SPLIT:
                g1 = (cgmesT2xModel.getEnd1().getG() + cgmesT2xModel.getEnd2().getG()) * 0.5;
                b1 = (cgmesT2xModel.getEnd1().getB() + cgmesT2xModel.getEnd2().getB()) * 0.5;
                g2 = (cgmesT2xModel.getEnd1().getG() + cgmesT2xModel.getEnd2().getG()) * 0.5;
                b2 = (cgmesT2xModel.getEnd1().getB() + cgmesT2xModel.getEnd2().getB()) * 0.5;
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
        if (cgmesT2xModel.getEnd1().getRatedU() == cgmesT2xModel.getEnd2().getRatedU()) {
            return false;
        }
        switch (alternative.getXfmr2StructuralRatio()) {
            case END1:
                return false;
            case END2:
                return true;
            case X:
                return !cgmesT2xModel.getEnd1().isXisZero();
        }
        return false;
    }

    public double getR() {
        return this.r;
    }

    public double getX() {
        return this.x;
    }

    public InterpretedEnd getEnd1() {
        return this.end1;
    }

    public InterpretedEnd getEnd2() {
        return this.end2;
    }

    public boolean isStructuralRatioAtEnd2() {
        return this.structuralRatioAtEnd2;
    }

    static class InterpretedEnd {
        private final double g;
        private final double b;
        private final TapChanger ratioTapChanger;
        private final TapChanger phaseTapChanger;
        private final double ratedU;
        private final String terminal;

        InterpretedEnd(double g, double b, TapChanger ratioTapChanger, TapChanger phaseTapChanger, double ratedU,
            String terminal) {
            this.g = g;
            this.b = b;
            this.ratioTapChanger = ratioTapChanger;
            this.phaseTapChanger = phaseTapChanger;
            this.ratedU = ratedU;
            this.terminal = terminal;
        }

        public double getG() {
            return this.g;
        }

        public double getB() {
            return this.b;
        }

        public TapChanger getRatioTapChanger() {
            return this.ratioTapChanger;
        }

        public TapChanger getPhaseTapChanger() {
            return this.phaseTapChanger;
        }

        public double getRatedU() {
            return this.ratedU;
        }

        public String getTerminal() {
            return this.terminal;
        }
    }
}
