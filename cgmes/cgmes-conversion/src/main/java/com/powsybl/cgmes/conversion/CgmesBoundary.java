/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.BoundaryEquipment.BoundaryEquipmentType;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */

public class CgmesBoundary {

    public CgmesBoundary(CgmesModel cgmes) {
        PropertyBags bns = cgmes.boundaryNodes();
        nodesName = new HashMap<>();
        lineAtNodes = new HashMap<>();
        hvdcNodes = new HashSet<>();
        if (bns != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}{}{}",
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
                nodesName.put(tn, node.get("topologicalNodeName"));
                if (node.containsKey("description") && node.getId("description").startsWith("HVDC")) {
                    hvdcNodes.add(cn);
                    hvdcNodes.add(tn);
                }
                if (node.containsKey("energyIdentCodeEicFromNode")) {
                    lineAtNodes.put(cn, node.getId("energyIdentCodeEicFromNode"));
                    lineAtNodes.put(tn, node.getId("energyIdentCodeEicFromNode"));
                } else if (node.containsKey("energyIdentCodeEicFromNodeContainer")) { // EIC code on node has priority but if absent, EIC code on Line is used
                    lineAtNodes.put(cn, node.getId("energyIdentCodeEicFromNodeContainer"));
                    lineAtNodes.put(tn, node.getId("energyIdentCodeEicFromNodeContainer"));
                }
            });
        } else {
            nodes = Collections.emptySet();
        }
        nodesEquipment = new HashMap<>();
        nodesEquivalentInjections = new HashMap<>();
        nodesPowerFlow = new HashMap<>();
        nodesVoltage = new HashMap<>();
    }

    public boolean containsNode(String id) {
        return nodes.contains(id);
    }

    public boolean hasPowerFlow(String node) {
        return nodesPowerFlow.containsKey(node);
    }

    public PowerFlow powerFlowAtNode(String node) {
        return nodesPowerFlow.get(node);
    }

    /**
     * @deprecated Not used anymore. To set an equipment at boundary node, use
     * {@link CgmesBoundary#addAcLineSegmentAtNode(PropertyBag, String)} or
     * {@link CgmesBoundary#addSwitchAtNode(PropertyBag, String)} or
     * {@link CgmesBoundary#addTransformerAtNode(PropertyBags, String)} or
     * {@link CgmesBoundary#addEquivalentBranchAtNode(PropertyBag, String)}  instead.
     */
    @Deprecated
    public void addEquipmentAtNode(PropertyBag line, String node) {
        throw new ConversionException("Deprecated. Not used anymore");
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

    public void addPowerFlowAtNode(String node, PowerFlow f) {
        nodesPowerFlow.compute(node, (n, f0) -> f0 == null ? f : f0.sum(f));
    }

    public void addVoltageAtBoundary(String node, double v, double angle) {
        Voltage voltage = new Voltage();
        voltage.v = v;
        voltage.angle = angle;
        nodesVoltage.put(node, voltage);
    }

    public boolean hasVoltage(String node) {
        return nodesVoltage.containsKey(node);
    }

    public double vAtBoundary(String node) {
        return nodesVoltage.containsKey(node) ? nodesVoltage.get(node).v : Double.NaN;
    }

    public double angleAtBoundary(String node) {
        return nodesVoltage.containsKey(node) ? nodesVoltage.get(node).angle : Double.NaN;
    }

    /**
     * @deprecated Not used anymore. To get the equipment at boundary node, use
     * {@link CgmesBoundary#boundaryEquipmentAtNode(String)} instead.
     */
    @Deprecated
    public List<PropertyBag> equipmentAtNode(String node) {
        throw new ConversionException("Deprecated. Not used anymore");
    }

    public List<BoundaryEquipment> boundaryEquipmentAtNode(String node) {
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

    private static class Voltage {
        double v;
        double angle;
    }

    private final Set<String> nodes;
    private final Map<String, List<BoundaryEquipment>> nodesEquipment;
    private final Map<String, List<PropertyBag>> nodesEquivalentInjections;
    private final Map<String, PowerFlow> nodesPowerFlow;
    private final Map<String, Voltage> nodesVoltage;
    private final Map<String, String> nodesName;
    private final Map<String, String> lineAtNodes;
    private final Set<String> hvdcNodes;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesBoundary.class);
}
