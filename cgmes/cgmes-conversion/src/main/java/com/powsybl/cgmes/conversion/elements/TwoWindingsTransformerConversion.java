/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TwoWindingsTransformerConversion extends AbstractConductingEquipmentConversion {

    protected static final String STRING_PHASE_ANGLE_CLOCK = "phaseAngleClock";

    public TwoWindingsTransformerConversion(PropertyBags ends, Map<String, PropertyBag> powerTransformerRatioTapChanger,
        Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super("PowerTransformer", ends, context);
        end1 = ends.get(0);
        end2 = ends.get(1);
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

        double r1 = end1.asDouble("r");
        double x1 = end1.asDouble("x");
        double b1 = end1.asDouble("b");
        double g1 = end1.asDouble("g", 0);
        double r2 = end2.asDouble("r");
        double x2 = end2.asDouble("x");
        double b2 = end2.asDouble("b");
        double g2 = end2.asDouble("g", 0);
        double ratedU1 = end1.asDouble("ratedU");
        double ratedU2 = end2.asDouble("ratedU");

        double rho0 = ratedU2 / ratedU1;
        double rho0Square = rho0 * rho0;
        double r0 = r1 * rho0Square + r2;
        double x0 = x1 * rho0Square + x2;
        double g0 = g1 / rho0Square + g2;
        double b0 = b1 / rho0Square + b2;

        TwoWindingsTransformerAdder adder = substation().newTwoWindingsTransformer()
                .setR(r0)
                .setX(x0)
                .setG(g0)
                .setB(b0)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2);
        identify(adder);
        connect(adder);
        TwoWindingsTransformer tx = adder.add();
        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        addTapChangers(tx);

        int phaseAngleClock1 = end1.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        int phaseAngleClock2 = end2.asInt(STRING_PHASE_ANGLE_CLOCK, 0);

        // add phaseAngleClock as an extension, cgmes does not allow pac at end1
        if (phaseAngleClock1 != 0) {
            String reason = "Unsupported modelling: twoWindingsTransformer with phaseAngleClock at end1";
            ignored("phaseAngleClock end1", reason);
        }
        if (phaseAngleClock2 != 0) {
            TwoWindingsTransformerPhaseAngleClock phaseAngleClock = new TwoWindingsTransformerPhaseAngleClock(tx, phaseAngleClock2);
            tx.addExtension(TwoWindingsTransformerPhaseAngleClock.class, phaseAngleClock);
        }
    }

    private void addTapChangers(TwoWindingsTransformer tx) {
        String rtcPropertyName = "RatioTapChanger";
        String rtc1 = end1.getId(rtcPropertyName);
        String rtc2 = end2.getId(rtcPropertyName);
        String ptcPropertyName = "PhaseTapChanger";
        String ptc1 = end1.getId(ptcPropertyName);
        String ptc2 = end2.getId(ptcPropertyName);

        // used only for debugging in the current conversion, it will not be needed in the full conversion
        if (context.config().allowUnsupportedTapChangers()) {
            context.tapChangerTransformers().add(rtc1, tx, "rtc", 1);
            context.tapChangerTransformers().add(rtc2, tx, "rtc", 2);
            context.tapChangerTransformers().add(ptc1, tx, "ptc", 1);
            context.tapChangerTransformers().add(ptc2, tx, "ptc", 2);

            boolean supported = true;
            String rtc = null;
            String ptc = null;
            if (rtc1 != null && rtc2 != null) {
                supported = false;
            } else if (rtc1 != null) {
                rtc = rtc1;
            } else if (rtc2 != null) {
                rtc = rtc2;
            }
            if (ptc1 != null && ptc2 != null) {
                supported = false;
            } else if (ptc1 != null) {
                ptc = ptc1;
            } else if (ptc2 != null) {
                ptc = ptc2;
            }
            if (supported) {
                setRegulatingControlContext(tx, rtc, ptc);
            }
            return;
        }

        // Ensure only one tap changer is defined
        String rtc = null;
        int rtcSide = 0;
        if (rtc1 != null) {
            if (rtc2 != null) {
                String reason = "Unsupported modelling: two winding transformer with two ratio tap changers";
                invalid(reason);
                throw new PowsyblException(
                        String.format("TwoWindingTransformer %s %s", id, reason));
            }
            rtc = rtc1;
            rtcSide = 1;
        } else if (rtc2 != null) {
            rtc = rtc2;
            rtcSide = 2;
        }
        if (rtc != null) {
            context.tapChangerTransformers().add(rtc, tx, "rtc", rtcSide);
        }
        String ptc = null;
        int ptcSide = 0;
        if (ptc1 != null) {
            if (ptc2 != null) {
                String reason = "Unsupported modelling: transformer with two phase tap changers";
                invalid(reason);
                throw new PowsyblException(
                        String.format("TwoWindingTransformer %s %s", id, reason));
            }
            ptc = ptc1;
            ptcSide = 1;
        } else if (ptc2 != null) {
            ptc = ptc2;
            ptcSide = 2;
        }
        if (ptc != null) {
            context.tapChangerTransformers().add(ptc, tx, "ptc", ptcSide);
        }
        if (rtcSide > 0 && ptcSide > 0 && rtcSide != ptcSide) {
            String reason = String.format(
                    "Unsupported modelling: transformer with ratio and tap changer not on the same winding, rtc: %s, ptc: %s",
                    rtc,
                    ptc);
            invalid(reason);
        }

        setRegulatingControlContext(tx, rtc, ptc);
    }

    private void setRegulatingControlContext(TwoWindingsTransformer tx, String rtcId, String ptcId) {
        PropertyBag rtc = null;
        if (rtcId != null) {
            rtc = powerTransformerRatioTapChanger.get(rtcId);
        }

        PropertyBag ptc = null;
        if (ptcId != null) {
            ptc = powerTransformerPhaseTapChanger.get(ptcId);
        }
        context.regulatingControlMapping().forTransformers().add(tx.getId(), rtcId, rtc, ptc);
    }

    private final PropertyBag end1;
    private final PropertyBag end2;
    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}


