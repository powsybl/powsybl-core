/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.AbstractObjectConversion;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
abstract class AbstractCgmesTapChangerBuilder {

    protected final Context context;
    protected final PropertyBag p;
    protected final TapChanger tapChanger;

    protected int lowStep;
    protected int highStep;

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
        switch (context.config().getProfileForInitialValuesShuntSectionsTapPositions()) {
            case SSH:
                return AbstractObjectConversion.fromContinuous(p.asDouble(CgmesNames.STEP, p.asDouble(CgmesNames.SV_TAP_STEP, defaultStep)));
            case SV:
                return AbstractObjectConversion.fromContinuous(p.asDouble(CgmesNames.SV_TAP_STEP, p.asDouble(CgmesNames.STEP, defaultStep)));
            default:
                throw new CgmesModelException("Unexpected profile used for initial values: " + context.config().getProfileForInitialValuesShuntSectionsTapPositions());
        }
    }

    protected TapChanger build() {
        lowStep = p.asInt(CgmesNames.LOW_STEP);
        highStep = p.asInt(CgmesNames.HIGH_STEP);
        addSteps();
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
        return tapChanger;
    }

    protected boolean isTableValid(String tableId, PropertyBags table) {
        int min = table.stream().map(step -> step.asInt(CgmesNames.STEP)).min(Integer::compareTo).orElseThrow(() -> new PowsyblException("Should at least contain one step"));
        for (int i = min; i < min + table.size(); i++) {
            int index = i;
            if (table.stream().noneMatch(step -> step.asInt(CgmesNames.STEP) == index)) {
                context.ignored("TapChanger table", () -> String.format("There is at least one missing step (%s) in table %s. Tap changer considered linear", index, tableId));
                return false;
            }
        }
        lowStep = min;
        highStep = min + table.size() - 1;
        return true;
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
