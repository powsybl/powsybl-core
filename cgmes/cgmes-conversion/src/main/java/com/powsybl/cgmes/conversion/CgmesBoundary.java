/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.ACLineSegmentConversion;
import com.powsybl.cgmes.conversion.elements.EquipmentAtBoundaryConversion;
import com.powsybl.cgmes.conversion.elements.EquivalentBranchConversion;
import com.powsybl.cgmes.conversion.elements.SwitchConversion;
import com.powsybl.cgmes.conversion.elements.transformers.TwoWindingsTransformerConversion;
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

        public EquipmentAtBoundaryConversion createConversion(Context context) {
            EquipmentAtBoundaryConversion c = null;
            switch (type) {
                case AC_LINE_SEGMENT:
                    c = new ACLineSegmentConversion(propertyBag, context);
                    break;
                case SWITCH:
                    c = new SwitchConversion(propertyBag, context);
                    break;
                case TRANSFORMER:
                    c = new TwoWindingsTransformerConversion(propertyBags, context);
                    break;
                case EQUIVALENT_BRANCH:
                    c = new EquivalentBranchConversion(propertyBag, context);
                    break;
            }
            return c;
        }

        public void log() {
            if (LOG.isDebugEnabled()) {
                if (propertyBag != null) {
                    LOG.debug(propertyBag.tabulateLocals(type.toString()));
                }
                if (propertyBags != null) {
                    propertyBags.forEach(p -> LOG.debug(p.tabulateLocals(type.toString())));
                }
            }
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
