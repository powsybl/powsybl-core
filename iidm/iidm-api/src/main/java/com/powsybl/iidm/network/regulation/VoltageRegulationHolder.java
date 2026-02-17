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
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulationHolder {

    /**
     * TODO MSA JAVADOC
     */
    VoltageRegulationBuilder newVoltageRegulation();

    /**
     * TODO MSA JAVADOC
     */
    VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation);

    /**
     * TODO MSA JAVADOC
     */
    VoltageRegulation getVoltageRegulation();

    /**
     * TODO MSA JAVADOC
     */
    void removeVoltageRegulation();

    /**
     * TODO MSA JAVADOC
     */
    Terminal getTerminal();

    /**
     * TODO MSA JAVADOC
     */
    default double getTargetV() {
        return Double.NaN;
    }

    /**
     * TODO MSA JAVADOC
     */
    default double getTargetQ() {
        return Double.NaN;
    }

    /**
     * TODO MSA JAVADOC
     */
    default boolean isRegulatingWithMode(RegulationMode mode) {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null
            && voltageRegulation.isRegulating()
            && (mode == null || mode.equals(voltageRegulation.getMode()));
    }

    /**
     * TODO MSA JAVADOC
     */
    default double getRegulatingTargetV() {
        if (isRegulatingWithMode(RegulationMode.VOLTAGE)) {
            return getVoltageRegulation().getTargetValue();
        }
        return getTargetV();
    }

    /**
     * TODO MSA JAVADOC
     */
    default double getLocalRegulatingTargetV() {
        if (isRegulatingWithMode(RegulationMode.VOLTAGE) && !isRemoteRegulating()) {
            return getVoltageRegulation().getTargetValue();
        }
        return getTargetV();
    }

    /**
     * TODO MSA JAVADOC
     * TODO MSA Other possible names : getEffectiveTargetQ / getApplicableTargetQ / resolveTargetQ / determineTargetQ
     */
    default double getRegulatingTargetQ() {
        if (isRegulatingWithMode(RegulationMode.REACTIVE_POWER)) {
            return getVoltageRegulation().getTargetValue();
        }
        return getTargetQ();
    }

    /**
     * TODO MSA JAVADOC
     */
    default double getLocalRegulatingTargetQ() {
        if (isRegulatingWithMode(RegulationMode.REACTIVE_POWER) && !isRemoteRegulating()) {
            return getVoltageRegulation().getTargetValue();
        }
        return getTargetQ();
    }

    /**
     * Get the terminal used for regulation.
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
     * TODO MSA JAVADOC
     */
    default boolean isRemoteRegulating() {
        return !Objects.equals(getRegulatingTerminal().getBusBreakerView().getConnectableBus(), getTerminal().getBusBreakerView().getConnectableBus());
    }

}
