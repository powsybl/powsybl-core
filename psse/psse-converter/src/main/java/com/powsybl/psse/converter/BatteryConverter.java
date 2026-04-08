/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.psse.model.pf.PsseLoad;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.powsybl.psse.converter.AbstractConverter.PsseEquipmentType.PSSE_LOAD;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BatteryConverter extends AbstractConverter {

    BatteryConverter(Network network) {
        super(network);
    }

    static void create(Network network, PssePowerFlowModel psseModel, ContextExport contextExport) {
        List<PsseLoad> loads = new ArrayList<>();
        network.getBatteries().forEach(battery -> loads.add(createBattery(battery, contextExport)));
        psseModel.addLoads(loads);
        psseModel.replaceAllLoads(psseModel.getLoads().stream().sorted(Comparator.comparingInt(PsseLoad::getI).thenComparing(PsseLoad::getId)).toList());
    }

    private static PsseLoad createBattery(Battery battery, ContextExport contextExport) {
        PsseLoad psseLoad = createDefaultLoad();

        int busI = getTerminalBusI(battery.getTerminal(), contextExport);
        psseLoad.setI(busI);
        psseLoad.setId(contextExport.getFullExport().getEquipmentCkt(battery.getId(), PSSE_LOAD.getTextCode(), busI));
        psseLoad.setStatus(getStatus(battery.getTerminal(), contextExport));
        psseLoad.setPl(getP(battery));
        psseLoad.setQl(getQ(battery));
        return psseLoad;
    }

    // generator convention for batteries
    private static double getP(Battery battery) {
        return Double.isNaN(battery.getTargetP()) ? 0.0 : -battery.getTargetP();
    }

    private static double getQ(Battery battery) {
        return Double.isNaN(battery.getTargetQ()) ? 0.0 : -battery.getTargetQ();
    }
}
