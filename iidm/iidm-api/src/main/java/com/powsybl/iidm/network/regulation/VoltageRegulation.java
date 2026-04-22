/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.*;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulation {

    /**
     * The TargetValue for RegulationMode set
     * @see VariantManager
     */
    double getTargetValue();

    /**
     * To set the targetValue. {@link #getTargetValue()}
     * @return the previous Value
     *
     * @see #getTargetValue()
     * @see VariantManager
     */
    double setTargetValue(double targetValue);

    /**
     * <p>
     *     Get the tap changer's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the tap changer is regulating.
     * </p>
     * <p>
     *     The targetDeadband is only pertinent for objects with discrete (as opposed to continuous) voltage regulation,
     * which is the case for {@link RatioTapChanger} and {@link ShuntCompensator}
     * </p>
     * @see VariantManager
     */
    double getTargetDeadband();

    /**
     * To set the targetDeadBand. {@link #getTargetDeadband()}
     * @return the previous Value
     *
     * @see #getTargetDeadband()
     * @see VariantManager
     */
    double setTargetDeadband(double targetDeadband);

    /**
     * The slope attribute is relevant for:
     * {@link RegulationMode#VOLTAGE_PER_REACTIVE_POWER}: it corresponds to the lambda in U0 = U + lambda*Q
     * {@link RegulationMode#REACTIVE_POWER_PER_ACTIVE_POWER}: it corresponds to the tan(phi) in Q = tan(phi)*P
     * @see VariantManager
     */
    double getSlope();

    /**
     * To set the slope. {@link #getSlope()}
     * @return the previous Value
     *
     * @see #getSlope()
     * @see VariantManager
     */
    double setSlope(double slope);

    /**
     * The Terminal uses to regulate. Can be local or remote but most be in the network
     */
    Terminal getTerminal();

    /**
     * To set the Terminal. {@link #getTerminal()}
     *
     * @see #getTerminal()
     */
    void setTerminal(Terminal terminal);

    /**
     * To remove the terminal. Do the same as setTerminal(null)
     */
    void removeTerminal();

    /**
     * RegulationMode is an enum describing the kinds of regulation. It has the following values:
     * <ul>
     *     <li>VOLTAGE</li>
     *     <li>REACTIVE_POWER</li>
     *     <li>VOLTAGE_PER_REACTIVE_POWER</li>
     *     <li>REACTIVE_POWER_PER_ACTIVE_POWER</li>
     * </ul>
     */
    RegulationMode getMode();

    /**
     * <p>To set the RegulationMode. {@link #getMode()}</p>
     * <p>The regulation mode is authorized for the following classes:</p>
     * <ul>
     *     <li>{@link Battery}: {@link RegulationMode#VOLTAGE}, {@link RegulationMode#REACTIVE_POWER}</li>
     *     <li>{@link Generator}: {@link RegulationMode#VOLTAGE}, {@link RegulationMode#REACTIVE_POWER}, {@link RegulationMode#REACTIVE_POWER_PER_ACTIVE_POWER}</li>
     *     <li>{@link RatioTapChanger}: {@link RegulationMode#VOLTAGE}, {@link RegulationMode#REACTIVE_POWER}</li>
     *     <li>{@link ShuntCompensator}: {@link RegulationMode#VOLTAGE}</li>
     *     <li>{@link StaticVarCompensator}: {@link RegulationMode#VOLTAGE}, {@link RegulationMode#REACTIVE_POWER}, {@link RegulationMode#VOLTAGE_PER_REACTIVE_POWER}</li>
     *     <li>{@link VscConverterStation}: {@link RegulationMode#VOLTAGE}, {@link RegulationMode#REACTIVE_POWER}</li>
     *     <li>{@link VoltageSourceConverter}: {@link RegulationMode#VOLTAGE}, {@link RegulationMode#REACTIVE_POWER}</li>
     * </ul>
     * @see #getMode()
     *
     */
    void setMode(RegulationMode mode);

    /**
     * To know if the object is regulating or not.
     * If false all VoltageRegulation attributes are ignored
     * @see VariantManager
     */
    boolean isRegulating();

    /**
     * To set the regulating boolean
     * @return the previous Value
     * @see VariantManager
     */
    boolean setRegulating(boolean regulating);
}
