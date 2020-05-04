/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Comparator;
import java.util.function.Supplier;

import com.powsybl.cgmes.conversion.elements.transformers.NewThreeWindingsTransformerConversion;
import com.powsybl.cgmes.model.CgmesModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 *
 * @deprecated Use {@link NewThreeWindingsTransformerConversion} or {@link NewThreeWindingsTransformerConversion} instead.
 */
@Deprecated
public class RatioTapChangerConversion extends AbstractIdentifiedObjectConversion {

    public RatioTapChangerConversion(PropertyBag rtc, Context context) {
        super("RatioTapChanger", rtc, context);

        tx2 = context.tapChangerTransformers().transformer2(id);
        tx3 = context.tapChangerTransformers().transformer3(id);
        lowStep = rtc.asInt("lowStep");
        highStep = rtc.asInt("highStep");
        neutralStep = rtc.asInt("neutralStep");
        position = getTapPosition(rtc.asInt("normalStep", neutralStep));
        ltcFlag = rtc.asBoolean("ltcFlag", false);
    }

    @Override
    public boolean valid() {
        if (tx2 == null && tx3 == null) {
            invalid("Missing transformer");
            return false;
        }
        if (tx3 != null) {
            int side = context.tapChangerTransformers().whichSide(id);
            if (side == 1) {
                String reason0 = String.format(
                        "Not supported at end 1 of 3wtx. txId 'name' 'substation': %s '%s' '%s'",
                        tx3.getId(),
                        tx3.getNameOrId(),
                        tx3.getSubstation().getNameOrId());
                // Check if the step is at neutral and regulating control is disabled
                boolean regulating = p.asBoolean("regulatingControlEnabled", false);
                if (position == neutralStep && !regulating) {
                    ignored(reason0 + ", but is at neutralStep and regulating control disabled");
                } else {
                    Supplier<String> reason = () -> String.format(
                            "%s, tap step: %d, regulating control enabled: %b",
                            reason0,
                            position,
                            regulating);
                    invalid(reason);
                }
                return false;
            }
        }
        return inRange("defaultStep", neutralStep, lowStep, highStep) &&
                inRange("position", position, lowStep, highStep);
    }

    @Override
    public void convert() {
        RatioTapChangerAdder rtca = adder();
        if (rtca == null) {
            invalid("Could not create ratio tap changer adder");
            return;
        }
        rtca.setLowTapPosition(lowStep).setTapPosition(position);
        if (tabular()) {
            addStepsFromTable(rtca);
        } else {
            addStepsFromStepVoltageIncrement(rtca);
        }

        rtca.setLoadTapChangingCapabilities(ltcFlag);
        rtca.add();
    }

    private RatioTapChangerAdder adder() {
        if (tx2 != null) {
            return tx2.newRatioTapChanger();
        } else if (tx3 != null) {
            int side = context.tapChangerTransformers().whichSide(id);
            if (side == 1) {
                // No supported in IIDM model
                return null;
            } else if (side == 2) {
                return tx3.getLeg2().newRatioTapChanger();
            } else if (side == 3) {
                return tx3.getLeg3().newRatioTapChanger();
            }
        }
        return null;
    }

