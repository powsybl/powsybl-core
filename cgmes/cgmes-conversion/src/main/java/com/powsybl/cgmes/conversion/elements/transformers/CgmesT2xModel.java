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
import org.apache.commons.math3.complex.Complex;

import static com.powsybl.iidm.modification.util.TransformerUtils.impedanceConversion;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class CgmesT2xModel {

    final double r;
    final double x;
    final CgmesPartialEnd end1;
    final CgmesPartialEnd end2;
    final boolean x1IsZero;
    final boolean structuralRatioAtEnd2;
    final Double ratedS;

    public CgmesT2xModel(PropertyBags ends, Context context) {
        PropertyBag bagEnd1 = ends.get(0);
        PropertyBag bagEnd2 = ends.get(1);

        double x1 = bagEnd1.asDouble(CgmesNames.X);
        double x2 = bagEnd2.asDouble(CgmesNames.X);
        double ratedU1 = bagEnd1.asDouble(CgmesNames.RATEDU);
        double ratedU2 = bagEnd2.asDouble(CgmesNames.RATEDU);
        this.x1IsZero = x1 == 0.0;
        this.structuralRatioAtEnd2 = InterpretedT2xModel.structuralRatioAlternative(ratedU1, ratedU2, x1IsZero, context.config());

        this.r = bagEnd1.asDouble(CgmesNames.R) + bagEnd2.asDouble(CgmesNames.R);
        this.x = x1 + x2;
        this.end1 = new CgmesPartialEnd(bagEnd1, x, ratedU1, context);
        double xx2 = x;
        if (structuralRatioAtEnd2) {
            double a0 = ratedU2 / ratedU1;
            xx2 = impedanceConversion(x, new Complex(a0, 0.0));
        }
        this.end2 = new CgmesPartialEnd(bagEnd2, xx2, ratedU2, context);
        this.ratedS = getRatedS(bagEnd1, bagEnd2);
    }

    static Double getRatedS(PropertyBag end1, PropertyBag end2) {
        // For a two-winding transformer the values for the high and low voltage sides shall be identical
        // But is an optional attribute, it may be specified at any end
        double ratedS1 = end1.asDouble(CgmesNames.RATEDS, 0);
        if (ratedS1 > 0) {
            return ratedS1;
        }
        double ratedS2 = end2.asDouble(CgmesNames.RATEDS, 0);
        return ratedS2 > 0.0 ? ratedS2 : null;
    }

    static class CgmesPartialEnd {
        final double g;
        final double b;
        final TapChanger ratioTapChanger;
        final TapChanger phaseTapChanger;
        final double ratedU;
        final String terminal;

        CgmesPartialEnd(PropertyBag bagEnd, double x, double ratedU, Context context) {
            this.g = bagEnd.asDouble(CgmesNames.G, 0);
            this.b = bagEnd.asDouble(CgmesNames.B);
            this.ratioTapChanger = TapChanger.ratioTapChangerFromEnd(bagEnd, context);
            this.phaseTapChanger = TapChanger.phaseTapChangerFromEnd(bagEnd, x, context);
            this.ratedU = ratedU;
            this.terminal = bagEnd.getId(CgmesNames.TERMINAL);
        }
    }
}
