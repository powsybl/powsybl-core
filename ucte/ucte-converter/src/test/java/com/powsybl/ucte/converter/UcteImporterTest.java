/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.entsoe.util.EntsoeArea;
import com.powsybl.entsoe.util.EntsoeGeographicalCode;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class UcteImporterTest {
    @Test
    public void trimIssueTest() {
        // Import network that could fail because of id conflicts due to trim mechanism
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("importIssue.uct", getClass().getResourceAsStream("/importIssue.uct"));
        new UcteImporter().importData(dataSource, null);
    }

    @Test
    public void countryAssociationIssueTest() {
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("countryIssue.uct", getClass().getResourceAsStream("/countryIssue.uct"));
        Network network = new UcteImporter().importData(dataSource, null);

        assertEquals(Country.ES, network.getSubstation("EHORTA").getCountry());
        assertEquals(1, network.getSubstation("EHORTA").getVoltageLevelStream().count());
        assertEquals(Country.BE, network.getSubstation("BHORTA").getCountry());
        assertEquals(1, network.getSubstation("BHORTA").getVoltageLevelStream().count());

    }

    @Test
    public void germanTsosImport() throws Exception {
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("germanTsos.uct", getClass().getResourceAsStream("/germanTsos.uct"));
        Network network = new UcteImporter().importData(dataSource, null);

        //Check D4 is correctly parsed
        EntsoeArea ext = network.getSubstation("D4NEUR").getExtension(EntsoeArea.class);
        assertNotNull(ext);
        assertEquals(EntsoeGeographicalCode.D4, ext.getCode());

        //Check that for other countries, no extension is added
        ext = network.getSubstation("BAVLGM").getExtension(EntsoeArea.class);
        assertNull(ext);

        //Check that for "D-nodes", no extension is added
        ext = network.getSubstation("DJA_KA").getExtension(EntsoeArea.class);
        assertNull(ext);

        //Check that for a "D-node" starting with "DE", no extension is added
        ext = network.getSubstation("DEA_KA").getExtension(EntsoeArea.class);
        assertNull(ext);

    }

    @Test
    public void elementNameTest() {
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("elementName.uct", getClass().getResourceAsStream("/elementName.uct"));
        Network network = new UcteImporter().importData(dataSource, null);
        // Test Element name Line
        assertEquals("Test Line", network.getLine("F_SU1_12 F_SU2_11 1").getProperties().getProperty("elementName"));
        // Test Dangling Line element name
        assertEquals("Test DL", network.getDanglingLine("XG__F_21 F_SU1_21 1").getProperties().getProperty("elementName"));
        // Test Switch element name
        assertEquals("Test Coupler", network.getSwitch("F_SU1_12 F_SU1_11 1").getProperties().getProperty("elementName"));
        // Test 2WT element name
        assertEquals("Test 2WT 1", network.getBranch("F_SU1_11 F_SU1_21 1").getProperties().getProperty("elementName"));
        assertEquals("Test 2WT 2", network.getBranch("B_SU1_11 B_SU1_21 1").getProperties().getProperty("elementName"));
        // Test tie line
        // cannot refer to side of tieline directly cause order of half lines may change
        // at import : due to HashSet iterator on dangling lines ?
        TieLine tieLine1 = (TieLine) network.getLineStream().filter(Line::isTieLine)
                .filter(line -> {
                    TieLine tl = (TieLine) line;
                    return tl.getHalf1().getId().equals("XB__F_11 B_SU1_11 1") || tl.getHalf2().getId().equals("XB__F_11 B_SU1_11 1");
                }).findAny().get();
        String expectedElementName1 = tieLine1.getHalf1().getId().equals("XB__F_11 B_SU1_11 1") ? "Test TL 1/2" : "Test TL 1/1";
        String expectedElementName2 = tieLine1.getHalf2().getId().equals("XB__F_11 B_SU1_11 1") ? "Test TL 1/2" : "Test TL 1/1";
        assertEquals(expectedElementName1, tieLine1.getProperties().getProperty("elementName_1"));
        assertEquals(expectedElementName2, tieLine1.getProperties().getProperty("elementName_2"));

        TieLine tieLine2 = (TieLine) network.getLineStream().filter(Line::isTieLine)
                .filter(line -> {
                    TieLine tl = (TieLine) line;
                    return tl.getHalf1().getId().equals("XB__F_21 B_SU1_21 1") || tl.getHalf2().getId().equals("XB__F_21 B_SU1_21 1");
                }).findAny().get();
        expectedElementName1 = tieLine2.getHalf1().getId().equals("XB__F_21 B_SU1_21 1") ? "Test TL 2/2" : "Test TL 2/1";
        expectedElementName2 = tieLine2.getHalf2().getId().equals("XB__F_21 B_SU1_21 1") ? "Test TL 2/2" : "Test TL 2/1";
        assertEquals(expectedElementName1, tieLine2.getProperties().getProperty("elementName_1"));
        assertEquals(expectedElementName2, tieLine2.getProperties().getProperty("elementName_2"));
    }

    @Test
    public void xnodeMergingIssueTest() {
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("mergedXnodeIssue.uct", getClass().getResourceAsStream("/mergedXnodeIssue.uct"));
        Network network = new UcteImporter().importData(dataSource, null);

        assertEquals(2, network.getVoltageLevelCount());
        assertEquals(1, network.getDanglingLineCount());
        assertEquals(1, network.getLineCount());
        Line l = network.getLineStream().findFirst().orElseThrow(AssertionError::new);
        assertEquals("ESNODE11 XXNODE11 1 + FRNODE11 XXNODE11 1", l.getId());
        MergedXnode mergedXnode = l.getExtension(MergedXnode.class);
        assertNotNull(mergedXnode);
    }

    @Test
    public void lineAndTransformerSameId() {
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("sameId.uct", getClass().getResourceAsStream("/sameId.uct"));
        Network network = new UcteImporter().importData(dataSource, null);

        assertEquals(0, network.getLineCount());
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getSwitchStream().count());
    }
}

