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
abstract class AbstractCgmesTapChangerBuilder {

    protected final Context context;
    protected final PropertyBag p;
    protected final TapChanger tapChanger;

    AbstractCgmesTapChangerBuilder(PropertyBag p, Context context) {
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

    protected TapChanger build() {
        int lowStep = p.asInt(CgmesNames.LOW_STEP);
        int highStep = p.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = p.asInt(CgmesNames.NEUTRAL_STEP);
        int normalStep = p.asInt(CgmesNames.NORMAL_STEP, neutralStep);
        int position = initialTapPosition(normalStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition(position);

        boolean ltcFlag = p.asBoolean(CgmesNames.LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addRegulationData();
        addSteps();
        return tapChanger;
    }

    protected abstract void addRegulationData();

    protected abstract void addSteps();

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