    private void addStepsFromTable(RatioTapChangerAdder rtca) {
        String tableId = p.getId(CgmesNames.RATIO_TAP_CHANGER_TABLE);
        if (tableId == null) {
            missing(CgmesNames.RATIO_TAP_CHANGER_TABLE);
            return;
        }
        LOG.debug("RatioTapChanger {} table {}", id, tableId);
        PropertyBags table = context.ratioTapChangerTable(tableId);
        if (table.isEmpty()) {
            missing("points for RatioTapChangerTable " + tableId);
            return;
        }
        Comparator<PropertyBag> byStep = Comparator.comparingInt((PropertyBag p) -> p.asInt("step"));
        table.sort(byStep);
        boolean rtcAtSide1 = rtcAtSide1();
        for (PropertyBag point : table) {

            // CGMES uses ratio to define the relationship between voltage ends while IIDM uses rho
            // ratio and rho as complex numbers are reciprocals. Given V1 and V2 the complex voltages at end 1 and end 2 of a branch we have:
            // V2 = V1 * rho and V2 = V1 / ratio
            // This is why we have: rho=1/ratio
            double rho = 1 / point.asDouble("ratio", 1.0);

            // When given in RatioTapChangerTablePoint
            // r, x, g, b of the step are already percentage deviations of nominal values
            int step = point.asInt("step");
            double r = fixing(point, "r", 0, tableId, step);
            double x = fixing(point, "x", 0, tableId, step);
            double g = fixing(point, "g", 0, tableId, step);
            double b = fixing(point, "b", 0, tableId, step);
            // Impedance/admittance deviation is required when tap changer is defined at
            // side 2
            // (In IIDM model the ideal ratio is always at side 1, left of impedance)
            double dz = 0;
            double dy = 0;
            if (!rtcAtSide1) {
                double rho2 = rho * rho;
                dz = (1 / rho2 - 1) * 100;
                dy = (rho2 - 1) * 100;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("    {} {} {} {} {} {} {} {}", step, rho, r, x, g, b, dz, dy);
            }
            // We have to merge previous explicit corrections defined for the tap
            // with dz, dy that appear when moving ideal ratio to side 1
            // R' = R * (1 + r/100) * (1 + dz/100) ==> r' = r + dz + r * dz / 100
            rtca.beginStep()
                    .setRho(rtcAtSide1 ? rho : 1 / rho)
                    .setR(r + dz + r * dz / 100)
                    .setX(x + dz + r * dz / 100)
                    .setG(g + dy + g * dy / 100)
                    .setB(b + dy + b * dy / 100)
                    .endStep();
        }
    }

    private double fixing(PropertyBag point, String attr, double defaultValue, String tableId, int step) {
        double value = point.asDouble(attr, defaultValue);
        if (Double.isNaN(value)) {
            fixed(
                "RatioTapChangerTablePoint " + attr + " for step " + step + " in table " + tableId,
                "invalid value " + point.get(attr));
            return defaultValue;
        }
        return value;
    }

    private void addStepsFromStepVoltageIncrement(RatioTapChangerAdder rtca) {
        boolean rtcAtSide1 = rtcAtSide1();
        if (LOG.isDebugEnabled() && rtcAtSide1 && tx2 != null) {
            LOG.debug(
                    "Transformer {} ratio tap changer moved from side 2 to side 1, impedance/admittance corrections",
                    tx2.getId());
        }
        double stepVoltageIncrement = p.asDouble("stepVoltageIncrement");
        double du = stepVoltageIncrement / 100;
        for (int step = lowStep; step <= highStep; step++) {
            int n = step - neutralStep;
            double rho = rtcAtSide1 ? 1 / (1 + n * du) : (1 + n * du);

            // Impedance/admittance deviation is required when ratio tap changer
            // is defined at side 2
            // (In IIDM model the ideal ratio is always at side 1)
            double dz = 0;
            double dy = 0;
            if (!rtcAtSide1) {
                double rho2 = rho * rho;
                dz = (rho2 - 1) * 100;     // Use the initial ratio before moving it
                dy = (1 / rho2 - 1) * 100; // Use the initial ratio before moving it
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("RTC2to1 corrections  %4d  %12.8f  %12.8f  %12.8f",
                            step, n * du, dz, dy));
                }
            }
            rtca.beginStep()
                    .setRho(rho)
                    .setR(dz)
                    .setX(dz)
                    .setG(dy)
                    .setB(dy)
                    .endStep();
        }
    }

    private boolean rtcAtSide1() {
        // From CIM1 converter:
        // For 2 winding transformers, rho is 1/(1 + n*du) if rtc is at side 1
        // For 3 winding transformers rho is always considered at side 1 (network side)
        if (tx2 != null) {
            return context.tapChangerTransformers().whichSide(id) == 1;
        } else if (tx3 != null) {
            return true;
        }
        return false;
    }

    private boolean tabular() {
        return p.containsKey(CgmesNames.RATIO_TAP_CHANGER_TABLE);
    }

    private int getTapPosition(int defaultStep) {
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                return fromContinuous(p.asDouble("step", p.asDouble("SVtapStep", defaultStep)));
            case SV:
                return fromContinuous(p.asDouble("SVtapStep", p.asDouble("step", defaultStep)));
            default:
                throw new CgmesModelException("Unexpected profile used for initial flows values: " + context.config().getProfileUsedForInitialStateValues());
        }
    }

    private final TwoWindingsTransformer tx2;
    private final ThreeWindingsTransformer tx3;
    private final int lowStep;
    private final int highStep;
    private final int neutralStep;
    private final int position;
    private final boolean ltcFlag;

    private static final Logger LOG = LoggerFactory.getLogger(RatioTapChangerConversion.class);
}
