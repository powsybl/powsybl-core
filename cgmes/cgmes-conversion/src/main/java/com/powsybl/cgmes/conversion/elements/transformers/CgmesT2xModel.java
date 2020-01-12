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

    final double r;
    final double x;
    final CgmesEnd end1;
    final CgmesEnd end2;
    final boolean x1IsZero;

    public CgmesT2xModel(PropertyBags ends, Context context) {

        PropertyBag bagEnd1 = ends.get(0);
        PropertyBag bagEnd2 = ends.get(1);

        double x1 = bagEnd1.asDouble(CgmesNames.X);
        double x2 = bagEnd2.asDouble(CgmesNames.X);
        double x = x1 + x2;

        this.r = bagEnd1.asDouble(CgmesNames.R) + bagEnd2.asDouble(CgmesNames.R);
        this.x = x;
        this.end1 = new CgmesEnd(bagEnd1, x, context);
        this.end2 = new CgmesEnd(bagEnd2, x, context);
        this.x1IsZero = x1 == 0.0;
    }

    static class CgmesEnd {
        final double g;
        final double b;
        final TapChanger ratioTapChanger;
        final TapChanger phaseTapChanger;
        final double ratedU;
        final String terminal;

        CgmesEnd(PropertyBag bagEnd, double x, Context context) {
            this.g = bagEnd.asDouble(CgmesNames.G, 0);
            this.b = bagEnd.asDouble(CgmesNames.B);
            this.ratioTapChanger = TapChanger.ratioTapChangerFromEnd(bagEnd, context);
            this.phaseTapChanger = TapChanger.phaseTapChangerFromEnd(bagEnd, x, context);
            this.ratedU = bagEnd.asDouble(CgmesNames.RATEDU);
            this.terminal = bagEnd.getId(CgmesNames.TERMINAL);
        }
    }
}
