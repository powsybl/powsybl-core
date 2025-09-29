/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.google.common.collect.Range;
import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.powsybl.cgmes.conversion.CgmesReports.badVoltageTargetValueRegulatingControlReport;
import static com.powsybl.cgmes.conversion.RegulatingControlMapping.isControlModeReactivePower;
import static com.powsybl.cgmes.conversion.RegulatingControlMapping.isControlModeVoltage;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractReactiveLimitsOwnerConversion extends AbstractConductingEquipmentConversion {

    public AbstractReactiveLimitsOwnerConversion(
        String type,
        PropertyBag p,
        Context context) {
        super(type, p, context);
    }

    private void getReactiveCapabilityCurveData(PropertyBag propertyBag, Map<Double, Range<Double>> qRanges) {
        double p = propertyBag.asDouble("xvalue");
        double minQ = propertyBag.asDouble("y1value");
        double maxQ = propertyBag.asDouble("y2value");

        if (!checkPointValidity(p, minQ, maxQ)) {
            return;
        }

        Range<Double> qRange = fixedMinMaxQ("ReactiveCapabilityCurve Point", minQ, maxQ);

        // check if there is a previous point with same p
        Range<Double> prev = qRanges.get(p);
        if (prev != null) {
            if (prev.isConnected(qRange)) {
                fixed("reactive capability curve", () -> String.format("point merged with another one with same p (%f)", p));
                qRanges.put(p, prev.span(qRange));
            } else {
                ignored("reactive capability curve point", () -> String.format("another point with same p (%f) and a disconnected reactive range", p));
            }
        } else {
            qRanges.put(p, qRange);
        }
    }

    protected void convertReactiveLimits(ReactiveLimitsHolder g) {
        // IIDM only accepts one definition of limits,
        // either MinMaxReactiveLimits or ReactiveCapabilityCurve.
        // If an element has both definitions we will select the
        // ReactiveCapabilityCurve.
        if (p.containsKey("ReactiveCapabilityCurve")) {
            String curveId = p.getId("ReactiveCapabilityCurve");
            PropertyBags curveData = context.reactiveCapabilityCurveData(curveId);

            Map<Double, Range<Double>> qRanges = new HashMap<>();
            curveData.forEach(d -> getReactiveCapabilityCurveData(d, qRanges));

            if (qRanges.size() >= 2) {
                // create curve
                ReactiveCapabilityCurveAdder rcca = g.newReactiveCapabilityCurve();
                for (Map.Entry<Double, Range<Double>> e : qRanges.entrySet()) {
                    double p = e.getKey();
                    Range<Double> qRange = e.getValue();
                    rcca.beginPoint()
                            .setP(p)
                            .setMinQ(qRange.lowerEndpoint())
                            .setMaxQ(qRange.upperEndpoint())
                            .endPoint();
                }
                rcca.add();
            } else if (qRanges.size() == 1) {
                fixed("reactive capability curve", "only one point");
                Map.Entry<Double, Range<Double>> e = qRanges.entrySet().iterator().next();
                Range<Double> qRange = e.getValue();
                g.newMinMaxReactiveLimits()
                            .setMinQ(qRange.lowerEndpoint())
                            .setMaxQ(qRange.upperEndpoint())
                            .add();
            } else {
                missing("Empty curve");
                convertMinMaxQ(g);
            }
        } else {
            convertMinMaxQ(g);
        }
    }

    private void convertMinMaxQ(ReactiveLimitsHolder g) {
        if (p.containsKey("minQ") && p.containsKey("maxQ")) {
            double minQ = p.asDouble("minQ");
            double maxQ = p.asDouble("maxQ");
            Range<Double> qRange = fixedMinMaxQ("Reactive Limits", minQ, maxQ);
            g.newMinMaxReactiveLimits()
                    .setMinQ(qRange.lowerEndpoint())
                    .setMaxQ(qRange.upperEndpoint())
                    .add();
        } else {
            missing("minQ/maxQ are missing, default to unbounded reactive limits");
        }
    }

    private boolean checkPointValidity(double p, double minQ, double maxQ) {
        if (Double.isNaN(p) || Double.isNaN(minQ) || Double.isNaN(maxQ)) {
            Supplier<String> reason = () -> String.format("Incomplete point p, minQ, maxQ = %f, %f, %f", p, minQ, maxQ);
            ignored("ReactiveCapabilityCurvePoint", reason);
            return false;
        }
        return true;
    }

    private Range<Double> fixedMinMaxQ(String context, double minQ, double maxQ) {
        if (minQ > maxQ) {
            Supplier<String> reason = () -> String.format("minQ > maxQ (%.4f > %.4f)", minQ, maxQ);
            fixed(context, reason);
            return Range.closed(maxQ, minQ);
        }
        return Range.closed(minQ, maxQ);
    }

    protected void setMinPMaxP(GeneratorAdder adder, double minP, double maxP) {
        if (minP > maxP) {
            context.fixed("Active power limits", String.format("minP (%f) > maxP (%f)", minP, maxP));
            adder.setMinP(maxP).setMaxP(minP);
            return;
        }
        adder.setMinP(minP).setMaxP(maxP);
    }

    protected static void updateRegulatingControl(Generator generator, boolean controlEnabled, Context context) {
        String mode = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.MODE);

        if (isControlModeVoltage(mode)) {
            updateRegulatingControlVoltage(generator, controlEnabled, context);
        } else if (isControlModeReactivePower(mode)) {
            updateRegulatingControlReactivePower(generator, controlEnabled, context);
        } else {
            context.ignored(mode, "Unsupported regulation mode for generator " + generator.getId());
        }
    }

    private static void updateRegulatingControlVoltage(Generator generator, boolean controlEnabled, Context context) {
        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(generator, context);

        double defaultTargetV = getDefaultTargetV(generator, context);
        double targetV = cgmesRegulatingControl.map(propertyBag -> findTargetV(propertyBag, defaultTargetV, DefaultValueUse.NOT_VALID)).orElse(defaultTargetV);
        boolean defaultRegulatingOn = getDefaultRegulatingOn(generator, context);
        boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

        boolean validTargetV = isValidTargetV(targetV);
        if (!validTargetV) {
            context.invalid(generator.getId(), "Regulating control has a bad target voltage " + targetV);
            badVoltageTargetValueRegulatingControlReport(context.getReportNode(), generator.getId(), targetV);
        }

        // Regulating control is enabled AND this equipment participates in regulating control
        setVoltageRegulation(generator, targetV, regulatingOn && controlEnabled && validTargetV);
    }

    // TargetV must be valid before the regulation is turned on,
    // and the regulation must be turned off before assigning potentially invalid regulation values,
    // to ensure consistency with the applied checks
    private static void setVoltageRegulation(Generator generator, double targetV, boolean regulatingOn) {
        if (regulatingOn) {
            generator
                    .setTargetV(targetV)
                    .setVoltageRegulatorOn(true);
        } else {
            generator
                    .setVoltageRegulatorOn(false)
                    .setTargetV(targetV);
        }
    }

    private static void updateRegulatingControlReactivePower(Generator generator, boolean controlEnabled, Context context) {
        RemoteReactivePowerControl remoteReactivePowerControl = generator.getExtension(RemoteReactivePowerControl.class);
        if (remoteReactivePowerControl == null || remoteReactivePowerControl.getRegulatingTerminal() == null) {
            return;
        }
        Optional<PropertyBag> cgmesRegulatingControl = findCgmesRegulatingControl(generator, context);
        int terminalSign = findTerminalSign(generator);
        double defaultTargetQ = getDefaultTargetQ(remoteReactivePowerControl, context);
        boolean defaultRegulatingOn = getDefaultRegulatingOn(remoteReactivePowerControl, context);

        double targetQ = cgmesRegulatingControl.map(propertyBag -> findTargetQ(propertyBag, terminalSign, defaultTargetQ, DefaultValueUse.NOT_DEFINED)).orElse(defaultTargetQ);
        boolean regulatingOn = cgmesRegulatingControl.map(propertyBag -> findRegulatingOn(propertyBag, defaultRegulatingOn, DefaultValueUse.NOT_DEFINED)).orElse(defaultRegulatingOn);

        setReactivePowerRegulation(remoteReactivePowerControl, targetQ, regulatingOn && controlEnabled && isValidTargetQ(targetQ));
    }

    // TargetQ must be valid before the regulation is turned on,
    // and the regulation must be turned off before assigning potentially invalid regulation values,
    // to ensure consistency with the applied checks
    private static void setReactivePowerRegulation(RemoteReactivePowerControl remoteReactivePowerControl, double targetQ, boolean regulatingOn) {
        if (regulatingOn) {
            remoteReactivePowerControl
                    .setTargetQ(targetQ)
                    .setEnabled(true);
        } else {
            remoteReactivePowerControl
                    .setEnabled(false)
                    .setTargetQ(targetQ);
        }
    }

    private static double getDefaultTargetV(Generator generator, Context context) {
        double defaultTargetV = Optional.ofNullable(generator.getRegulatingTerminal())
                .orElse(generator.getTerminal())
                .getVoltageLevel().getNominalV();
        return getDefaultValue(null, generator.getTargetV(), defaultTargetV, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(Generator generator, Context context) {
        return getDefaultValue(false, generator.isVoltageRegulatorOn(), false, false, context);
    }

    private static double getDefaultTargetQ(RemoteReactivePowerControl remoteReactivePowerControl, Context context) {
        return getDefaultValue(null, remoteReactivePowerControl.getTargetQ(), Double.NaN, Double.NaN, context);
    }

    private static boolean getDefaultRegulatingOn(RemoteReactivePowerControl remoteReactivePowerControl, Context context) {
        return getDefaultValue(false, remoteReactivePowerControl.isEnabled(), false, false, context);
    }
}
