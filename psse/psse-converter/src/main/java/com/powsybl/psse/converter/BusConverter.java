/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import java.util.Objects;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BusConverter extends AbstractConverter {

    BusConverter(PsseBus psseBus, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseBus = Objects.requireNonNull(psseBus);
    }

    void create(VoltageLevel voltageLevel) {
        String busId = getBusId(psseBus.getI());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
            .setId(busId)
            .setName(psseBus.getName())
            .add();
        bus.setV(psseBus.getVm() * voltageLevel.getNominalV())
            .setAngle(psseBus.getVa());
    }

    // At the moment we do not consider new buses
    static void updateBuses(Network network, PssePowerFlowModel psseModel) {
        psseModel.getBuses().forEach(psseBus -> {
            String busId = AbstractConverter.getBusId(psseBus.getI());
            Bus bus = network.getBusBreakerView().getBus(busId);
            if (bus == null) {
                psseBus.setVm(0.0);
                psseBus.setVa(0.0);
                psseBus.setIde(4);
            } else {
                psseBus.setVm(bus.getV() / bus.getVoltageLevel().getNominalV());
                psseBus.setVa(bus.getAngle());
            }
        });
    }

    private final PsseBus psseBus;
}
