/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class NodeConversion extends AbstractIdentifiedObjectConversion {

    public NodeConversion(String nodeTypeName, PropertyBag n, Context context) {
        super(nodeTypeName, n, context);
    }

    @Override
    public boolean insideBoundary() {
        return context.boundary().containsNode(id);
    }

    @Override
    public void convertInsideBoundary() {
        if (context.config().convertBoundary()) {
            String bv = p.getId("BaseVoltage");
            double v = context.cgmes().nominalVoltage(bv);
            LOG.info("Boundary node will be converted {}, nominalVoltage {}", id, v);
            VoltageLevel voltageLevel = context.createSubstationVoltageLevel(id, v);
            newBus(voltageLevel);
        } else {
            // TODO(Luma): when the boundary nodes are not converted to IIDM buses
            // they are not exported (the SV is built from buses of IIDM network)
            // if we try to re-import the exported CGMES, those nodes do not have voltage
            if (p.containsKey(CgmesNames.VOLTAGE) && p.containsKey(CgmesNames.ANGLE)) {
                double v = p.asDouble(CgmesNames.VOLTAGE);
                double angle = p.asDouble(CgmesNames.ANGLE);
                if (valid(v, angle)) {
                    context.boundary().addVoltageAtBoundary(id, v, angle);
                }
            }
        }
    }

    @Override
    public boolean valid() {
        if (voltageLevel() == null) {
            missing(String.format("VoltageLevel %s", p.getId(CgmesNames.VOLTAGE_LEVEL)));
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        VoltageLevel vl = voltageLevel();
        Objects.requireNonNull(vl);
        if (context.nodeBreaker()) {
            newNode(vl);
        } else {
            newBus(vl);
        }
    }

    static class FirstTerminalTraverser implements VoltageLevel.NodeBreakerView.Traverser {
        FirstTerminalTraverser(VoltageLevel.NodeBreakerView topology, int node) {
            this.topology = topology;
            this.node = node;
        }

        Terminal firstTerminal() {
            topology.traverse(node, this);
            return terminal;
        }

        @Override
        public boolean traverse(int node1, Switch sw, int node2) {
            // If the first terminal has already been found,
            // do not continue traversal
            if (terminal != null) {
                return false;
            }
            // Proceed only to new nodes that can be reached
            // through internal connections or closed switches
            if (sw != null && sw.isOpen()) {
                return false;
            }
            terminal = topology.getTerminal(node2);
            // Continue traversal if no terminal at node2
            return terminal == null;
        }

        private final VoltageLevel.NodeBreakerView topology;
        private final int node;
        private Terminal terminal;
    }

    public void setVoltageAngleNodeBreaker() {
        if (!context.nodeBreaker()) {
            return;
        }
        VoltageLevel vl = voltageLevel();
        Objects.requireNonNull(vl);
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        String connectivityNode = id;
        int iidmNode = context.nodeMapping().iidmNodeForConnectivityNode(connectivityNode, vl);
        // To obtain a bus for which we want to set voltage:
        // If there no Terminal at this IIDM node,
        // then find from it the first connected node with a Terminal
        Terminal t = topo.getTerminal(iidmNode);
        if (t == null) {
            t = new FirstTerminalTraverser(topo, iidmNode).firstTerminal();
        }
        if (t == null) {
            LOG.error("Can't find a Terminal to obtain a Bus to set Voltage, Angle. ConnectivityNode {}", id);
            return;
        }
        Bus bus = t.getBusView().getBus();
        if (bus == null) {
            bus = t.getBusBreakerView().getBus();
            if (bus == null) {
                LOG.error("Can't find a Bus from Terminal to set Voltage, Angle. Connectivity Node {}", id);
                return;
            }
            LOG.warn("Can't find a calculated Bus to set Voltage, Angle, but found a configured Bus {}. Connectivity node {}", bus, id);
        }
        setVoltageAngle(bus);
    }

    private VoltageLevel voltageLevel() {
        String containerId = p.getId("ConnectivityNodeContainer");
        String cgmesId = context.cgmes().container(containerId).voltageLevel();
        String iidmId = context.namingStrategy().getId(CgmesNames.VOLTAGE_LEVEL, cgmesId);
        return iidmId != null ? context.network().getVoltageLevel(iidmId) : null;
    }

    private void newNode(VoltageLevel vl) {
        VoltageLevel.NodeBreakerView nbv = vl.getNodeBreakerView();
        String connectivityNode = id;
        int iidmNode = context.nodeMapping().iidmNodeForConnectivityNode(connectivityNode, vl);

        // Busbar sections are created for every connectivity node to be
        // able to easily check the topology calculated by IIDM
        // against the topology present in the CGMES model
        if (context.config().createBusbarSectionForEveryConnectivityNode()) {
            BusbarSection bus = nbv.newBusbarSection()
                    .setId(context.namingStrategy().getId("Bus", id))
                    .setName(context.namingStrategy().getName("Bus", name))
                    .setNode(iidmNode)
                    .add();
            LOG.debug("    BusbarSection added at node {} : {} {} : {}", iidmNode, id, name, bus);
        }
    }

    private void newBus(VoltageLevel voltageLevel) {
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(context.namingStrategy().getId("Bus", id))
                .setName(context.namingStrategy().getName("Bus", name))
                .add();
        setVoltageAngle(bus);
    }

    private void setVoltageAngle(Bus bus) {
        double v = p.asDouble(CgmesNames.VOLTAGE);
        double angle = p.asDouble(CgmesNames.ANGLE);
        if (valid(v, angle)) {
            Objects.requireNonNull(bus);
            bus.setV(v);
            bus.setAngle(angle);
        } else {
            String reason = String.format(
                    "v = %f, angle = %f. Node %s",
                    v,
                    angle,
                    id);
            String location = bus == null
                    ? "No bus"
                    : String.format("Bus %s, Substation %s, Voltage level %s",
                            bus.getId(),
                            bus.getVoltageLevel().getSubstation().getName(),
                            bus.getVoltageLevel().getName());
            String message = String.format("%s. %s", reason, location);
            context.invalid("SvVoltage", message);
        }
    }

    private boolean valid(double v, double angle) {
        // TTG data for DACF has some 380 kV buses connected with v=0 and bad angle

        // LITGRID data for DACF contains some buses with v=0, angle=0
        // They are connected through a closed switch to a node
        // with correct values for v,angle.
        // If we ignore the SV values (v=0, angle=0),
        // then the IIDM configured bus will be left with (v=NaN, angle=NaN).
        // When using LoadFlow validation to check the initial state,
        // the bus view is queried for its v,angle. With this fix,
        // the configured bus with absent (v, angle) values will be ignored,
        // and the right values returned.
        // Another option could be to keep storing SV values (v=0, angle=0),
        // but perform a when a switch is converted,
        // and ensure its both ends have the same values (v,angle).
        // This is what HELM integration layer does when mapping from IIDM to HELM.

        boolean valid = v > 0;
        LOG.debug("valid voltage ({}, {}) ? {}", v, angle, valid);
        return valid;
    }

    private static final Logger LOG = LoggerFactory.getLogger(NodeConversion.class);
}
