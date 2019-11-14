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
public class NewTwoWindingsTransformerConversion extends AbstractTransformerConversion {

    public NewTwoWindingsTransformerConversion(PropertyBags ends,
        Map<String, PropertyBag> powerTransformerRatioTapChanger,
        Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super(STRING_POWER_TRANSFORMER, ends, context);
        this.powerTransformerRatioTapChanger = powerTransformerRatioTapChanger;
        this.powerTransformerPhaseTapChanger = powerTransformerPhaseTapChanger;
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (context.boundary().containsNode(nodeId(1))
            || context.boundary().containsNode(nodeId(2))) {
            invalid("2 windings transformer end point at boundary is not supported");
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        CgmesT2xModel cgmesT2xModel = load();
    }

    private CgmesT2xModel load() {
        // ends = ps
        PropertyBag end1 = ps.get(0);
        PropertyBag end2 = ps.get(1);

        double x1 = end1.asDouble(STRING_X);
        double x2 = end2.asDouble(STRING_X);
        double r = end1.asDouble(STRING_R) + end2.asDouble(STRING_R);
        double x = x1 + x2;

        String terminal1 = end1.getId(CgmesNames.TERMINAL);
        String terminal2 = end2.getId(CgmesNames.TERMINAL);

        PropertyBag rtc1 = getTransformerTapChanger(end1, STRING_RATIO_TAP_CHANGER, powerTransformerRatioTapChanger);
        PropertyBag ptc1 = getTransformerTapChanger(end1, STRING_PHASE_TAP_CHANGER, powerTransformerPhaseTapChanger);
        PropertyBag rtc2 = getTransformerTapChanger(end2, STRING_RATIO_TAP_CHANGER, powerTransformerRatioTapChanger);
        PropertyBag ptc2 = getTransformerTapChanger(end2, STRING_PHASE_TAP_CHANGER, powerTransformerPhaseTapChanger);

        double ratedU1 = end1.asDouble(STRING_RATEDU);
        double ratedU2 = end2.asDouble(STRING_RATEDU);

        TapChangerConversion ratioTapChanger1 = getRatioTapChanger(rtc1);
        TapChangerConversion ratioTapChanger2 = getRatioTapChanger(rtc2);
        TapChangerConversion phaseTapChanger1 = getPhaseTapChanger(ptc1, x);
        TapChangerConversion phaseTapChanger2 = getPhaseTapChanger(ptc2, x);

        CgmesT2xModel cgmesT2xModel = new CgmesT2xModel();
        cgmesT2xModel.end1.g = end1.asDouble(STRING_G, 0);
        cgmesT2xModel.end1.b = end1.asDouble(STRING_B);
        cgmesT2xModel.end1.ratioTapChanger = ratioTapChanger1;
        cgmesT2xModel.end1.phaseTapChanger = phaseTapChanger1;
        cgmesT2xModel.end1.ratedU = ratedU1;
        cgmesT2xModel.end1.phaseAngleClock = end1.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesT2xModel.end1.terminal = terminal1;

        if (x1 == 0.0) {
            cgmesT2xModel.end1.xIsZero = true;
        } else {
            cgmesT2xModel.end1.xIsZero = false;
        }
        cgmesT2xModel.end1.rtcDefined = rtc1 != null && rtc1.asDouble(STRING_STEP_VOLTAGE_INCREMENT) != 0.0;

        cgmesT2xModel.end2.g = end2.asDouble(STRING_G, 0);
        cgmesT2xModel.end2.b = end2.asDouble(STRING_B);
        cgmesT2xModel.end2.ratioTapChanger = ratioTapChanger2;
        cgmesT2xModel.end2.phaseTapChanger = phaseTapChanger2;
        cgmesT2xModel.end2.ratedU = ratedU2;
        cgmesT2xModel.end2.phaseAngleClock = end2.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesT2xModel.end2.terminal = terminal2;

        if (x2 == 0.0) {
            cgmesT2xModel.end2.xIsZero = true;
        } else {
            cgmesT2xModel.end2.xIsZero = false;
        }
        cgmesT2xModel.end2.rtcDefined = rtc2 != null && rtc2.asDouble(STRING_STEP_VOLTAGE_INCREMENT) != 0.0;

        cgmesT2xModel.r = r;
        cgmesT2xModel.x = x;

        return cgmesT2xModel;
    }

    static class CgmesT2xModel {
        double r;
        double x;
        CgmesEnd end1 = new CgmesEnd();
        CgmesEnd end2 = new CgmesEnd();
    }

    static class CgmesEnd {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        int phaseAngleClock;
        String terminal;
        boolean xIsZero;
        boolean rtcDefined;
    }

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}
