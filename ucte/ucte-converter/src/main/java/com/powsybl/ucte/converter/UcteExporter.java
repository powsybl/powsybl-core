/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.ucte.converter.util.UcteConverterHelper;
import com.powsybl.ucte.network.*;
import com.powsybl.ucte.network.io.UcteWriter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.powsybl.ucte.converter.util.UcteConstants.*;
import static com.powsybl.ucte.converter.util.UcteConverterHelper.*;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(Exporter.class)
public class UcteExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExporter.class);

    public static final String NAMING_STRATEGY = "ucte.export.naming-strategy";

    private static final Parameter NAMING_STRATEGY_PARAMETER = new Parameter(NAMING_STRATEGY, ParameterType.STRING, "Default naming strategy for UCTE codes conversion", "Default");

    private static final List<Parameter> STATIC_PARAMETERS = List.of(NAMING_STRATEGY_PARAMETER);

    private static final Supplier<List<NamingStrategy>> NAMING_STRATEGY_SUPPLIERS
            = Suppliers.memoize(() -> new ServiceLoaderCache<>(NamingStrategy.class).getServices());

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
    public void export(Network network, Properties parameters, DataSource dataSource) {
        if (network == null) {
            throw new IllegalArgumentException("network is null");
        }

        String namingStrategyName = Parameter.readString(getFormat(), parameters, NAMING_STRATEGY_PARAMETER, defaultValueConfig);
        NamingStrategy namingStrategy = findNamingStrategy(namingStrategyName, NAMING_STRATEGY_SUPPLIERS.get());

        UcteNetwork ucteNetwork = createUcteNetwork(network, namingStrategy);

        try (OutputStream os = dataSource.newOutputStream(null, "uct", false);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            new UcteWriter(ucteNetwork).write(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Parameter> getParameters() {
        return STATIC_PARAMETERS;
    }

    private static boolean isYNode(Bus bus) {
        return bus.getId().startsWith("YNODE_");
        // TODO(UCTETransformerAtBoundary) Some YNodes could have an id that does not follow this naming convention
        // We could check if this is a bus that has only the following connectable equipment:
        // - A low-impedance line to an XNode
        // - A transformer
        // If it is connected this way, we could conclude it is a YNode
    }

    private static boolean isDanglingLineYNode(DanglingLine danglingLine) {
        return isYNode(danglingLine.getTerminal().getBusBreakerView().getConnectableBus());
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
    private static UcteNetwork createUcteNetwork(Network network, NamingStrategy namingStrategy) {

        if (network.getShuntCompensatorCount() > 0 ||
            network.getStaticVarCompensatorCount() > 0 ||
            network.getBatteryCount() > 0 ||
            network.getLccConverterStationCount() > 0 ||
            network.getVscConverterStationCount() > 0 ||
            network.getHvdcLineCount() > 0 ||
            network.getThreeWindingsTransformerCount() > 0) {

            throw new UcteException("This network contains unsupported equipments");
        }

        UcteExporterContext context = new UcteExporterContext(namingStrategy);

        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        ucteNetwork.setVersion(UcteFormatVersion.SECOND);

        for (Substation substation : network.getSubstations()) {
            for (VoltageLevel voltageLevel : substation.getVoltageLevels()) {
                for (Bus bus : voltageLevel.getBusBreakerView().getBuses()) {
                    if (isYNode(bus)) {
                        LOGGER.warn("Ignoring YNode {}", bus.getId());
                    } else {
                        convertBus(ucteNetwork, bus, context);
                    }
                }
                for (Switch sw : voltageLevel.getBusBreakerView().getSwitches()) {
                    convertSwitch(ucteNetwork, sw, context);
                }
            }
        }
        for (DanglingLine danglingLine : network.getDanglingLines()) {
            convertDanglingLine(ucteNetwork, danglingLine, context);
        }
        for (Line line : network.getLines()) {
            convertLine(ucteNetwork, line, context);
        }
        for (TwoWindingsTransformer transformer : network.getTwoWindingsTransformers()) {
            convertTwoWindingsTransformer(ucteNetwork, transformer, context);
        }

        ucteNetwork.getComments().add("Generated by powsybl, " + DateTime.now());
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

        if (bus.getGeneratorStream().count() > 1) {
            throw new UcteException("Too many generators connected to this bus");
        }
        if (bus.getLoadStream().count() > 1) {
            throw new UcteException("Too many loads connected to this bus");
        }

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
        convertGenerators(ucteNode, bus);

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
     * Initialize the power generation fields from the generators connected to the specified bus.
     *
     * @param ucteNode The UCTE node to fill
     * @param bus The bus the generators are connected to
     */
    private static void convertGenerators(UcteNode ucteNode, Bus bus) {
        double activePowerGeneration = -0.0;
        double reactivePowerGeneration = -0.0;
        double voltageReference = Double.NaN;
        double minP = Double.NaN;
        double maxP = Double.NaN;
        double minQ = Double.NaN;
        double maxQ = Double.NaN;
        UcteNodeTypeCode nodeType = UcteNodeTypeCode.PQ;
        UctePowerPlantType powerPlantType = null;
        for (Generator generator : bus.getGenerators()) {
            if (!Double.isNaN(generator.getTargetP())) {
                activePowerGeneration += generator.getTargetP();
            }
            if (!Double.isNaN(generator.getTargetQ())) {
                reactivePowerGeneration += generator.getTargetQ();
            }
            if (!Double.isNaN(generator.getTargetV())) {
                // FIXME(mathbagu): what if not all the generators have the same targetV?
                // Should we use bus.getV() instead?
                voltageReference = generator.getTargetV();
            }
            if (generator.isVoltageRegulatorOn()) {
                nodeType = UcteNodeTypeCode.PU;
            }
            minP = generator.getMinP();
            maxP = generator.getMaxP();
            // FIXME(mathbagu): how to get minQ and maxQ for an aggregated generator
            minQ = generator.getReactiveLimits().getMinQ(activePowerGeneration);
            maxQ = generator.getReactiveLimits().getMaxQ(activePowerGeneration);

            // FIXME(mathbagu): what if not all the generators have the same energy source?
            powerPlantType = energySourceToUctePowerPlantType(generator);
        }
        ucteNode.setActivePowerGeneration(activePowerGeneration != 0 ? -activePowerGeneration : 0);
        ucteNode.setReactivePowerGeneration(reactivePowerGeneration != 0 ? -reactivePowerGeneration : 0);
        ucteNode.setVoltageReference(voltageReference);
        ucteNode.setPowerPlantType(powerPlantType);
        ucteNode.setTypeCode(nodeType);
        // FIXME(mathbagu): to be changed in UcteImporter?
        if (minP != -DEFAULT_POWER_LIMIT) {
            ucteNode.setMinimumPermissibleActivePowerGeneration(-minP);
        }
        if (maxP != DEFAULT_POWER_LIMIT) {
            ucteNode.setMaximumPermissibleActivePowerGeneration(-maxP);
        }
        if (minQ != -DEFAULT_POWER_LIMIT) {
            ucteNode.setMinimumPermissibleReactivePowerGeneration(-minQ);
        }
        if (maxQ != DEFAULT_POWER_LIMIT) {
            ucteNode.setMaximumPermissibleReactivePowerGeneration(-maxQ);
        }
    }

    /**
     * Create a {@link UcteNode} object from a DanglingLine and add it to the {@link UcteNetwork}.
     *
     * @param ucteNetwork The target network in ucte
     * @param danglingLine The danglingLine used to create the XNode
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertXNode(UcteNetwork ucteNetwork, DanglingLine danglingLine, UcteExporterContext context) {
        UcteNodeCode xnodeCode = context.getNamingStrategy().getUcteNodeCode(danglingLine);
        String geographicalName = danglingLine.getProperty(GEOGRAPHICAL_NAME_PROPERTY_KEY, null);

        UcteNodeStatus ucteNodeStatus = getXnodeStatus(danglingLine);
        UcteNode ucteNode = convertXNode(ucteNetwork, xnodeCode, geographicalName, ucteNodeStatus);
        ucteNode.setActiveLoad(danglingLine.getP0());
        ucteNode.setReactiveLoad(danglingLine.getQ0());
        double generatorTargetP = danglingLine.getGeneration().getTargetP();
        ucteNode.setActivePowerGeneration(Double.isNaN(generatorTargetP) ? 0 : -generatorTargetP);
        double generatorTargetQ = danglingLine.getGeneration().getTargetQ();
        ucteNode.setReactivePowerGeneration(Double.isNaN(generatorTargetQ) ? 0 : -generatorTargetQ);
        if (danglingLine.getGeneration().isVoltageRegulationOn()) {
            ucteNode.setTypeCode(UcteNodeTypeCode.PU);
            ucteNode.setVoltageReference(danglingLine.getGeneration().getTargetV());
            double minP = danglingLine.getGeneration().getMinP();
            double maxP = danglingLine.getGeneration().getMaxP();
            double minQ = danglingLine.getGeneration().getReactiveLimits().getMinQ(danglingLine.getGeneration().getTargetP());
            double maxQ = danglingLine.getGeneration().getReactiveLimits().getMaxQ(danglingLine.getGeneration().getTargetP());
            if (minP != -DEFAULT_POWER_LIMIT) {
                ucteNode.setMinimumPermissibleActivePowerGeneration(-minP);
            }
            if (maxP != DEFAULT_POWER_LIMIT) {
                ucteNode.setMaximumPermissibleActivePowerGeneration(-maxP);
            }
            if (minQ != -DEFAULT_POWER_LIMIT) {
                ucteNode.setMinimumPermissibleReactivePowerGeneration(-minQ);
            }
            if (maxQ != DEFAULT_POWER_LIMIT) {
                ucteNode.setMaximumPermissibleReactivePowerGeneration(-maxQ);
            }
        }
    }

    /**
     * Create a {@link UcteNode} object from a MergedXnode and add it to the {@link UcteNetwork}.
     *
     * @param ucteNetwork The target network in ucte
     * @param mergedXnode The MergedXnode extension used to create the XNode
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertXNode(UcteNetwork ucteNetwork, MergedXnode mergedXnode, UcteExporterContext context) {
        UcteNodeCode xnodeCode = context.getNamingStrategy().getUcteNodeCode(mergedXnode.getCode());
        String geographicalName = mergedXnode.getExtendable().getProperty(GEOGRAPHICAL_NAME_PROPERTY_KEY, "");

        UcteNodeStatus xnodeStatus = getXnodeStatus(mergedXnode.getExtendable());
        convertXNode(ucteNetwork, xnodeCode, geographicalName, xnodeStatus);
    }

    /**
     * Create a {@link UcteNode} object from a TieLine and add it to the {@link UcteNetwork}.
     *
     * @param ucteNetwork The target network in ucte
     * @param tieLine The TieLine used to create the XNode
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertXNode(UcteNetwork ucteNetwork, TieLine tieLine, UcteExporterContext context) {
        UcteNodeCode xnodeCode = context.getNamingStrategy().getUcteNodeCode(tieLine.getUcteXnodeCode());
        String geographicalName = tieLine.getProperty(GEOGRAPHICAL_NAME_PROPERTY_KEY, "");
        UcteNodeStatus ucteNodeStatus = getXnodeStatus(tieLine);
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

        setSwitchCurrentLimit(ucteLine, sw);
    }

    /**
     * Convert {@link Line} and {@link TieLine} objects to {@link UcteLine} object and it to the network.
     *
     * @param ucteNetwork The target network in ucte
     * @param line The line to convert to {@link UcteLine}
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertLine(UcteNetwork ucteNetwork, Line line, UcteExporterContext context) {
        if (line.isTieLine()) {
            convertTieLine(ucteNetwork, line, context);
            return;
        }

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
     * Convert a tie line to two {@link UcteLine} connected by a Xnode. In IIDM, tie lines might be instance of {@link TieLine} or have {@link MergedXnode} extension.
     *
     * @param ucteNetwork The target UcteNetwork
     * @param line The tie line to dispatch
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertTieLine(UcteNetwork ucteNetwork, Line line, UcteExporterContext context) {
        MergedXnode mergedXnode = line.getExtension(MergedXnode.class);
        if (mergedXnode != null) {
            convertTieLine(ucteNetwork, mergedXnode, context);
        } else if (line instanceof TieLine) {
            convertTieLine(ucteNetwork, (TieLine) line, context);
        } else {
            throw new IllegalStateException("Unexpected TieLine type: " + line.getClass());
        }
    }

    /**
     * Convert a {@link MergedXnode} to two {@link UcteLine} connected by a Xnode. Add the two {@link UcteLine} and the {@link UcteNode} to the network.
     *
     * @param ucteNetwork The target UcteNetwork
     * @param mergedXnode The MergedXnode extension used to create the XNode
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertTieLine(UcteNetwork ucteNetwork, MergedXnode mergedXnode, UcteExporterContext context) {
        Line line = mergedXnode.getExtendable();

        LOGGER.trace("Converting TieLine {}", line.getId());

        // Create XNode
        convertXNode(ucteNetwork, mergedXnode, context);

        // Create half line 1
        UcteElementId ucteElementId1 = context.getNamingStrategy().getUcteElementId(mergedXnode.getLine1Name());
        String elementName1 = line.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_1", null);
        UcteElementStatus status1 = line instanceof TieLine ? getStatusHalf((TieLine) line, Branch.Side.ONE) : getStatus(line, Branch.Side.ONE);
        UcteLine ucteLine1 = new UcteLine(
                ucteElementId1,
                status1,
                line.getR() * mergedXnode.getRdp(),
                line.getX() * mergedXnode.getXdp(),
                line.getB1(),
                line.getCurrentLimits1().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName1);
        ucteNetwork.addLine(ucteLine1);

        // Create half line2
        UcteElementId ucteElementId2 = context.getNamingStrategy().getUcteElementId(mergedXnode.getLine2Name());
        String elementName2 = line.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_2", null);
        UcteElementStatus status2 = line instanceof TieLine ? getStatusHalf((TieLine) line, Branch.Side.TWO) : getStatus(line, Branch.Side.TWO);
        UcteLine ucteLine2 = new UcteLine(
                ucteElementId2,
                status2,
                line.getR() * (1.0d - mergedXnode.getRdp()),
                line.getX() * (1.0d - mergedXnode.getXdp()),
                line.getB2(),
                line.getCurrentLimits2().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName2);
        ucteNetwork.addLine(ucteLine2);
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

        // Create half line 1
        TieLine.HalfLine half1 = tieLine.getHalf1();
        UcteElementId ucteElementId1 = context.getNamingStrategy().getUcteElementId(half1.getId());
        String elementName1 = tieLine.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_1", null);
        UcteElementStatus status1 = getStatusHalf(tieLine, Branch.Side.ONE);
        UcteLine ucteLine1 = new UcteLine(
                ucteElementId1,
                status1,
                half1.getR(),
                half1.getX(),
                half1.getB1() + half1.getB2(),
                tieLine.getCurrentLimits1().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName1);
        ucteNetwork.addLine(ucteLine1);

        // Create half line2
        TieLine.HalfLine half2 = tieLine.getHalf2();
        UcteElementId ucteElementId2 = context.getNamingStrategy().getUcteElementId(half2.getId());
        String elementName2 = tieLine.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_2", null);
        UcteElementStatus status2 = getStatusHalf(tieLine, Branch.Side.TWO);
        UcteLine ucteLine2 = new UcteLine(
                ucteElementId2,
                status2,
                half2.getR(),
                half2.getX(),
                half2.getB1() + half2.getB2(),
                tieLine.getCurrentLimits2().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName2);
        ucteNetwork.addLine(ucteLine2);
    }

    /**
     * Convert a {@link DanglingLine} object to an {@link UcteNode} and a {@link UcteLine} objects.
     *
     * @param ucteNetwork The target network in ucte
     * @param danglingLine The danglingLine to convert to UCTE
     * @param context The context used to store temporary data during the conversion
     */
    private static void convertDanglingLine(UcteNetwork ucteNetwork, DanglingLine danglingLine, UcteExporterContext context) {
        LOGGER.trace("Converting DanglingLine {}", danglingLine.getId());

        // Create XNode
        convertXNode(ucteNetwork, danglingLine, context);

        // Always create the XNode,
        // But do not export the dangling line if it was related to a YNode
        // The corresponding transformer will be connected to the XNode
        if (isDanglingLineYNode(danglingLine)) {
            LOGGER.warn("Ignoring DanglingLine at YNode in the export {}", danglingLine.getId());
            return;
        }

        // Create line
        UcteElementId elementId = context.getNamingStrategy().getUcteElementId(danglingLine);
        String elementName = danglingLine.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);
        UcteElementStatus ucteElementStatus = getStatus(danglingLine);

        UcteLine ucteLine = new UcteLine(
                elementId,
                ucteElementStatus,
                danglingLine.getR(),
                danglingLine.getX(),
                danglingLine.getB(),
                danglingLine.getCurrentLimits().map(l -> (int) l.getPermanentLimit()).orElse(null),
                elementName);
        ucteNetwork.addLine(ucteLine);
    }

    private static UcteNodeStatus getXnodeStatus(Identifiable<?> identifiable) {
        String statusNode = identifiable.getProperty(STATUS_PROPERTY_KEY + "_XNode");
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

    private static UcteElementStatus getStatus(Branch<?> branch, Branch.Side side) {
        if (branch.isFictitious()) {
            if (branch.getTerminal(side).isConnected()) {
                return UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            }
        } else {
            if (branch.getTerminal(side).isConnected()) {
                return UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION;
            }
        }
    }

    private static UcteElementStatus getStatusHalf(TieLine tieLine, Branch.Side side) {
        if (tieLine.getHalf(side).isFictitious()) {
            if (tieLine.getTerminal(side).isConnected()) {
                return UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            }
        } else {
            if (tieLine.getTerminal(side).isConnected()) {
                return UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION;
            }
        }
    }

    private static UcteElementStatus getStatus(DanglingLine danglingLine) {
        if (Boolean.parseBoolean(danglingLine.getProperty(IS_COUPLER_PROPERTY_KEY, "false"))) {
            if (danglingLine.getTerminal().isConnected()) {
                return UcteElementStatus.BUSBAR_COUPLER_IN_OPERATION;
            } else {
                return UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION;
            }
        }

        if (danglingLine.isFictitious()) {
            if (danglingLine.getTerminal().isConnected()) {
                return UcteElementStatus.EQUIVALENT_ELEMENT_IN_OPERATION;
            } else {
                return UcteElementStatus.EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            }
        } else {
            if (danglingLine.getTerminal().isConnected()) {
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
     * @see UcteExporter#convertRegulation(UcteNetwork, UcteElementId, TwoWindingsTransformer)
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

        convertRegulation(ucteNetwork, elementId, twoWindingsTransformer);
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
    private static void convertRegulation(UcteNetwork ucteNetwork, UcteElementId ucteElementId, TwoWindingsTransformer twoWindingsTransformer) {
        if (twoWindingsTransformer.hasRatioTapChanger() || twoWindingsTransformer.hasPhaseTapChanger()) {
            UctePhaseRegulation uctePhaseRegulation = twoWindingsTransformer.getOptionalRatioTapChanger()
                    .map(rtc -> convertRatioTapChanger(twoWindingsTransformer)).orElse(null);
            UcteAngleRegulation ucteAngleRegulation = twoWindingsTransformer.getOptionalPhaseTapChanger()
                    .map(ptc -> convertPhaseTapChanger(twoWindingsTransformer)).orElse(null);
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
        if (!Double.isNaN(twoWindingsTransformer.getRatioTapChanger().getTargetV())) {
            uctePhaseRegulation.setU(twoWindingsTransformer.getRatioTapChanger().getTargetV());
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
    private static UcteAngleRegulation convertPhaseTapChanger(TwoWindingsTransformer twoWindingsTransformer) {
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
            return new UcteAngleRegulation(calculateAsymmAngleDu(twoWindingsTransformer),
                    calculateAsymmAngleTheta(twoWindingsTransformer),
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

    private static void setSwitchCurrentLimit(UcteLine ucteLine, Switch sw) {
        if (sw.hasProperty(CURRENT_LIMIT_PROPERTY_KEY)) {
            try {
                ucteLine.setCurrentLimit(Integer.parseInt(sw.getProperty(CURRENT_LIMIT_PROPERTY_KEY)));
            } catch (NumberFormatException exception) {
                ucteLine.setCurrentLimit(null);
                LOGGER.warn("Switch {}: No current limit provided", sw.getId());
            }
        } else {
            ucteLine.setCurrentLimit(null);
            LOGGER.warn("Switch {}: No current limit provided", sw.getId());
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
            // (that is be the most common use case)
            return namingStrategies.get(0);
        } else {
            if (namingStrategies.size() > 1 && name == null) {
                // several naming strategies and no information to select which one to choose, we can only throw
                // an exception
                List<String> namingStrategyNames = namingStrategies.stream().map(NamingStrategy::getName).collect(Collectors.toList());
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
