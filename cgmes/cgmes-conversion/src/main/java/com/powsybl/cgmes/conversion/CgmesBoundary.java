/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */

public class CgmesBoundary {

    private static final String UNEXPECTED_BOUNDARY_EQUIPMENT = "Unexpected boundary equipment %s. Must be %s";

    public enum BoundaryEquipmentType {
        AC_LINE_SEGMENT, SWITCH, TRANSFORMER, EQUIVALENT_BRANCH
    }

    public CgmesBoundary(CgmesModel cgmes) {
        PropertyBags bns = cgmes.boundaryNodes();
        nodesName = new HashMap<>();
        if (bns != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}{}{}",
                        "CgmesBoundary nodes",
                        System.lineSeparator(),
                        bns.tabulateLocals());
            }
            nodes = new HashSet<>(bns.size());
            bns.forEach(node -> {
                String id = node.getId("Node");
                nodes.add(id);
                nodesName.put(id, node.get("Name"));
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

    public List<BoundaryEquipment> equipmentAtNode(String node) {
        return nodesEquipment.getOrDefault(node, Collections.emptyList());
    }

    public static PropertyBag getPropertyBagOfAcLineSegmentAtBoundary(BoundaryEquipment eq) {
        if (eq.type == BoundaryEquipmentType.AC_LINE_SEGMENT) {
            return eq.propertyBag;
        } else {
            throw new ConversionException(String.format(UNEXPECTED_BOUNDARY_EQUIPMENT, eq.type, BoundaryEquipmentType.AC_LINE_SEGMENT));
        }
    }

    public static PropertyBag getPropertyBagOfSwitchAtBoundary(BoundaryEquipment eq) {
        if (eq.type == BoundaryEquipmentType.SWITCH) {
            return eq.propertyBag;
        } else {
            throw new ConversionException(String.format(UNEXPECTED_BOUNDARY_EQUIPMENT, eq.type, BoundaryEquipmentType.SWITCH));
        }
    }

    public static PropertyBags getPropertyBagsOfTransformerAtBoundary(BoundaryEquipment eq) {
        if (eq.type == BoundaryEquipmentType.TRANSFORMER) {
            return eq.propertyBags;
        } else {
            throw new ConversionException(String.format(UNEXPECTED_BOUNDARY_EQUIPMENT, eq.type, BoundaryEquipmentType.TRANSFORMER));
        }
    }

    public static PropertyBag getPropertyBagOfEquivalentBranchAtBoundary(BoundaryEquipment eq) {
        if (eq.type == BoundaryEquipmentType.EQUIVALENT_BRANCH) {
            return eq.propertyBag;
        } else {
            throw new ConversionException(String.format(UNEXPECTED_BOUNDARY_EQUIPMENT, eq.type, BoundaryEquipmentType.EQUIVALENT_BRANCH));
        }
    }

    public List<PropertyBag> equivalentInjectionsAtNode(String node) {
        return nodesEquivalentInjections.getOrDefault(node, Collections.emptyList());
    }

    public String nameAtBoundary(String node) {
        return nodesName.containsKey(node) ? nodesName.get(node) : "XnodeCode-unknown";
    }

    public static final class BoundaryEquipment {
        private BoundaryEquipment(BoundaryEquipmentType type, PropertyBag propertyBag) {
            this.type = type;
            this.propertyBag = propertyBag;
            propertyBags = null;
        }

        private BoundaryEquipment(BoundaryEquipmentType type, PropertyBags propertyBags) {
            this.type = type;
            this.propertyBag = null;
            this.propertyBags = propertyBags;
        }

        public BoundaryEquipmentType getBoundaryEquipmentType() {
            return type;
        }

        private final BoundaryEquipmentType type;
        private final PropertyBag propertyBag;
        private final PropertyBags propertyBags;
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

    private static final Logger LOG = LoggerFactory.getLogger(CgmesBoundary.class);
}
