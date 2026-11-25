/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UcteElementStatus;
import com.powsybl.ucte.network.UcteLine;
import com.powsybl.ucte.network.UcteNetwork;
import com.powsybl.ucte.network.UcteNode;
import com.powsybl.ucte.network.UctePowerPlantType;

import java.util.Comparator;
import java.util.Optional;

/**
 * Replaces the "further" power plant type by gas, and sets the line {@value #LINE_ID} out of service, reporting each
 * modification. Orders nodes in reverse natural order.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(UcteExportPostProcessor.class)
public class ModificationsTestPostProcessor implements UcteExportPostProcessor {

    static final String LINE_ID = "FA____12 FB____11 1";

    @Override
    public String getName() {
        return "test-modifications";
    }

    @Override
    public void process(Network network, UcteNetwork ucteNetwork, UcteExporterContext context) {
        for (UcteNode node : ucteNetwork.getNodes()) {
            if (node.getPowerPlantType() == UctePowerPlantType.F) {
                node.setPowerPlantType(UctePowerPlantType.G);
                context.getReportNode().newReportNode()
                        .withMessageTemplate("testPowerPlantTypeChanged")
                        .withUntypedValue("node", node.getCode().toString())
                        .withUntypedValue("oldType", UctePowerPlantType.F.name())
                        .withUntypedValue("newType", UctePowerPlantType.G.name())
                        .add();
            }
        }

        UcteLine line = ucteNetwork.getLine(context.getNamingStrategy().getUcteElementId(network.getLine(LINE_ID)));
        line.setStatus(UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION);
        context.getReportNode().newReportNode()
                .withMessageTemplate("testLineOutOfService")
                .withUntypedValue("line", line.getId().toString())
                .add();
    }

    @Override
    public Optional<Comparator<UcteNode>> getNodeComparator(Network network, UcteNetwork ucteNetwork, UcteExporterContext context) {
        return Optional.of(Comparator.<UcteNode>naturalOrder().reversed());
    }
}
