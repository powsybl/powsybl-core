/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.ucte.converter.util.UcteConverterConstants;
import com.powsybl.ucte.converter.util.UcteConverterHelper;
import com.powsybl.ucte.converter.util.UcteExporterReports;
import com.powsybl.ucte.network.*;
import com.powsybl.ucte.network.io.UcteWriter;
import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.DoublePredicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.ucte.converter.util.UcteConverterConstants.*;
import static com.powsybl.ucte.converter.util.UcteConverterHelper.*;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(Exporter.class)
public class UcteExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExporter.class);

    public static final String NAMING_STRATEGY = "ucte.export.naming-strategy";

    public static final String COMBINE_PHASE_ANGLE_REGULATION = "ucte.export.combine-phase-angle-regulation";

    private static final Parameter NAMING_STRATEGY_PARAMETER
            = new Parameter(NAMING_STRATEGY, ParameterType.STRING, "Default naming strategy for UCTE codes conversion", "Default");

    private static final Parameter COMBINE_PHASE_ANGLE_REGULATION_PARAMETER
            = new Parameter(COMBINE_PHASE_ANGLE_REGULATION, ParameterType.BOOLEAN, "Combine phase and angle regulation", false);

    private static final List<Parameter> STATIC_PARAMETERS = List.of(NAMING_STRATEGY_PARAMETER, COMBINE_PHASE_ANGLE_REGULATION_PARAMETER);

    private final ParameterDefaultValueConfig defaultValueConfig;

    public UcteExporter() {
        this(PlatformConfig.defaultConfig());
    }

    public UcteExporter(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    @Override
    public String getFormat() {
        return "UCTE";
    }

    @Override
    public String getComment() {
        return "IIDM to UCTE converter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, ReportNode reportNode) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }

        String namingStrategyName = Parameter.readString(getFormat(), parameters, NAMING_STRATEGY_PARAMETER, defaultValueConfig);
        // a new instance is requested from the ServiceLoader for each export, instead of caching and
        // reusing one across exports, so that concurrent exports don't share (and corrupt) the same
        // NamingStrategy's internal id-mapping state
        List<NamingStrategy> namingStrategies = ServiceLoader.load(NamingStrategy.class, UcteExporter.class.getClassLoader())
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        NamingStrategy namingStrategy = findNamingStrategy(namingStrategyName, namingStrategies);
        namingStrategy.initializeNetwork(network);
        boolean combinePhaseAngleRegulation = Parameter.readBoolean(getFormat(), parameters, COMBINE_PHASE_ANGLE_REGULATION_PARAMETER, defaultValueConfig);

        ReportNode networkCreationReportNode = UcteExporterReports.networkCreation(reportNode);
        UcteNetwork ucteNetwork = createUcteNetwork(network, namingStrategy, combinePhaseAngleRegulation, networkCreationReportNode);

        try (OutputStream os = dataSource.newOutputStream(null, "uct", false);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            new UcteWriter(ucteNetwork).write(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        UcteExporterReports.fileWritten(reportNode, DataSourceUtil.getFileName(dataSource.getBaseName(), null, "uct"));
    }

    @Override
    public List<Parameter> getParameters() {
        return ConfiguredParameter.load(STATIC_PARAMETERS, getFormat(), defaultValueConfig);
    }

    private static boolean isYNode(Bus bus) {
        return bus.getId().startsWith("YNODE_");
        // TODO(UCTETransformerAtBoundary) Some YNodes could have an id that does not follow this naming convention
        // We could check if this is a bus that has only the following connectable equipment:
        // - A low-impedance line to an XNode
        // - A transformer
        // If it is connected this way, we could conclude it is a YNode
    }

    private static boolean isBoundaryLineYNode(BoundaryLine boundaryLine) {
        return isYNode(boundaryLine.getTerminal().getBusBreakerView().getConnectableBus());
    }

    private static boolean isTransformerYNode(TwoWindingsTransformer twoWindingsTransformer) {
        Bus bus1 = twoWindingsTransformer.getTerminal1().getBusBreakerView().getConnectableBus();
        Bus bus2 = twoWindingsTransformer.getTerminal2().getBusBreakerView().getConnectableBus();
        return isYNode(bus1) || isYNode(bus2);
    }

    /**
     * Convert an IIDM network to an UCTE network
     *
     * @param network the IIDM network to convert
     * @param namingStrategy the naming strategy to generate UCTE nodes name and elements name
     * @return the UcteNetwork corresponding to the IIDM network
     */
    private static UcteNetwork createUcteNetwork(Network network, NamingStrategy namingStrategy, boolean combinePhaseAngleRegulation, ReportNode reportNode) {

        if (network.getShuntCompensatorCount() > 0 ||
            network.getStaticVarCompensatorCount() > 0 ||
            network.getBatteryCount() > 0 ||
            network.getLccConverterStationCount() > 0 ||
            network.getVscConverterStationCount() > 0 ||
            network.getHvdcLineCount() > 0 ||
            network.getThreeWindingsTransformerCount() > 0) {

            throw new UcteException("This network contains unsupported equipments");
        }

        UcteExporterContext context = new UcteExporterContext(namingStrategy, combinePhaseAngleRegulation, reportNode);

        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        ucteNetwork.setVersion(UcteFormatVersion.SECOND);

        UcteExporterContext busesAndSwitchesContext = context.withReportNode(UcteExporterReports.busesAndSwitches(reportNode));
        network.getSubstations().forEach(substation -> substation.getVoltageLevels().forEach(voltageLevel -> {
            voltageLevel.getBusBreakerView().getBuses().forEach(bus -> {
                if (isYNode(bus)) {
                    LOGGER.warn("Ignoring YNode {}", bus.getId());
                } else {
                    convertBus(ucteNetwork, bus, busesAndSwitchesContext);
                }
            });
            voltageLevel.getBusBreakerView().getSwitches().forEach(sw -> convertSwitch(ucteNetwork, sw, busesAndSwitchesContext));
        }));

        UcteExporterContext boundaryLinesContext = context.withReportNode(UcteExporterReports.boundaryLines(reportNode));
        network.getBoundaryLines(BoundaryLineFilter.UNPAIRED).forEach(boundaryLine -> convertBoundaryLine(ucteNetwork, boundaryLine, boundaryLinesContext));

        UcteExporterContext linesContext = context.withReportNode(UcteExporterReports.lines(reportNode));
        network.getLines().forEach(line -> convertLine(ucteNetwork, line, linesContext));

        UcteExporterContext tieLinesContext = context.withReportNode(UcteExporterReports.tieLines(reportNode));
        network.getTieLines().forEach(tieLine -> convertTieLine(ucteNetwork, tieLine, tieLinesContext));

        UcteExporterContext transformersContext = context.withReportNode(UcteExporterReports.transformers(reportNode));
        network.getTwoWindingsTransformers().forEach(transformer -> convertTwoWindingsTransformer(ucteNetwork, transformer, transformersContext));

        ucteNetwork.getComments().add("Generated by powsybl, " + ZonedDateTime.now());
        ucteNetwork.getComments().add("Case date: " + network.getCaseDate());
        return ucteNetwork;
    }

    /**
     * Create a {@link UcteNode} object from the bus and add it to the {@link UcteNetwork}.
     *
     * @param ucteNetwork the target network in ucte
     * @param bus the bus to convert to UCTE
     * @param context the context used to store temporary data during the conversion
     */
    private static void convertBus(UcteNetwork ucteNetwork, Bus bus, UcteExporterContext context) {
        LOGGER.trace("Converting bus {}", bus.getId());

        UcteNodeCode ucteNodeCode = context.getNamingStrategy().getUcteNodeCode(bus);
        String geographicalName = bus.getProperty(GEOGRAPHICAL_NAME_PROPERTY_KEY, null);

        // FIXME(mathbagu): how to initialize active/reactive load and generation: 0 vs NaN vs DEFAULT_MAX_POWER?
        UcteNode ucteNode = new UcteNode(
                ucteNodeCode,
                geographicalName,
                getStatus(bus),
                UcteNodeTypeCode.PQ,
                Double.NaN,
                0,
                0,
                0,
                0,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                null
        );
        ucteNetwork.addNode(ucteNode);

        convertLoads(ucteNode, bus);
        convertGenerators(ucteNode, bus, context.getReportNode());

        if (isSlackBus(bus)) {
            ucteNode.setTypeCode(UcteNodeTypeCode.UT);
        }
    }

    /**
     * Initialize the power consumption fields from the loads connected to the specified bus.
     *
     * @param ucteNode The UCTE node to fill
     * @param bus The bus the loads are connected to
     */
    private static void convertLoads(UcteNode ucteNode, Bus bus) {
        double activeLoad = 0.0;
        double reactiveLoad = 0.0;
        for (Load load : bus.getLoads()) {
            activeLoad += load.getP0();
            reactiveLoad += load.getQ0();
        }
        ucteNode.setActiveLoad(activeLoad);
        ucteNode.setReactiveLoad(reactiveLoad);
    }

    /**
     * Initialize the power generation fields from the generators connected to the specified bus. If there are several
     * generators, their injections are aggregated (see {@code docs/grid_exchange_formats/ucte/export.md}).
     *
     * @param ucteNode The UCTE node to fill
     * @param bus The bus the generators are connected to
     * @param reportNode The report node used to report aggregation issues
     */
    private static void convertGenerators(UcteNode ucteNode, Bus bus, ReportNode reportNode) {
        double activePowerGeneration = -0.0;
        double reactivePowerGeneration = -0.0;
        UcteNodeTypeCode nodeType = UcteNodeTypeCode.PQ;
        for (Generator generator : bus.getGenerators()) {
            if (!Double.isNaN(generator.getTargetP())) {
                activePowerGeneration += generator.getTargetP();
            }
            if (!Double.isNaN(generator.getLocalTargetQ())) {
                reactivePowerGeneration += generator.getLocalTargetQ();
            }
            if (generator.isRegulatingWithMode(RegulationMode.VOLTAGE)) {
                nodeType = UcteNodeTypeCode.PU;
            }
        }
        ucteNode.setActivePowerGeneration(activePowerGeneration != 0 ? -activePowerGeneration : 0);
        ucteNode.setReactivePowerGeneration(reactivePowerGeneration != 0 ? -reactivePowerGeneration : 0);
        ucteNode.setVoltageReference(selectVoltageReference(bus, reportNode));
        ucteNode.setPowerPlantType(aggregatePowerPlantType(bus, reportNode));
        ucteNode.setTypeCode(nodeType);
        setNodePowerGenerationLimits(ucteNode,
                aggregateLimit(Generator::getMinP, UcteExporter::isMinLimitInbounds, "minP", bus, reportNode),
                aggregateLimit(Generator::getMaxP, UcteExporter::isMaxLimitInbounds, "maxP", bus, reportNode),
                aggregateLimit(UcteExporter::ownMinReactiveLimit, UcteExporter::isMinLimitInbounds, "minQ", bus, reportNode),
                aggregateLimit(UcteExporter::ownMaxReactiveLimit, UcteExporter::isMaxLimitInbounds, "maxQ", bus, reportNode));
    }

    private static double ownTargetP(Generator generator) {
        return Double.isNaN(generator.getTargetP()) ? 0 : generator.getTargetP();
    }

    /**
     * @return the minimum reactive limit of the generator, evaluated at its own target active power
     */
    private static double ownMinReactiveLimit(Generator generator) {
        return generator.getReactiveLimits().getMinQ(ownTargetP(generator));
    }

    /**
     * @return the maximum reactive limit of the generator, evaluated at its own target active power
     */
    private static double ownMaxReactiveLimit(Generator generator) {
        return generator.getReactiveLimits().getMaxQ(ownTargetP(generator));
    }

    /**
     * Aggregate one generator power limit over all the generators of a bus. If any generator has no limit (value out
     * of bounds), the aggregated limit is "no limit". Otherwise it is the sum of the limits; if the sum is itself out
     * of bounds, a warning is reported.
     *
     * @return the aggregated limit, or NaN if the limit must be left undefined
     */
    private static double aggregateLimit(ToDoubleFunction<Generator> limit,
                                         DoublePredicate isInbounds,
                                         String limitName,
                                         Bus bus,
                                         ReportNode reportNode) {
        if (bus.getGeneratorStream().findAny().isEmpty()) {
            return Double.NaN;
        }
        double sum = -0.0;
        for (Generator generator : bus.getGenerators()) {
            double value = limit.applyAsDouble(generator);
            if (!isInbounds.test(value)) {
                return Double.NaN;
            }
            sum += value;
        }
        if (!isInbounds.test(sum)) {
            UcteExporterReports.aggregatedPowerLimitOutOfBounds(reportNode, bus.getId(), limitName, sum);
            return Double.NaN;
        }
        return sum;
    }

    /**
     * A candidate voltage reference for a node: its value (in the node's voltage level) and, for remote regulation,
     * the regulated bus and the original target.
     */
    private record VoltageCandidate(Generator generator, double value, Bus remoteBus, double remoteTargetV) {
    }

    /**
     * Select the voltage reference of a node. A UCTE-DEF node has a single voltage reference, so when several
     * generators are connected to the bus, the target voltage of one of them has to be selected.
     * <p>
     * Each generator provides at most one candidate target, in one of the following cases:
     * <ul>
     *     <li><i>local</i>: a voltage-regulating generator provides its local target voltage ({@code localTargetV}) if
     *     defined; otherwise the target value of its voltage regulation, if its regulating terminal is on the exported
     *     bus;</li>
     *     <li><i>remote</i>: a voltage-regulating generator whose regulating terminal is on another bus provides the
     *     target value of its voltage regulation, rescaled to the nominal voltage of the exported bus:
     *     {@code targetValue * localNominalV / remoteNominalV};</li>
     *     <li><i>non-regulating</i>: a generator that does not regulate voltage provides its local target voltage, if
     *     defined. This keeps the voltage reference of a PQ node imported from a UCTE-DEF file.</li>
     * </ul>
     * <p>
     * The candidates of the first non-empty case are considered, in this order: local, remote, non-regulating. Among
     * them, the one of the generator with the largest target active power is kept, ties being broken by the smallest
     * generator id.
     *
     * @return the voltage reference, or NaN if there is no candidate
     */
    private static double selectVoltageReference(Bus bus, ReportNode reportNode) {
        List<VoltageCandidate> local = new ArrayList<>();
        List<VoltageCandidate> remote = new ArrayList<>();
        List<VoltageCandidate> nonRegulating = new ArrayList<>();
        List<Generator> sortedGenerators = bus.getGeneratorStream()
                                              .sorted(Comparator.comparingDouble(UcteExporter::ownTargetP)
                                                                .reversed().thenComparing(Generator::getId))
                                              .toList();
        for (Generator generator : sortedGenerators) {
            double localTargetV = generator.getLocalTargetV();
            if (!generator.isRegulatingWithMode(RegulationMode.VOLTAGE)) {
                if (!Double.isNaN(localTargetV)) {
                    nonRegulating.add(new VoltageCandidate(generator, localTargetV, null, Double.NaN));
                }
            } else if (!Double.isNaN(localTargetV)) {
                local.add(new VoltageCandidate(generator, localTargetV, null, Double.NaN));
            } else if (generator.getVoltageRegulation() != null &&
                    !Double.isNaN(generator.getVoltageRegulation().getTargetValue())) {
                Terminal regulatingTerminal = generator.getRegulatingTerminal();
                Bus regulatedBus = regulatingTerminal.getBusBreakerView().getBus() != null
                                   ? regulatingTerminal.getBusBreakerView().getBus()
                                   : regulatingTerminal.getBusBreakerView().getConnectableBus();
                double targetValue = generator.getVoltageRegulation().getTargetValue();
                if (regulatedBus.getId().equals(bus.getId())) {
                    local.add(new VoltageCandidate(generator, targetValue, null, Double.NaN));
                } else {
                    double rescaled = targetValue * bus.getVoltageLevel().getNominalV() /
                            regulatedBus.getVoltageLevel().getNominalV();
                    remote.add(new VoltageCandidate(generator, rescaled, regulatedBus, targetValue));
                }
            }
        }
        List<VoltageCandidate> candidates = Stream.of(local, remote, nonRegulating)
                                                  .filter(list -> !list.isEmpty())
                                                  .findFirst()
                                                  .orElse(List.of());
        if (candidates.isEmpty()) {
            if (sortedGenerators.stream().anyMatch(generator -> generator.isRegulatingWithMode(RegulationMode.VOLTAGE))) {
                UcteExporterReports.voltageTargetMissing(reportNode, bus.getId());
            }
            return Double.NaN;
        }
        VoltageCandidate kept = candidates.getFirst();
        if (candidates.stream().anyMatch(candidate -> Double.compare(candidate.value(), kept.value()) != 0)) {
            UcteExporterReports.voltageTargetConflict(reportNode, bus.getId(), kept.generator().getId(), kept.value());
        }
        if (kept.remoteBus() != null &&
                Double.compare(bus.getVoltageLevel().getNominalV(), kept.remoteBus().getVoltageLevel().getNominalV()) !=
                        0) {
            UcteExporterReports.remoteVoltageTargetRescaled(reportNode, bus.getId(), kept.generator().getId(),
                    kept.remoteBus().getId(), kept.remoteTargetV(), kept.value());
        }
        return kept.value();
    }

    /**
     * @return the common power plant type of the generators, type F if they differ, or null if there is no generator
     */
    private static UctePowerPlantType aggregatePowerPlantType(Bus bus, ReportNode reportNode) {
        Set<UctePowerPlantType> types = bus.getGeneratorStream()
                                                  .map(UcteExporter::energySourceToUctePowerPlantType)
                                                  .collect(Collectors.toSet());
        if (types.size() > 1) {
            UcteExporterReports.mixedPowerPlantTypes(reportNode, bus.getId());
            return UctePowerPlantType.F;
        }
        return types.stream().findFirst().orElse(null);
    }

    /**
     * If provided generator power limits are permissible, set the ucteNode corresponding active and reactive power
     * generation limits. Max limit is valid if it is lower than {@code 9999} and min limit is valid of it is greater
     * than {@code -9999}. See {@link UcteConverterConstants#DEFAULT_POWER_LIMIT} that defines the special "no value" in
     * UCTE import. Invalid values are simply not exported (cell left empty in the export file)
     *
     * @param ucteNode an exported node
     * @param minP min active power
     * @param maxP max active power
     * @param minQ min reactive power
     * @param maxQ max reactive power
     */
    private static void setNodePowerGenerationLimits(UcteNode ucteNode,
                                                     double minP,
                                                     double maxP,
                                                     double minQ,
                                                     double maxQ) {
        if (isMinLimitInbounds(minP)) {
            ucteNode.setMinimumPermissibleActivePowerGeneration(-minP);
        }
        if (isMaxLimitInbounds(maxP)) {
            ucteNode.setMaximumPermissibleActivePowerGeneration(-maxP);
        }
        if (isMinLimitInbounds(minQ)) {
            ucteNode.setMinimumPermissibleReactivePowerGeneration(-minQ);
        }
        if (isMaxLimitInbounds(maxQ)) {
            ucteNode.setMaximumPermissibleReactivePowerGeneration(-maxQ);
        }
    }

    /**
     * Create a {@link UcteNode} object from a BoundaryLine and add it to the {@link UcteNetwork}.
     *
     * @param ucteNetwork The target network in ucte
     * @param boundaryLine The boundaryLine used to create the XNode
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertXNode(UcteNetwork ucteNetwork, BoundaryLine boundaryLine, UcteExporterContext context) {
        UcteNodeCode xnodeCode = context.getNamingStrategy().getUcteNodeCode(boundaryLine);
        String geographicalName = boundaryLine.getProperty(GEOGRAPHICAL_NAME_PROPERTY_KEY, null);

        UcteNodeStatus ucteNodeStatus = getXnodeStatus(boundaryLine);
        UcteNode ucteNode = convertXNode(ucteNetwork, xnodeCode, geographicalName, ucteNodeStatus);
        ucteNode.setActiveLoad(boundaryLine.getP0());
        ucteNode.setReactiveLoad(boundaryLine.getQ0());
        double generatorTargetP = boundaryLine.getGeneration().getTargetP();
        ucteNode.setActivePowerGeneration(Double.isNaN(generatorTargetP) ? 0 : -generatorTargetP);
        double generatorTargetQ = boundaryLine.getGeneration().getTargetQ();
        ucteNode.setReactivePowerGeneration(Double.isNaN(generatorTargetQ) ? 0 : -generatorTargetQ);
        if (boundaryLine.getGeneration().isVoltageRegulationOn()) {
            ucteNode.setTypeCode(UcteNodeTypeCode.PU);
            ucteNode.setVoltageReference(boundaryLine.getGeneration().getTargetV());
            double minP = boundaryLine.getGeneration().getMinP();
            double maxP = boundaryLine.getGeneration().getMaxP();
            double minQ = boundaryLine.getGeneration().getReactiveLimits().getMinQ(boundaryLine.getGeneration().getTargetP());
            double maxQ = boundaryLine.getGeneration().getReactiveLimits().getMaxQ(boundaryLine.getGeneration().getTargetP());
            setNodePowerGenerationLimits(ucteNode, minP, maxP, minQ, maxQ);
        }
    }

    /**
     * Generator min power limits must be strictly grater than -9999 (see
     * {@link UcteConverterConstants#DEFAULT_POWER_LIMIT}). Values that are out of bounds must be ignored and exported
     * blank.
     *
     * @param value a generator min power limit
     * @return whether this max power limit should be exported
     */
    private static boolean isMinLimitInbounds(double value) {
        return value > -DEFAULT_POWER_LIMIT;
    }

    /**
     * Generator max power limits must be strictly smaller than 9999 (see
     * {@link UcteConverterConstants#DEFAULT_POWER_LIMIT}). Values that are out of bounds must be ignored and exported
     * blank.
     *
     * @param value a generator max power limit
     * @return whether this max power limit should be exported
     */
    private static boolean isMaxLimitInbounds(double value) {
        return value < DEFAULT_POWER_LIMIT;
    }

    /**
     * Create a {@link UcteNode} object from a TieLine and add it to the {@link UcteNetwork}.
     *
     * @param ucteNetwork The target network in ucte
     * @param tieLine The TieLine used to create the XNode
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertXNode(UcteNetwork ucteNetwork, TieLine tieLine, UcteExporterContext context) {
        UcteNodeCode xnodeCode = context.getNamingStrategy().getUcteNodeCode(tieLine.getPairingKey());
        String geographicalName = mergedProperty(tieLine.getBoundaryLine1(), tieLine.getBoundaryLine2(), GEOGRAPHICAL_NAME_PROPERTY_KEY);
        UcteNodeStatus ucteNodeStatus = getXnodeStatus(mergedProperty(tieLine.getBoundaryLine1(), tieLine.getBoundaryLine2(), STATUS_PROPERTY_KEY + "_XNode"));
        convertXNode(ucteNetwork, xnodeCode, geographicalName, ucteNodeStatus);
    }

    /**
     * Create a {@link UcteNode} object from a {@link UcteNodeCode} object and an optional geographical name and add it to the {@link UcteNetwork}.
     * @param ucteNetwork The target network in ucte
     * @param xnodeCode The UCTE code of the XNode
     * @param geographicalName The geographical name of the XNode
     * @param ucteNodeStatus The UcteNodeStatus of the XNode
     * @return the UcteNode
     */
    private static UcteNode convertXNode(UcteNetwork ucteNetwork, UcteNodeCode xnodeCode, String geographicalName, UcteNodeStatus ucteNodeStatus) {
        if (xnodeCode.getUcteCountryCode() != UcteCountryCode.XX) {
            throw new UcteException("Invalid xnode code: " + xnodeCode);
        }

        UcteNode ucteNode = new UcteNode(
                xnodeCode,
                geographicalName,
                ucteNodeStatus,
                UcteNodeTypeCode.PQ,
                Double.NaN,
                0,
                0,
                0,
                0,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                null
        );
        ucteNetwork.addNode(ucteNode);

        return ucteNode;
    }

    /**
     * Convert a switch to an {@link UcteLine}. Busbar couplers are UCTE lines with resistance, reactance and susceptance set to 0.
     *
     * @param ucteNetwork The target network in ucte
     * @param sw The switch to convert to a busbar coupler
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertSwitch(UcteNetwork ucteNetwork, Switch sw, UcteExporterContext context) {
        LOGGER.trace("Converting switch {}", sw.getId());

        UcteElementId ucteElementId = context.getNamingStrategy().getUcteElementId(sw);
        UcteElementStatus status = getStatus(sw);
        String elementName = sw.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);

        UcteLine ucteLine = new UcteLine(ucteElementId, status, 0, 0, 0, null, elementName);
        ucteNetwork.addLine(ucteLine);

        setSwitchCurrentLimit(ucteLine, sw, context);
    }

    /**
     * Convert {@link Line} and {@link TieLine} objects to {@link UcteLine} object and it to the network.
     *
     * @param ucteNetwork The target network in ucte
     * @param line The line to convert to {@link UcteLine}
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertLine(UcteNetwork ucteNetwork, Line line, UcteExporterContext context) {
        LOGGER.trace("Converting line {}", line.getId());

        UcteElementId lineId = context.getNamingStrategy().getUcteElementId(line);
        UcteElementStatus status = getStatus(line);
        String elementName = line.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);

        UcteLine ucteLine = new UcteLine(
                lineId,
                status,
                line.getR(),
                line.getX(),
                line.getB1() + line.getB2(),
                getPermanentLimit(line),
                elementName);
        ucteNetwork.addLine(ucteLine);
    }

    /**
     * Convert a {@link TieLine} to two {@link UcteLine} connected by a Xnode. Add the two {@link UcteLine} and the {@link UcteNode} to the network.
     *
     * @param ucteNetwork The target UcteNetwork
     * @param tieLine The TieLine object to convert
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertTieLine(UcteNetwork ucteNetwork, TieLine tieLine, UcteExporterContext context) {
        LOGGER.trace("Converting TieLine {}", tieLine.getId());

        // Create XNode
        convertXNode(ucteNetwork, tieLine, context);

        // Create boundary line 1
        BoundaryLine boundaryLine1 = tieLine.getBoundaryLine1();
        UcteElementId ucteElementId1 = context.getNamingStrategy().getUcteElementId(boundaryLine1.getId());
        String elementName1 = boundaryLine1.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);
        UcteElementStatus status1 = getStatusHalf(tieLine, TwoSides.ONE);
        UcteLine ucteLine1 = new UcteLine(
                ucteElementId1,
                status1,
                boundaryLine1.getR(),
                boundaryLine1.getX(),
                boundaryLine1.getB(),
                tieLine.getBoundaryLine1().getCurrentLimits().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName1);
        ucteNetwork.addLine(ucteLine1);

        // Create boundary line2
        BoundaryLine boundaryLine2 = tieLine.getBoundaryLine2();
        UcteElementId ucteElementId2 = context.getNamingStrategy().getUcteElementId(boundaryLine2.getId());
        String elementName2 = boundaryLine2.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);
        UcteElementStatus status2 = getStatusHalf(tieLine, TwoSides.TWO);
        UcteLine ucteLine2 = new UcteLine(
                ucteElementId2,
                status2,
                boundaryLine2.getR(),
                boundaryLine2.getX(),
                boundaryLine2.getB(),
                tieLine.getBoundaryLine2().getCurrentLimits().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName2);
        ucteNetwork.addLine(ucteLine2);
    }

    /**
     * Convert a {@link BoundaryLine} object to an {@link UcteNode} and a {@link UcteLine} objects.
     *
     * @param ucteNetwork The target network in ucte
     * @param boundaryLine The boundaryLine to convert to UCTE
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertBoundaryLine(UcteNetwork ucteNetwork, BoundaryLine boundaryLine, UcteExporterContext context) {
        LOGGER.trace("Converting BoundaryLine {}", boundaryLine.getId());

        // Create XNode
        convertXNode(ucteNetwork, boundaryLine, context);

        // Always create the XNode,
        // But do not export the boundary line if it was related to a YNode
        // The corresponding transformer will be connected to the XNode
        if (isBoundaryLineYNode(boundaryLine)) {
            LOGGER.warn("Ignoring BoundaryLine at YNode in the export {}", boundaryLine.getId());
            return;
        }

        // Create line
        UcteElementId elementId = context.getNamingStrategy().getUcteElementId(boundaryLine);
        String elementName = boundaryLine.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);
        UcteElementStatus ucteElementStatus = getStatus(boundaryLine);

        UcteLine ucteLine = new UcteLine(
                elementId,
                ucteElementStatus,
                boundaryLine.getR(),
                boundaryLine.getX(),
                boundaryLine.getB(),
                boundaryLine.getCurrentLimits().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName);
        ucteNetwork.addLine(ucteLine);
    }

    private static String mergedProperty(Identifiable<?> identifiable1, Identifiable<?> identifiable2, String key) {
        String value;
        String value1 = identifiable1.getProperty(key, "");
        String value2 = identifiable2.getProperty(key, "");
        if (value1.equals(value2)) {
            value = value1;
        } else if (value1.isEmpty()) {
            value = value2;
            LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 1 is empty, keeping side 2 value '{}'", key, value2);
        } else if (value2.isEmpty()) {
            value = value1;
            LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. Side 2 is empty, keeping side 1 value '{}'", key, value1);
        } else {
            // Inconsistent values, declare the result value empty
            value = "";
            LOGGER.debug("Inconsistencies of property '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Ignoring the property on the merged line",
                    key,
                    value1,
                    value2);
        }
        return value;
    }

    private static UcteNodeStatus getXnodeStatus(Identifiable<?> identifiable) {
        return getXnodeStatus(identifiable.getProperty(STATUS_PROPERTY_KEY + "_XNode"));
    }

    private static UcteNodeStatus getXnodeStatus(String statusNode) {
        UcteNodeStatus ucteNodeStatus = UcteNodeStatus.REAL;
        if (statusNode != null && statusNode.equals(UcteNodeStatus.EQUIVALENT.toString())) {
            ucteNodeStatus = UcteNodeStatus.EQUIVALENT;
        }
        return ucteNodeStatus;
    }

    private static UcteNodeStatus getStatus(Identifiable<?> identifiable) {
        if (identifiable.isFictitious()) {
            return UcteNodeStatus.EQUIVALENT;
        } else {
            return UcteNodeStatus.REAL;
        }
    }

    private static UcteElementStatus getStatus(Branch<?> branch) {
        if (branch.isFictitious()) {
            if (branch.getTerminal1().isConnected() && branch.getTerminal2().isConnected()) {
                return UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            }
        } else {
            if (branch.getTerminal1().isConnected() && branch.getTerminal2().isConnected()) {
                return UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION;
            }
        }
    }

    private static UcteElementStatus getStatusHalf(TieLine tieLine, TwoSides side) {
        if (tieLine.getBoundaryLine(side).isFictitious()) {
            if (tieLine.getBoundaryLine(side).getTerminal().isConnected()) {
                return UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            }
        } else {
            if (tieLine.getBoundaryLine(side).getTerminal().isConnected()) {
                return UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION;
            }
        }
    }

    private static UcteElementStatus getStatus(BoundaryLine boundaryLine) {
        if (Boolean.parseBoolean(boundaryLine.getProperty(IS_COUPLER_PROPERTY_KEY, "false"))) {
            if (boundaryLine.getTerminal().isConnected()) {
                return UcteElementStatus.BUSBAR_COUPLER_IN_OPERATION;
            } else {
                return UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION;
            }
        }

        if (boundaryLine.isFictitious()) {
            if (boundaryLine.getTerminal().isConnected()) {
                return UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            }
        } else {
            if (boundaryLine.getTerminal().isConnected()) {
                return UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION;
            }
        }
    }

    private static UcteElementStatus getStatus(Switch switchEl) {
        if (switchEl.isOpen()) {
            return UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION;
        } else {
            return UcteElementStatus.BUSBAR_COUPLER_IN_OPERATION;
        }
    }

    private static boolean isSlackBus(Bus bus) {
        VoltageLevel vl = bus.getVoltageLevel();
        SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
        if (slackTerminal != null) {
            Terminal terminal = slackTerminal.getTerminal();
            return terminal.getBusBreakerView().getBus() == bus;
        }
        return false;
    }

    /**
     * Converts the {@link TwoWindingsTransformer} into a {@link UcteTransformer} and adds it to the ucteNetwork.
     * Also creates the adds the linked {@link UcteRegulation}
     *
     * @param ucteNetwork The target UcteNetwork
     * @param twoWindingsTransformer The two windings transformer we want to convert
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertTwoWindingsTransformer(UcteNetwork ucteNetwork, TwoWindingsTransformer twoWindingsTransformer, UcteExporterContext context) {
        if (isTransformerYNode(twoWindingsTransformer)) {
            LOGGER.info("Transformer at boundary is exported {}", twoWindingsTransformer.getId());
            // The transformer element id contains references to the original UCTE nodes
            // (Inner node inside network and boundary XNode)
            // We can export it as a regular transformer
        }

        UcteElementId elementId = context.getNamingStrategy().getUcteElementId(twoWindingsTransformer);
        UcteElementStatus status = getStatus(twoWindingsTransformer);
        String elementName = twoWindingsTransformer.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);
        double nominalPower = Double.NaN;
        if (twoWindingsTransformer.hasProperty(NOMINAL_POWER_KEY)) {
            nominalPower = Double.parseDouble(twoWindingsTransformer.getProperty(NOMINAL_POWER_KEY, null));
        }

        UcteTransformer ucteTransformer = new UcteTransformer(
                elementId,
                status,
                twoWindingsTransformer.getR(),
                twoWindingsTransformer.getX(),
                twoWindingsTransformer.getB(),
                getPermanentLimit(twoWindingsTransformer),
                elementName,
                twoWindingsTransformer.getRatedU2(),
                twoWindingsTransformer.getRatedU1(),
                nominalPower,
                twoWindingsTransformer.getG());
        ucteNetwork.addTransformer(ucteTransformer);

        convertRegulation(ucteNetwork, elementId, twoWindingsTransformer, context.withCombinePhaseAngleRegulation());
    }

    /**
     * Creates and adds to the ucteNetwork the {@link UcteRegulation} linked to the TwoWindingsTransformer.
     * <li>{@link RatioTapChanger} into {@link UctePhaseRegulation}</li>
     * <li>{@link PhaseTapChanger} into {@link UcteAngleRegulation}</li>
     *
     * @param ucteNetwork The target UcteNetwork
     * @param ucteElementId The UcteElementId corresponding to the TwoWindingsTransformer
     * @param twoWindingsTransformer The TwoWindingTransformer we want to convert
     */
    private static void convertRegulation(UcteNetwork ucteNetwork, UcteElementId ucteElementId, TwoWindingsTransformer twoWindingsTransformer, boolean combinePhaseAngleRegulation) {
        if (twoWindingsTransformer.hasRatioTapChanger() || twoWindingsTransformer.hasPhaseTapChanger()) {
            UctePhaseRegulation uctePhaseRegulation = twoWindingsTransformer.getOptionalRatioTapChanger()
                    .map(rtc -> convertRatioTapChanger(twoWindingsTransformer)).orElse(null);
            UcteAngleRegulation ucteAngleRegulation = twoWindingsTransformer.getOptionalPhaseTapChanger()
                    .map(ptc -> convertPhaseTapChanger(twoWindingsTransformer, combinePhaseAngleRegulation)).orElse(null);
            UcteRegulation ucteRegulation = new UcteRegulation(ucteElementId, uctePhaseRegulation, ucteAngleRegulation);
            ucteNetwork.addRegulation(ucteRegulation);
        }
    }

    /**
     * Creates the {@link UcteRegulation} linked to the twoWindingsTransformer
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the RatioTapChanger we want to convert
     * @return the UctePhaseRegulation needed to create a {@link UcteRegulation}
     * @see UcteConverterHelper#calculatePhaseDu(TwoWindingsTransformer)
     */
    private static UctePhaseRegulation convertRatioTapChanger(TwoWindingsTransformer twoWindingsTransformer) {
        LOGGER.trace("Converting iidm ratio tap changer of transformer {}", twoWindingsTransformer.getId());

        double du = calculatePhaseDu(twoWindingsTransformer);
        UctePhaseRegulation uctePhaseRegulation = new UctePhaseRegulation(
                du,
                twoWindingsTransformer.getRatioTapChanger().getHighTapPosition(),
                twoWindingsTransformer.getRatioTapChanger().getTapPosition(),
                Double.NaN);
        if (!Double.isNaN(twoWindingsTransformer.getRatioTapChanger().getRegulatingTargetV())) {
            uctePhaseRegulation.setU(twoWindingsTransformer.getRatioTapChanger().getRegulatingTargetV());
        }
        return uctePhaseRegulation;
    }

    /**
     * Determines the UcteAngleRegulationType and depending on it, creates the UcteAngleRegulation
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the PhaseTapChanger we want to convert
     * @return the UcteAngleRegulation needed to create a {@link UcteRegulation}
     * @see UcteAngleRegulation
     * @see UcteExporter#findRegulationType(TwoWindingsTransformer)
     */
    private static UcteAngleRegulation convertPhaseTapChanger(TwoWindingsTransformer twoWindingsTransformer, boolean combinePhaseAngleRegulation) {
        LOGGER.trace("Converting iidm Phase tap changer of transformer {}", twoWindingsTransformer.getId());
        UcteAngleRegulationType ucteAngleRegulationType = findRegulationType(twoWindingsTransformer);
        if (ucteAngleRegulationType == UcteAngleRegulationType.SYMM) {
            return new UcteAngleRegulation(calculateSymmAngleDu(twoWindingsTransformer),
                    90,
                    twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(),
                    twoWindingsTransformer.getPhaseTapChanger().getTapPosition(),
                    calculateAngleP(twoWindingsTransformer),
                    ucteAngleRegulationType);
        } else {
            Complex duAndAngle = calculateAsymmAngleDuAndAngle(twoWindingsTransformer, combinePhaseAngleRegulation);
            return new UcteAngleRegulation(duAndAngle.abs(),
                    Math.toDegrees(duAndAngle.getArgument()),
                    twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(),
                    twoWindingsTransformer.getPhaseTapChanger().getTapPosition(),
                    calculateAngleP(twoWindingsTransformer),
                    ucteAngleRegulationType);
        }
    }

    /**
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return P (MW) of the angle regulation for the two windings transformer
     */
    private static double calculateAngleP(TwoWindingsTransformer twoWindingsTransformer) {
        return -twoWindingsTransformer.getPhaseTapChanger().getRegulationValue();
    }

    /**
     * Give the type of the UcteAngleRegulation
     *
     * @param twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return The type of the UcteAngleRegulation
     */
    private static UcteAngleRegulationType findRegulationType(TwoWindingsTransformer twoWindingsTransformer) {
        if (isSymm(twoWindingsTransformer)) {
            return UcteAngleRegulationType.SYMM;
        } else {
            return UcteAngleRegulationType.ASYM;
        }
    }

    private static boolean isSymm(TwoWindingsTransformer twoWindingsTransformer) {
        for (int i = twoWindingsTransformer.getPhaseTapChanger().getLowTapPosition();
             i < twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(); i++) {
            if (twoWindingsTransformer.getPhaseTapChanger().getStep(i).getRho() != 1) {
                return false;
            }
        }
        return true;
    }

    private static void setSwitchCurrentLimit(UcteLine ucteLine, Switch sw, UcteExporterContext context) {
        if (sw.hasProperty(CURRENT_LIMIT_PROPERTY_KEY)) {
            try {
                ucteLine.setCurrentLimit(Integer.parseInt(sw.getProperty(CURRENT_LIMIT_PROPERTY_KEY)));
            } catch (NumberFormatException exception) {
                ucteLine.setCurrentLimit(null);
                LOGGER.warn("Switch {}: No current limit provided", sw.getId());
                UcteExporterReports.switchCurrentLimitMissing(context.getReportNode(), sw.getId());
            }
        } else {
            ucteLine.setCurrentLimit(null);
            LOGGER.warn("Switch {}: No current limit provided", sw.getId());
            UcteExporterReports.switchCurrentLimitMissing(context.getReportNode(), sw.getId());
        }
    }

    private static UctePowerPlantType energySourceToUctePowerPlantType(Generator generator) {
        if (generator.hasProperty(POWER_PLANT_TYPE_PROPERTY_KEY)) {
            return UctePowerPlantType.valueOf(generator.getProperty(POWER_PLANT_TYPE_PROPERTY_KEY));
        }
        switch (generator.getEnergySource()) {
            case HYDRO:
                return UctePowerPlantType.H;
            case NUCLEAR:
                return UctePowerPlantType.N;
            case THERMAL:
                return UctePowerPlantType.C;
            case WIND:
                return UctePowerPlantType.W;
            default:
                return UctePowerPlantType.F;
        }
    }

    private static Integer getPermanentLimit(Branch<?> branch) {
        Optional<Double> permanentLimit1 = branch.getCurrentLimits1().map(CurrentLimits::getPermanentLimit);
        Optional<Double> permanentLimit2 = branch.getCurrentLimits2().map(CurrentLimits::getPermanentLimit);
        if (permanentLimit1.isPresent() && permanentLimit2.isPresent()) {
            return (int) Double.min(permanentLimit1.get(), permanentLimit2.get());
        } else {
            return permanentLimit1.map(Double::intValue).orElseGet(() -> permanentLimit2.isPresent() ? permanentLimit2.get().intValue() : null);
        }
    }

    static NamingStrategy findNamingStrategy(String name, List<NamingStrategy> namingStrategies) {
        Objects.requireNonNull(namingStrategies);

        if (namingStrategies.size() == 1 && name == null) {
            // no information to select the implementation but only one naming strategy, so we can use it by default
            // (that is the most common use case)
            return namingStrategies.getFirst();
        } else {
            if (namingStrategies.size() > 1 && name == null) {
                // several naming strategies and no information to select which one to choose, we can only throw
                // an exception
                List<String> namingStrategyNames = namingStrategies.stream().map(NamingStrategy::getName).toList();
                throw new PowsyblException("Several naming strategy implementations found (" + namingStrategyNames
                        + "), you must add properties to select the implementation");
            }
            return namingStrategies.stream()
                    .filter(ns -> ns.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new PowsyblException("NamingStrategy '" + name + "' not found"));
        }
    }

}
