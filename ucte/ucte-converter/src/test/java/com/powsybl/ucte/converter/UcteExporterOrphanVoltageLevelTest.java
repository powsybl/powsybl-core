/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.ucte.converter.OrphanVoltageLevelNetworks.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Export of voltage levels without substation: the file must contain them and be importable again.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class UcteExporterOrphanVoltageLevelTest {

    private static final String DEFAULT = "Default";
    private static final String COUNTER = "Counter";

    /** Exports with the given naming strategy, then imports the written file back. */
    private static Network exportAndReimport(Network network, String namingStrategy) {
        Properties parameters = new Properties();
        parameters.put(UcteExporter.NAMING_STRATEGY, namingStrategy);
        MemDataSource exported = new MemDataSource();
        new UcteExporter().export(network, parameters, exported, ReportNode.NO_OP);

        ReadOnlyMemDataSource toImport = new ReadOnlyMemDataSource("reimport");
        toImport.putData("reimport.uct", exported.getData(null, "uct"));
        return new UcteImporter().importData(toImport, NetworkFactory.findDefault(), null);
    }

    private static Set<String> busIds(Network network) {
        return network.getBusBreakerView().getBusStream().map(Identifiable::getId).collect(Collectors.toSet());
    }

    /** Counter naming: there are {@code nodeCount} nodes, all Dutch. */
    private static void assertDutchNodes(String topology, Network reimported, int nodeCount) {
        Set<String> busIds = busIds(reimported);
        assertEquals(nodeCount, busIds.size(), () -> topology + ": every node, including those of voltage levels without substation, must be exported. Nodes: " + busIds);
        assertTrue(busIds.stream().allMatch(id -> id.startsWith("N")), () -> topology + ": every node must get the Dutch country code 'N'. Nodes: " + busIds);
    }

    private static void assertLineCount(String topology, Network reimported, int lineCount) {
        assertEquals(lineCount, reimported.getLineCount(), () -> topology + ": every line must be exported and importable again");
    }

    @Test
    @DisplayName("Default naming, T-line: the junction node is exported, named after its bus id")
    void tLineDefault() {
        Network reimported = exportAndReimport(tLine(Ids.UCTE, Country.NL), DEFAULT);
        assertEquals(Set.of("NAAAAA11", "NBBBBB11", "NCCCCC11", "NTJUNC11"), busIds(reimported),
                "T-line: the junction node must be exported, named after its bus id");
        assertLineCount("T-line", reimported, 3);
    }

    @Test
    @DisplayName("Counter naming, T-line: the junction node is exported with the country of its neighbours")
    void tLineCounter() {
        Network reimported = exportAndReimport(tLine(Ids.FREE, Country.NL), COUNTER);
        assertDutchNodes("T-line", reimported, 4);
        assertLineCount("T-line", reimported, 3);
    }

    @Test
    @DisplayName("Default naming, chained segments in one voltage level: both intermediate nodes are exported")
    void chainedSegmentsOneVoltageLevelDefault() {
        Network reimported = exportAndReimport(chainedSegmentsOneVoltageLevel(Ids.UCTE), DEFAULT);
        assertEquals(Set.of("NAAAAA11", "NBBBBB11", "NSEGMT11", "NSEGMT12"), busIds(reimported),
                "Chained segments in one voltage level: both intermediate nodes must be exported");
        assertLineCount("Chained segments in one voltage level", reimported, 3);
    }

    @Test
    @DisplayName("Counter naming, chained segments in one voltage level: both intermediate nodes are exported")
    void chainedSegmentsOneVoltageLevelCounter() {
        Network reimported = exportAndReimport(chainedSegmentsOneVoltageLevel(Ids.FREE), COUNTER);
        assertDutchNodes("Chained segments in one voltage level", reimported, 4);
        assertLineCount("Chained segments in one voltage level", reimported, 3);
    }

    @Test
    @DisplayName("Default naming, chained segments in two voltage levels: both intermediate nodes are exported")
    void chainedSegmentsTwoVoltageLevelsDefault() {
        Network reimported = exportAndReimport(chainedSegmentsTwoVoltageLevels(Ids.UCTE), DEFAULT);
        assertEquals(Set.of("NAAAAA11", "NBBBBB11", "NSEGAA11", "NSEGBB11"), busIds(reimported),
                "Chained segments in two voltage levels: both intermediate nodes must be exported");
        assertLineCount("Chained segments in two voltage levels", reimported, 3);
    }

    @Test
    @DisplayName("Counter naming, chained segments in two voltage levels: both intermediate nodes are exported")
    void chainedSegmentsTwoVoltageLevelsCounter() {
        Network reimported = exportAndReimport(chainedSegmentsTwoVoltageLevels(Ids.FREE), COUNTER);
        assertDutchNodes("Chained segments in two voltage levels", reimported, 4);
        assertLineCount("Chained segments in two voltage levels", reimported, 3);
    }

    @Test
    @DisplayName("Counter naming, T-line connected to two countries: the junction's country is ambiguous, the export fails")
    void tLineAcrossTwoCountriesCounter() {
        Network network = tLine(Ids.FREE, Country.BE);
        UcteException e = assertThrows(UcteException.class, () -> exportAndReimport(network, COUNTER),
                "T-line connected to two countries: the junction's country is ambiguous, the export must fail");
        assertEquals("Cannot determine the country of voltage levels [VL_TJUNC]: they are connected to substations of several countries [BE, NL]",
                e.getMessage());
    }

    @Test
    @DisplayName("Default naming, voltage level without substation holding a switch and a load: nodes, switch and load are exported")
    void orphanWithSwitchAndLoadDefault() {
        Network reimported = exportAndReimport(orphanWithSwitchAndLoad(Ids.UCTE), DEFAULT);
        assertEquals(Set.of("NAAAAA11", "NORPHA11", "NORPHA12"), busIds(reimported),
                "Switch and load: both nodes of the voltage level without substation must be exported");
        assertEquals(1, reimported.getSwitchCount(), "Switch and load: the switch must be exported");
        assertEquals(42, reimported.getBusBreakerView().getBus("NORPHA11").getLoadStream().mapToDouble(Load::getP0).sum(),
                "Switch and load: the load must be exported on its node");
    }

    @Test
    @DisplayName("Counter naming, voltage level without substation holding a switch and a load: nodes, switch and load are exported")
    void orphanWithSwitchAndLoadCounter() {
        Network reimported = exportAndReimport(orphanWithSwitchAndLoad(Ids.FREE), COUNTER);
        assertDutchNodes("Switch and load", reimported, 3);
        assertEquals(1, reimported.getSwitchCount(), "Switch and load: the switch must be exported");
        assertEquals(42, reimported.getLoadStream().mapToDouble(Load::getP0).sum(), "Switch and load: the load must be exported");
    }

    @Test
    @DisplayName("Counter naming, node-breaker T-line: the junction node is exported with the country of its neighbours")
    void nodeBreakerTLineCounter() {
        Network reimported = exportAndReimport(nodeBreakerTLine(), COUNTER);
        assertDutchNodes("Node-breaker T-line", reimported, 4);
        assertLineCount("Node-breaker T-line", reimported, 3);
    }

    @Test
    @DisplayName("Counter naming, isolated voltage level without substation holding equipment: no country can be found, the export fails")
    void isolatedOrphanHoldingEquipmentCounter() {
        Network network = newNetwork();
        String a = addSubstationBus(network, Ids.FREE, Country.NL, "AAAAA");
        String b = addSubstationBus(network, Ids.FREE, Country.BE, "BBBBB");
        addLine(network, Ids.FREE, a, b);
        String isolated = addBus(addOrphanVoltageLevel(network, "ISOLA"), Ids.FREE, "ISOLA", '1');
        addLoad(network, isolated, "LOAD_ISOLA");

        UcteException e = assertThrows(UcteException.class, () -> exportAndReimport(network, COUNTER),
                "Isolated voltage level holding equipment: no country can be found and its equipment cannot be dropped, the export must fail");
        assertEquals("Voltage levels connected to no substation cannot hold equipment: VL_ISOLA [LOAD_ISOLA]", e.getMessage());
    }

    @Test
    @DisplayName("Counter naming, isolated voltage level without substation nor equipment: it is excluded from the export")
    void counterExcludesEmptyIsolatedOrphan() {
        Network network = newNetwork();
        String a = addSubstationBus(network, Ids.FREE, Country.NL, "AAAAA");
        String b = addSubstationBus(network, Ids.FREE, Country.BE, "BBBBB");
        addLine(network, Ids.FREE, a, b);
        addBus(addOrphanVoltageLevel(network, "EMPTY"), Ids.FREE, "EMPTY", '1');

        Network reimported = exportAndReimport(network, COUNTER);

        assertEquals(2, busIds(reimported).size(),
                () -> "Isolated voltage level without equipment: it must be excluded from the export. Nodes: " + busIds(reimported));
    }
}
