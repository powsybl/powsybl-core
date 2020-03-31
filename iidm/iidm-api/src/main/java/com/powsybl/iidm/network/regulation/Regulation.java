/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Connectable;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface Regulation {

    boolean isRegulating();

    Regulation setRegulating(boolean regulating);

    RegulatingControl getRegulatingControl();

    Regulation setRegulatingControl(RegulatingControl control);

    Connectable getRegulatingEquipment();

    EquipmentSide getRegulatingEquipmentSide();

    void remove();
}
