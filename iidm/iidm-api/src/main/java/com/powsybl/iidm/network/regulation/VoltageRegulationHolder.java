/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

/**
 * This interface defines methods for managing voltageRegulation
 *
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulationHolder {

    /**
     * Creates a new VoltageRegulationBuilder instance
     */
    VoltageRegulationBuilder newVoltageRegulation();

    /**
     * Gets the current VoltageRegulation instance
     */
    VoltageRegulation getVoltageRegulation();

    /**
     * Removes the current VoltageRegulation instance
     */
    void removeVoltageRegulation();

    /**
     * Gets the terminal associated
     *
     * @return the terminal
     */
    Terminal getTerminal();

    /**
     * TODO MSA JAVADOC
     */
    VoltageRegulationHolder setLocalTargetV(double targetV);

    /**
     * Gets the local target voltage value at the equipment's terminal
     *
     * @return the target voltage value, or Double.NaN if not applicable
     */
    default double getLocalTargetV() {
        return Double.NaN;
    }

    /**
     * TODO MSA JAVADOC
     */
    default VoltageRegulationHolder setLocalTargetQ(double localTargetQ) {
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
     * Specific case with the mode REACTIVE_POWER, if
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
            && isWithMode(mode);
    }

    /**
     * Get the regulating status.
     */
    default boolean isRegulating() {
        return getVoltageRegulation() != null && getVoltageRegulation().isRegulating();
    }

    /**
     * Checks if the object is regulating with the specified mode
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

}
