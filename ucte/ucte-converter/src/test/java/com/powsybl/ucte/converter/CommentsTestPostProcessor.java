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
import com.powsybl.ucte.network.UcteNetwork;
import com.powsybl.ucte.network.UcteNode;

import java.util.Comparator;
import java.util.Optional;

/**
 * Adds comments to the first comment block, mapping IIDM buses to UCTE nodes, and in front of some blocks.
 * Orders nodes by busbar.
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
    public Optional<Comparator<UcteNode>> getNodeComparator() {
        return Optional.of(Comparator.comparing(node -> node.getCode().getBusbar()));
    }
}
