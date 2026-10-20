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

import java.util.List;

import static com.powsybl.ucte.converter.UcteExporterTestNetworks.*;
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

    @Test
    void invalidCountriesAreRejected() {
        Network withoutCountry = createWithTLine(IDS, null);
        UcteException e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(withoutCountry, ReportNode.NO_OP));
        assertEquals("Substation S_BBBBB has no country", e.getMessage());

        Network withUnsupportedCountry = createWithTLine(IDS, Country.US);
        com.powsybl.ucte.network.UcteException e2 = assertThrows(com.powsybl.ucte.network.UcteException.class,
                () -> ExportedVoltageLevels.compute(withUnsupportedCountry, ReportNode.NO_OP));
        assertEquals("No UCTE country found for US", e2.getMessage());

        Network acrossTwoCountries = createWithTLine(IDS, Country.BE);
        e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(acrossTwoCountries, ReportNode.NO_OP));
        assertEquals("Cannot determine the country of voltage levels [VL_TJUNC]: they are connected to substations of several countries [BE, NL]",
                e.getMessage());
    }

    @Test
    void orphansGetTheCountryOfTheirComponent() {
        Network network = createWithOrphanVoltageLevels(IDS);
        // Connection status is ignored: an orphan linked by a disconnected line still gets the country
        addOrphanWithDisconnectedLine(network);

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);

        assertEquals(UcteCountryCode.BE, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_DDDDD")));
        for (String id : List.of("VL_AAAAA", "VL_TJUNC", "VL_SEGMT", "VL_SEGAA", "VL_SEGBB", "VL_ORPHA", "VL_DISCO")) {
            VoltageLevel voltageLevel = network.getVoltageLevel(id);
            assertTrue(exportedVoltageLevels.isExported(voltageLevel), () -> id + " must be exported");
            assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(voltageLevel), () -> id + " must get the country NL");
        }
    }

    @Test
    void singleCountryIsGivenToUnconnectedOrphan() {
        Network network = createWithSingleCountryAndUnconnectedOrphan();

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, ReportNode.NO_OP);

        assertEquals(UcteCountryCode.NL, exportedVoltageLevels.getCountry(network.getVoltageLevel("VL_ALONE")));
        assertTrue(exportedVoltageLevels.isExported(network.getVoltageLevel("VL_ALONE")));
    }

    /** Boundary lines and tie lines are not edges of the graph: they don't give a country to an orphan. */
    @Test
    void isolatedOrphansHoldingEquipmentAreRejected() {
        Network network = createWithIsolatedOrphansHoldingEquipment();

        UcteException e = assertThrows(UcteException.class, () -> ExportedVoltageLevels.compute(network, ReportNode.NO_OP));
        assertEquals("Voltage levels connected to no substation cannot hold equipment: "
                        + "VL_ISOL1 [BR_BUS_ISOL1_1_BUS_ISOL2_1, LOAD_ISOL1], VL_ISOL2 [BR_BUS_ISOL1_1_BUS_ISOL2_1], VL_XORPH [BL_X], VL_XTIEL [BL_1]",
                e.getMessage());
    }

    @Test
    void isolatedOrphansWithoutEquipmentAreExcluded() {
        Network network = createWithIsolatedOrphansWithoutEquipment();
        ReportNode reportNode = newRootReportNode();

        ExportedVoltageLevels exportedVoltageLevels = ExportedVoltageLevels.compute(network, reportNode);

        VoltageLevel empty = network.getVoltageLevel("VL_EMPTY");
        assertFalse(exportedVoltageLevels.isExported(empty));
        assertFalse(exportedVoltageLevels.isExported(network.getVoltageLevel("VL_NBBBS")));
        assertTrue(exportedVoltageLevels.isExported(network.getVoltageLevel("VL_AAAAA")));
        UcteException e = assertThrows(UcteException.class, () -> exportedVoltageLevels.getCountry(empty));
        assertEquals("Voltage level VL_EMPTY is connected to no substation: no country", e.getMessage());
        assertEquals(List.of("Voltage level VL_EMPTY is connected to no substation and holds no equipment: it is not exported",
                        "Voltage level VL_NBBBS is connected to no substation and holds no equipment: it is not exported"),
                reportNode.getChildren().stream().map(ReportNode::getMessage).toList());
        for (ReportNode notExported : reportNode.getChildren()) {
            assertEquals(TypedValue.WARN_SEVERITY.getValue(), notExported.getValue(ReportConstants.SEVERITY_KEY).orElseThrow().getValue());
        }
    }
}
