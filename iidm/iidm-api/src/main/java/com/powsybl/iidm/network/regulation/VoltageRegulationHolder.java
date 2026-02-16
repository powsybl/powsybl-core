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
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulationHolder {

    VoltageRegulationBuilder newVoltageRegulation();

    VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation);

    VoltageRegulation getVoltageRegulation();

    void removeVoltageRegulation();

    Terminal getFirstTerminal(); // TODO MSA not in the interface??

    double getTargetV();

    // TODO MSA add default indirection methods??
//    default boolean isRegulating() {
//        VoltageRegulation voltageRegulation = getVoltageRegulation();
//        return voltageRegulation != null && voltageRegulation.isRegulating();
//    }

    default boolean isRegulatingWithMode(RegulationMode mode) {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null
            && voltageRegulation.isRegulating()
            && (mode == null || mode.equals(voltageRegulation.getMode()));
    }

    default double getRegulatingTargetV() {
        if (isRegulatingWithMode(RegulationMode.VOLTAGE)) {
            return getVoltageRegulation().getTargetValue();
        }
        return getTargetV();
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
        return getFirstTerminal();
    }

}
