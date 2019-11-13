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
import com.powsybl.ieeecdf.model.IeeeCdfBranch;
import com.powsybl.ieeecdf.model.IeeeCdfBus;
import com.powsybl.ieeecdf.model.IeeeCdfModel;
import com.powsybl.ieeecdf.model.IeeeCdfReader;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.parameters.Parameter;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.io.*;
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

    private ContainersMapping findContainerMapping(IeeeCdfModel ieeeCdfModel) {
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

    private Map<Integer, IeeeCdfBus> createBuses(IeeeCdfModel ieeeCdfModel, ContainersMapping containerMapping,
                                                 Network network) {
        Map<Integer, IeeeCdfBus> ieeeCdfBusesByNum = new HashMap<>();
        for (IeeeCdfBus ieeeCdfBus : ieeeCdfModel.getBuses()) {
            ieeeCdfBusesByNum.put(ieeeCdfBus.getNumber(), ieeeCdfBus);

            String voltageLevelId = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBus.getNumber());
            String substationId = containerMapping.voltageLevelIdToSubstationId.get(voltageLevelId);

            // create substation
            Substation substation = network.getSubstation(substationId);
            if (substation == null) {
                substation = network.newSubstation()
                        .setId(substationId)
                        .add();
            }

            // create voltage level
            double nominalV = ieeeCdfBus.getBaseVoltage() == 0 ? 1 : ieeeCdfBus.getBaseVoltage();
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                voltageLevel = substation.newVoltageLevel()
                        .setId(voltageLevelId)
                        .setNominalV(nominalV)
                        .setTopologyKind(TopologyKind.BUS_BREAKER)
                        .add();
            }

            // create bus
            Bus bus = voltageLevel.getBusBreakerView().newBus()
                    .setId(getBusId(ieeeCdfBus.getNumber()))
                    .setName(ieeeCdfBus.getName())
                    .add();
            bus.setV(ieeeCdfBus.getFinalVoltage())
                    .setAngle(ieeeCdfBus.getFinalAngle());

            // create injections
            switch (ieeeCdfBus.getType()) {
                case UNREGULATED:
                    // nothing to do
                    break;
                case HOLD_MVAR_GENERATION_WITHIN_VOLTAGE_LIMITS:
                    break;
                case HOLD_VOLTAGE_WITHIN_VAR_LIMITS:
                case HOLD_VOLTAGE_AND_ANGLE:
                    voltageLevel.newGenerator()
                            .setId(ieeeCdfBus.getName() + "G")
                            .setConnectableBus(bus.getId())
                            .setBus(bus.getId())
                            .setTargetP(ieeeCdfBus.getActiveGeneration())
                            .setTargetV(ieeeCdfBus.getDesiredVoltage() * nominalV)
                            .setVoltageRegulatorOn(true)
                            .setMaxP(Double.MAX_VALUE)
                            .setMinP(-Double.MAX_VALUE)
                            .add();
                    break;
                default:
                    throw new IllegalStateException("Unexpected bus type: " + ieeeCdfBus.getType());
            }
        }
        return ieeeCdfBusesByNum;
    }

    private void createLine(IeeeCdfBranch ieeeCdfBranch, ContainersMapping containerMapping, Network network) {
        String id = "L" + ieeeCdfBranch.getTapBusNumber() + "-" + ieeeCdfBranch.getzBusNumber();
        String bus1Id = getBusId(ieeeCdfBranch.getTapBusNumber());
        String bus2Id = getBusId(ieeeCdfBranch.getzBusNumber());
        String voltageLevelId1 = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getTapBusNumber());
        String voltageLevelId2 = containerMapping.busNumToVoltageLevelId.get(ieeeCdfBranch.getzBusNumber());
        network.newLine()
                .setId(id)
                .setBus1(bus1Id)
                .setConnectableBus1(bus1Id)
                .setVoltageLevel1(voltageLevelId1)
                .setBus2(bus2Id)
                .setConnectableBus2(bus2Id)
                .setVoltageLevel2(voltageLevelId2)
                .setR(ieeeCdfBranch.getResistance())
                .setX(ieeeCdfBranch.getReactance())
                .setG1(0)
                .setB1(ieeeCdfBranch.getChargingSusceptance() / 2)
                .setG2(0)
                .setB2(ieeeCdfBranch.getChargingSusceptance() / 2)
                .add();
    }

    private void createBranches(IeeeCdfModel ieeeCdfModel, Map<Integer, IeeeCdfBus> ieeeCdfBusesByNum,
                                ContainersMapping containerMapping, Network network) {
        for (IeeeCdfBranch ieeeCdfBranch : ieeeCdfModel.getBranches()) {
            switch (ieeeCdfBranch.getType()) {
                case TRANSMISSION_LINE:
                    createLine(ieeeCdfBranch, containerMapping, network);
                    break;
                case FIXED_TAP:
                    break;
                case VARIABLE_TAP_FOR_VOLTAVE_CONTROL:
                    break;
                case VARIABLE_TAP_FOR_REACTIVE_POWER_CONTROL:
                    break;
                case VARIABLE_PHASE_ANGLE_FOR_ACTIVE_POWER_CONTROL:
                    break;
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
            IeeeCdfModel ieeeCdfModel = new IeeeCdfReader().read(reader);
            ContainersMapping containerMapping = findContainerMapping(ieeeCdfModel);
            Map<Integer, IeeeCdfBus> ieeeCdfBusesByNum = createBuses(ieeeCdfModel, containerMapping, network);
            createBranches(ieeeCdfModel, ieeeCdfBusesByNum, containerMapping, network);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return network;
    }
}
