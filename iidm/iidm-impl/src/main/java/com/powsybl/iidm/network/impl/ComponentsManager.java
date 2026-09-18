/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.List;

/**
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
interface ComponentsManager extends NetworkListener {

    void invalidate();

    void invalidate(VoltageLevel voltageLevel);

    List<Component> getConnectedComponents();

    Component getComponent(BusExt bus);

    boolean isInMainComponent(BusExt bus);

    Component getComponent(DcBusImpl bus);

    boolean isInMainComponent(DcBusImpl bus);
}
