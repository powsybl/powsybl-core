/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.*;

/**
 * @author Antoine Limouzin {@literal <antoine.limouzin at rte.com>}
 */
@AutoService(UcteImportPostProcessor.class)
public class ElementNameTestPostProcessor implements UcteImportPostProcessor {

    @Override
    public String getName() {
        return "test-import";
    }

    @Override
    public void process(Network network, UcteNetwork ucteNetwork, ReportNode reportNode) {
        network.getBusBreakerView().getBusStream()
                .forEach(bus-> {
                            bus.setProperty("ucteName", ucteNetwork.getNodes().stream()
                                    .filter(ucteNode -> ucteNode.getCode().toString().equals(bus.getId()))
                                    .findFirst()
                                    .map(UcteNode::getGeographicalName)
                                    .orElse(""));
                            reportNode.newReportNode().withMessageTemplate("geoTestImportReportNode")
                                    .withUntypedValue("bus", bus.getId())
                                    .withUntypedValue("name", bus.getProperty("ucteName")).add();
                        }
                );
    }

}
