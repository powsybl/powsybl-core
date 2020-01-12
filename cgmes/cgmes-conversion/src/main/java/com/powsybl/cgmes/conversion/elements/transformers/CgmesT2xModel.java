/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesT2xModel {

    private final double r;
    private final double x;
    private final CgmesEnd end1;
    private final CgmesEnd end2;

    public CgmesT2xModel(PropertyBags ends, Context context) {

        PropertyBag bagEnd1 = ends.get(0);
        PropertyBag bagEnd2 = ends.get(1);

        double x1 = bagEnd1.asDouble(CgmesNames.X);
        double x2 = bagEnd2.asDouble(CgmesNames.X);
        double x = x1 + x2;

        TapChanger ratioTapChanger1 = TapChanger.ratioTapChangerFromEnd(bagEnd1, context);
        TapChanger ratioTapChanger2 = TapChanger.ratioTapChangerFromEnd(bagEnd2, context);
        TapChanger phaseTapChanger1 = TapChanger.phaseTapChangerFromEnd(bagEnd1, x, context);
        TapChanger phaseTapChanger2 = TapChanger.phaseTapChangerFromEnd(bagEnd2, x, context);

        double g1 = bagEnd1.asDouble(CgmesNames.G, 0);
        double b1 = bagEnd1.asDouble(CgmesNames.B);
        String terminal1 = bagEnd1.getId(CgmesNames.TERMINAL);
        double ratedU1 = bagEnd1.asDouble(CgmesNames.RATEDU);
        boolean x1IsZero = x1 == 0.0;

        double g2 = bagEnd2.asDouble(CgmesNames.G, 0);
        double b2 = bagEnd2.asDouble(CgmesNames.B);
        String terminal2 = bagEnd2.getId(CgmesNames.TERMINAL);
        double ratedU2 = bagEnd2.asDouble(CgmesNames.RATEDU);
        boolean x2IsZero = x2 == 0.0;

        this.r = bagEnd1.asDouble(CgmesNames.R) + bagEnd2.asDouble(CgmesNames.R);
        this.x = x;
        this.end1 = new CgmesEnd(g1, b1, ratioTapChanger1, phaseTapChanger1, ratedU1, terminal1, x1IsZero);
        this.end2 = new CgmesEnd(g2, b2, ratioTapChanger2, phaseTapChanger2, ratedU2, terminal2, x2IsZero);
    }

    public double getR() {
        return this.r;
    }

    public double getX() {
        return this.x;
    }

    public CgmesEnd getEnd1() {
        return this.end1;
    }

    public CgmesEnd getEnd2() {
        return this.end2;
    }

    static class CgmesEnd {
        private final double g;
        private final double b;
        private final TapChanger ratioTapChanger;
        private final TapChanger phaseTapChanger;
        private final double ratedU;
        private final String terminal;
        private final boolean xIsZero;

        CgmesEnd(double g, double b, TapChanger ratioTapChanger, TapChanger phaseTapChanger, double ratedU,
            String terminal, boolean xIsZero) {
            this.g = g;
            this.b = b;
            this.ratioTapChanger = ratioTapChanger;
            this.phaseTapChanger = phaseTapChanger;
            this.ratedU = ratedU;
            this.terminal = terminal;
            this.xIsZero = xIsZero;
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

        public boolean isXisZero() {
            return this.xIsZero;
        }
    }
}
