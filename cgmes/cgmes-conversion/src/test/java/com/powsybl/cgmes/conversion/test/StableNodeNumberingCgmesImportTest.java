/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ZipArchiveDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Node;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue <a href="https://github.com/powsybl/powsybl-core/issues/3927">#3927</a>: the IIDM nodes, and therefore the
 * bus IDs, of a node-breaker CGMES import must not depend on the order of the elements in the CGMES files.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class StableNodeNumberingCgmesImportTest extends AbstractSerDeTest {

    private static final Predicate<String> ALL_FILES = name -> true;
    private static final Map<String, Predicate<String>> PROFILES = new LinkedHashMap<>();

    static {
        PROFILES.put("EQ", profile("EQ"));
        PROFILES.put("TP", profile("TP"));
        PROFILES.put("SSH", profile("SSH"));
        PROFILES.put("SV", profile("SV"));
        PROFILES.put("ALL", ALL_FILES);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void issue3927DisconnectedLoadBusBreakerRoundtrip(boolean createBusbarSectionForEveryConnectivityNode) {
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.TOPOLOGY_KIND, "NODE_BREAKER");
        createDisconnectedLoadBBNetwork().write("CGMES", exportParams, new ZipArchiveDataSource(tmpDir, "disconnected_load_bb"));
        ReadOnlyDataSource exported = DataSource.fromPath(tmpDir.resolve("disconnected_load_bb.zip"));
        Properties importParams = importParams(createBusbarSectionForEveryConnectivityNode);

        Network network = Network.read(exported, importParams);
        String[] expectedBusIds = createBusbarSectionForEveryConnectivityNode
                ? new String[] {"voltageLevel1_0", "voltageLevel1_2"}
                : new String[] {"voltageLevel1_0", "voltageLevel1_1"};
        assertArrayEquals(expectedBusIds, busBreakerViewBusIds(network));

        // Same exported files, the connectables being written in another order
        for (Map.Entry<String, Predicate<String>> profile : PROFILES.entrySet()) {
            for (UnaryOperator<List<Node>> reordering : reorderings()) {
                ReorderedCgmesDataSource reordered = new ReorderedCgmesDataSource(exported, profile.getValue(), reordering);
                Network reorderedNetwork = Network.read(reordered, importParams);
                assertTrue(reordered.getReorderedFiles() > 0, profile.getKey());
                assertArrayEquals(expectedBusIds, busBreakerViewBusIds(reorderedNetwork), profile.getKey());
                assertEquals(topology(network), topology(reorderedNetwork), profile.getKey());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"miniNodeBreaker", "smallNodeBreakerHvdc", "miniNodeBreakerSvInjection"})
    void nodeNumberingDoesNotDependOnElementOrder(String gridModel) {
        for (boolean createBusbarSectionForEveryConnectivityNode : new boolean[] {false, true}) {
            Properties importParams = importParams(createBusbarSectionForEveryConnectivityNode);
            Map<String, String> expected = topology(Network.read(dataSource(gridModel), importParams));
            for (Map.Entry<String, Predicate<String>> profile : PROFILES.entrySet()) {
                for (UnaryOperator<List<Node>> reordering : reorderings()) {
                    ReorderedCgmesDataSource reordered = new ReorderedCgmesDataSource(dataSource(gridModel), profile.getValue(), reordering);
                    Map<String, String> actual = topology(Network.read(reordered, importParams));
                    assertTrue(reordered.getReorderedFiles() > 0, profile.getKey());
                    assertEquals(expected, actual, gridModel + " " + profile.getKey() + " busbars=" + createBusbarSectionForEveryConnectivityNode);
                }
            }
        }
    }

    private static Predicate<String> profile(String profile) {
        return name -> name.toUpperCase(Locale.ROOT).matches(".*_" + profile + "(_.*)?\\.XML");
    }

    private static List<UnaryOperator<List<Node>>> reorderings() {
        return List.of(
                nodes -> {
                    Collections.reverse(nodes);
                    return nodes;
                },
                nodes -> {
                    Collections.shuffle(nodes, new Random(3927));
                    return nodes;
                });
    }

    private static ReadOnlyDataSource dataSource(String gridModel) {
        return switch (gridModel) {
            case "miniNodeBreaker" -> CgmesConformity1Catalog.miniNodeBreaker().dataSource();
            case "smallNodeBreakerHvdc" -> CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource();
            case "miniNodeBreakerSvInjection" -> CgmesConformity1ModifiedCatalog.miniNodeBreakerSvInjection().dataSource();
            default -> throw new IllegalArgumentException(gridModel);
        };
    }

    private static Properties importParams(boolean createBusbarSectionForEveryConnectivityNode) {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        importParams.put(CgmesImport.CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE,
                Boolean.toString(createBusbarSectionForEveryConnectivityNode));
        return importParams;
    }

    private static String[] busBreakerViewBusIds(Network network) {
        return network.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray(String[]::new);
    }

    /**
     * Describe the topology of a network: nodes and buses of every terminal, switches and internal connections.
     */
    private static Map<String, String> topology(Network network) {
        Map<String, String> topology = new TreeMap<>();
        for (Connectable<?> connectable : network.getConnectables()) {
            List<? extends Terminal> terminals = connectable.getTerminals();
            for (int i = 0; i < terminals.size(); i++) {
                Terminal terminal = terminals.get(i);
                String node = terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER
                        ? Integer.toString(terminal.getNodeBreakerView().getNode())
                        : "-";
                topology.put("terminal " + connectable.getId() + " " + i, terminal.getVoltageLevel().getId()
                        + " node=" + node
                        + " busBreakerViewBus=" + busId(terminal.getBusBreakerView().getBus())
                        + " busViewBus=" + busId(terminal.getBusView().getBus()));
            }
        }
        for (VoltageLevel voltageLevel : network.getVoltageLevels()) {
            if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
                VoltageLevel.NodeBreakerView view = voltageLevel.getNodeBreakerView();
                view.getSwitchStream().forEach(sw -> topology.put("switch " + sw.getId(),
                        view.getNode1(sw.getId()) + "-" + view.getNode2(sw.getId()) + " open=" + sw.isOpen()));
                topology.put("internalConnections " + voltageLevel.getId(), view.getInternalConnectionStream()
                        .map(ic -> Math.min(ic.getNode1(), ic.getNode2()) + "-" + Math.max(ic.getNode1(), ic.getNode2()))
                        .sorted()
                        .toList()
                        .toString());
            }
        }
        topology.put("busBreakerViewBuses", Arrays.toString(busBreakerViewBusIds(network)));
        topology.put("busViewBuses", network.getBusView().getBusStream().map(Bus::getId).sorted().toList().toString());
        return topology;
    }

    private static String busId(Bus bus) {
        return bus != null ? bus.getId() : null;
    }

    /**
     * The network described in issue #3927: a bus with a connected load and a disconnected one.
     */
    private static Network createDisconnectedLoadBBNetwork() {
        Network network = Network.create("disconnected_load_bb", "iidm");
        VoltageLevel voltageLevel = network.newSubstation()
                .setId("substation1")
                .setCountry(Country.FR)
                .add()
                .newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus().setId("voltageLevel1Bus1").add();
        for (String load : List.of("load1", "load2")) {
            LoadAdder adder = voltageLevel.newLoad()
                    .setId(load)
                    .setP0(10)
                    .setQ0(5)
                    .setConnectableBus("voltageLevel1Bus1");
            if ("load1".equals(load)) {
                adder.setBus("voltageLevel1Bus1");
            }
            adder.add();
        }
        return network;
    }
}
