/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.regulation;

import java.util.List;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface RegulatingControl {

    String getId();

    double getTargetValue();

    RegulatingControl setTargetValue(double targetValue);

    double getTargetDeadband();

    RegulatingControl setTargetDeadband(double targetDeadband);

    RegulationKind getRegulationKind();

    String getRegulatedEquipmentId();

    EquipmentSide getRegulatedEquipmentSide();

    List<Regulation> getRegulations();

    void remove();
}
