/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class RatioTapChangerConversion extends AbstractIdentifiedObjectConversion {

    public RatioTapChangerConversion(PropertyBag rtc, Context context) {
        super("RatioTapChanger", rtc, context);

        tx2 = context.tapChangerTransformers().transformer2(id);
        tx3 = context.tapChangerTransformers().transformer3(id);
        lowStep = rtc.asInt("lowStep");
        highStep = rtc.asInt("highStep");
        neutralStep = rtc.asInt("neutralStep");
        position = fromContinuous(p.asDouble("SVtapStep", neutralStep));
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
                    tx3.getName(),
                    tx3.getSubstation().getName());
                // Check if the step is at neutral and regulating control is disabled
                boolean regulating = p.asBoolean("regulatingControlEnabled", false);
                if (position == neutralStep && !regulating) {
                    ignored(reason0 + ", but is at neutralStep and regulating control disabled");
                } else {
                    String reason = String.format(
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
        String tapChangerControl = p.getId("TapChangerControl");
        if (tapChangerControl != null) {
            addRegulatingControl(rtca);
        } else {
            rtca.setLoadTapChangingCapabilities(false);
        }
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
                return tx3.getLeg2().newRatioTapChanger();
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
        if (LOG.isDebugEnabled() && rtcAtSide1) {
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
                dz = (rho2 - 1) * 100;
                dy = (1 / rho2 - 1) * 100;
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
        // For 3 winding transformers rho is always 1 + n*du
        if (tx2 != null) {
            return context.tapChangerTransformers().whichSide(id) == 1;
        }
        return false;
    }

    private void addRegulatingControl(RatioTapChangerAdder rtca) {
        String mode = p.getLocal("regulatingControlMode").toLowerCase();
        if (mode.endsWith("voltage")) {
            addRegulatingControlVoltage(rtca);
        } else if (mode.endsWith("fixed")) {
            rtca.setLoadTapChangingCapabilities(false);
        } else {
            rtca.setLoadTapChangingCapabilities(false);
            ignored(mode, "Unsupported regulation mode");
        }
    }

    private void addRegulatingControlVoltage(RatioTapChangerAdder rtca) {
        double regulatingControlValue = p.asDouble("regulatingControlTargetValue");
        boolean regulating = p.asBoolean("regulatingControlEnabled", false);
        // Even if regulating is false, we reset the target voltage if it is not valid
        double targetV = regulatingControlValue;
        if (targetV <= 0) {
            String reg = p.getId("TapChangerControl");
            ignored(reg, String.format("Regulating control has a bad target voltage %f", targetV));
            regulating = false;
            targetV = Float.NaN;
        }
        Terminal regulationTerminal = null;
        // TODO Find the Network terminal mapped to the rtc terminal,
        // If original terminal has not been mapped,
        // find the IIDM terminal of the CGMES topological node
        // associated with the rtc terminal
        // (Check code in CIM1 Importer)
        regulationTerminal = terminal();

        rtca.setLoadTapChangingCapabilities(true)
            .setRegulating(regulating)
            .setTargetV(targetV)
            .setRegulationTerminal(regulationTerminal);
    }

    private Terminal terminal() {
        int side = context.tapChangerTransformers().whichSide(id);
        if (tx2 != null) {
            if (side == 1) {
                return tx2.getTerminal1();
            } else if (side == 2) {
                return tx2.getTerminal2();
            }
        } else if (tx3 != null) {
            if (side == 1) {
                // invalid
            } else if (side == 2) {
                return tx3.getLeg2().getTerminal();
            } else if (side == 3) {
                return tx3.getLeg3().getTerminal();
            }
        }
        return null;
    }

    private boolean tabular() {
        return p.containsKey(CgmesNames.RATIO_TAP_CHANGER_TABLE);
    }

    private final TwoWindingsTransformer tx2;
    private final ThreeWindingsTransformer tx3;
    private final int lowStep;
    private final int highStep;
    private final int neutralStep;
    private final int position;

    private static final Logger LOG = LoggerFactory.getLogger(RatioTapChangerConversion.class);
}
