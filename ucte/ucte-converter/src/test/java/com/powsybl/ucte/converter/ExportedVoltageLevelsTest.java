/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportConstants;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteCountryCode;
import org.junit.jupiter.api.Test;

import static com.powsybl.ucte.converter.OrphanVoltageLevelNetworks.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class ExportedVoltageLevelsTest {

    private static final Ids IDS = Ids.FREE;

    private static ReportNode newRootReportNode() {
        return ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testExportReportNode")
                .build();
    }

    /** Network with substations AAAAA (NL) and BBBBB (BE), linked by a line: two countries, graph path. */
    private static Network twoCountries() {
        Network network = newNetwork();
        String a = addSubstationBus(network, IDS, Country.NL, "AAAAA");
        String b = addSubstationBus(network, IDS, Country.BE, "BBBBB");
        addLine(network, IDS, a, b);
        return network;
    }

    @Test
    void substationWithoutCountryIsRejected() {
        Network network = tLine(IDS, null);
        UcteException e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(network, ReportNode.NO_OP));
        assertEquals("Substation S_BBBBB has no country", e.getMessage());
    }

    @Test
    void substationWithUnsupportedCountryIsRejected() {
        Network network = tLine(IDS, Country.US);
        com.powsybl.ucte.network.UcteException e = assertThrows(com.powsybl.ucte.network.UcteException.class,
                () -> ExportedVoltageLevels.compute(network, ReportNode.NO_OP));
        assertEquals("No UCTE country found for US", e.getMessage());
    }

    @Test
    void withoutOrphanEveryVoltageLevelHasItsSubstationCountry() {
        Network network = twoCountries();
        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);
        assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_AAAAA")));
        assertEquals(UcteCountryCode.BE, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_BBBBB")));
        assertTrue(exportedVoltageLevels.isExported(network.getVoltageLevel("VL_AAAAA")));
    }

    @Test
    void singleCountryIsGivenToUnconnectedOrphan() {
        Network network = newNetwork();
        addSubstationBus(network, IDS, Country.NL, "AAAAA");
        String alone = addBus(addOrphanVoltageLevel(network, "ALONE"), IDS, "ALONE", '1');
        addLoad(network, alone, "LOAD_ALONE");

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);

        assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_ALONE")));
        assertTrue(exportedVoltageLevels.isExported(network.getVoltageLevel("VL_ALONE")));
    }

    @Test
    void orphansGetTheCountryOfTheirComponent() {
        Network network = chainedSegmentsTwoVoltageLevels(IDS);
        addSubstationBus(network, IDS, Country.BE, "OTHER");

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);

        assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_SEGAA")));
        assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_SEGBB")));
    }

    @Test
    void orphanVoltageLevelWithInternalLineGetsCountry() {
        Network network = chainedSegmentsOneVoltageLevel(IDS);
        addSubstationBus(network, IDS, Country.BE, "OTHER");

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);

        assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_SEGMT")));
    }

    @Test
    void orphanBetweenTwoCountriesIsRejected() {
        Network network = tLine(IDS, Country.BE);
        UcteException e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(network, ReportNode.NO_OP));
        assertEquals("Cannot determine the country of voltage levels [VL_TJUNC]: they are connected to substations of several countries [BE, NL]",
                e.getMessage());
    }

    @Test
    void isolatedOrphansHoldingEquipmentAreRejected() {
        Network network = twoCountries();
        String i1 = addBus(addOrphanVoltageLevel(network, "ISOL1"), IDS, "ISOL1", '1');
        String i2 = addBus(addOrphanVoltageLevel(network, "ISOL2"), IDS, "ISOL2", '1');
        addLoad(network, i1, "LOAD_ISOL1");
        addLine(network, IDS, i1, i2);

        UcteException e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(network, ReportNode.NO_OP));
        assertEquals("Voltage levels connected to no substation cannot hold equipment: "
                        + "VL_ISOL1 [BR_BUS_ISOL1_1_BUS_ISOL2_1, LOAD_ISOL1], VL_ISOL2 [BR_BUS_ISOL1_1_BUS_ISOL2_1]",
                e.getMessage());
    }

    @Test
    void isolatedOrphanWithoutEquipmentIsExcluded() {
        Network network = twoCountries();
        addBus(addOrphanVoltageLevel(network, "EMPTY"), IDS, "EMPTY", '1');
        ReportNode reportNode = newRootReportNode();

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, reportNode);

        VoltageLevel empty = network.getVoltageLevel("VL_EMPTY");
        assertFalse(exportedVoltageLevels.isExported(empty));
        UcteException e = assertThrows(UcteException.class, () -> exportedVoltageLevels.getCountry(empty));
        assertEquals("Voltage level VL_EMPTY is connected to no substation: no country", e.getMessage());
        assertEquals(1, reportNode.getChildren().size());
        ReportNode notExported = reportNode.getChildren().get(0);
        assertEquals("Voltage level VL_EMPTY is connected to no substation and holds no equipment: it is not exported",
                notExported.getMessage());
        assertEquals(TypedValue.WARN_SEVERITY.getValue(), notExported.getValue(ReportConstants.SEVERITY_KEY).orElseThrow().getValue());
    }

    @Test
    void isolatedNodeBreakerOrphanWithOnlyBusbarSectionIsExcluded() {
        Network network = twoCountries();
        VoltageLevel orphan = network.newVoltageLevel()
                .setId("VL_NBBBS")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        orphan.getNodeBreakerView().newBusbarSection().setId("BBS").setNode(0).add();

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);

        assertFalse(exportedVoltageLevels.isExported(orphan));
    }

    @Test
    void connectionStatusIsIgnored() {
        Network network = twoCountries();
        String orphanBus = addBus(addOrphanVoltageLevel(network, "SEGAA"), IDS, "SEGAA", '1');
        Line line = addLine(network, IDS, network.getVoltageLevel("VL_AAAAA").getBusBreakerView().getBuses().iterator().next().getId(), orphanBus);
        line.getTerminal1().disconnect();
        line.getTerminal2().disconnect();

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);

        assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_SEGAA")));
    }

    @Test
    void boundaryLineIsNotAnEdge() {
        Network network = twoCountries();
        VoltageLevel orphan = addOrphanVoltageLevel(network, "XORPH");
        String bus = addBus(orphan, IDS, "XORPH", '1');
        orphan.newBoundaryLine().setId("BL_X").setBus(bus).setConnectableBus(bus)
                .setR(1).setX(10).setG(0).setB(0).setP0(0).setQ0(0).add();

        UcteException e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(network, ReportNode.NO_OP));
        assertEquals("Voltage levels connected to no substation cannot hold equipment: VL_XORPH [BL_X]", e.getMessage());
    }

    @Test
    void tieLineIsNotAnEdge() {
        Network network = twoCountries();
        VoltageLevel orphan = addOrphanVoltageLevel(network, "XORPH");
        String orphanBus = addBus(orphan, IDS, "XORPH", '1');
        orphan.newBoundaryLine().setId("BL_1").setBus(orphanBus).setConnectableBus(orphanBus)
                .setR(1).setX(10).setG(0).setB(0).setP0(0).setQ0(0).setPairingKey("XKEY").add();
        VoltageLevel nl = network.getVoltageLevel("VL_AAAAA");
        String nlBus = nl.getBusBreakerView().getBuses().iterator().next().getId();
        nl.newBoundaryLine().setId("BL_2").setBus(nlBus).setConnectableBus(nlBus)
                .setR(1).setX(10).setG(0).setB(0).setP0(0).setQ0(0).setPairingKey("XKEY").add();
        network.newTieLine().setId("TL").setBoundaryLine1("BL_1").setBoundaryLine2("BL_2").add();

        UcteException e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(network, ReportNode.NO_OP));
        assertEquals("Voltage levels connected to no substation cannot hold equipment: VL_XORPH [BL_1]", e.getMessage());
    }
}
