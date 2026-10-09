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
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UcteBlock;
import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteNetwork;
import com.powsybl.ucte.network.UcteNode;
import com.powsybl.ucte.network.UcteNodeCode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adds comments to the first comment block, mapping IIDM buses to UCTE nodes, and in front of some blocks.
 * Orders French nodes by nominal voltage of the IIDM voltage level, leaving the order of other countries unchanged.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(UcteExportPostProcessor.class)
public class CommentsTestPostProcessor implements UcteExportPostProcessor {

    @Override
    public String getName() {
        return "test-comments";
    }

    @Override
    public void process(Network network, UcteNetwork ucteNetwork, UcteExporterContext context) {
        int count = 0;
        for (Bus bus : network.getBusBreakerView().getBuses()) {
            UcteNode node = ucteNetwork.getNode(context.getNamingStrategy().getUcteNodeCode(bus));
            ucteNetwork.getComments().add(node.getCode() + " <- " + bus.getId());
            count++;
        }
        context.getReportNode().newReportNode()
                .withMessageTemplate("testNodesMapped")
                .withUntypedValue("count", count)
                .add();

        ucteNetwork.getComments(UcteBlock.NODES).add("Nodes: " + ucteNetwork.getNodes().size());
        ucteNetwork.getComments(UcteBlock.LINES).add("Lines: " + ucteNetwork.getLines().size());
        ucteNetwork.getComments(UcteBlock.LINES).add("Second comment line");
        ucteNetwork.getComments(UcteBlock.REGULATIONS).add("Regulations: " + ucteNetwork.getRegulations().size());
    }

    @Override
    public Optional<Comparator<UcteNode>> getNodeComparator(Network network, UcteNetwork ucteNetwork, UcteExporterContext context) {
        Map<UcteNodeCode, Double> nominalVoltages = new HashMap<>();
        for (Bus bus : network.getBusBreakerView().getBuses()) {
            nominalVoltages.put(context.getNamingStrategy().getUcteNodeCode(bus), bus.getVoltageLevel().getNominalV());
        }
        // compared nodes always belong to the same country
        return Optional.of((node1, node2) -> node1.getCode().getUcteCountryCode() == UcteCountryCode.FR
                ? Double.compare(nominalVoltages.get(node1.getCode()), nominalVoltages.get(node2.getCode()))
                : 0);
    }
}
