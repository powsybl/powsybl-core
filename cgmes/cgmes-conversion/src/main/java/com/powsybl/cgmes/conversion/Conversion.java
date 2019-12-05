/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.*;
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

import static com.powsybl.cgmes.conversion.Conversion.Config.StateProfile.SSH;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class Conversion {

    public Conversion(CgmesModel cgmes) {
        this(cgmes, new Config());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config) {
        this(cgmes, config, Collections.emptyList());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        this(cgmes, config, postProcessors, NetworkFactory.findDefault());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors,
                      NetworkFactory networkFactory) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.config = Objects.requireNonNull(config);
        this.postProcessors = Objects.requireNonNull(postProcessors);
        this.networkFactory = Objects.requireNonNull(networkFactory);
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
        cgmes.regulatingControls().forEach(p -> context.regulatingControlMapping().cacheRegulatingControls(p));

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

        if (isOldCgmesConversion()) {
            convertTransformers(context);
            convert(cgmes.ratioTapChangers(), rtc -> new RatioTapChangerConversion(rtc, context));
            convert(cgmes.phaseTapChangers(), ptc -> new PhaseTapChangerConversion(ptc, context));
        } else {
            newConvertTransformers(context);
        }

        // DC Converters must be converted first
        convert(cgmes.acDcConverters(), c -> new AcDcConverterConversion(c, context));
        convert(cgmes.dcLineSegments(), l -> new DcLineSegmentConversion(l, context));

        convert(cgmes.operationalLimits(), l -> new OperationalLimitConversion(l, context));
        context.currentLimitsMapping().addAll();

        // set all regulating controls
        context.regulatingControlMapping().setAllRegulatingControls(network);

        if (config.convertSvInjections()) {
            convert(cgmes.svInjections(), si -> new SvInjectionConversion(si, context));
        }

        clearUnattachedHvdcConverterStations(network, context); // in case of faulty CGMES files, remove HVDC Converter Stations without HVDC lines
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

    private boolean isOldCgmesConversion() {
        return true;
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
        Network network = networkFactory.createNetwork(networkId, sourceFormat);
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
        context.network().setProperty(NETWORK_PS_CGMES_MODEL_DETAIL,
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

    private void newConvertTransformers(Context context) {
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
                    c = new NewTwoWindingsTransformerConversion(ends, powerTransformerRatioTapChanger, powerTransformerPhaseTapChanger, context);
                } else if (ends.size() == 3) {
                    c = new NewThreeWindingsTransformerConversion(ends, powerTransformerRatioTapChanger, powerTransformerPhaseTapChanger, context);
                } else {
                    String what = String.format("PowerTransformer %s", t);
                    String reason = String.format("Has %d ends. Only 2 or 3 ends are supported", ends.size());
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
                        c = new TwoWindingsTransformerConversion(ends, powerTransformerRatioTapChanger, powerTransformerPhaseTapChanger, context);
                    } else if (ends.size() == 3) {
                        c = new ThreeWindingsTransformerConversion(ends, powerTransformerRatioTapChanger, context);
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

    private void clearUnattachedHvdcConverterStations(Network network, Context context) {
        network.getHvdcConverterStationStream().filter(converter -> converter.getHvdcLine() == null).forEach(converter -> {
            context.ignored(String.format("HVDC Converter Station %s", converter.getId()), "No correct linked HVDC line found.");
            converter.remove();
        });
    }

    private void debugTopology(Context context) {
        profiling.start();
        context.network().getVoltageLevels().forEach(vl -> {
            String name = vl.getSubstation().getName() + "-" + vl.getName();
            name = name.replace('/', '-');
            Path file = Paths.get(System.getProperty("java.io.tmpdir"), "temp-cgmes-" + name + ".dot");
            try {
                vl.exportTopology(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        profiling.end("debugTopology");
    }

    public static class Config {

        public enum StateProfile {
            SSH,
            SV
        }

        public List<String> substationIdsExcludedFromMapping() {
            return Collections.emptyList();
        }

        public boolean debugTopology() {
            return false;
        }

        public boolean allowUnsupportedTapChangers() {
            return allowUnsupportedTapChangers;
        }

        public Config setAllowUnsupportedTapChangers(boolean allowUnsupportedTapChangers) {
            this.allowUnsupportedTapChangers = allowUnsupportedTapChangers;
            return this;
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

        public Config setConvertBoundary(boolean convertBoundary) {
            this.convertBoundary = convertBoundary;
            return this;
        }

        public boolean mergeLinesUsingQuadripole() {
            return true;
        }

        public boolean changeSignForShuntReactivePowerFlowInitialState() {
            return changeSignForShuntReactivePowerFlowInitialState;
        }

        public Config setChangeSignForShuntReactivePowerFlowInitialState(boolean b) {
            changeSignForShuntReactivePowerFlowInitialState = b;
            return this;
        }

        public boolean computeFlowsAtBoundaryDanglingLines() {
            return true;
        }

        public boolean createBusbarSectionForEveryConnectivityNode() {
            return createBusbarSectionForEveryConnectivityNode;
        }

        public Config setCreateBusbarSectionForEveryConnectivityNode(boolean b) {
            createBusbarSectionForEveryConnectivityNode = b;
            return this;
        }

        public boolean convertSvInjections() {
            return convertSvInjections;
        }

        public Config setConvertSvInjections(boolean convertSvInjections) {
            this.convertSvInjections = convertSvInjections;
            return this;
        }

        public StateProfile getProfileUsedForInitialStateValues() {
            return profileUsedForInitialStateValues;
        }

        public Config setProfileUsedForInitialStateValues(String profileUsedForInitialFlowsValues) {
            switch (Objects.requireNonNull(profileUsedForInitialFlowsValues)) {
                case "SSH":
                case "SV":
                    this.profileUsedForInitialStateValues = StateProfile.valueOf(profileUsedForInitialFlowsValues);
                    break;
                default:
                    throw new CgmesModelException("Unexpected profile used for state hypothesis: " + profileUsedForInitialFlowsValues);
            }
            return this;
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

        public boolean isXfmr2RatioPhaseX() {
            return xfmr2RatioPhaseX;
        }

        public void setXfmr2RatioPhaseX(boolean xfmr2RatioPhaseX) {
            this.xfmr2RatioPhaseX = xfmr2RatioPhaseX;
        }

        public boolean isXfmr2PhaseNegate() {
            return xfmr2PhaseNegate;
        }

        public void setXfmr2PhaseNegate(boolean xfmr2PhaseNegate) {
            this.xfmr2PhaseNegate = xfmr2PhaseNegate;
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

        public boolean isXfmr2PhaseAngleClockOn() {
            return xfmr2PhaseAngleClockOn;
        }

        public void setXfmr2PhaseAngleClockOn(boolean xfmr2PhaseAngleClockOn) {
            this.xfmr2PhaseAngleClockOn = xfmr2PhaseAngleClockOn;
        }

        public Config setXfmr2StructuralRatio(String value) {
            if (value.equals("end1")) {
                setXfmr2Ratio0End1(true);
                setXfmr2Ratio0End2(false);
                setXfmr2Ratio0X(false);
            } else if (value.equals("end2")) {
                setXfmr2Ratio0End1(false);
                setXfmr2Ratio0End2(true);
                setXfmr2Ratio0X(false);
            } else if (value.equals("x")) {
                setXfmr2Ratio0End1(false);
                setXfmr2Ratio0End2(false);
                setXfmr2Ratio0X(true);
            }
            return this;
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

        public boolean isXfmr3PhaseNegate() {
            return xfmr3PhaseNegate;
        }

        public void setXfmr3PhaseNegate(boolean xfmr3PhaseNegate) {
            this.xfmr3PhaseNegate = xfmr3PhaseNegate;
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

        public boolean isXfmr3PhaseAngleClockOn() {
            return xfmr3PhaseAngleClockOn;
        }

        public void setXfmr3PhaseAngleClockOn(boolean xfmr3PhaseAngleClockOn) {
            this.xfmr3PhaseAngleClockOn = xfmr3PhaseAngleClockOn;
        }

        public boolean isXfmr3Ratio0StarBusSide() {
            return xfmr3Ratio0StarBusSide;
        }

        public void setXfmr3Ratio0StarBusSide(boolean xfmr3Ratio0StarBusSide) {
            this.xfmr3Ratio0StarBusSide = xfmr3Ratio0StarBusSide;
        }

        public boolean isXfmr3Ratio0NetworkSide() {
            return xfmr3Ratio0NetworkSide;
        }

        public void setXfmr3Ratio0NetworkSide(boolean xfmr3Ratio0NetworkSide) {
            this.xfmr3Ratio0NetworkSide = xfmr3Ratio0NetworkSide;
        }

        public boolean isXfmr3Ratio0End1() {
            return xfmr3Ratio0End1;
        }

        public void setXfmr3Ratio0End1(boolean xfmr3Ratio0End1) {
            this.xfmr3Ratio0End1 = xfmr3Ratio0End1;
        }

        public boolean isXfmr3Ratio0End2() {
            return xfmr3Ratio0End2;
        }

        public void setXfmr3Ratio0End2(boolean xfmr3Ratio0End2) {
            this.xfmr3Ratio0End2 = xfmr3Ratio0End2;
        }

        public boolean isXfmr3Ratio0End3() {
            return xfmr3Ratio0End3;
        }

        public void setXfmr3Ratio0End3(boolean xfmr3Ratio0End3) {
            this.xfmr3Ratio0End3 = xfmr3Ratio0End3;
        }

        private boolean allowUnsupportedTapChangers = true;
        private boolean convertBoundary = false;
        private boolean changeSignForShuntReactivePowerFlowInitialState = false;
        private double lowImpedanceLineR = 7.0E-5;
        private double lowImpedanceLineX = 7.0E-5;

        private boolean createBusbarSectionForEveryConnectivityNode = false;
        private boolean convertSvInjections = true;
        private StateProfile profileUsedForInitialStateValues = SSH;

        // Default configuration. See CgmesImport.java config()
        private boolean xfmr2RatioPhaseEnd1 = false;
        private boolean xfmr2RatioPhaseEnd2 = false;
        private boolean xfmr2RatioPhaseEnd1End2 = true;
        private boolean xfmr2RatioPhaseX = false;
        private boolean xfmr2PhaseNegate = false;
        private boolean xfmr2ShuntEnd1 = true;
        private boolean xfmr2ShuntEnd2 = false;
        private boolean xfmr2ShuntEnd1End2 = false;
        private boolean xfmr2ShuntSplit = false;
        private boolean xfmr2PhaseAngleClockOn = false;
        private boolean xfmr2Ratio0End1 = false;
        private boolean xfmr2Ratio0End2 = false;
        private boolean xfmr2Ratio0X = true;

        private boolean xfmr3RatioPhaseNetworkSide = true;
        private boolean xfmr3PhaseNegate = false;
        private boolean xfmr3ShuntNetworkSide = true;
        private boolean xfmr3ShuntStarBusSide = false;
        private boolean xfmr3ShuntSplit = false;
        private boolean xfmr3PhaseAngleClockOn = false;
        private boolean xfmr3Ratio0StarBusSide = true;
        private boolean xfmr3Ratio0NetworkSide = false;
        private boolean xfmr3Ratio0End1 = false;
        private boolean xfmr3Ratio0End2 = false;
        private boolean xfmr3Ratio0End3 = false;
    }

    private final CgmesModel cgmes;
    private final Config config;
    private final List<CgmesImportPostProcessor> postProcessors;
    private final NetworkFactory networkFactory;

    private Profiling profiling;

    private static final Logger LOG = LoggerFactory.getLogger(Conversion.class);

    public static final String NETWORK_PS_CGMES_MODEL_DETAIL = "CGMESModelDetail";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH = "bus-branch";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER = "node-breaker";
}
