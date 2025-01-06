/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.Conversion.Config;
import com.powsybl.cgmes.conversion.elements.hvdc.DcMapping;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
        dcMapping = new DcMapping(this);
        loadingLimitsMapping = new LoadingLimitsMapping(this);
        regulatingControlMapping = new RegulatingControlMapping(this);
        nodeMapping = new NodeMapping(this);

        cachedGroupedTransformerEnds = new HashMap<>();
        cachedGroupedRatioTapChangers = new HashMap<>();
        cachedGroupedPhaseTapChangers = new HashMap<>();
        ratioTapChangerTables = new HashMap<>();
        phaseTapChangerTables = new HashMap<>();
        reactiveCapabilityCurveData = new HashMap<>();

        buildCaches();
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

    private boolean setPQAllowed(Terminal t) {
        return t.getConnectable().getType() != IdentifiableType.BUSBAR_SECTION;
    }

    public NodeMapping nodeMapping() {
        return nodeMapping;
    }

    public NodeContainerMapping nodeContainerMapping() {
        return nodeContainerMapping;
    }

    public CgmesBoundary boundary() {
        return cgmesBoundary;
    }

    public DcMapping dc() {
        return dcMapping;
    }

    public LoadingLimitsMapping loadingLimitsMapping() {
        return loadingLimitsMapping;
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
        buildCache(cachedGroupedPhaseTapChangers, cgmes().phaseTapChangers(), CgmesNames.POWER_TRANSFORMER);
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

    public PropertyBags phaseTapChangers(String transformerId) {
        return cachedGroupedPhaseTapChangers.getOrDefault(transformerId, new PropertyBags());
    }

    public void loadReactiveCapabilityCurveData() {
        PropertyBags rccdata = cgmes.reactiveCapabilityCurveData();
        if (rccdata == null) {
            return;
        }
        rccdata.forEach(p -> {
            String curveId = p.getId("ReactiveCapabilityCurve");
            reactiveCapabilityCurveData.computeIfAbsent(curveId, cid -> new PropertyBags()).add(p);
        });
    }

    public PropertyBags reactiveCapabilityCurveData(String curveId) {
        return reactiveCapabilityCurveData.get(curveId);
    }

    public void loadRatioTapChangerTables() {
        PropertyBags rtcpoints = cgmes.ratioTapChangerTablesPoints();
        if (rtcpoints == null) {
            return;
        }
        rtcpoints.forEach(p -> {
            String tableId = p.getId("RatioTapChangerTable");
            ratioTapChangerTables.computeIfAbsent(tableId, tid -> new PropertyBags()).add(p);
        });
    }

    public void loadPhaseTapChangerTables() {
        PropertyBags ptcpoints = cgmes.phaseTapChangerTablesPoints();
        if (ptcpoints == null) {
            return;
        }
        ptcpoints.forEach(p -> {
            String tableId = p.getId("PhaseTapChangerTable");
            phaseTapChangerTables.computeIfAbsent(tableId, tid -> new PropertyBags()).add(p);
        });
    }

    public PropertyBags ratioTapChangerTable(String tableId) {
        return ratioTapChangerTables.get(tableId);
    }

    public PropertyBags phaseTapChangerTable(String tableId) {
        return phaseTapChangerTables.get(tableId);
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
            LOG.warn("{}: {}. Reason: {}", category, what, reason.get());
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
    private final DcMapping dcMapping;
    private final LoadingLimitsMapping loadingLimitsMapping;
    private final RegulatingControlMapping regulatingControlMapping;

    private final Map<String, PropertyBags> cachedGroupedTransformerEnds;
    private final Map<String, PropertyBags> cachedGroupedRatioTapChangers;
    private final Map<String, PropertyBags> cachedGroupedPhaseTapChangers;
    private final Map<String, PropertyBags> ratioTapChangerTables;
    private final Map<String, PropertyBags> phaseTapChangerTables;
    private final Map<String, PropertyBags> reactiveCapabilityCurveData;

    private static final Logger LOG = LoggerFactory.getLogger(Context.class);
}
