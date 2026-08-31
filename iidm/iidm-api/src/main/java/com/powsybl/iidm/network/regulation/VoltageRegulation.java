/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VariantManager;
import org.jspecify.annotations.Nullable;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulation {

    /**
     * <p>Get the TargetValue for RegulationMode set.</p>
     * <p>This value is variant-dependant.</p>
     * @see VariantManager
     */
    double getTargetValue();

    /**
     * <p>Set the targetValue.</p>
     * <p>This value is variant-dependant.</p>
     *
     * @return the current instance for method chaining
     * @see #getTargetValue()
     * @see VariantManager
     */
    VoltageRegulation setTargetValue(double targetValue);

    /**
     * <p>
     * Get the tap changer's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the tap changer is regulating.
     * </p>
     * <p>
     * The targetDeadband is only pertinent for objects with discrete (as opposed to continuous) voltage regulation,
     * which is the case for {@link RatioTapChanger} and {@link ShuntCompensator}
     * </p>
     * <p>This value is variant-dependent.</p>
     * @see VariantManager
     */
    double getTargetDeadband();

    /**
     * <p>Set the targetDeadBand.</p>
     * <p>This value is variant-dependent.</p>
     *
     * @return the current instance for method chaining
     * @see #getTargetDeadband()
     * @see VariantManager
     */
    VoltageRegulation setTargetDeadband(double targetDeadband);

    /**
     * Get the slope. It is relevant for:
     * <ul>
     * <li>{@link RegulationMode#VOLTAGE_PER_REACTIVE_POWER}: it corresponds to the lambda in <code>U0 = U + lambda*Q</code></li>
     * <li>Not yet supported: RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER: it corresponds to the tan(phi) in <code>Q = tan(phi)*P</code></li>
     * </ul>
     * <p>This value is variant-dependent.</p>
     * @see VariantManager
     */
    double getSlope();

    /**
     * Set the slope.
     * <p>This value is variant-dependent.</p>
     * @return the current instance for method chaining
     * @see #getSlope()
     * @see VariantManager
     */
    VoltageRegulation setSlope(double slope);

    /**
     * <p>The Terminal used for regulation. Can be local or remote but must be in the network</p>
     * <p>This value is <b>NOT</b> variant-dependent.</p>
     */
    Terminal getTerminal();

    /**
     * <p>Set the Terminal with the expected targetValue.</p>
     * <p>This value is <b>NOT</b> variant-dependent.</p>
     *
     * @return the current instance for method chaining
     * @see #getTerminal()
     */
    VoltageRegulation setTerminal(Terminal terminal, double targetValue);

    boolean isWithTerminal();

    /**
     * <p>Get the regulation mode.</p>
     * <p>Returns {@code null} when no regulationMode is defined for the current variant.
     * This can happen in a multi-variant context, for instance when voltage regulation
     * has been added only in another variant.</p>
     * <p>This value is variant-dependent.</p>
     * @see VariantManager
     */
    @Nullable
    RegulationMode getMode();

    /**
     * <p>Set the regulation mode.</p>
     * <p>This value is variant-dependent.</p>
     * @return the current instance for method chaining
     * @see RegulationMode
     */
    VoltageRegulation setMode(RegulationMode mode);

    /**
     * Tell if the holder is regulating or not.
     * If false all VoltageRegulation attributes are ignored
     *
     * @see VariantManager
     */
    boolean isRegulating();

    /**
     * <p>Set the regulating status.</p>
     * <p>This value is variant-dependent.</p>
     * @return the current instance for method chaining
     */
    VoltageRegulation setRegulating(boolean regulating);

    default AttributesWithTerminal getAttributes() {
        return new AttributesWithTerminal(
            getTargetValue(),
            getTargetDeadband(),
            getSlope(),
            getMode(),
            isRegulating(),
            getTerminal()
        );
    }

    /**
     * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
     */
    record Attributes(
        double targetValue,
        double targetDeadband,
        double slope,
        RegulationMode mode,
        boolean isRegulating
    ) {
    }

    /**
     * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
     */
    record AttributesWithTerminal(
        Attributes attributes,
        Terminal terminal
    ) {
        public AttributesWithTerminal(
            double targetValue,
            double targetDeadband,
            double slope,
            RegulationMode mode,
            boolean isRegulating,
            Terminal terminal) {
            this(new Attributes(targetValue, targetDeadband, slope, mode, isRegulating), terminal);
        }

        public double targetValue() {
            return attributes.targetValue();
        }

        public double targetDeadband() {
            return attributes.targetDeadband();
        }

        public double slope() {
            return attributes.slope();
        }

        public RegulationMode mode() {
            return attributes.mode();
        }

        public boolean isRegulating() {
            return attributes.isRegulating();
        }

        public AttributesWithTerminal withMode(RegulationMode newMode) {
            return new AttributesWithTerminal(targetValue(), targetDeadband(), slope(), newMode, isRegulating(), terminal());
        }

        public AttributesWithTerminal withRegulating(boolean newRegulating) {
            return new AttributesWithTerminal(targetValue(), targetDeadband(), slope(), mode(), newRegulating, terminal());
        }

        public AttributesWithTerminal withTerminalAndTargetValue(Terminal newTerminal, double newTargetValue) {
            return new AttributesWithTerminal(newTargetValue, targetDeadband(), slope(), mode(), isRegulating(), newTerminal);
        }

        public AttributesWithTerminal withTargetValue(double newTargetValue) {
            return new AttributesWithTerminal(newTargetValue, targetDeadband(), slope(), mode(), isRegulating(), terminal());
        }

        public AttributesWithTerminal withTargetDeadband(double newTargetDeadband) {
            return new AttributesWithTerminal(targetValue(), newTargetDeadband, slope(), mode(), isRegulating(), terminal());
        }

        public AttributesWithTerminal withSlope(double newSlope) {
            return new AttributesWithTerminal(targetValue(), targetDeadband(), newSlope, mode(), isRegulating(), terminal());
        }
    }
}
