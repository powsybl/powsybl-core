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
public class Boundary {
    public Boundary(CgmesModel cgmes) {
        PropertyBags bns = cgmes.boundaryNodes();
        nodesName = new HashMap<>();
        if (bns != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}{}{}",
                        "Boundary nodes",
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
        nodesLines = new HashMap<>();
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

    public void addLineAtNode(PropertyBag line, String node) {
        List<PropertyBag> lines;
        lines = nodesLines.computeIfAbsent(node, ls -> new ArrayList<>(2));
        lines.add(line);
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

    public List<PropertyBag> linesAtNode(String node) {
        return nodesLines.getOrDefault(node, Collections.emptyList());
    }


    public String nameAtBoundary(String node) {
        return nodesName.containsKey(node) ? nodesName.get(node) : "XnodeCode-unknown";
    }

    private static class Voltage {
        double v;
        double angle;
    }

    private final Set<String> nodes;
    private final Map<String, List<PropertyBag>> nodesLines;
    private final Map<String, PowerFlow> nodesPowerFlow;
    private final Map<String, Voltage> nodesVoltage;
    private final Map<String, String> nodesName;

    private static final Logger LOG = LoggerFactory.getLogger(Boundary.class);
}
