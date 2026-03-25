/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import com.powsybl.loadflow.validation.io.ValidationWriterFactory;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public final class ValidationUtils {

    public static final String VALIDATION_ERROR = "validation error";
    public static final String VALIDATION_WARNING = "validation warning";

    private ValidationUtils() {
    }

    public static ValidationWriter createValidationWriter(String id, ValidationConfig config, Writer writer, ValidationType validationType) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(validationType);
        try {
            ValidationWriterFactory factory = config.getValidationOutputWriter().getValidationWriterFactory().getDeclaredConstructor().newInstance();
            return factory.create(id, config.getTableFormatterFactory(), writer, config.isVerbose(), validationType, config.isCompareResults());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ConfigurationException(e);
        }
    }

    public static boolean areNaN(ValidationConfig config, float... values) {
        Objects.requireNonNull(config);
        if (config.areOkMissingValues()) {
            return false;
        }
        boolean areNaN = false;
        for (float value : values) {
            if (Float.isNaN(value)) {
                areNaN = true;
                break;
            }
        }
        return areNaN;
    }

    public static boolean areNaN(double... values) {
        boolean areMissing = false;
        for (double value : values) {
            if (Double.isNaN(value)) {
                areMissing = true;
                break;
            }
        }
        return areMissing;
    }

    public static boolean areNaN(ValidationConfig config, double... values) {
        Objects.requireNonNull(config);
        if (config.areOkMissingValues()) {
            return false;
        }
        boolean areNaN = false;
        for (double value : values) {
            if (Double.isNaN(value)) {
                areNaN = true;
                break;
            }
        }
        return areNaN;
    }

    public static boolean boundedWithin(double lowerBound, double upperBound, double value, double margin) {
        if (Double.isNaN(value)
                || Double.isNaN(lowerBound) && Double.isNaN(upperBound)) {
            return false;
        }
        if (Double.isNaN(lowerBound)) {
            return value - margin <= upperBound;
        }
        if (Double.isNaN(upperBound)) {
            return value + margin >= lowerBound;
        }
        return value + margin >= lowerBound && value - margin <= upperBound;
    }

    public static boolean isMainComponent(ValidationConfig config, boolean mainComponent) {
        Objects.requireNonNull(config);
        return !config.isCheckMainComponentOnly() || mainComponent;
    }

    public record TerminalState(double v, boolean connected, boolean mainComponent) { }

    public static TerminalState getTerminalState(Terminal terminal) {
        Objects.requireNonNull(terminal);
        Bus bus = terminal.getBusView().getBus();
        Bus connectableBus = terminal.getBusView().getConnectableBus();
        boolean connected = bus != null;
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        boolean mainComponent = connected ? bus.isInMainConnectedComponent() : connectableMainComponent;
        double v = connected ? bus.getV() : Double.NaN;
        return new TerminalState(v, connected, mainComponent);
    }

    public static boolean isUndefinedOrZero(double value, double epsilon) {
        return Double.isNaN(value) || Math.abs(value) <= epsilon;
    }

    public static boolean isOutsideTolerance(double actual, double expected, double epsilon) {
        return Math.abs(actual - expected) > epsilon;
    }

    public static boolean isConnectedAndMainComponent(boolean connected, boolean mainComponent, ValidationConfig config) {
        Objects.requireNonNull(config);
        return connected && isMainComponent(config, mainComponent);
    }

    // expectedQ = - #sections * B * v^2
    public static double computeShuntExpectedQ(double bPerSection, int sectionCount, double v) {
        return -bPerSection * sectionCount * v * v;
    }

    public static double voltageFrom(double vBus, double nominalV) {
        return (Double.isNaN(vBus) || vBus == 0.0) ? nominalV : vBus;
    }

    public static boolean isActivePowerKo(double p, double expectedP, ValidationConfig config, double threshold) {
        return areNaN(config, expectedP) || Math.abs(p + expectedP) > threshold;
    }

    /**
     * Generator: rule for valid result <code> | targetQ - Q | < threshold </code>
     */
    public static boolean isReactivePowerKo(double q, double targetQ, double threshold) {
        return Math.abs(q + targetQ) >= threshold;
    }

    /**
     * Generator: rules for valid result:</p>
     * <code> targetV - V < threshold && |Q - minQ| <= threshold</code></p>
     * <code> V - targetV < threshold && |Q - maxQ| <= threshold</code></p>
     * <code> |V - targetV|  < threshold && minQ <= Q <= maxQ </code>
     */
    public static boolean isVoltageRegulationKo(double qGen, double v, double targetV, double minQ, double maxQ, double threshold) {

        // When V is higher than g.getTargetV() then q must equal to g.getReactiveLimits().getMinQ(p)
        // When V is lower than g.getTargetV() q must equal to g.getReactiveLimits().getMaxQ(p)
        // When V is equal to g.getTargetV() then q (reactive bounds) must satisfy
        return v > targetV + threshold && Math.abs(qGen - Math.min(minQ, maxQ)) > threshold
                || v < targetV - threshold && Math.abs(qGen - Math.max(minQ, maxQ)) > threshold
                || Math.abs(v - targetV) <= threshold && !boundedWithin(minQ, maxQ, qGen, threshold);
    }

    /**
     * Generator: rule for valid result <p/>
     * If reactive limits are inverted (`maxQ < minQ`) and noRequirementIfReactiveBoundInversion = true, generator validation OK.
     */
    public static boolean isReactiveBoundInverted(double minQ, double maxQ, double threshold, boolean isNoRequirementIfReactiveBoundInversion) {
        return maxQ < minQ - threshold && isNoRequirementIfReactiveBoundInversion;
    }

    /**
     * Generator: rule for valid result<p/>
     * Active setpoint outside bounds, if `targetP` is outside `[minP, maxP]` and noRequirementIfSetpointOutsidePowerBounds = true, generator validation OK
     */
    public static boolean isSetpointOutsidePowerBounds(double targetP, double minP, double maxP, double threshold, boolean isNoRequirementIfSetpointOutsidePowerBounds) {
        return (targetP < minP - threshold || targetP > maxP + threshold) && isNoRequirementIfSetpointOutsidePowerBounds;
    }

}
