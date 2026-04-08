/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class CgmesT3xModel {

    final CgmesEnd end1;
    final CgmesEnd end2;
    final CgmesEnd end3;

    public CgmesT3xModel(PropertyBags ends, Context context) {
        this.end1 = new CgmesEnd(ends.get(0), context);
        this.end2 = new CgmesEnd(ends.get(1), context);
        this.end3 = new CgmesEnd(ends.get(2), context);
    }

    static class CgmesEnd {
        final double r;
        final double x;
        final double g;
        final double b;
        final TapChanger ratioTapChanger;
        final TapChanger phaseTapChanger;
        final double ratedU;
        final String terminal;
        final Double ratedS;

        CgmesEnd(PropertyBag end, Context context) {
            this.r = end.asDouble(CgmesNames.R);
            this.x = end.asDouble(CgmesNames.X);
            this.g = end.asDouble(CgmesNames.G, 0);
            this.b = end.asDouble(CgmesNames.B);
            this.ratioTapChanger = TapChanger.ratioTapChangerFromEnd(end, context);
            this.phaseTapChanger = TapChanger.phaseTapChangerFromEnd(end, x, context);
            this.ratedU = end.asDouble(CgmesNames.RATEDU);
            this.terminal = end.getId(CgmesNames.TERMINAL);
            double ratedS0 = end.asDouble(CgmesNames.RATEDS, 0);
            this.ratedS = ratedS0 > 0 ? ratedS0 : null;
        }
    }
}
