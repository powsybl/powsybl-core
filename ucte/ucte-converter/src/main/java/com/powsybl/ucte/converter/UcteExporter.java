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
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
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
import static com.powsybl.ucte.network.UcteElementStatus.*;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(Exporter.class)
public class UcteExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExporter.class);

    public static final String NAMING_STRATEGY = "ucte.export.naming-strategy";

    private static final Parameter NAMING_STRATEGY_PARAMETER = new Parameter(NAMING_STRATEGY, ParameterType.STRING, "Default naming strategy for UCTE codes conversion", "Default");

    private static final Supplier<List<NamingStrategy>> NAMING_STRATEGY_SUPPLIERS
            = Suppliers.memoize(() -> new ServiceLoaderCache<>(NamingStrategy.class).getServices())::get;

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

        String namingStrategyName = ConversionParameters.readStringParameter(getFormat(), parameters, NAMING_STRATEGY_PARAMETER, defaultValueConfig);
        NamingStrategy namingStrategy = findNamingStrategy(namingStrategyName, NAMING_STRATEGY_SUPPLIERS.get());

        UcteNetwork ucteNetwork = createUcteNetwork(network, namingStrategy);

        try (OutputStream os = dataSource.newOutputStream(null, "uct", false);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            new UcteWriter(ucteNetwork).write(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
                    convertBus(ucteNetwork, bus, context);
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

        ucteNetwork.getComments().add("Generated by powsybl, " +  DateTime.now());
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
        // FIXME(mathbagu): how to find the slack node?
        UcteNode ucteNode = new UcteNode(
                ucteNodeCode,
                geographicalName,
                UcteNodeStatus.REAL,
                UcteNodeTypeCode.PQ,
                Float.NaN,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                null
        );
        ucteNetwork.addNode(ucteNode);

        convertLoads(ucteNode, bus);
        convertGenerators(ucteNode, bus);
    }

    /**
     * Initialize the power consumption fields from the loads connected to the specified bus.
     *
     * @param ucteNode The UCTE node to fill
     * @param bus The bus the loads are connected to
     */
    private static void convertLoads(UcteNode ucteNode, Bus bus) {
        float activeLoad = 0.0f;
        float reactiveLoad = 0.0f;
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
        float activePowerGeneration = -0.0f;
        float reactivePowerGeneration = -0.0f;
        float voltageReference = Float.NaN;
        float minP = Float.NaN;
        float maxP = Float.NaN;
        float minQ = Float.NaN;
        float maxQ = Float.NaN;
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
                voltageReference = (float) generator.getTargetV();
            }
            if (generator.isVoltageRegulatorOn()) {
                nodeType = UcteNodeTypeCode.PU;
            }
            minP = (float) generator.getMinP();
            maxP = (float) generator.getMaxP();
            // FIXME(mathbagu): how to get minQ and maxQ for an aggregated generator
            minQ = (float) generator.getReactiveLimits().getMinQ(activePowerGeneration);
            maxQ = (float) generator.getReactiveLimits().getMaxQ(activePowerGeneration);

            // FIXME(mathbagu): what if not all the generators have the same energy source?
            powerPlantType = energySourceToUctePowerPlantType(generator.getEnergySource());
        }
        ucteNode.setActivePowerGeneration(-activePowerGeneration);
        ucteNode.setReactivePowerGeneration(-reactivePowerGeneration);
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

        UcteNode ucteNode = convertXNode(ucteNetwork, xnodeCode, geographicalName);
        ucteNode.setActiveLoad((float) danglingLine.getP0());
        ucteNode.setReactiveLoad((float) danglingLine.getQ0());
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

        convertXNode(ucteNetwork, xnodeCode, geographicalName);
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

        convertXNode(ucteNetwork, xnodeCode, geographicalName);
    }

    /**
     * Create a {@link UcteNode} object from a {@link UcteNodeCode} object and an optional geographical name and add it to the {@link UcteNetwork}.
     * @param ucteNetwork The target network in ucte
     * @param xnodeCode The UCTE code of the XNode
     * @param geographicalName The geographical name of the XNode
     * @return the UcteNode
     */
    private static UcteNode convertXNode(UcteNetwork ucteNetwork, UcteNodeCode xnodeCode, String geographicalName) {
        if (xnodeCode.getUcteCountryCode() != UcteCountryCode.XX) {
            throw new UcteException("Invalid xnode code: " + xnodeCode);
        }

        UcteNode ucteNode = new UcteNode(
                xnodeCode,
                geographicalName,
                UcteNodeStatus.EQUIVALENT,
                UcteNodeTypeCode.PQ,
                Float.NaN,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
                Float.NaN,
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
        UcteElementStatus status = sw.isOpen() ? BUSBAR_COUPLER_OUT_OF_OPERATION : BUSBAR_COUPLER_IN_OPERATION;
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
        UcteElementStatus status = line.getTerminal1().isConnected() && line.getTerminal2().isConnected() ? REAL_ELEMENT_IN_OPERATION : REAL_ELEMENT_OUT_OF_OPERATION;
        String elementName = line.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);

        UcteLine ucteLine = new UcteLine(
                lineId,
                status,
                (float) line.getR(),
                (float) line.getX(),
                (float) line.getB1() + (float) line.getB2(),
                (int) getPermanentLimit(line),
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
            throw new AssertionError("Unexpected TieLine type: " + line.getClass());
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

        // FIXME(mathbagu) REAL or EQUIVALENT ?
        UcteElementStatus status = line.getTerminal1().isConnected() && line.getTerminal2().isConnected() ? EQUIVALENT_ELEMENT_IN_OPERATION : EQUIVALENT_ELEMENT_OUT_OF_OPERATION;

        // Create half line 1
        UcteElementId ucteElementId1 = context.getNamingStrategy().getUcteElementId(mergedXnode.getLine1Name());
        String elementName1 = line.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_1", null);
        UcteLine ucteLine1 = new UcteLine(
                ucteElementId1,
                status,
                (float) line.getR() * mergedXnode.getRdp(),
                (float) line.getX() * mergedXnode.getXdp(),
                (float) line.getB1(),
                (int) getPermanentLimit(line),
                elementName1);
        ucteNetwork.addLine(ucteLine1);

        // Create half line2
        UcteElementId ucteElementId2 = context.getNamingStrategy().getUcteElementId(mergedXnode.getLine2Name());
        String elementName2 = line.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_2", null);
        UcteLine ucteLine2 = new UcteLine(
                ucteElementId2,
                status,
                (float) line.getR() * (1.0f - mergedXnode.getRdp()),
                (float) line.getX() * (1.0f - mergedXnode.getXdp()),
                (float) line.getB2(),
                (int) getPermanentLimit(line),
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

        // FIXME(mathbagu) REAL or EQUIVALENT ?
        UcteElementStatus status = tieLine.getTerminal1().isConnected() && tieLine.getTerminal2().isConnected() ? EQUIVALENT_ELEMENT_IN_OPERATION : EQUIVALENT_ELEMENT_OUT_OF_OPERATION;

        // Create half line 1
        TieLine.HalfLine half1 = tieLine.getHalf1();
        UcteElementId ucteElementId1 = context.getNamingStrategy().getUcteElementId(half1.getId());
        String elementName1 = tieLine.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_1", null);
        UcteLine ucteLine1 = new UcteLine(
                ucteElementId1,
                status,
                (float) half1.getR(),
                (float) half1.getX(),
                (float) (half1.getB1() + half1.getB2()),
                (int) getPermanentLimit(tieLine),
                elementName1);
        ucteNetwork.addLine(ucteLine1);

        // Create half line2
        TieLine.HalfLine half2 = tieLine.getHalf2();
        UcteElementId ucteElementId2 = context.getNamingStrategy().getUcteElementId(half2.getId());
        String elementName2 = tieLine.getProperty(ELEMENT_NAME_PROPERTY_KEY + "_2", null);
        UcteLine ucteLine2 = new UcteLine(
                ucteElementId2,
                status,
                (float) half2.getR(),
                (float) half2.getX(),
                (float) (half2.getB1() + half2.getB2()),
                (int) getPermanentLimit(tieLine),
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

        // Create line
        UcteElementId elementId = context.getNamingStrategy().getUcteElementId(danglingLine);
        String elementName = danglingLine.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);
        double permanentLimit = danglingLine.getCurrentLimits() == null ? DEFAULT_MAX_CURRENT : danglingLine.getCurrentLimits().getPermanentLimit();

        UcteLine ucteLine = new UcteLine(
                elementId,
                getStatus(danglingLine),
                (float) danglingLine.getR(),
                (float) danglingLine.getX(),
                (float) danglingLine.getB(),
                (int) permanentLimit,
                elementName);
        ucteNetwork.addLine(ucteLine);
    }

    /**
     * Convert the connection status to an UcteElementStatus value.
     * Some dangling lines are defined as coupler in the original UCTE file. In that case, the R and X of the dangling line are set to 0.
     * @param dl The dangling line.
     * @return The connection status of the specified dangling line.
     */
    private static UcteElementStatus getStatus(DanglingLine dl) {
        if (dl.getR() == 0.0 && dl.getX() == 0.0) {
            return dl.getTerminal().isConnected() ? BUSBAR_COUPLER_IN_OPERATION : BUSBAR_COUPLER_OUT_OF_OPERATION;
        }
        return dl.getTerminal().isConnected() ? EQUIVALENT_ELEMENT_IN_OPERATION : EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
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
        UcteElementId elementId = context.getNamingStrategy().getUcteElementId(twoWindingsTransformer);
        UcteElementStatus status = twoWindingsTransformer.getTerminal1().isConnected() && twoWindingsTransformer.getTerminal2().isConnected() ? REAL_ELEMENT_IN_OPERATION : REAL_ELEMENT_OUT_OF_OPERATION;
        String elementName = twoWindingsTransformer.getProperty(ELEMENT_NAME_PROPERTY_KEY, null);
        double currentLimits = getPermanentLimit(twoWindingsTransformer);
        float nominalPower = Float.parseFloat(twoWindingsTransformer.getProperty(NOMINAL_POWER_KEY, null));

        UcteTransformer ucteTransformer = new UcteTransformer(
                elementId,
                status,
                (float) twoWindingsTransformer.getR(),
                (float) twoWindingsTransformer.getX(),
                (float) twoWindingsTransformer.getB(),
                (int) currentLimits,
                elementName,
                (float) twoWindingsTransformer.getRatedU2(),
                (float) twoWindingsTransformer.getRatedU1(),
                nominalPower,
                (float) twoWindingsTransformer.getG());
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
        if (twoWindingsTransformer.getRatioTapChanger() != null || twoWindingsTransformer.getPhaseTapChanger() != null) {
            UctePhaseRegulation uctePhaseRegulation = null;
            UcteAngleRegulation ucteAngleRegulation = null;
            if (twoWindingsTransformer.getRatioTapChanger() != null) {
                uctePhaseRegulation = convertRatioTapChanger(twoWindingsTransformer);
            }
            if (twoWindingsTransformer.getPhaseTapChanger() != null) {
                ucteAngleRegulation = convertPhaseTapChanger(twoWindingsTransformer);
            }
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

        float du = (float) calculatePhaseDu(twoWindingsTransformer);
        UctePhaseRegulation uctePhaseRegulation = new UctePhaseRegulation(
                du,
                twoWindingsTransformer.getRatioTapChanger().getHighTapPosition(),
                twoWindingsTransformer.getRatioTapChanger().getTapPosition(),
                Float.NaN);
        if (!Double.isNaN(twoWindingsTransformer.getRatioTapChanger().getTargetV())) {
            uctePhaseRegulation.setU((float) twoWindingsTransformer.getRatioTapChanger().getTargetV());
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
            return new UcteAngleRegulation((float) calculateSymmAngleDu(twoWindingsTransformer),
                    90,
                    twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(),
                    twoWindingsTransformer.getPhaseTapChanger().getTapPosition(),
                    calculateAngleP(twoWindingsTransformer),
                    ucteAngleRegulationType);
        } else {
            return new UcteAngleRegulation((float) calculateAsymmAngleDu(twoWindingsTransformer),
                    (float) calculateAsymmAngleTheta(twoWindingsTransformer),
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
    private static float calculateAngleP(TwoWindingsTransformer twoWindingsTransformer) {
        return (float) twoWindingsTransformer.getPhaseTapChanger().getRegulationValue();
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
                ucteLine.setCurrentLimit((int) DEFAULT_MAX_CURRENT);
                LOGGER.warn("Switch {}: No current limit, set value to {}", sw.getId(), DEFAULT_MAX_CURRENT);
            }
        } else {
            ucteLine.setCurrentLimit((int) DEFAULT_MAX_CURRENT);
            LOGGER.warn("Switch {}: No current limit, set value to {}", sw.getId(), DEFAULT_MAX_CURRENT);
        }
    }

    private static UctePowerPlantType energySourceToUctePowerPlantType(EnergySource energySource) {
        switch (energySource) {
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

    private static double getPermanentLimit(Branch branch) {
        double permanentLimit1 = Optional.ofNullable(branch.getCurrentLimits1()).map(CurrentLimits::getPermanentLimit).orElse(DEFAULT_MAX_CURRENT);
        double permanentLimit2 = Optional.ofNullable(branch.getCurrentLimits2()).map(CurrentLimits::getPermanentLimit).orElse(DEFAULT_MAX_CURRENT);

        return Double.min(permanentLimit1, permanentLimit2);
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
