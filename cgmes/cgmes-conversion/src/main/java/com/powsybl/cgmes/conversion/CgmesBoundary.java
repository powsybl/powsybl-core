/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.BoundaryEquipment.BoundaryEquipmentType;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

public class CgmesBoundary {

    private static final String ENERGY_IDENT_CODE_EIC_FROM_NODE = "energyIdentCodeEicFromNode";
    private static final String ENERGY_IDENT_CODE_EIC_FROM_NODE_CONTAINER = "energyIdentCodeEicFromNodeContainer";

    public CgmesBoundary(CgmesModel cgmes) {
        PropertyBags bns = cgmes.boundaryNodes();
        nodesName = new HashMap<>();
        topologicalNodes = new HashMap<>();
        lineAtNodes = new HashMap<>();
        hvdcNodes = new HashSet<>();
        if (bns != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("{}{}{}",
                        "CgmesBoundary nodes",
                        System.lineSeparator(),
                        bns.tabulateLocals());
            }
            nodes = new HashSet<>(bns.size());
            bns.forEach(node -> {
                String cn = node.getId("ConnectivityNode");
                String tn = node.getId("TopologicalNode");

                nodes.add(cn);
                nodes.add(tn);
                nodesName.put(cn, node.get("name"));
                String tnName = node.get("topologicalNodeName");
                nodesName.put(tn, tnName);
                topologicalNodes.put(tnName, tn);
                if (isDcNode(node)) {
                    hvdcNodes.add(cn);
                    hvdcNodes.add(tn);
                }
                if (node.containsKey(ENERGY_IDENT_CODE_EIC_FROM_NODE)) {
                    lineAtNodes.put(cn, node.getId(ENERGY_IDENT_CODE_EIC_FROM_NODE));
                    lineAtNodes.put(tn, node.getId(ENERGY_IDENT_CODE_EIC_FROM_NODE));
                } else if (node.containsKey(ENERGY_IDENT_CODE_EIC_FROM_NODE_CONTAINER)) { // EIC code on node has priority but if absent, EIC code on Line is used
                    lineAtNodes.put(cn, node.getId(ENERGY_IDENT_CODE_EIC_FROM_NODE_CONTAINER));
                    lineAtNodes.put(tn, node.getId(ENERGY_IDENT_CODE_EIC_FROM_NODE_CONTAINER));
                }
            });
        } else {
            nodes = Collections.emptySet();
        }
        nodesEquipment = new HashMap<>();
        nodesEquivalentInjections = new HashMap<>();
    }

    public static boolean isDcNode(PropertyBag node) {
        if (node.containsKey("boundaryPointIsDirectCurrent")) {
            return Boolean.parseBoolean(node.getId("boundaryPointIsDirectCurrent"));
        } else if (node.containsKey("description") && node.getId("description").startsWith("HVDC")) {
            return true;
        } else {
            return node.containsKey("topologicalNodeDescription") && node.getId("topologicalNodeDescription").startsWith("HVDC");
        }
    }

    public boolean containsNode(String id) {
        return nodes.contains(id);
    }

    public void addAcLineSegmentAtNode(PropertyBag line, String node) {
        List<BoundaryEquipment> equipment;
        equipment = nodesEquipment.computeIfAbsent(node, ls -> new ArrayList<>(2));
        equipment.add(new BoundaryEquipment(BoundaryEquipmentType.AC_LINE_SEGMENT, line));
    }

    public void addSwitchAtNode(PropertyBag sw, String node) {
        List<BoundaryEquipment> equipment;
        equipment = nodesEquipment.computeIfAbsent(node, ls -> new ArrayList<>(2));
        equipment.add(new BoundaryEquipment(BoundaryEquipmentType.SWITCH, sw));
    }

    public void addTransformerAtNode(PropertyBags transformerEnds, String node) {
        List<BoundaryEquipment> equipment;
        equipment = nodesEquipment.computeIfAbsent(node, ls -> new ArrayList<>(2));
        equipment.add(new BoundaryEquipment(BoundaryEquipmentType.TRANSFORMER, transformerEnds));
    }

    public void addEquivalentBranchAtNode(PropertyBag equivalentBranch, String node) {
        List<BoundaryEquipment> equipment;
        equipment = nodesEquipment.computeIfAbsent(node, ls -> new ArrayList<>(2));
        equipment.add(new BoundaryEquipment(BoundaryEquipmentType.EQUIVALENT_BRANCH, equivalentBranch));
    }

    public void addEquivalentInjectionAtNode(PropertyBag equivalentInjection, String node) {
        nodesEquivalentInjections.computeIfAbsent(node, ls -> new ArrayList<>(2)).add(equivalentInjection);
    }

    List<BoundaryEquipment> boundaryEquipmentAtNode(String node) {
        return nodesEquipment.getOrDefault(node, Collections.emptyList());
    }

    public List<PropertyBag> equivalentInjectionsAtNode(String node) {
        return nodesEquivalentInjections.getOrDefault(node, Collections.emptyList());
    }

    public String nameAtBoundary(String node) {
        return nodesName.getOrDefault(node, "XnodeCode-unknown");
    }

    public String lineAtBoundary(String node) {
        return lineAtNodes.get(node);
    }

    public boolean isHvdc(String node) {
        return hvdcNodes.contains(node);
    }

    public Collection<String> xnodesNames() {
        return topologicalNodes.keySet();
    }

    private final Set<String> nodes;
    private final Map<String, List<BoundaryEquipment>> nodesEquipment;
    private final Map<String, List<PropertyBag>> nodesEquivalentInjections;
    private final Map<String, String> nodesName;
    private final Map<String, String> topologicalNodes;
    private final Map<String, String> lineAtNodes;
    private final Set<String> hvdcNodes;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesBoundary.class);
}
