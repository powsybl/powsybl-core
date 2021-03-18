/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.CountryConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.Networks;
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
            if (context.nodeBreaker()) {
                newNode(newBoundarySubstationVoltageLevel());
            } else {
                newBus(newBoundarySubstationVoltageLevel());
            }
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

    private VoltageLevel newBoundarySubstationVoltageLevel() {
        double nominalVoltage = context.cgmes().nominalVoltage(p.getId("BaseVoltage"));
        LOG.warn("Boundary node will be converted {}, nominalVoltage {} from base voltage {}", id, nominalVoltage, p.getId("BaseVoltage"));
        String substationId = Context.boundarySubstationId(this.id);
        String vlId = Context.boundaryVoltageLevelId(this.id);
        String substationName = "boundary";
        String vlName = "boundary";
        return context.network()
            .newSubstation()
            .setId(context.namingStrategy().getId("Substation", substationId))
            .setName(substationName)
            .setCountry(boundaryCountryCode())
            .add()
            .newVoltageLevel()
            .setId(context.namingStrategy().getId("VoltageLevel", vlId))
            .setName(vlName)
            .setNominalV(nominalVoltage)
            .setTopologyKind(context.nodeBreaker() ? TopologyKind.NODE_BREAKER : TopologyKind.BUS_BREAKER)
            .add();
    }

    private Country boundaryCountryCode() {
        // Selection of country code when ENTSO-E extensions are present
        return CountryConversion.fromIsoCode(p.getLocal("fromEndIsoCode"))
            .orElseGet(() -> CountryConversion.fromIsoCode(p.getLocal("toEndIsoCode"))
                .orElseGet(() -> {
                    Supplier<String> countryCodes = () -> String.format("Country. ISO codes %s %s",
                        p.getLocal("fromEndIsoCode"),
                        p.getLocal("toEndIsoCode"));
                    ignored(countryCodes);
                    return null;
                }));
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

    public void setVoltageAngleNodeBreaker() {
        if (!context.nodeBreaker()) {
            return;
        }
        // Before trying to find a bus, check that values are valid
        if (!checkValidVoltageAngle(null)) {
            return;
        }

        VoltageLevel vl = voltageLevel();
        if (vl == null) { // if inside boundary but boundaries must not be converted
            return;
        }
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        String connectivityNode = id;
        int iidmNode = context.nodeMapping().iidmNodeForConnectivityNode(connectivityNode, vl);
        if (!topo.hasAttachedEquipment(iidmNode)) {
            LOG.error("ConnectivityNode {} with voltage and angle is not valid in IIDM", connectivityNode);
            return;
        }
        // To obtain a bus for which we want to set voltage:
        // If there no Terminal at this IIDM node,
        // then find from it the first connected node with a Terminal
        Terminal t = topo.getOptionalTerminal(iidmNode)
                .orElseGet(() -> Networks.getEquivalentTerminal(vl, iidmNode));
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
            LOG.warn(
                    "Can't find a bus from the Bus View to set Voltage and Angle, we use the bus {} from the Bus/Breaker view. Connectivity node {}",
                    bus, id);
        }
        setVoltageAngle(bus);
    }

    private VoltageLevel voltageLevel() {
        if (insideBoundary() && context.config().convertBoundary()) {
            return context.network().getVoltageLevel(Context.boundaryVoltageLevelId(this.id));
        } else if (!insideBoundary()) {
            String containerId = p.getId("ConnectivityNodeContainer");
            String cgmesId = context.cgmes().container(containerId).voltageLevel();

            String iidm = context.namingStrategy().getId(CgmesNames.VOLTAGE_LEVEL, cgmesId);
            String iidmId = context.substationIdMapping().voltageLevelIidm(iidm);
            return iidmId != null ? context.network().getVoltageLevel(iidmId) : null;
        }
        return null;
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
        if (checkValidVoltageAngle(bus)) {
            setVoltageAngle(bus);
        }
    }

    private boolean checkValidVoltageAngle(Bus bus) {
        double v = p.asDouble(CgmesNames.VOLTAGE);
        double angle = p.asDouble(CgmesNames.ANGLE);
        boolean valid = valid(v, angle);
        if (!valid) {
            Supplier<String> reason = () -> String.format(
                "v = %f, angle = %f. Node %s",
                v,
                angle,
                id);
            Supplier<String> location = () -> bus == null
                ? "No bus"
                : String.format("Bus %s, Substation %s, Voltage level %s",
                    bus.getId(),
                    bus.getVoltageLevel().getSubstation().getNameOrId(),
                    bus.getVoltageLevel().getNameOrId());
            Supplier<String> message = () -> reason.get() + ". " + location.get();
            context.invalid("SvVoltage", message);
        }
        return valid;
    }

    private void setVoltageAngle(Bus bus) {
        Objects.requireNonNull(bus);
        double v = p.asDouble(CgmesNames.VOLTAGE);
        double angle = p.asDouble(CgmesNames.ANGLE);
        bus.setV(v);
        bus.setAngle(angle);
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
