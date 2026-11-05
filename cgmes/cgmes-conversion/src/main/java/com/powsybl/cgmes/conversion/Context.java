/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.Conversion.Config;
import com.powsybl.cgmes.conversion.elements.dc.DCMapping;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.DcConnectable;
import com.powsybl.iidm.network.DcSwitch;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class Context {

    public Context(CgmesModel cgmes, Config config, Network network) {
        this(cgmes, config, network, ReportNode.NO_OP);
    }

    public Context(CgmesModel cgmes, Config config, Network network, ReportNode reportNode) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.config = Objects.requireNonNull(config);
        this.network = Objects.requireNonNull(network);
        pushReportNode(Objects.requireNonNull(reportNode));

        // Even if the CGMES model is node-breaker,
        // we could decide to ignore the connectivity nodes and
        // create buses directly from topological nodes,
        // the configuration says if we are performing the conversion
        // based on existing node-breaker info
        nodeBreaker = cgmes.isNodeBreaker() && !config.importNodeBreakerAsBusBreaker();

        namingStrategy = config.getNamingStrategy();
        cgmesBoundary = new CgmesBoundary(cgmes);
        nodeContainerMapping = new NodeContainerMapping(this);
        terminalMapping = new TerminalMapping();
        limitsMapping = new LimitsMapping(this);
        regulatingControlMapping = new RegulatingControlMapping(this);
        nodeMapping = new NodeMapping(this);
        dcMapping = new DCMapping(this);

        cachedGroupedTransformerEnds = new HashMap<>();
        cachedGroupedRatioTapChangers = new HashMap<>();
        cachedGroupedRatioTapChangerTablePoints = new HashMap<>();
        cachedGroupedPhaseTapChangers = new HashMap<>();
        cachedGroupedPhaseTapChangerTablePoints = new HashMap<>();
        cachedGroupedShuntCompensatorPoints = new HashMap<>();
        cachedGroupedReactiveCapabilityCurveData = new HashMap<>();

        buildCaches();

        cgmesTerminals = new HashMap<>();
        cgmesDcTerminals = new HashMap<>();
        ratioTapChangers = new HashMap<>();
        phaseTapChangers = new HashMap<>();
        regulatingControls = new HashMap<>();
        operationalLimits = new HashMap<>();
        generatingUnits = new HashMap<>();
        equivalentInjections = new HashMap<>();
        svVoltages = new HashMap<>();
        switches = new HashMap<>();
        equipmentInService = new HashMap<>();
        terminalsOfOutOfServiceEquipment = new HashSet<>();
    }

    public CgmesModel cgmes() {
        return cgmes;
    }

    public Network network() {
        return network;
    }

    public Config config() {
        return config;
    }

    public boolean nodeBreaker() {
        return nodeBreaker;
    }

    public NamingStrategy namingStrategy() {
        return namingStrategy;
    }

    public TerminalMapping terminalMapping() {
        return terminalMapping;
    }

    public void convertedTerminal(String terminalId, Terminal t, int n, PowerFlow f) {
        // Record the mapping between CGMES and IIDM terminals
        terminalMapping().add(terminalId, t, n);
        // Update the power flow at terminal. Check that IIDM allows setting it
        if (f.defined() && setPQAllowed(t)) {
            t.setP(f.p());
            t.setQ(f.q());
        }
    }

    public void convertedTerminalWithOnlyEq(String terminalId, Terminal t, int n) {
        // Record the mapping between CGMES and IIDM terminals
        terminalMapping().add(terminalId, t, n);
    }

    private boolean setPQAllowed(Terminal t) {
        return t.getConnectable().getType() != IdentifiableType.BUSBAR_SECTION;
    }

    public NodeMapping nodeMapping() {
        return nodeMapping;
    }

    public NodeContainerMapping nodeContainerMapping() {
        return nodeContainerMapping;
    }

    public DCMapping dcMapping() {
        return dcMapping;
    }

    public CgmesBoundary boundary() {
        return cgmesBoundary;
    }

    public LimitsMapping limitsMapping() {
        return limitsMapping;
    }

    public RegulatingControlMapping regulatingControlMapping() {
        return regulatingControlMapping;
    }

    public static String boundaryVoltageLevelId(String nodeId) {
        Objects.requireNonNull(nodeId);
        return nodeId + "_VL";
    }

    public static String boundarySubstationId(String nodeId) {
        Objects.requireNonNull(nodeId);
        return nodeId + "_S";
    }

    private void buildCaches() {
        buildCache(cachedGroupedTransformerEnds, cgmes().transformerEnds(), CgmesNames.POWER_TRANSFORMER);
        buildCache(cachedGroupedRatioTapChangers, cgmes().ratioTapChangers(), CgmesNames.POWER_TRANSFORMER);
        buildCache(cachedGroupedRatioTapChangerTablePoints, cgmes().ratioTapChangerTablePoints(), CgmesNames.RATIO_TAP_CHANGER_TABLE);
        buildCache(cachedGroupedPhaseTapChangers, cgmes().phaseTapChangers(), CgmesNames.POWER_TRANSFORMER);
        buildCache(cachedGroupedPhaseTapChangerTablePoints, cgmes().phaseTapChangerTablePoints(), CgmesNames.PHASE_TAP_CHANGER_TABLE);
        buildCache(cachedGroupedShuntCompensatorPoints, cgmes().nonlinearShuntCompensatorPoints(), "Shunt");
        buildCache(cachedGroupedReactiveCapabilityCurveData, cgmes().reactiveCapabilityCurveData(), "ReactiveCapabilityCurve");
    }

    private void buildCache(Map<String, PropertyBags> cache, PropertyBags ps, String groupName) {
        ps.forEach(p -> {
            String groupId = p.getId(groupName);
            cache.computeIfAbsent(groupId, b -> new PropertyBags()).add(p);
        });
    }

    public PropertyBags transformerEnds(String transformerId) {
        return cachedGroupedTransformerEnds.getOrDefault(transformerId, new PropertyBags());
    }

    public PropertyBags ratioTapChangers(String transformerId) {
        return cachedGroupedRatioTapChangers.getOrDefault(transformerId, new PropertyBags());
    }

    public PropertyBags ratioTapChangerTablePoints(String tableId) {
        return cachedGroupedRatioTapChangerTablePoints.getOrDefault(tableId, new PropertyBags());
    }

    public PropertyBags phaseTapChangers(String transformerId) {
        return cachedGroupedPhaseTapChangers.getOrDefault(transformerId, new PropertyBags());
    }

    public PropertyBags phaseTapChangerTablePoints(String tableId) {
        return cachedGroupedPhaseTapChangerTablePoints.getOrDefault(tableId, new PropertyBags());
    }

    public PropertyBags nonlinearShuntCompensatorPoints(String shuntId) {
        return cachedGroupedShuntCompensatorPoints.getOrDefault(shuntId, new PropertyBags());
    }

    public PropertyBags reactiveCapabilityCurveData(String curveId) {
        return cachedGroupedReactiveCapabilityCurveData.getOrDefault(curveId, new PropertyBags());
    }

    /**
     * Read the CGMES data used by the update (SSH, SV...) and cache it by id, for the update of the network.
     */
    public void buildUpdateCache() {
        buildUpdateCache(cgmesTerminals, cgmes.terminals(), CgmesNames.TERMINAL);
        buildUpdateCache(cgmesDcTerminals, cgmes.dcTerminals(), CgmesNames.DC_TERMINAL);
        buildUpdateCache(ratioTapChangers, cgmes.ratioTapChangers(), CgmesNames.RATIO_TAP_CHANGER);
        buildUpdateCache(phaseTapChangers, cgmes.phaseTapChangers(), CgmesNames.PHASE_TAP_CHANGER);
        buildUpdateCache(regulatingControls, cgmes.regulatingControls(), CgmesNames.REGULATING_CONTROL);
        buildUpdateCache(operationalLimits, cgmes.operationalLimits(), CgmesNames.OPERATIONAL_LIMIT);
        buildUpdateCache(generatingUnits, cgmes.generatingUnits(), CgmesNames.GENERATING_UNIT);
        buildUpdateCache(equivalentInjections, cgmes.equivalentInjections(), CgmesNames.EQUIVALENT_INJECTION);
        buildUpdateCache(svVoltages, cgmes.svVoltages(), CgmesNames.TOPOLOGICAL_NODE);
        buildUpdateCache(switches, cgmes.switches(), CgmesNames.SWITCH);
        buildOutOfServiceCache();
    }

    private static void buildUpdateCache(Map<String, PropertyBag> cache, PropertyBags cgmesPropertyBags, String tagId) {
        cgmesPropertyBags.forEach(p -> {
            String id = p.getId(tagId);
            cache.put(id, p);
        });
    }

    /**
     * Read the {@code Equipment.inService} values of the CGMES model (CGMES 3 only), and record the CGMES terminals of the
     * out-of-service equipment and of the machines of the out-of-service generating units. These terminals are then seen
     * as disconnected by the update (see {@link #isCgmesTerminalConnected(String)}).
     */
    private void buildOutOfServiceCache() {
        cgmes.equipmentInService().forEach(p -> equipmentInService.put(p.getId(CgmesNames.EQUIPMENT), p.asBoolean(CgmesNames.IN_SERVICE, true)));
        equipmentInService.forEach((equipmentId, inService) -> {
            if (Boolean.FALSE.equals(inService)) {
                Identifiable<?> identifiable = network.getIdentifiable(equipmentId);
                if (identifiable != null) {
                    addTerminalsOfOutOfServiceEquipment(identifiable);
                }
            }
        });
        network.getGeneratorStream()
                .filter(generator -> isOutOfService(generator.getProperty(Conversion.PROPERTY_GENERATING_UNIT)))
                .forEach(this::addTerminalsOfOutOfServiceEquipment);
    }

    /**
     * Record the CGMES terminals of an out-of-service equipment. They are found through the terminal aliases of its IIDM
     * object, because an SSH-only update does not describe the terminals of the equipment.
     * Switches are skipped: an out-of-service switch is opened instead. DC equipment, converters included, is skipped:
     * its {@code inService} value is ignored.
     *
     * @param identifiable the IIDM object of the out-of-service equipment
     */
    private void addTerminalsOfOutOfServiceEquipment(Identifiable<?> identifiable) {
        if (identifiable instanceof Switch || identifiable instanceof HvdcConverterStation<?>
                || identifiable instanceof DcConnectable<?> || identifiable instanceof DcSwitch) {
            return;
        }
        for (String alias : identifiable.getAliases()) {
            if (TERMINAL_ALIAS_TYPES.contains(identifiable.getAliasType(alias).orElse(""))) {
                terminalsOfOutOfServiceEquipment.add(alias);
            }
        }
    }

    /**
     * Tell whether a CGMES equipment is out of service ({@code Equipment.inService=false}).
     *
     * @param equipmentId the CGMES id of the equipment, can be {@code null}
     * @return {@code true} if the equipment is out of service; {@code false} if it is in service, if its {@code inService}
     * value is unknown, or if the id is {@code null}
     */
    public boolean isOutOfService(String equipmentId) {
        return equipmentId != null && Boolean.FALSE.equals(equipmentInService.get(equipmentId));
    }

    /**
     * Get the connected status of a CGMES terminal for the update. The terminal of an out-of-service equipment is
     * disconnected, whatever its {@code connected} value.
     *
     * @param cgmesTerminalId the CGMES id of the terminal
     * @return {@code false} for the terminal of an out-of-service equipment; otherwise the {@code connected} value of the
     * terminal, or an empty optional if the CGMES model does not give it
     */
    public Optional<Boolean> isCgmesTerminalConnected(String cgmesTerminalId) {
        if (terminalsOfOutOfServiceEquipment.contains(cgmesTerminalId)) {
            return Optional.of(false);
        }
        return Optional.ofNullable(cgmesTerminals.get(cgmesTerminalId)).flatMap(cgmesTerminal -> cgmesTerminal.asBoolean(CgmesNames.CONNECTED));
    }

    /**
     * Tell whether a CGMES terminal belongs to an out-of-service equipment.
     *
     * @param cgmesTerminalId the CGMES id of the terminal
     * @return {@code true} if the terminal belongs to an out-of-service equipment
     */
    public boolean isTerminalOfOutOfServiceEquipment(String cgmesTerminalId) {
        return terminalsOfOutOfServiceEquipment.contains(cgmesTerminalId);
    }

    /**
     * Get the ids of the CGMES terminals known by the update: the terminals read from the CGMES model, and the terminals
     * of the out-of-service equipment, which the CGMES model may not describe.
     *
     * @return the ids of the CGMES terminals
     */
    public Set<String> cgmesTerminalIds() {
        Set<String> ids = new HashSet<>(cgmesTerminals.keySet());
        ids.addAll(terminalsOfOutOfServiceEquipment);
        return ids;
    }

    public PropertyBag cgmesTerminal(String id) {
        return cgmesTerminals.get(id);
    }

    public PropertyBag cgmesDcTerminal(String id) {
        return cgmesDcTerminals.get(id);
    }

    public PropertyBag ratioTapChanger(String id) {
        return ratioTapChangers.get(id);
    }

    public PropertyBag phaseTapChanger(String id) {
        return phaseTapChangers.get(id);
    }

    public PropertyBag regulatingControl(String id) {
        return regulatingControls.get(id);
    }

    public PropertyBag operationalLimit(String id) {
        return operationalLimits.get(id);
    }

    public PropertyBag generatingUnit(String id) {
        return generatingUnits.get(id);
    }

    public PropertyBag equivalentInjection(String id) {
        return equivalentInjections.get(id);
    }

    public PropertyBag svVoltage(String id) {
        return svVoltages.get(id);
    }

    public PropertyBag cgmesSwitch(String id) {
        return switches.get(id);
    }

    // Handling issues found during conversion

    public ReportNode getReportNode() {
        return network.getReportNodeContext().getReportNode();
    }

    public void pushReportNode(ReportNode node) {
        network.getReportNodeContext().pushReportNode(node);
    }

    public ReportNode popReportNode() {
        return network.getReportNodeContext().popReportNode();
    }

    private enum ConversionIssueCategory {
        INVALID("Invalid"),
        IGNORED("Ignored"),
        MISSING("Missing"),
        FIXED("Fixed"),
        PENDING("Pending");

        ConversionIssueCategory(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        private final String description;
    }

    public void invalid(String what, String reason) {
        handleIssue(ConversionIssueCategory.INVALID, what, reason);
    }

    public void invalid(String what, Supplier<String> reason) {
        handleIssue(ConversionIssueCategory.INVALID, what, reason);
    }

    public void ignored(String what, String reason) {
        handleIssue(ConversionIssueCategory.IGNORED, what, reason);
    }

    public void ignored(String what, Supplier<String> reason) {
        handleIssue(ConversionIssueCategory.IGNORED, what, reason);
    }

    public void pending(String what, Supplier<String> reason) {
        handleIssue(ConversionIssueCategory.PENDING, what, reason);
    }

    public void fixed(String what, String reason) {
        handleIssue(ConversionIssueCategory.FIXED, what, reason);
    }

    public void fixed(String what, Supplier<String> reason) {
        handleIssue(ConversionIssueCategory.FIXED, what, reason);
    }

    public void fixed(String what, String reason, double wrong, double fixed) {
        Supplier<String> reason1 = () -> String.format("%s. Wrong %.4f, was fixed to %.4f", reason, wrong, fixed);
        handleIssue(ConversionIssueCategory.FIXED, what, reason1);
    }

    public void missing(String what) {
        String reason1 = "";
        handleIssue(ConversionIssueCategory.MISSING, what, reason1);
    }

    public void missing(String what, Supplier<String> reason) {
        handleIssue(ConversionIssueCategory.MISSING, what, reason);
    }

    public void missing(String what, double defaultValue) {
        Supplier<String> reason1 = () -> String.format("Using default value %.4f", defaultValue);
        handleIssue(ConversionIssueCategory.MISSING, what, reason1);
    }

    private void handleIssue(ConversionIssueCategory category, String what, String reason) {
        handleIssue(category, what, () -> reason);
    }

    private void handleIssue(ConversionIssueCategory category, String what, Supplier<String> reason) {
        logIssue(category, what, reason);
    }

    private static void logIssue(ConversionIssueCategory category, String what, Supplier<String> reason) {
        if (LOG.isWarnEnabled()) {
            String r = reason.get();
            if (r.isEmpty()) {
                LOG.warn("{}: {}", category, what);
            } else {
                LOG.warn("{}: {}. Reason: {}", category, what, r);
            }
        }
    }

    private final CgmesModel cgmes;
    private final Network network;
    private final Config config;

    private final boolean nodeBreaker;
    private final NamingStrategy namingStrategy;
    private final NodeContainerMapping nodeContainerMapping;
    private final CgmesBoundary cgmesBoundary;
    private final TerminalMapping terminalMapping;
    private final NodeMapping nodeMapping;
    private final LimitsMapping limitsMapping;
    private final RegulatingControlMapping regulatingControlMapping;
    private final DCMapping dcMapping;

    private final Map<String, PropertyBags> cachedGroupedTransformerEnds;
    private final Map<String, PropertyBags> cachedGroupedRatioTapChangers;
    private final Map<String, PropertyBags> cachedGroupedRatioTapChangerTablePoints;
    private final Map<String, PropertyBags> cachedGroupedPhaseTapChangers;
    private final Map<String, PropertyBags> cachedGroupedPhaseTapChangerTablePoints;
    private final Map<String, PropertyBags> cachedGroupedShuntCompensatorPoints;
    private final Map<String, PropertyBags> cachedGroupedReactiveCapabilityCurveData;

    private final Map<String, PropertyBag> cgmesTerminals;
    private final Map<String, PropertyBag> cgmesDcTerminals;
    private final Map<String, PropertyBag> ratioTapChangers;
    private final Map<String, PropertyBag> phaseTapChangers;
    private final Map<String, PropertyBag> regulatingControls;
    private final Map<String, PropertyBag> operationalLimits;
    private final Map<String, PropertyBag> generatingUnits;
    private final Map<String, PropertyBag> equivalentInjections;
    private final Map<String, PropertyBag> svVoltages;
    private final Map<String, PropertyBag> switches;
    private final Map<String, Boolean> equipmentInService;
    private final Set<String> terminalsOfOutOfServiceEquipment;

    private static final Set<String> TERMINAL_ALIAS_TYPES = Set.of(Conversion.ALIAS_TERMINAL, Conversion.ALIAS_TERMINAL1,
            Conversion.ALIAS_TERMINAL2, Conversion.ALIAS_TERMINAL3, Conversion.ALIAS_TERMINAL_BOUNDARY);
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);
}
