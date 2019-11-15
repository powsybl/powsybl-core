/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class NewThreeWindingsTransformerConversion extends AbstractTransformerConversion {

    public NewThreeWindingsTransformerConversion(PropertyBags ends,
        Map<String, PropertyBag> powerTransformerRatioTapChanger,
        Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super(STRING_POWER_TRANSFORMER, ends, context);
        this.powerTransformerRatioTapChanger = powerTransformerRatioTapChanger;
        this.powerTransformerPhaseTapChanger = powerTransformerPhaseTapChanger;
    }

    @Override
    public void convert() {
        CgmesT3xModel cgmesT3xModel = load();
    }

    private CgmesT3xModel load() {
        CgmesT3xModel cgmesT3xModel = new CgmesT3xModel();

        // ends = ps
        loadWinding(ps.get(0), cgmesT3xModel.winding1);
        loadWinding(ps.get(1), cgmesT3xModel.winding2);
        loadWinding(ps.get(2), cgmesT3xModel.winding3);

        return cgmesT3xModel;
    }

    private void loadWinding(PropertyBag winding, CgmesWinding cgmesModelWinding) {
        PropertyBag rtc = getTransformerTapChanger(winding, STRING_RATIO_TAP_CHANGER,
            powerTransformerRatioTapChanger);
        PropertyBag ptc = getTransformerTapChanger(winding, STRING_PHASE_TAP_CHANGER,
            powerTransformerPhaseTapChanger);

        String terminal = winding.getId(CgmesNames.TERMINAL);
        double ratedU = winding.asDouble(STRING_RATEDU);
        double x = winding.asDouble(STRING_X);

        TapChangerConversion ratioTapChanger = getRatioTapChanger(rtc);
        TapChangerConversion phaseTapChanger = getPhaseTapChanger(ptc, x);

        cgmesModelWinding.r = winding.asDouble(STRING_R);
        cgmesModelWinding.x = x;
        cgmesModelWinding.g = winding.asDouble(STRING_G, 0);
        cgmesModelWinding.b = winding.asDouble(STRING_B);
        cgmesModelWinding.ratioTapChanger = ratioTapChanger;
        cgmesModelWinding.phaseTapChanger = phaseTapChanger;
        cgmesModelWinding.ratedU = ratedU;
        cgmesModelWinding.phaseAngleClock = winding.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesModelWinding.terminal = terminal;
    }

    static class CgmesT3xModel {
        CgmesWinding winding1 = new CgmesWinding();
        CgmesWinding winding2 = new CgmesWinding();
        CgmesWinding winding3 = new CgmesWinding();
    }

    static class CgmesWinding {
        double r;
        double x;
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        int phaseAngleClock;
        String terminal;
    }

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
