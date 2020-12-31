/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import java.util.List;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseBus;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SlackConverter extends AbstractConverter {

    public SlackConverter(List<PsseBus> psseBusList, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseBusList = psseBusList;
    }

    public void create() {

        for (PsseBus psseBus : psseBusList) {
            if (psseBus.getIde() == 3) {
                String busId = AbstractConverter.getBusId(psseBus.getI());
                Bus bus = getNetwork().getBusBreakerView().getBus(busId);
                if (bus != null) {
                    SlackTerminal.attach(bus);
                }
            }
        }
    }

    private final List<PsseBus> psseBusList;
}
