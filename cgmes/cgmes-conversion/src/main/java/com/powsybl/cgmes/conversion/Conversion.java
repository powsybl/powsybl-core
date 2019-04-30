/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.*;
import com.powsybl.cgmes.conversion.elements.full.ThreeWindingsTransformerFullConversion;
import com.powsybl.cgmes.conversion.elements.full.TwoWindingsTransformerFullConversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Conversion {

    private static final boolean EXTENDED_CGMES_CONVERSION = true;

    public Conversion(CgmesModel cgmes) {
        this(cgmes, new Config());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config) {
        this(cgmes, config, Collections.emptyList());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.config = Objects.requireNonNull(config);
        this.postProcessors = Objects.requireNonNull(postProcessors);
    }

    public void report(Consumer<String> out) {
        new ReportTapChangers(cgmes, out).report();
    }

    public Network convert() {
        profiling = new Profiling();

        if (LOG.isDebugEnabled() && cgmes.baseVoltages() != null) {
            LOG.debug(cgmes.baseVoltages().tabulate());
        }
        // Check that at least we have an EquipmentCore profile
        if (!cgmes.hasEquipmentCore()) {
            throw new CgmesModelException("Data source does not contain EquipmentCore data");
        }
        Network network = createNetwork();
        Context context = createContext(network);
        assignNetworkProperties(context);

        Function<PropertyBag, AbstractObjectConversion> convf;

        cgmes.terminals().forEach(p -> context.terminalMapping().buildTopologicalNodesMapping(p));

        convert(cgmes.substations(), s -> new SubstationConversion(s, context));
        convert(cgmes.voltageLevels(), vl -> new VoltageLevelConversion(vl, context));
        PropertyBags nodes = context.nodeBreaker()
                ? cgmes.connectivityNodes()
                : cgmes.topologicalNodes();
        String nodeTypeName = context.nodeBreaker()
                ? "ConnectivityNode"
                : "TopologicalNode";
        convert(nodes, n -> new NodeConversion(nodeTypeName, n, context));
        if (!context.config().createBusbarSectionForEveryConnectivityNode()) {
            convert(cgmes.busBarSections(), bbs -> new BusbarSectionConversion(bbs, context));
        }

        convert(cgmes.energyConsumers(), ec -> new EnergyConsumerConversion(ec, context));
        convert(cgmes.energySources(), es -> new EnergySourceConversion(es, context));
        convf = eqi -> new EquivalentInjectionConversion(eqi, context);
        convert(cgmes.equivalentInjections(), convf);
        convf = eni -> new ExternalNetworkInjectionConversion(eni, context);
        convert(cgmes.externalNetworkInjections(), convf);
        convert(cgmes.shuntCompensators(), sh -> new ShuntConversion(sh, context));
        convf = svc -> new StaticVarCompensatorConversion(svc, context);
        convert(cgmes.staticVarCompensators(), convf);
        convf = asm -> new AsynchronousMachineConversion(asm, context);
        convert(cgmes.asynchronousMachines(), convf);
        convert(cgmes.synchronousMachines(), sm -> new SynchronousMachineConversion(sm, context));

        convert(cgmes.switches(), sw -> new SwitchConversion(sw, context));
        convertACLineSegmentsToLines(context);
        convert(cgmes.equivalentBranches(), eqb -> new EquivalentBranchConversion(eqb, context));
        convert(cgmes.seriesCompensators(), sc -> new SeriesCompensatorConversion(sc, context));

        if (EXTENDED_CGMES_CONVERSION) {
            convertFullTransformers(context);
        } else {
            convertTransformers(context);
            convert(cgmes.ratioTapChangers(), rtc -> new RatioTapChangerConversion(rtc, context));
            convert(cgmes.phaseTapChangers(), ptc -> new PhaseTapChangerConversion(ptc, context));
        }

        // DC Converters must be converted first
        convert(cgmes.acDcConverters(), c -> new AcDcConverterConversion(c, context));
        convert(cgmes.dcLineSegments(), l -> new DcLineSegmentConversion(l, context));

        convert(cgmes.operationalLimits(), l -> new OperationalLimitConversion(l, context));
        context.currentLimitsMapping().addAll();

        // set all remote regulating terminals
        context.setAllRemoteRegulatingTerminals();

        voltageAngles(nodes, context);
        if (context.config().debugTopology()) {
            debugTopology(context);
        }

        // apply post-processors
        for (CgmesImportPostProcessor postProcessor : postProcessors) {
            postProcessor.process(network, cgmes.tripleStore(), profiling);
        }

        profiling.report();
        return network;
    }

    private void convert(
            PropertyBags elements,
            Function<PropertyBag, AbstractObjectConversion> f) {
        String conversion = null;
        profiling.start();
        for (PropertyBag element : elements) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(element.tabulateLocals());
            }
            AbstractObjectConversion c = f.apply(element);
            if (conversion == null) {
                conversion = c.getClass().getName();
                conversion = conversion.substring(conversion.lastIndexOf('.') + 1);
                conversion = conversion.replace("Conversion", "");
            }
            if (c.insideBoundary()) {
                c.convertInsideBoundary();
            } else if (c.valid()) {
                c.convert();
            }
        }
        if (conversion != null) {
            profiling.end(conversion);
        }
    }

    private Network createNetwork() {
        profiling.start();
        String networkId = cgmes.modelId();
        String sourceFormat = "CGMES";
        Network network = NetworkFactory.create(networkId, sourceFormat);
        profiling.end("createNetwork");
        return network;
    }

    private Context createContext(Network network) {
        profiling.start();
        Context context = new Context(cgmes, config, network);
        context.substationIdMapping().build();
        context.dc().initialize();
        context.loadRatioTapChangerTables();
        context.loadPhaseTapChangerTables();
        context.loadReactiveCapabilityCurveData();
        profiling.end("createContext");
        return context;
    }

    private void assignNetworkProperties(Context context) {
        profiling.start();
        context.network().getProperties().put(NETWORK_PS_CGMES_MODEL_DETAIL,
                context.nodeBreaker()
                        ? NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER
                        : NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH);
        DateTime modelScenarioTime = cgmes.scenarioTime();
        DateTime modelCreated = cgmes.created();
        long forecastDistance = new Duration(modelCreated, modelScenarioTime).getStandardMinutes();
        context.network().setForecastDistance(forecastDistance >= 0 ? (int) forecastDistance : 0);
        context.network().setCaseDate(modelScenarioTime);
        LOG.info("cgmes scenarioTime       : {}", modelScenarioTime);
        LOG.info("cgmes modelCreated       : {}", modelCreated);
        LOG.info("network caseDate         : {}", context.network().getCaseDate());
        LOG.info("network forecastDistance : {}", context.network().getForecastDistance());
        profiling.end("networkProperties");
    }

    private void convertACLineSegmentsToLines(Context context) {
        profiling.start();
        PropertyBags lines = cgmes.acLineSegments();

        // Context stores some statistics about line conversion
        context.startLinesConversion();

        // We will delay the conversion of some lines that have an end point on boundary
        // They have to be processed after all lines have been reviewed
        // (in fact we should review after all potential elements that could be present
        // in the model boundary have been put there, not only lines)
        Set<String> delayedBoundaryNodes = new HashSet<>();

        Iterator<PropertyBag> k = lines.stream().iterator();
        while (k.hasNext()) {
            PropertyBag line = k.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(line.tabulateLocals("ACLineSegment"));
            }
            ACLineSegmentConversion c = new ACLineSegmentConversion(line, context);
            context.anotherLineConversion(c);
            if (c.valid()) {
                String node = c.boundaryNode();
                if (node != null) {
                    context.boundary().addLineAtNode(line, node);
                    delayedBoundaryNodes.add(node);
                } else {
                    c.convert();
                }
            }
        }
        delayedBoundaryNodes.forEach(node -> {
            // At least each delayed boundary node should have one line
            PropertyBag line = context.boundary().linesAtNode(node).get(0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(line.tabulateLocals("Line"));
            }
            ACLineSegmentConversion c = new ACLineSegmentConversion(line, context);
            c.convert();
        });
        context.endLinesConversion();
        profiling.end("ACLineSegments");
    }

    private void convertFullTransformers(Context context) {
        profiling.start();
        Map<String, PropertyBag> powerTransformerRatioTapChanger = new HashMap<>();
        Map<String, PropertyBag> powerTransformerPhaseTapChanger = new HashMap<>();
        cgmes.ratioTapChangers().forEach(ratio -> {
            String id = ratio.getId("RatioTapChanger");
            powerTransformerRatioTapChanger.put(id, ratio);
        });
        cgmes.phaseTapChangers().forEach(phase -> {
            String id = phase.getId("PhaseTapChanger");
            powerTransformerPhaseTapChanger.put(id, phase);
        });
        cgmes.groupedTransformerEnds().entrySet()
                .forEach(tends -> {
                    String t = tends.getKey();
                    PropertyBags ends = tends.getValue();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Transformer {}, {}-winding", t, ends.size());
                        ends.forEach(e -> LOG.debug(e.tabulateLocals("TransformerEnd")));
                    }
                    AbstractConductingEquipmentConversion c = null;
                    if (ends.size() == 2) {
                        c = new TwoWindingsTransformerFullConversion(ends, powerTransformerRatioTapChanger, powerTransformerPhaseTapChanger,
                                context);
                    } else if (ends.size() == 3) {
                        c = new ThreeWindingsTransformerFullConversion(ends, powerTransformerRatioTapChanger,
                                powerTransformerPhaseTapChanger, context);
                    } else {
                        String what = String.format("PowerTransformer %s", t);
                        String reason = String.format("Has %d ends. Only 2 or 3 ends are supported",
                                ends.size());
                        context.invalid(what, reason);
                    }
                    if (c != null && c.valid()) {
                        c.convert();
                    }
                });
        profiling.end("Transfomers");
    }

    private void convertTransformers(Context context) {
        profiling.start();
        cgmes.groupedTransformerEnds().entrySet()
                .forEach(tends -> {
                    String t = tends.getKey();
                    PropertyBags ends = tends.getValue();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Transformer {}, {}-winding", t, ends.size());
                        ends.forEach(e -> LOG.debug(e.tabulateLocals("TransformerEnd")));
                    }
                    AbstractConductingEquipmentConversion c = null;
                    if (ends.size() == 2) {
                        c = new TwoWindingsTransformerConversion(ends, context);
                    } else if (ends.size() == 3) {
                        c = new ThreeWindingsTransformerConversion(ends, context);
                    } else {
                        String what = String.format("PowerTransformer %s", t);
                        String reason = String.format("Has %d ends. Only 2 or 3 ends are supported",
                                ends.size());
                        context.invalid(what, reason);
                    }
                    if (c != null && c.valid()) {
                        c.convert();
                    }
                });
        profiling.end("Transfomers");
    }

    private void voltageAngles(PropertyBags nodes, Context context) {
        profiling.start();
        if (context.nodeBreaker()) {
            // TODO(Luma): we create again one conversion object for every node
            // In node-breaker conversion,
            // set (voltage, angle) values after all nodes have been created and connected
            for (PropertyBag n : nodes) {
                NodeConversion nc = new NodeConversion("ConnectivityNode", n, context);
                if (!nc.insideBoundary()) {
                    nc.setVoltageAngleNodeBreaker();
                }
            }
        }
        profiling.end("voltageAngles");
    }

    private void debugTopology(Context context) {
        profiling.start();
        context.network().getVoltageLevels().forEach(vl -> {
            String name = vl.getSubstation().getName() + "-" + vl.getName();
            name = name.replace('/', '-');
            Path file = Paths.get(System.getProperty("java.io.tmpdir"),
                    "temp-cgmes-" + name + ".dot");
            try {
                vl.exportTopology(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        profiling.end("debugTopology");
    }

    public static class Config {
        public List<String> substationIdsExcludedFromMapping() {
            return Collections.emptyList();
        }

        public boolean debugTopology() {
            return false;
        }

        public boolean allowUnsupportedTapChangers() {
            return true;
        }

        public boolean useNodeBreaker() {
            return true;
        }

        public double lowImpedanceLineR() {
            return lowImpedanceLineR;
        }

        public double lowImpedanceLineX() {
            return lowImpedanceLineX;
        }

        public boolean convertBoundary() {
            return convertBoundary;
        }

        public void setConvertBoundary(boolean convertBoundary) {
            this.convertBoundary = convertBoundary;
        }

        public boolean mergeLinesUsingQuadripole() {
            return true;
        }

        public boolean changeSignForShuntReactivePowerFlowInitialState() {
            return changeSignForShuntReactivePowerFlowInitialState;
        }

        public void setChangeSignForShuntReactivePowerFlowInitialState(boolean b) {
            changeSignForShuntReactivePowerFlowInitialState = b;
        }

        public boolean computeFlowsAtBoundaryDanglingLines() {
            return true;
        }

        public boolean createBusbarSectionForEveryConnectivityNode() {
            return createBusbarSectionForEveryConnectivityNode;
        }

        public void setCreateBusbarSectionForEveryConnectivityNode(boolean b) {
            createBusbarSectionForEveryConnectivityNode = b;
        }

        public boolean isXfmr2RatioPhaseEnd1() {
            return xfmr2RatioPhaseEnd1;
        }

        public void setXfmr2RatioPhaseEnd1(boolean xfmr2RatioPhaseEnd1) {
            this.xfmr2RatioPhaseEnd1 = xfmr2RatioPhaseEnd1;
        }

        public boolean isXfmr2RatioPhaseEnd2() {
            return xfmr2RatioPhaseEnd2;
        }

        public void setXfmr2RatioPhaseEnd2(boolean xfmr2RatioPhaseEnd2) {
            this.xfmr2RatioPhaseEnd2 = xfmr2RatioPhaseEnd2;
        }

        public boolean isXfmr2RatioPhaseEnd1End2() {
            return xfmr2RatioPhaseEnd1End2;
        }

        public void setXfmr2RatioPhaseEnd1End2(boolean xfmr2RatioPhaseEnd1End2) {
            this.xfmr2RatioPhaseEnd1End2 = xfmr2RatioPhaseEnd1End2;
        }

        public boolean isXfmr2RatioPhaseRtc() {
            return xfmr2RatioPhaseRtc;
        }

        public void setXfmr2RatioPhaseRtc(boolean xfmr2RatioPhaseRtc) {
            this.xfmr2RatioPhaseRtc = xfmr2RatioPhaseRtc;
        }

        public boolean isXfmr2Phase1Negate() {
            return xfmr2Phase1Negate;
        }

        public void setXfmr2Phase1Negate(boolean xfmr2Phase1Negate) {
            this.xfmr2Phase1Negate = xfmr2Phase1Negate;
        }

        public boolean isXfmr2Phase2Negate() {
            return xfmr2Phase2Negate;
        }

        public void setXfmr2Phase2Negate(boolean xfmr2Phase2Negate) {
            this.xfmr2Phase2Negate = xfmr2Phase2Negate;
        }

        public boolean isXfmr2ShuntEnd1() {
            return xfmr2ShuntEnd1;
        }

        public void setXfmr2ShuntEnd1(boolean xfmr2ShuntEnd1) {
            this.xfmr2ShuntEnd1 = xfmr2ShuntEnd1;
        }

        public boolean isXfmr2ShuntEnd2() {
            return xfmr2ShuntEnd2;
        }

        public void setXfmr2ShuntEnd2(boolean xfmr2ShuntEnd2) {
            this.xfmr2ShuntEnd2 = xfmr2ShuntEnd2;
        }

        public boolean isXfmr2ShuntEnd1End2() {
            return xfmr2ShuntEnd1End2;
        }

        public void setXfmr2ShuntEnd1End2(boolean xfmr2ShuntEnd1End2) {
            this.xfmr2ShuntEnd1End2 = xfmr2ShuntEnd1End2;
        }

        public boolean isXfmr2ShuntSplit() {
            return xfmr2ShuntSplit;
        }

        public void setXfmr2ShuntSplit(boolean xfmr2ShuntSplit) {
            this.xfmr2ShuntSplit = xfmr2ShuntSplit;
        }

        public boolean isXfmr2PhaseAngleClockEnd1End2() {
            return xfmr2PhaseAngleClockEnd1End2;
        }

        public void setXfmr2PhaseAngleClockEnd1End2(boolean xfmr2PhaseAngleClockEnd1End2) {
            this.xfmr2PhaseAngleClockEnd1End2 = xfmr2PhaseAngleClockEnd1End2;
        }

        public boolean isXfmr2PhaseAngleClock1Negate() {
            return xfmr2PhaseAngleClock1Negate;
        }

        public void setXfmr2PhaseAngleClock1Negate(boolean xfmr2PhaseAngleClock1Negate) {
            this.xfmr2PhaseAngleClock1Negate = xfmr2PhaseAngleClock1Negate;
        }

        public boolean isXfmr2PhaseAngleClock2Negate() {
            return xfmr2PhaseAngleClock2Negate;
        }

        public void setXfmr2PhaseAngleClock2Negate(boolean xfmr2PhaseAngleClock2Negate) {
            this.xfmr2PhaseAngleClock2Negate = xfmr2PhaseAngleClock2Negate;
        }

        public boolean isXfmr2Ratio0End1() {
            return xfmr2Ratio0End1;
        }

        public void setXfmr2Ratio0End1(boolean xfmr2Ratio0End1) {
            this.xfmr2Ratio0End1 = xfmr2Ratio0End1;
        }

        public boolean isXfmr2Ratio0End2() {
            return xfmr2Ratio0End2;
        }

        public void setXfmr2Ratio0End2(boolean xfmr2Ratio0End2) {
            this.xfmr2Ratio0End2 = xfmr2Ratio0End2;
        }

        public boolean isXfmr2Ratio0Rtc() {
            return xfmr2Ratio0Rtc;
        }

        public void setXfmr2Ratio0Rtc(boolean xfmr2Ratio0Rtc) {
            this.xfmr2Ratio0Rtc = xfmr2Ratio0Rtc;
        }

        public boolean isXfmr2Ratio0X() {
            return xfmr2Ratio0X;
        }

        public void setXfmr2Ratio0X(boolean xfmr2Ratio0X) {
            this.xfmr2Ratio0X = xfmr2Ratio0X;
        }

        public boolean isXfmr3RatioPhaseNetworkSide() {
            return xfmr3RatioPhaseNetworkSide;
        }

        public void setXfmr3RatioPhaseNetworkSide(boolean xfmr3RatioPhaseNetworkSide) {
            this.xfmr3RatioPhaseNetworkSide = xfmr3RatioPhaseNetworkSide;
        }

        public boolean isXfmr3ShuntNetworkSide() {
            return xfmr3ShuntNetworkSide;
        }

        public void setXfmr3ShuntNetworkSide(boolean xfmr3ShuntNetworkSide) {
            this.xfmr3ShuntNetworkSide = xfmr3ShuntNetworkSide;
        }

        public boolean isXfmr3ShuntStarBusSide() {
            return xfmr3ShuntStarBusSide;
        }

        public void setXfmr3ShuntStarBusSide(boolean xfmr3ShuntStarBusSide) {
            this.xfmr3ShuntStarBusSide = xfmr3ShuntStarBusSide;
        }

        public boolean isXfmr3ShuntSplit() {
            return xfmr3ShuntSplit;
        }

        public void setXfmr3ShuntSplit(boolean xfmr3ShuntSplit) {
            this.xfmr3ShuntSplit = xfmr3ShuntSplit;
        }

        public boolean isXfmr3PhaseAngleClockNetworkSide() {
            return xfmr3PhaseAngleClockNetworkSide;
        }

        public void setXfmr3PhaseAngleClockNetworkSide(boolean xfmr3PhaseAngleClockNetworkSide) {
            this.xfmr3PhaseAngleClockNetworkSide = xfmr3PhaseAngleClockNetworkSide;
        }

        public boolean isXfmr3PhaseAngleClockStarBusSide() {
            return xfmr3PhaseAngleClockStarBusSide;
        }

        public void setXfmr3PhaseAngleClockStarBusSide(boolean xfmr3PhaseAngleClockStarBusSide) {
            this.xfmr3PhaseAngleClockStarBusSide = xfmr3PhaseAngleClockStarBusSide;
        }

        public boolean isXfmr3Ratio0NetworkSide() {
            return xfmr3Ratio0NetworkSide;
        }

        public void setXfmr3Ratio0NetworkSide(boolean xfmr3Ratio0NetworkSide) {
            this.xfmr3Ratio0NetworkSide = xfmr3Ratio0NetworkSide;
        }

        private boolean convertBoundary                                 = false;
        private boolean changeSignForShuntReactivePowerFlowInitialState = false;
        private double  lowImpedanceLineR                               = 0.05;
        private double  lowImpedanceLineX                               = 0.05;

        private boolean createBusbarSectionForEveryConnectivityNode     = false;

        private boolean xfmr2RatioPhaseEnd1                             = false;
        private boolean xfmr2RatioPhaseEnd2                             = false;
        private boolean xfmr2RatioPhaseEnd1End2                         = false;
        private boolean xfmr2RatioPhaseRtc                              = false;
        private boolean xfmr2Phase1Negate                               = false;
        private boolean xfmr2Phase2Negate                               = false;
        private boolean xfmr2ShuntEnd1                                  = false;
        private boolean xfmr2ShuntEnd2                                  = false;
        private boolean xfmr2ShuntEnd1End2                              = false;
        private boolean xfmr2ShuntSplit                                 = false;
        private boolean xfmr2PhaseAngleClockEnd1End2                    = false;
        private boolean xfmr2PhaseAngleClock1Negate                     = false;
        private boolean xfmr2PhaseAngleClock2Negate                     = false;
        private boolean xfmr2Ratio0End1                                 = false;
        private boolean xfmr2Ratio0End2                                 = false;
        private boolean xfmr2Ratio0Rtc                                  = false;
        private boolean xfmr2Ratio0X                                    = false;

        private boolean xfmr3RatioPhaseNetworkSide                      = false;
        private boolean xfmr3ShuntNetworkSide                           = false;
        private boolean xfmr3ShuntStarBusSide                           = false;
        private boolean xfmr3ShuntSplit                                 = false;
        private boolean xfmr3PhaseAngleClockNetworkSide                 = false;
        private boolean xfmr3PhaseAngleClockStarBusSide                 = false;
        private boolean xfmr3Ratio0NetworkSide                          = false;
    }

    private final CgmesModel                     cgmes;
    private final Config                         config;
    private final List<CgmesImportPostProcessor> postProcessors;

    private Profiling                            profiling;

    private static final Logger                  LOG                                        = LoggerFactory
            .getLogger(Conversion.class);

    public static final String                   NETWORK_PS_CGMES_MODEL_DETAIL              = "CGMESModelDetail";
    public static final String                   NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH   = "bus-branch";
    public static final String                   NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER = "node-breaker";
}
