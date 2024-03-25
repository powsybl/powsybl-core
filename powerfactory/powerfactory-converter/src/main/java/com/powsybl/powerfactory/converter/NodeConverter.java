/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.primitives.Ints;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class NodeConverter extends AbstractConverter {

    NodeConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    /**
     * A cubicle defines the connection between a node and a side of an element.
     * A staSwitch object is automatically created inside the cubicle when an element is connected to a node.
     * An ElmCoup is also connected to a node through a cubicle, but without any additional StaSwitch.
     *
     * An iidm node is created by each elmTerm and by each staSwitch connected to an element.
     */
    void createAndMapConnectedObjs(DataObject elmTerm) {
        VoltageLevel vl = findVoltageLevel(elmTerm);

        // Create the node defined by elmTerm
        int node = createNode(vl);

        getImportContext().elmTermIdToNode.putIfAbsent(elmTerm.getId(), new NodeRef(vl.getId(), node, 0));

        NodeModel nodeModel = NodeModel.create(vl, elmTerm);
        createBusbarSectionIfNodeHasBeenDefinedAsSuch(vl, node, nodeModel);

        // Create additional nodes associated to switches (staSwitch) included in cubicles
        createAdditionalNodesAndMapConnectedObjs(vl, node, elmTerm);
    }

    private VoltageLevel findVoltageLevel(DataObject elmTerm) {
        String voltageLevelId = getImportContext().containerMapping.getVoltageLevelId(Ints.checkedCast(elmTerm.getId()));

        Objects.requireNonNull(voltageLevelId);
        VoltageLevel vl = getNetwork().getVoltageLevel(voltageLevelId);
        if (vl == null) {
            vl = createVoltageLevel(voltageLevelId, getNominalVoltage(elmTerm));
        }
        return vl;
    }

    private VoltageLevel createVoltageLevel(String voltageLevelId, double nominalV) {
        String substationId = getImportContext().containerMapping.getSubstationId(voltageLevelId);
        Substation s = getNetwork().getSubstation(substationId);
        if (s == null) {
            s = getNetwork().newSubstation()
                .setId(substationId)
                .add();
        }
        return s.newVoltageLevel()
            .setId(voltageLevelId)
            .setNominalV(nominalV)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
    }

    static double getNominalVoltage(DataObject elmTerm) {
        return elmTerm.getFloatAttributeValue("uknom");
    }

    private int createNode(VoltageLevel vl) {
        MutableInt nodeCount = getImportContext().nodeCountByVoltageLevelId.computeIfAbsent(vl.getId(), k -> new MutableInt());
        int node = nodeCount.intValue();
        nodeCount.increment();
        return node;
    }

    private void createBusbarSectionIfNodeHasBeenDefinedAsSuch(VoltageLevel vl, int node, NodeModel nodeModel) {
        if (!nodeModel.isBusbarSection) {
            return;
        }
        vl.getNodeBreakerView().newBusbarSection()
            .setId(nodeModel.busbarId)
            .setEnsureIdUnicity(true)
            .setNode(node)
            .add();
    }

    private void createAdditionalNodesAndMapConnectedObjs(VoltageLevel vl, int node, DataObject elmTerm) {
        for (DataObject staCubic : elmTerm.getChildrenByClass("StaCubic")) {
            DataObject connectedObj = staCubic.findObjectAttributeValue("obj_id")
                    .flatMap(DataObjectRef::resolve)
                    .orElse(null);

            int busIndexIn = staCubic.getIntAttributeValue("obj_bus");

            if (connectedObj == null) {
                getImportContext().cubiclesObjectNotFound.add(staCubic);
            } else {
                int outOfService = connectedObj.findIntAttributeValue("outserv").orElse(0);
                int referenceNode = createNodeSwitchAndInternalConnection(vl, node, staCubic, connectedObj.getDataClassName().equals("ElmCoup"),
                    connectedObj.getLocName(), outOfService == 1);
                mapConnectedObjToNode(vl, referenceNode, busIndexIn, connectedObj);
            }
        }
    }

    private int createNodeSwitchAndInternalConnection(VoltageLevel vl, int node, DataObject staCubic,
        boolean isConnectedObjSwitch, String connectedObjId, boolean outOfService) {
        List<DataObject> staSwitches = staCubic.getChildrenByClass("StaSwitch");

        int referenceNode = node;

        // An internalConnection is created, as in iidm only one element can be connected to a node,
        // except when the element is a ElmCoup (breaker).
        if (staSwitches.isEmpty()) {
            if (!isConnectedObjSwitch) {
                int additionalNode = createNode(vl);
                createInternalConnection(vl, node, additionalNode);
                referenceNode = additionalNode;
            }
        } else if (staSwitches.size() == 1) {
            int additionalNode = createNode(vl);
            new SwitchConverter(getImportContext(), getNetwork()).createFromStaSwitch(vl, node, additionalNode, staSwitches.get(0));
            referenceNode = additionalNode;
        } else {
            throw new PowerFactoryException("Only one staSwitch is allowed inside a cubicle");
        }

        // we have decided to create fictitious switches to map the outOfService attribute to IIDM
        if (outOfService) {
            int additionalNode = createNode(vl);
            new SwitchConverter(getImportContext(), getNetwork()).createFictitiousSwitch(vl, referenceNode, additionalNode, connectedObjId);
            referenceNode = additionalNode;
        }

        return referenceNode;
    }

    private void mapConnectedObjToNode(VoltageLevel vl, int node, int busIndexIn, DataObject connectedObj) {
        getImportContext().objIdToNode.computeIfAbsent(connectedObj.getId(), k -> new ArrayList<>())
            .add(new NodeRef(vl.getId(), node, busIndexIn));
    }

    private static final class NodeModel {
        private final boolean isBusbarSection;
        private final String busbarId;

        private NodeModel(boolean isBusbarSection, String busbarId) {
            this.isBusbarSection = isBusbarSection;
            this.busbarId = busbarId;
        }

        // Usage:0=Busbar:1=Junction Node:2=Internal Node
        private static NodeModel create(VoltageLevel vl, DataObject elmTerm) {
            int iUsage = elmTerm.getIntAttributeValue("iUsage");
            String busbarId = vl.getId() + "_" + elmTerm.getLocName();

            return new NodeModel(iUsage == 0, busbarId);
        }
    }
}
