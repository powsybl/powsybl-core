/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

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
     * Creates a new VoltageRegulation instance based on the provided VoltageRegulation
     */
    VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation);

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
     * Gets the target voltage value
     *
     * @return the target voltage value, or Double.NaN if not applicable
     */
    default double getTargetV() {
        return Double.NaN;
    }

    /**
     * Gets the target reactive power value
     *
     * @return the target reactive power value, or Double.NaN if not applicable
     */
    default double getTargetQ() {
        return Double.NaN;
    }

    /**
     * Checks if the object is associated with the specified regulation mode.
     *
     * @param mode the regulation mode to check
     * @return true if associated with the specified mode, false otherwise
     */
    default boolean isRegulatingWithMode(RegulationMode mode) {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null
            && voltageRegulation.isRegulating()
            && (mode == null || mode.equals(voltageRegulation.getMode()));
    }

    /**
     * Checks if the object is regulating with the specified mode
     *
     * @param mode the regulation mode to check
     * @return true if regulating with the specified mode, false otherwise
     */
    default boolean isWithMode(RegulationMode mode) {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null
            && (mode == null || mode.equals(voltageRegulation.getMode()));
    }

    /**
     * Gets the regulating target voltage value using the targetValue if the RegulatingMode is equals to {@link RegulationMode#VOLTAGE}
     */
    default double getRegulatingTargetV() {
        if (isWithMode(RegulationMode.VOLTAGE)) {
            return getVoltageRegulation().getTargetValue();
        }
        return getTargetV();
    }

    /**
     * Gets the regulating target reactive power value using the targetValue if the RegulatingMode is equals to {@link RegulationMode#REACTIVE_POWER}
     * TODO MSA Other possible names : getEffectiveTargetQ / getApplicableTargetQ / resolveTargetQ / determineTargetQ
     */
    default double getRegulatingTargetQ() {
        if (isWithMode(RegulationMode.REACTIVE_POWER)) {
            return getVoltageRegulation().getTargetValue();
        }
        return getTargetQ();
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
        return !Objects.equals(getRegulatingTerminal().getBusBreakerView().getConnectableBus(), getTerminal().getBusBreakerView().getConnectableBus());
    }

}
