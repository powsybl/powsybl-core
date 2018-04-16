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
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class UcteImporterTest {
    @Test
    public void trimIssueTest() throws Exception {
        // Import network that could fail because of id conflicts due to trim mechanism
        ReadOnlyMemDataSource dataSource = DataSourceUtil.createReadOnlyMemDataSource("importIssue.uct", getClass().getResourceAsStream("/importIssue.uct"));
        new UcteImporter().importData(dataSource, null);
    }

    @Test
    public void countryAssociationIssueTest() throws Exception {
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
        ext = network.getSubstation("DJA_KA").getExtension(EntsoeArea.class);
        assertNull(ext);
    }
}
