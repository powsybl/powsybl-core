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
public class CgmesT3xModel {

    private final CgmesWinding winding1;
    private final CgmesWinding winding2;
    private final CgmesWinding winding3;

    public CgmesT3xModel(PropertyBags ends, Context context) {
        this.winding1 = new CgmesWinding(ends.get(0), context);
        this.winding2 = new CgmesWinding(ends.get(1), context);
        this.winding3 = new CgmesWinding(ends.get(2), context);
    }

    public CgmesWinding getWinding1() {
        return this.winding1;
    }

    public CgmesWinding getWinding2() {
        return this.winding2;
    }

    public CgmesWinding getWinding3() {
        return this.winding3;
    }

    static class CgmesWinding {
        private final double r;
        private final double x;
        private final double g;
        private final double b;
        private final TapChanger ratioTapChanger;
        private final TapChanger phaseTapChanger;
        private final double ratedU;
        private final String terminal;

        CgmesWinding(PropertyBag end, Context context) {
            double x = end.asDouble(CgmesNames.X);

            this.r = end.asDouble(CgmesNames.R);
            this.x = x;
            this.g = end.asDouble(CgmesNames.G, 0);
            this.b = end.asDouble(CgmesNames.B);
            this.ratioTapChanger = TapChanger.ratioTapChangerFromEnd(end, context);
            this.phaseTapChanger = TapChanger.phaseTapChangerFromEnd(end, x, context);
            this.ratedU = end.asDouble(CgmesNames.RATEDU);
            this.terminal = end.getId(CgmesNames.TERMINAL);
        }

        public double getR() {
            return this.r;
        }

        public double getX() {
            return this.x;
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
