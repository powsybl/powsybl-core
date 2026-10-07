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
     * <p>In case of reactive power regulation, this value is defined in the load sign convention.</p>
     * @see VariantManager
     */
    double getTargetValue();

    /**
     * <p>Set the targetValue.</p>
     * <p>This value is variant-dependant.</p>
     * <p>In case of reactive power regulation, this value is defined in the load sign convention.</p>
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
     * <p>Set the Terminal with the expected targetValue (defined in the load sign convention in case of reactive power regulation) ({@link #setTargetValue(double)}).</p>
     * <p>This value is <b>NOT</b> variant-dependent.</p>
     *
     * @return the current instance for method chaining
     * @see #getTerminal()
     * @see #setTargetValue(double)
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

    default VoltageRegulationAttributes getAttributes() {
        return new VoltageRegulationAttributes(
            getTargetValue(),
            getTargetDeadband(),
            getSlope(),
            getMode(),
            isRegulating(),
            getTerminal()
        );
    }

    /**
     * Constructs a new instance of the {@link VoltageRegulationAttributes} class.
     *
     * @param targetValue The target value for voltage regulation. In case of reactive power regulation, this value is defined in the load sign convention.
     * @param targetDeadband The acceptable range around the target value where no regulation action is taken.
     * @param slope The slope of the regulation curve, which defines the sensitivity of the regulation.
     * @param mode The regulation mode, specifying how the regulation is applied (e.g., voltage, reactive power).
     * @param isRegulating A boolean indicating whether the regulation is currently active.
     * @param terminal The terminal associated with the voltage regulation.
     */
    record VoltageRegulationAttributes(
        double targetValue,
        double targetDeadband,
        double slope,
        RegulationMode mode,
        boolean isRegulating,
        Terminal terminal
    ) {

        /**
         * Constructs a new instance of the {@link VoltageRegulationAttributes} class.
         *
         * @param targetValue The target value for voltage regulation. In case of reactive power regulation, this value is defined in the load sign convention.
         * @param targetDeadband The acceptable range around the target value where no regulation action is taken.
         * @param slope The slope of the regulation curve, which defines the sensitivity of the regulation.
         * @param mode The regulation mode, specifying how the regulation is applied (e.g., voltage, reactive power).
         * @param isRegulating A boolean indicating whether the regulation is currently active.
         */
        public VoltageRegulationAttributes(
            double targetValue,
            double targetDeadband,
            double slope,
            RegulationMode mode,
            boolean isRegulating) {
            this(targetValue, targetDeadband, slope, mode, isRegulating, null);
        }

        public VoltageRegulationAttributes withMode(RegulationMode newMode) {
            return new VoltageRegulationAttributes(targetValue(), targetDeadband(), slope(), newMode, isRegulating(), terminal());
        }

        public VoltageRegulationAttributes withRegulating(boolean newRegulating) {
            return new VoltageRegulationAttributes(targetValue(), targetDeadband(), slope(), mode(), newRegulating, terminal());
        }

        /**
         * Creates a new instance of {@link VoltageRegulationAttributes} with the specified terminal and target value
         * while retaining the other properties of the current instance.
         * <p>In case of reactive power regulation, the target value is defined in the load sign convention.</p>
         */
        public VoltageRegulationAttributes withTerminalAndTargetValue(Terminal newTerminal, double newTargetValue) {
            return new VoltageRegulationAttributes(newTargetValue, targetDeadband(), slope(), mode(), isRegulating(), newTerminal);
        }

        /**
         * Creates a new instance of {@link VoltageRegulationAttributes} with the specified target value
         * while retaining the other properties of the current instance.
         * <p>In case of reactive power regulation, the target value is defined in the load sign convention.</p>
         */
        public VoltageRegulationAttributes withTargetValue(double newTargetValue) {
            return new VoltageRegulationAttributes(newTargetValue, targetDeadband(), slope(), mode(), isRegulating(), terminal());
        }

        public VoltageRegulationAttributes withTargetDeadband(double newTargetDeadband) {
            return new VoltageRegulationAttributes(targetValue(), newTargetDeadband, slope(), mode(), isRegulating(), terminal());
        }

        public VoltageRegulationAttributes withSlope(double newSlope) {
            return new VoltageRegulationAttributes(targetValue(), targetDeadband(), newSlope, mode(), isRegulating(), terminal());
        }
    }
}
