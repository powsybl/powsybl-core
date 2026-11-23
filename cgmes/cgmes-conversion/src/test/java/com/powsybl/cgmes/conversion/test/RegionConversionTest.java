/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class RegionConversionTest extends AbstractSerDeTest {

    @Test
    void noRegionsTest() throws IOException {
        Network network = createNoRegionsNetwork();
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        assertRegions(eqFile, "S1", "default+region_SGR", "default region", "default+region_GR", "default region");
        assertRegions(eqFile, "S2", "default+region_SGR", "default region", "default+region_GR", "default region");
        assertRegions(eqFile, "S3", "default+region_SGR", "default region", "default+region_GR", "default region");
        assertRegions(eqFile, "S4", "default+region_SGR", "default region", "default+region_GR", "default region");
    }

    @Test
    void withRegionsTest() throws IOException {
        Network network = createWithRegions();
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        assertRegions(eqFile, "S1", "FR_SGR", "FR", "FR_GR", "FR");
        assertRegions(eqFile, "S2", "FR_SGR", "FR", "FR_GR", "FR");
        assertRegions(eqFile, "S3", "BE_SGR", "BE", "BE_GR", "BE");
        assertRegions(eqFile, "S4", "BE_SGR", "BE", "BE_GR", "BE");
    }

    @Test
    void withRegionsAndSubRegionsTest() throws IOException {
        Network network = createWithRegionsAndSubRegions();
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        assertRegions(eqFile, "S1", "FR1_SGR", "FR1", "FR_GR", "FR");
        assertRegions(eqFile, "S2", "FR2_SGR", "FR2", "FR_GR", "FR");
        assertRegions(eqFile, "S3", "BE1_SGR", "BE1", "BE_GR", "BE");
        assertRegions(eqFile, "S4", "BE2_SGR", "BE2", "BE_GR", "BE");
    }

    @Test
    void withRegionsAndSubRegionsAndPropertiesTest() throws IOException {
        Network network = createWithRegionsAndSubRegionsAndProperties();
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        assertRegions(eqFile, "S1", "FR1", "FR1", "FR", "FRANCE");
        assertRegions(eqFile, "S2", "FR2_SGR", "FR2", "FR_GR", "FR");
        assertRegions(eqFile, "S3", "BE1_SGR", "BE1", "BE", "BE");
        assertRegions(eqFile, "S4", "BE2_SGR", "BE2", "BE_GR", "BE");
    }

    private void assertRegions(String eqFile, String substationId, String subRegionId, String subRegionName, String regionId, String regionName) {
        String substation = getElement(eqFile, "Substation", substationId);

        String subGeographicalRegionId = getResource(substation, "Substation.Region");
        assertEquals(subRegionId, subGeographicalRegionId);

        String subGeographicalRegion = getElement(eqFile, "SubGeographicalRegion", subGeographicalRegionId);
        String subGeographicalRegionName = getAttribute(subGeographicalRegion, "IdentifiedObject.name");
        assertEquals(subRegionName, subGeographicalRegionName);

        String geographicalRegionId = getResource(subGeographicalRegion, "SubGeographicalRegion.Region");
        assertEquals(regionId, geographicalRegionId);

        String geographicalRegion = getElement(eqFile, "GeographicalRegion", geographicalRegionId);
        String geographicalRegionName = getAttribute(geographicalRegion, "IdentifiedObject.name");
        assertEquals(regionName, geographicalRegionName);
    }

    private Network createNoRegionsNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("regionsTest", "test");
        network.setCaseDate(ZonedDateTime.parse("2021-12-07T19:43:00.0000+02:00"));

        network.newSubstation().setId("S1").add();
        network.newSubstation().setId("S2").add();
        network.newSubstation().setId("S3").add();
        network.newSubstation().setId("S4").add();

        return network;
    }

    private Network createWithRegions() {
        Network network = createNoRegionsNetwork();
        network.getSubstation("S1").setCountry(Country.FR);
        network.getSubstation("S2").setCountry(Country.FR);
        network.getSubstation("S3").setCountry(Country.BE);
        network.getSubstation("S4").setCountry(Country.BE);

        return network;
    }

    private Network createWithRegionsAndSubRegions() {
        Network network = createWithRegions();
        network.getSubstation("S1").addGeographicalTag("FR1");
        network.getSubstation("S2").addGeographicalTag("FR2");
        network.getSubstation("S3").addGeographicalTag("BE1");
        network.getSubstation("S4").addGeographicalTag("BE2");

        return network;
    }

    private Network createWithRegionsAndSubRegionsAndProperties() {
        Network network = createWithRegionsAndSubRegions();
        network.getSubstation("S1").setProperty(PROPERTY_SUB_REGION_ID, "FR1");
        network.getSubstation("S1").setProperty(PROPERTY_REGION_ID, "FR");
        network.getSubstation("S1").setProperty(PROPERTY_REGION_NAME, "FRANCE");
        network.getSubstation("S3").setProperty(PROPERTY_REGION_ID, "BE");

        return network;
    }

}
