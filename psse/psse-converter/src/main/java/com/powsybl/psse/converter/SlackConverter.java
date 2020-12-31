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
import com.powsybl.psse.model.pf.PsseArea;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SlackConverter extends AbstractConverter {

    public SlackConverter(List<PsseArea> psseAreaList, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseAreaList = psseAreaList;
    }

    public void create() {

        for (PsseArea psseArea : psseAreaList) {
            if (psseArea.getIsw() != 0) {
                String busId = AbstractConverter.getBusId(psseArea.getIsw());
                Bus bus = getNetwork().getBusBreakerView().getBus(busId);
                SlackTerminal.attach(bus);
            }
        }
    }

    private final List<PsseArea> psseAreaList;
}
