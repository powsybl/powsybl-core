/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.converter;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.ieeecdf.model.*;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.parameters.Parameter;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.joda.time.DateTime;

import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IeeeCdfImporter implements Importer {

    private static final String FORMAT = "IEEE CDF";
    private static final String EXT = "txt";

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getComment() {
        return "IEEE Common Data Format to IIDM converter";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            if (dataSource.exists(null, EXT)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, EXT)))) {
                    String titleLine = reader.readLine();
                    if (titleLine != null) {
                        return titleLine.length() >= 44
                                && titleLine.charAt(3) == '/'
                                && titleLine.charAt(6) == '/'
                                && (titleLine.charAt(43) == 'S' || titleLine.charAt(43) == 'W');
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return false;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            try (InputStream is = fromDataSource.newInputStream(null, EXT);
                 OutputStream os = toDataSource.newOutputStream(null, EXT, false)) {
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class ContainersMapping {

        private final Map<Integer, String> busNumToVoltageLevelId = new HashMap<>();

        private final Map<String, Set<Integer>> voltageLevelIdToBusNums = new HashMap<>();

        private final Map<String, String> voltageLevelIdToSubstationId = new HashMap<>();
    }

    private static ContainersMapping findContainerMapping(IeeeCdfModel ieeeCdfModel) {
        ContainersMapping containersMapping = new ContainersMapping();

        // group buses connected to non impedant lines to voltage levels
        UndirectedGraph<Integer, Object> vlGraph = new Pseudograph<>(Object.class);
        for (IeeeCdfBus ieeeCdfBus : ieeeCdfModel.getBuses()) {
            vlGraph.addVertex(ieeeCdfBus.getNumber());
        }
        for (IeeeCdfBranch ieeeCdfBranch : ieeeCdfModel.getBranches()) {
            if (ieeeCdfBranch.getResistance() == 0 && ieeeCdfBranch.getReactance() == 0) {
                vlGraph.addEdge(ieeeCdfBranch.getTapBusNumber(), ieeeCdfBranch.getzBusNumber());
            }
        }
        int voltageLevelNum = 1;
        for (Set<Integer> busNums : new ConnectivityInspector<>(vlGraph).connectedSets()) {
            String voltageLevelId = "VL" + voltageLevelNum++;
            containersMapping.voltageLevelIdToBusNums.put(voltageLevelId, busNums);
            for (int busNum : busNums) {
                containersMapping.busNumToVoltageLevelId.put(busNum, voltageLevelId);
            }
        }

        // group voltage levels connected by transformers to substations
        UndirectedGraph<String, Object> sGraph = new Pseudograph<>(Object.class);
        for (String voltageLevelId : containersMapping.voltageLevelIdToBusNums.keySet()) {
            sGraph.addVertex(voltageLevelId);
        }
        for (IeeeCdfBranch ieeeCdfBranch : ieeeCdfModel.getBranches()) {
            if (ieeeCdfBranch.getType() != IeeeCdfBranch.Type.TRANSMISSION_LINE) {
                sGraph.addEdge(containersMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getTapBusNumber()),
                        containersMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getzBusNumber()));
            }
        }
        int substationNum = 1;
        for (Set<String> voltageLevelIds : new ConnectivityInspector<>(sGraph).connectedSets()) {
            String substationId = "S" + substationNum++;
            for (String voltageLevelId : voltageLevelIds) {
                containersMapping.voltageLevelIdToSubstationId.put(voltageLevelId, substationId);
            }
        }

        return containersMapping;
    }

    private static String getBusId(int busNum) {
        return "B" + busNum;
    }

    private static void createBuses(IeeeCdfModel ieeeCdfModel, ContainersMapping containerMapping, double sb, Network network) {
        for (IeeeCdfBus ieeeCdfBus : ieeeCdfModel.getBuses()) {
            String voltageLevelId = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBus.getNumber());
            String substationId = containerMapping.voltageLevelIdToSubstationId.get(voltageLevelId);

            // create substation
            Substation substation = createSubstation(network, substationId);

            // create voltage level
            VoltageLevel voltageLevel = createVoltageLevel(ieeeCdfBus, voltageLevelId, substation, network);

            // create bus
            createBus(ieeeCdfBus, voltageLevel);

            // create load
            createLoad(ieeeCdfBus, voltageLevel);

            // create shunt compensator
            createShuntCompensator(ieeeCdfBus, sb, voltageLevel);

            // create generator
            switch (ieeeCdfBus.getType()) {
                case UNREGULATED:
                    // nothing to do
                    break;

                case HOLD_MVAR_GENERATION_WITHIN_VOLTAGE_LIMITS:
                    newGeneratorAdder(ieeeCdfBus, voltageLevel)
                            .setVoltageRegulatorOn(false)
                            .add();
                    break;

                case HOLD_VOLTAGE_WITHIN_VAR_LIMITS:
                case HOLD_VOLTAGE_AND_ANGLE:
                    Generator generator = newGeneratorAdder(ieeeCdfBus, voltageLevel)
                            .setTargetV(ieeeCdfBus.getDesiredVoltage() * voltageLevel.getNominalV())
                            .setVoltageRegulatorOn(true)
                            .add();
                    if (ieeeCdfBus.getMinReactivePowerOrVoltageLimit() != 0 || ieeeCdfBus.getMaxReactivePowerOrVoltageLimit() != 0) {
                        generator.newMinMaxReactiveLimits()
                                .setMinQ(ieeeCdfBus.getMinReactivePowerOrVoltageLimit())
                                .setMaxQ(ieeeCdfBus.getMaxReactivePowerOrVoltageLimit())
                                .add();
                    }
                    break;

                default:
                    throw new IllegalStateException("Unexpected bus type: " + ieeeCdfBus.getType());
            }
        }
    }

    private static Bus createBus(IeeeCdfBus ieeeCdfBus, VoltageLevel voltageLevel) {
        String busId = getBusId(ieeeCdfBus.getNumber());
        Bus bus = voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .setName(ieeeCdfBus.getName())
                .add();
        bus.setV(ieeeCdfBus.getFinalVoltage() * voltageLevel.getNominalV())
                .setAngle(ieeeCdfBus.getFinalAngle());
        return bus;
    }

    private static Substation createSubstation(Network network, String substationId) {
        Substation substation = network.getSubstation(substationId);
        if (substation == null) {
            substation = network.newSubstation()
                    .setId(substationId)
                    .add();
        }
        return substation;
    }

    private static VoltageLevel createVoltageLevel(IeeeCdfBus ieeeCdfBus, String voltageLevelId, Substation substation, Network network) {
        double nominalV = ieeeCdfBus.getBaseVoltage() == 0 ? 1 : ieeeCdfBus.getBaseVoltage();
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            voltageLevel = substation.newVoltageLevel()
                    .setId(voltageLevelId)
                    .setNominalV(nominalV)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .add();
        }
        return voltageLevel;
    }

    private static void createLoad(IeeeCdfBus ieeeCdfBus, VoltageLevel voltageLevel) {
        if (ieeeCdfBus.getActiveLoad() != 0 || ieeeCdfBus.getReactiveLoad() != 0) {
            String busId = getBusId(ieeeCdfBus.getNumber());
            voltageLevel.newLoad()
                    .setId(busId + "-L")
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setP0(ieeeCdfBus.getActiveLoad())
                    .setQ0(ieeeCdfBus.getReactiveLoad())
                    .add();
        }
    }

    private static GeneratorAdder newGeneratorAdder(IeeeCdfBus ieeeCdfBus, VoltageLevel voltageLevel) {
        String busId = getBusId(ieeeCdfBus.getNumber());
        return voltageLevel.newGenerator()
                .setId(busId + "-G")
                .setConnectableBus(busId)
                .setBus(busId)
                .setTargetP(ieeeCdfBus.getActiveGeneration())
                .setMaxP(Double.MAX_VALUE)
                .setMinP(-Double.MAX_VALUE);
    }

    private static void createShuntCompensator(IeeeCdfBus ieeeCdfBus, double sb, VoltageLevel voltageLevel) {
        if (ieeeCdfBus.getShuntSusceptance() != 0) {
            String busId = getBusId(ieeeCdfBus.getNumber());
            double zb = Math.pow(voltageLevel.getNominalV(), 2) / sb;
            voltageLevel.newShuntCompensator()
                    .setId(busId + "-SH")
                    .setConnectableBus(busId)
                    .setBus(busId)
                    .setbPerSection(ieeeCdfBus.getShuntSusceptance() / zb)
                    .setCurrentSectionCount(0)
                    .setMaximumSectionCount(1)
                    .add();
        }
    }

    private static void createLine(IeeeCdfBranch ieeeCdfBranch, ContainersMapping containerMapping, double sb, Network network) {
        String id = "L" + ieeeCdfBranch.getTapBusNumber() + "-" + ieeeCdfBranch.getzBusNumber();
        String bus1Id = getBusId(ieeeCdfBranch.getTapBusNumber());
        String bus2Id = getBusId(ieeeCdfBranch.getzBusNumber());
        String voltageLevelId1 = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getTapBusNumber());
        String voltageLevelId2 = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getzBusNumber());
        VoltageLevel voltageLevel2 = network.getVoltageLevel(voltageLevelId2);
        double zb = Math.pow(voltageLevel2.getNominalV(), 2) / sb;
        network.newLine()
                .setId(id)
                .setBus1(bus1Id)
                .setConnectableBus1(bus1Id)
                .setVoltageLevel1(voltageLevelId1)
                .setBus2(bus2Id)
                .setConnectableBus2(bus2Id)
                .setVoltageLevel2(voltageLevelId2)
                .setR(ieeeCdfBranch.getResistance() * zb)
                .setX(ieeeCdfBranch.getReactance() * zb)
                .setG1(0)
                .setB1(ieeeCdfBranch.getChargingSusceptance() / zb / 2)
                .setG2(0)
                .setB2(ieeeCdfBranch.getChargingSusceptance() / zb / 2)
                .add();
    }

    private static void createBranches(IeeeCdfModel ieeeCdfModel, ContainersMapping containerMapping, double sb, Network network) {
        for (IeeeCdfBranch ieeeCdfBranch : ieeeCdfModel.getBranches()) {
            switch (ieeeCdfBranch.getType()) {
                case TRANSMISSION_LINE:
                    createLine(ieeeCdfBranch, containerMapping, sb, network);
                    break;

                case FIXED_TAP:
                case VARIABLE_TAP_FOR_VOLTAVE_CONTROL:
                case VARIABLE_TAP_FOR_REACTIVE_POWER_CONTROL:
                case VARIABLE_PHASE_ANGLE_FOR_ACTIVE_POWER_CONTROL:
                    throw new UnsupportedOperationException("Transformers not yet implemented");

                default:
                    throw new IllegalStateException("Unexpected branch type: " + ieeeCdfBranch.getType());
            }
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, EXT)))) {
            // parse file
            IeeeCdfModel ieeeCdfModel = new IeeeCdfReader().read(reader);

            // set date and time
            IeeeCdfTitle ieeeCdfTitle = ieeeCdfModel.getTitle();
            ZonedDateTime caseDateTime = ieeeCdfTitle.getDate().atStartOfDay(ZoneOffset.UTC.normalized());
            network.setCaseDate(new DateTime(caseDateTime.toInstant().toEpochMilli()));

            // build container to fit IIDM requirements
            ContainersMapping containerMapping = findContainerMapping(ieeeCdfModel);

            // create objects
            createBuses(ieeeCdfModel, containerMapping, ieeeCdfTitle.getMvaBase(), network);
            createBranches(ieeeCdfModel, containerMapping, ieeeCdfTitle.getMvaBase(), network);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return network;
    }
}
