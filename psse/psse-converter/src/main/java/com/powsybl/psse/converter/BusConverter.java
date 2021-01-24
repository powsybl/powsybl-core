/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseBus;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class BusConverter extends AbstractConverter {

    public BusConverter(PsseBus psseBus, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseBus = psseBus;
    }

    public void create(VoltageLevel voltageLevel) {
        String busId = getBusId(psseBus.getI());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
            .setId(busId)
            .setName(psseBus.getName())
            .add();
        bus.setV(psseBus.getVm() * voltageLevel.getNominalV())
            .setAngle(psseBus.getVa());
    }

    private final PsseBus psseBus;
}
