/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.AbstractObjectConversion;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesTapChangerBuilder {

    protected final Context context;
    protected final PropertyBag p;
    protected final TapChanger tapChanger;

    CgmesTapChangerBuilder(PropertyBag p, Context context) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(context);
        this.context = context;
        this.p = p;
        tapChanger = new TapChanger();
    }

    static CgmesRatioTapChangerBuilder newRatioTapChanger(PropertyBag ratioTapChanger, Context context) {
        return new CgmesRatioTapChangerBuilder(ratioTapChanger, context);
    }

    static CgmesPhaseTapChangerBuilder newPhaseTapChanger(PropertyBag phaseTapChanger, double xtx, Context context) {
        return new CgmesPhaseTapChangerBuilder(phaseTapChanger, xtx, context);
    }

    protected int initialTapPosition(int defaultStep) {
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                return AbstractObjectConversion.fromContinuous(p.asDouble(CgmesNames.STEP, p.asDouble(CgmesNames.SV_TAP_STEP, defaultStep)));
            case SV:
                return AbstractObjectConversion.fromContinuous(p.asDouble(CgmesNames.SV_TAP_STEP, p.asDouble(CgmesNames.STEP, defaultStep)));
            default:
                throw new CgmesModelException("Unexpected profile used for initial flows values: " + context.config().getProfileUsedForInitialStateValues());
        }
    }

    double fixing(PropertyBag point, String attr, double defaultValue, String tableId, int step) {
        double value = point.asDouble(attr, defaultValue);
        if (Double.isNaN(value)) {
            context.fixed(
                "RatioTapChangerTablePoint " + attr + " for step " + step + " in table " + tableId,
                "invalid value " + point.get(attr));
            return defaultValue;
        }
        return value;
    }
}
