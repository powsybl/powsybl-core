/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryException;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class SwitchConverter extends AbstractConverter {

    SwitchConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void createFromElmCoup(DataObject elmCoup) {
        List<NodeRef> nodeRefs = findNodes(elmCoup);
        if (nodeRefs.size() != 2) {
            LOGGER.warn("ElemCoup discarded as it does not have two ends '{}'", elmCoup);
            return;
        }
        NodeRef nodeRef1 = nodeRefs.get(0);
        NodeRef nodeRef2 = nodeRefs.get(1);
        if (!nodeRef1.voltageLevelId.equals(nodeRef2.voltageLevelId)) {
            throw new PowerFactoryException("ElmCoup not connected to same ElmSubstat at both sides: " + elmCoup);
        }

        VoltageLevel vl = getNetwork().getVoltageLevel(nodeRef1.voltageLevelId);
        createSwitch(vl, nodeRef1.node, nodeRef2.node, elmCoup);
    }

    void createFromStaSwitch(VoltageLevel vl, int node1, int node2, DataObject staSwitch) {
        createSwitch(vl, node1, node2, staSwitch);
    }

    void createFictitiousSwitch(VoltageLevel vl, int node1, int node2, String dataObjectId) {
        vl.getNodeBreakerView().newSwitch()
            .setFictitious(true)
            .setId(dataObjectId + "_sw_fict")
            .setEnsureIdUnicity(true)
            .setKind(SwitchKind.BREAKER)
            .setNode1(node1)
            .setNode2(node2)
            .setOpen(true)
            .add();
    }

    private static void createSwitch(VoltageLevel vl, int node1, int node2, DataObject dataObject) {
        SwitchModel switchModel = SwitchModel.create(vl.getId(), dataObject);

        vl.getNodeBreakerView().newSwitch()
            .setId(switchModel.switchId)
            .setEnsureIdUnicity(true)
            .setKind(switchModel.switchKind)
            .setNode1(node1)
            .setNode2(node2)
            .setOpen(switchModel.open)
            .add();
    }

    private static final class SwitchModel {
        private final String switchId;
        private final SwitchKind switchKind;
        private final boolean open;

        private SwitchModel(String switchId, SwitchKind switchKind, boolean open) {
            this.switchId = switchId;
            this.switchKind = switchKind;
            this.open = open;
        }

        private static SwitchModel create(String voltageLevelId, DataObject dataObject) {

            String switchId = createSwitchId(voltageLevelId, dataObject);
            SwitchKind switchKind = createSwitchKind(dataObject);

            // State, 1=Closed, 0=Open
            boolean open = dataObject.findIntAttributeValue("on_off").orElse(0) == 0;

            return new SwitchModel(switchId, switchKind, open);
        }

        private static String createSwitchId(String voltageLevelId, DataObject dataObject) {
            return voltageLevelId + "_" + dataObject.getLocName();
        }

        // Switch Type cbk=Circuit-Breaker, dct=Disconnector, sdc=Load-Break-Disconnector, swt=Load-Switch
        private static SwitchKind createSwitchKind(DataObject dataObject) {
            Optional<String> aUsage = dataObject.findStringAttributeValue("aUsage");

            SwitchKind switchKind;

            if (aUsage.isPresent()) {
                switchKind = switch (aUsage.get()) {
                    case "cbk", "swt" -> SwitchKind.BREAKER;
                    case "dct", "sdc" -> SwitchKind.DISCONNECTOR;
                    default -> throw new PowerFactoryException("Unknown switch type: " + aUsage);
                };
            } else {
                switchKind = SwitchKind.BREAKER;
            }
            return switchKind;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchConverter.class);
}
