/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
interface TerminalExt extends Terminal, Stateful {

    interface BusBreakerViewExt extends BusBreakerView {

        @Override BusExt getBus();

    }

    interface BusViewExt extends BusView {

        @Override BusExt getBus();

    }

    @Override
    AbstractConnectable getConnectable();

    void setConnectable(AbstractConnectable connectable);

    @Override VoltageLevelExt getVoltageLevel();

    void setVoltageLevel(VoltageLevelExt voltageLevel);

    @Override BusBreakerViewExt getBusBreakerView();

    @Override BusViewExt getBusView();

    void setNum(int num);

    void traverse(VoltageLevel.TopologyTraverser traverser, Set<Terminal> traversedTerminals);

}
