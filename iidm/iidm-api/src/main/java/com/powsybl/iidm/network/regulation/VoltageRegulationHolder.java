/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VariantManager;

/**
 * This interface defines methods for managing voltageRegulation
 *
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulationHolder<T extends VoltageRegulationHolder<T>> {

    /**
     * Creates a new VoltageRegulationBuilder instance
     */
    VoltageRegulationBuilder newVoltageRegulation();

    /**
     * Gets the current VoltageRegulation instance
     */
    VoltageRegulation getVoltageRegulation();

    /**
     * Removes the current VoltageRegulation instance for all existing variants
     */
    void removeVoltageRegulation();

    /**
     * Gets the terminal associated
     *
     * @return the terminal
     */
    Terminal getTerminal();

    /**
     * <p>
     *  Sets the local target voltage value in kV at the equipment's terminal.
     * </p>
     * <p>Depends on the working variant.</p>
     * @param targetV the target voltage value to set
     * @return the current instance for method chaining
     * @see VariantManager
     */
    T setLocalTargetV(double targetV);

    /**
     * Gets the local target voltage value.
     *
     * @return the target voltage value, or Double.NaN if not applicable
     */
    default double getLocalTargetV() {
        return Double.NaN;
    }

    /**
     * <p>
     *  Sets the local target reactive power value in MVAR.
     * </p>
     * <p>Depends on the working variant.</p>
     * @param localTargetQ the target reactive power value to set
     * @return the current instance for method chaining
     * @see VariantManager
     */
    default T setLocalTargetQ(double localTargetQ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the target reactive power value
     *
     * @return the target reactive power value, or Double.NaN if not applicable
     */
    default double getLocalTargetQ() {
        return Double.NaN;
    }

    /**
     * Checks if the object is associated with the specified regulation mode.
     * In the case of the REACTIVE_POWER mode, we return true if the VoltageRegulation is missing.
     *
     * @param mode the regulation mode to check
     * @return true if associated with the specified mode, false otherwise
     */
    default boolean isRegulatingWithMode(RegulationMode mode) {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (RegulationMode.REACTIVE_POWER == mode && voltageRegulation == null) {
            return true;
        }
        return voltageRegulation != null
            && voltageRegulation.isRegulating()
            && voltageRegulation.getMode() == mode;
    }

    /**
     * Get the regulating status.
     */
    default boolean isRegulating() {
        return getVoltageRegulation() != null && getVoltageRegulation().isRegulating();
    }

    /**
     * Checks if the object is regulating with the specified mode.
     * In the case of the REACTIVE_POWER mode, we return true if the VoltageRegulation is missing.
     *
     * @param mode the regulation mode to check
     * @return true if regulating with the specified mode, false otherwise
     */
    default boolean isWithMode(RegulationMode mode) {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (mode == null) {
            return false;
        }
        if (RegulationMode.REACTIVE_POWER.equals(mode) && voltageRegulation == null) {
            return true;
        }
        return voltageRegulation != null && mode.equals(voltageRegulation.getMode());
    }

    /**
     * Gets the regulating target voltage value using the targetValue if the RegulatingMode is equals to {@link RegulationMode#VOLTAGE}
     */
    default double getRegulatingTargetV() {
        if (isWithMode(RegulationMode.VOLTAGE) && isRemoteRegulating()) {
            return getVoltageRegulation().getTargetValue();
        }
        return getLocalTargetV();
    }

    /**
     * Gets the regulating target reactive power value using the targetValue if the RegulatingMode is equals to {@link RegulationMode#REACTIVE_POWER}
     * TODO MSA Other possible names : getEffectiveTargetQ / getApplicableTargetQ / resolveTargetQ / determineTargetQ
     */
    default double getRegulatingTargetQ() {
        if (isWithMode(RegulationMode.REACTIVE_POWER) && isRemoteRegulating()) {
            return getVoltageRegulation().getTargetValue();
        }
        return getLocalTargetQ();
    }

    /**
     * Gets the terminal used for regulation
     *
     * @return the terminal used for regulation
     */
    default Terminal getRegulatingTerminal() {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null && voltageRegulation.getTerminal() != null) {
            return voltageRegulation.getTerminal();
        }
        return getTerminal();
    }

    /**
     * Checks if the regulation is performed remotely
     *
     * @return true if regulating remotely, false otherwise
     */
    default boolean isRemoteRegulating() {
        return getVoltageRegulation() != null && getVoltageRegulation().isWithTerminal();
    }

    /**
     * <p>
     *     Updates the target reactive power value in both local and remote regulation scenarios
     * </p>
     * <p>
     *     In all cases, the local target reactive power value is set to the negated reactive power
     *     value at the equipment's terminal
     * </p>
     * <p>
     *     If the reactive power regulation mode is enabled and regulation is performed remotely,
     *     the target value is updated to the negated reactive power value at the remote terminal
     * </p>
     */
    default void setTargetQToQ() {
        // If remote reactive power regulation is enabled, the target value is updated
        if (this.isRegulatingWithMode(RegulationMode.REACTIVE_POWER) && isRemoteRegulating()) {
            double remoteQ = getVoltageRegulation().getTerminal().getQ();
            if (!Double.isNaN(remoteQ)) {
                getVoltageRegulation().setTargetValue(-remoteQ);
            }
        }
        double q = this.getTerminal().getQ();
        if (!Double.isNaN(q)) {
            // In any cases we set the localTargetQ
            this.setLocalTargetQ(-this.getTerminal().getQ());
        }
    }

    /**
     * <p>
     *     Updates the target voltage value in both local and remote regulation scenarios
     * </p>
     * <p>
     *     In all cases, the local target voltage value is set to the voltage value at the equipment's terminal
     * </p>
     * <p>
     *     If the voltage regulation mode is enabled and regulation is performed remotely,
     *     the target value is updated to the voltage value at the remote terminal
     * </p>
     */
    default void setTargetVToV() {
        if (this.isRegulatingWithMode(RegulationMode.VOLTAGE) && isRemoteRegulating()) {
            Bus remoteBus = getVoltageRegulation().getTerminal().getBusView().getBus();
            if (remoteBus != null && !Double.isNaN(remoteBus.getV())) {
                getVoltageRegulation().setTargetValue(remoteBus.getV());
            }
        }
        Bus bus = this.getTerminal().getBusView().getBus();
        if (bus != null && !Double.isNaN(bus.getV())) {
            // In any cases we set the localTargetV
            this.setLocalTargetV(bus.getV());
        }
    }

}
