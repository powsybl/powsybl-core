/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseLoad;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class LoadConverter extends AbstractConverter {

    public LoadConverter(PsseLoad psseLoad, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseLoad = psseLoad;
    }

    public void create() {

        String busId = getBusId(psseLoad.getI());
        VoltageLevel voltageLevel = getNetwork()
            .getVoltageLevel(getContainersMapping().getVoltageLevelId(psseLoad.getI()));

        // Only constant power is considered at the moment
        // S = VI*, S = YVV*
        // .setIp(psseLoad.getIp())
        // .setIq(psseLoad.getIq())
        // .setYp(psseLoad.getYp())
        // .setYq(psseLoad.getYq())

        Load load = voltageLevel.newLoad()
            .setId(getLoadId(busId))
            .setConnectableBus(busId)
            .setP0(psseLoad.getPl())
            .setQ0(psseLoad.getQl())
            .add();

        if (psseLoad.getStatus() == 1) {
            load.getTerminal().connect();
        }
    }

    private String getLoadId(String busId) {
        return busId + "-L" + psseLoad.getId();
    }

    private final PsseLoad psseLoad;
}
