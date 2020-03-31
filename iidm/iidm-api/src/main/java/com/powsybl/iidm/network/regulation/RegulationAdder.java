/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.regulation;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface RegulationAdder {

    RegulationAdder setRegulating(boolean regulating);

    RegulationAdder setRegulatingSide(EquipmentSide side);

    RegulationAdder setRegulatingControl(RegulatingControl regulatingControl);

    Regulation add();
}
