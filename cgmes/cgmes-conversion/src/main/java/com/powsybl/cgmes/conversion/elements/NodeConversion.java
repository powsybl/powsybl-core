/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.CountryConversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.powsybl.cgmes.conversion.CgmesReports.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
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
        }
    }

    private VoltageLevel newBoundarySubstationVoltageLevel() {
        double nominalVoltage = context.cgmes().nominalVoltage(p.getId("BaseVoltage"));
        if (LOG.isWarnEnabled()) {
            LOG.warn("Boundary node will be converted {}, nominalVoltage {} from base voltage {}", id, nominalVoltage, p.getId("BaseVoltage"));
        }
        String substationId = Context.boundarySubstationId(this.id);
        String vlId = Context.boundaryVoltageLevelId(this.id);
        String substationName = "boundary";
        String vlName = "boundary";
        SubstationAdder adder = context.network()
                .newSubstation()
                .setId(context.namingStrategy().getIidmId("Substation", substationId))
                .setName(substationName)
                .setCountry(boundaryCountryCode());
        if (boundaryCountryCode() != null) {
            adder.setGeographicalTags(boundaryCountryCode().toString());
        }
        Substation substation = adder.add();
        return substation.newVoltageLevel()
                .setId(context.namingStrategy().getIidmId("VoltageLevel", vlId))
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

    private VoltageLevel voltageLevel() {
        if (insideBoundary() && context.config().convertBoundary()) {
            return context.network().getVoltageLevel(Context.boundaryVoltageLevelId(this.id));
        } else if (!insideBoundary()) {
            String containerId = p.getId(CgmesNames.CONNECTIVITY_NODE_CONTAINER);
            String cgmesId = context.cgmes().container(containerId).voltageLevel();
            if (cgmesId == null) {
                // A CGMES Voltage Level can not be obtained from the connectivity node container
                // The connectivity node container is a cim:Line, and
                // the conversion has created a fictitious voltage level in IIDM
                cgmesId = context.nodeContainerMapping().getFictitiousVoltageLevelForContainer(containerId, this.id);
            }
            String iidm = context.namingStrategy().getIidmId(CgmesNames.VOLTAGE_LEVEL, cgmesId);
            String iidmId = context.nodeContainerMapping().voltageLevelIidm(iidm);
            return iidmId != null ? context.network().getVoltageLevel(iidmId) : null;
        }
        return null;
    }

    private void newNode(VoltageLevel vl) {
        VoltageLevel.NodeBreakerView nbv = vl.getNodeBreakerView();
        // id is the connectivityNode
        int iidmNode = context.nodeMapping().iidmNodeForConnectivityNode(id, vl);

        // Busbar sections are created for every connectivity node to be
        // able to easily check the topology calculated by IIDM
        // against the topology present in the CGMES model
        if (context.config().createBusbarSectionForEveryConnectivityNode()) {
            BusbarSection bus = nbv.newBusbarSection()
                    .setId(context.namingStrategy().getIidmId("Bus", id))
                    .setName(context.namingStrategy().getIidmName("Bus", name))
                    .setNode(iidmNode)
                    .add();
            LOG.debug("    BusbarSection added at node {} : {} {} : {}", iidmNode, id, name, bus);
        }
    }

    private void newBus(VoltageLevel voltageLevel) {
        voltageLevel.getBusBreakerView().newBus()
                .setId(context.namingStrategy().getIidmId("Bus", id))
                .setName(context.namingStrategy().getIidmName("Bus", name))
                .add();
    }

    private static boolean valid(double v, double angle) {
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
        // and ensure that both ends have the same values (v,angle).
        // This is what HELM integration layer does when mapping from IIDM to HELM.

        return v > 0 && Double.isFinite(angle);
    }

    public static void update(Bus bus, Context context) {
        if (bus.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            updateBusBreakerBus(bus, context);
        } else {
            updateNodeBreakerBus(bus, context);
        }
    }

    private static void updateBusBreakerBus(Bus bus, Context context) {
        bus.getConnectedTerminalStream()
                .map(terminal -> getBusBreakerSvVoltage(terminal, context))
                .flatMap(Optional::stream)
                .findFirst()
                .ifPresent(svVoltage -> {
                    double v = svVoltage.asDouble(CgmesNames.VOLTAGE);
                    double angle = svVoltage.asDouble(CgmesNames.ANGLE);
                    if (valid(v, angle)) {
                        bus.setV(v).setAngle(angle);
                    } else {
                        String topologicalNode = svVoltage.getId(CgmesNames.TOPOLOGICAL_NODE);
                        invalidAngleVoltageReport(context.getReportNode(), bus, topologicalNode, v, angle);
                    }
                });
    }

    private static Optional<PropertyBag> getBusBreakerSvVoltage(Terminal terminal, Context context) {
        // busBreakerId is always a topologicalNode
        return getSvVoltage(terminal.getBusBreakerView().getBus().getId(), context);
    }

    private static void updateNodeBreakerBus(Bus bus, Context context) {
        bus.getConnectedTerminalStream()
                .map(terminal -> getNodeBreakerSvVoltage(terminal, context))
                .flatMap(Optional::stream)
                .findFirst()
                .ifPresent(svVoltage -> {
                    double v = svVoltage.asDouble(CgmesNames.VOLTAGE);
                    double angle = svVoltage.asDouble(CgmesNames.ANGLE);
                    if (valid(v, angle)) {
                        bus.setV(v).setAngle(angle);
                    } else {
                        String topologicalNode = svVoltage.getId(CgmesNames.TOPOLOGICAL_NODE);
                        invalidAngleVoltageReport(context.getReportNode(), bus, topologicalNode, v, angle);
                    }
                });
    }

    private static Optional<PropertyBag> getNodeBreakerSvVoltage(Terminal terminal, Context context) {
        return getTerminalId(terminal.getConnectable(), terminal.getSide())
                .flatMap(terminalId -> getTopologicalNodeId(terminalId, context))
                .flatMap(topologicalNodeId -> getSvVoltage(topologicalNodeId, context));
    }

    private static Optional<String> getTerminalId(Connectable<?> connectable, ThreeSides side) {
        if (side == null) {
            return connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL)
                    .or(() -> connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1));
        }
        return switch (side) {
            case ONE -> connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1)
                    .or(() -> connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL));
            case TWO -> connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2);
            case THREE -> connectable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 3);
        };
    }

    private static Optional<String> getTopologicalNodeId(String terminalId, Context context) {
        return Optional.ofNullable(context.cgmesTerminal(terminalId))
                .map(cgmesTerminal -> cgmesTerminal.getId(CgmesNames.TOPOLOGICAL_NODE));
    }

    private static Optional<PropertyBag> getSvVoltage(String topologicalNodeId, Context context) {
        return Optional.ofNullable(context.svVoltage(topologicalNodeId));
    }

    private static final Logger LOG = LoggerFactory.getLogger(NodeConversion.class);
}
