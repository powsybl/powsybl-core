/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import java.util.Set;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulationHolder<T extends VoltageRegulationHolder<T>> {

    VoltageRegulationAdder<T> newVoltageRegulation();

    void removeVoltageRegulation();

    void setVoltageRegulation(VoltageRegulation voltageRegulation); // TODO MSA to remove

    VoltageRegulation getVoltageRegulation();

    Set<RegulationMode> getAllowedRegulationModes();

    // TODO MSA add default indirection methods??

}
