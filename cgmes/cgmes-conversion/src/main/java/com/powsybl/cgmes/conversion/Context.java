/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Conversion.Config;
import com.powsybl.cgmes.conversion.elements.ACLineSegmentConversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Context {
    public Context(CgmesModel cgmes, Config config, Network network) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.config = Objects.requireNonNull(config);
        this.network = Objects.requireNonNull(network);

        // Even if the CGMES model is node-breaker,
        // we could decide to ignore the connectivity nodes and
        // create buses directly from topological nodes,
        // the configuration says if we are performing the conversion
        // based on existing node-breaker info
        nodeBreaker = cgmes.isNodeBreaker() && config.useNodeBreaker();

        namingStrategy = new NamingStrategy.Identity();
        boundary = new Boundary(cgmes);
        substationIdMapping = new SubstationIdMapping(this);
        terminalMapping = new TerminalMapping();
        tapChangerTransformers = new TapChangerTransformers();
        dcMapping = new DcMapping(this);
        currentLimitsMapping = new CurrentLimitsMapping();
        nodeMapping = new NodeMapping();

        ratioTapChangerTables = new HashMap<>();
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

    public NodeMapping nodeMapping() {
        return nodeMapping;
    }

    public TapChangerTransformers tapChangerTransformers() {
        return tapChangerTransformers;
    }

    public SubstationIdMapping substationIdMapping() {
        return substationIdMapping;
    }

    public Boundary boundary() {
        return boundary;
    }

    public DcMapping dc() {
        return dcMapping;
    }

    public CurrentLimitsMapping currentLimitsMapping() {
        return currentLimitsMapping;
    }

    public static String boundaryVoltageLevelId(String nodeId) {
        Objects.requireNonNull(nodeId);
        return nodeId + "_VL";
    }

    public static String boundarySubstationId(String nodeId) {
        Objects.requireNonNull(nodeId);
        return nodeId + "_S";
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

    public PropertyBags ratioTapChangerTable(String tableId) {
        return ratioTapChangerTables.get(tableId);
    }

    public void startLinesConversion() {
        countLines = 0;
        countLinesWithSvPowerFlowsAtEnds = 0;
    }

    public void anotherLineConversion(ACLineSegmentConversion c) {
        Objects.requireNonNull(c);
        countLines++;
        if (c.terminalPowerFlow(1).defined() && c.terminalPowerFlow(2).defined()) {
            countLinesWithSvPowerFlowsAtEnds++;
        }
    }

    public void endLinesConversion() {
        String enough = countLinesWithSvPowerFlowsAtEnds < countLines ? "FEW" : "ENOUGH";
        LOG.info("{} lines with SvPowerFlow values at ends: {} / {}",
                enough,
                countLinesWithSvPowerFlowsAtEnds,
                countLines);
    }

    public void invalid(String what, String reason) {
        LOG.warn("Invalid {}. Reason: {}", what, reason);
    }

    public void ignored(String what, String reason) {
        LOG.warn("Ignored {}. Reason: {}", what, reason);
    }

    public void pending(String what, String reason) {
        LOG.info("PENDING {}. Reason: {}", what, reason);
    }

    public void fixed(String what, String reason) {
        LOG.warn("Fixed {}. Reason: {}", what, reason);
    }

    public void fixed(String what, String reason, double wrong, double fixed) {
        LOG.warn("Fixed {}. Reason: {}. Wrong {}, fixed {}", what, reason, wrong, fixed);
    }

    public void missing(String what) {
        LOG.warn("Missing {}", what);
    }

    public void missing(String what, double defaultValue) {
        LOG.warn("Missing {}. Used default value {}", what, defaultValue);
    }

    private final CgmesModel cgmes;
    private final Network network;
    private final Config config;
    private final boolean nodeBreaker;
    private final NamingStrategy namingStrategy;
    private final SubstationIdMapping substationIdMapping;
    private final Boundary boundary;
    private final TerminalMapping terminalMapping;
    private final NodeMapping nodeMapping;
    private final TapChangerTransformers tapChangerTransformers;
    private final DcMapping dcMapping;
    private final CurrentLimitsMapping currentLimitsMapping;

    private final Map<String, PropertyBags> ratioTapChangerTables;

    private int countLines;
    private int countLinesWithSvPowerFlowsAtEnds;

    private static final Logger LOG = LoggerFactory.getLogger(Context.class);
}
