package com.powsybl.cgmes.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Conversion.Config;
import com.powsybl.cgmes.conversion.elements.ACLineSegmentConversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;

public class Context {
    public Context(CgmesModel cgmes, Network network, Config config) {
        this.cgmes = cgmes;
        this.network = network;
        this.config = config;

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
        nodeMapping = new NodeMapping();
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

    public void initialize() {
        substationIdMapping.build();
        dcMapping.initialize();
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

    public String substationNameEqContainer(PropertyBag p) {
        String eqcId = p.getId("EquipmentContainer");
        if (eqcId == null) {
            return null;
        }
        Substation substation = network.getSubstation(substationIdMapping().iidm(eqcId));
        if (substation != null) {
            return substation.getName();
        }
        return eqcId;
    }

    public Boundary boundary() {
        return boundary;
    }

    public DcMapping dc() {
        return dcMapping;
    }

    public String boundaryVoltageLevelId(String nodeId) {
        return nodeId + "_VL";
    }

    public String boundarySubstationId(String nodeId) {
        return nodeId + "_S";
    }

    public VoltageLevel createSubstationVoltageLevel(String nodeId, double nominalV) {
        String substationId = boundarySubstationId(nodeId);
        String vlId = boundaryVoltageLevelId(nodeId);
        String substationName = "boundary";
        String vlName = "boundary";
        return network()
                .newSubstation()
                .setId(namingStrategy().getId("Substation", substationId))
                .setName(substationName)
                // A non-null country code must be set
                // This is an arbitrary country code, Bangladesh code BD also matches with
                // BounDary
                .setCountry(Country.BD)
                .add()
                .newVoltageLevel()
                .setId(namingStrategy().getId("VoltageLevel", vlId))
                .setName(vlName)
                .setNominalV(nominalV)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
    }

    public void startLinesConversion() {
        countLines = 0;
        countLinesWithSvPowerFlowsAtEnds = 0;
    }

    public void anotherLineConversion(ACLineSegmentConversion c) {
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
        LOG.info("Ignored {}. Reason: {}", what, reason);
    }

    public void pending(String what, String reason) {
        LOG.debug("PENDING {}. Reason: {}", what, reason);
    }

    public void fixed(String what, String reason) {
        LOG.info("Fixed {}. Reason: {}", what, reason);
    }

    public void fixed(String what, String reason, double wrong, double fixed) {
        LOG.info("Fixed {}. Reason: {}. Wrong {}, fixed {}", what, reason, wrong, fixed);
    }

    public void missing(String what) {
        LOG.warn("Missing {}", what);
    }

    public void missing(String what, double defaultValue) {
        LOG.info("Missing {}. Used default value {}", what, defaultValue);
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

    private int countLines;
    private int countLinesWithSvPowerFlowsAtEnds;

    private static final Logger LOG = LoggerFactory.getLogger(Context.class);
}
