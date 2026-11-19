/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.extensions.CimCharacteristicsAdder;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesGLExporterTest {

    private static final Coordinate SUBSTATION_1 = new Coordinate(51.380348205566406, 0.5492960214614868);
    private static final Coordinate SUBSTATION_2 = new Coordinate(52.00010299682617, 0.30759671330451965);
    private static final Coordinate LINE_1 = new Coordinate(51.529258728027344, 0.5132722854614258);
    private static final Coordinate LINE_2 = new Coordinate(51.944923400878906, 0.4120868146419525);

    private Network network;

    @BeforeEach
    void setUp() {
        network = Network.create("Network", "test");
        network.setCaseDate(ZonedDateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation1 = network.newSubstation()
                .setId("Substation1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        Line line = network.newLine()
                .setId("Line")
                .setVoltageLevel1(voltageLevel1.getId())
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setVoltageLevel2(voltageLevel2.getId())
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        substation1.newExtension(SubstationPositionAdder.class).withCoordinate(SUBSTATION_1).add();
        substation2.newExtension(SubstationPositionAdder.class).withCoordinate(SUBSTATION_2).add();
        line.newExtension(LinePositionAdder.class).withCoordinates(List.of(SUBSTATION_1, LINE_1, LINE_2, SUBSTATION_2)).add();
    }

    @Test
    void testCim16WithoutCimCharacteristics() {
        String gl = exportGL(network);

        assertTrue(gl.contains("xmlns:cim=\"" + CgmesNamespace.CIM_16_NAMESPACE + "\""));
        assertTrue(gl.contains("<md:Model.profile>" + CgmesGLUtils.CIM_16_GL_PROFILE + "</md:Model.profile>"));
        assertFalse(gl.contains("IdentifiedObject.mRID"));
        checkPositions(gl);
    }

    @Test
    void testCim100() {
        network.newExtension(CimCharacteristicsAdder.class)
                .setTopologyKind(CgmesTopologyKind.BUS_BRANCH)
                .setCimVersion(100)
                .add();
        String gl = exportGL(network);

        assertTrue(gl.contains("xmlns:cim=\"" + CgmesNamespace.CIM_100_NAMESPACE + "\""));
        assertTrue(gl.contains("<md:Model.profile>" + CgmesGLUtils.CIM_100_GL_PROFILE + "</md:Model.profile>"));
        // 1 coordinate system, 3 locations, 1 + 1 + 4 position points
        assertEquals(10, count(gl, "<cim:IdentifiedObject.mRID>"));
        assertTrue(gl.contains("<cim:Location rdf:ID=\"_Substation1_S_Location\">"));
        assertTrue(gl.contains("<cim:IdentifiedObject.mRID>Substation1_S_Location</cim:IdentifiedObject.mRID>"));
        checkPositions(gl);
    }

    @Test
    void testCgmes3RoundTrip() throws IOException {
        Properties importParams = new Properties();
        importParams.put("iidm.import.cgmes.post-processors", "cgmesGLImport");
        Network cgmes3 = Network.read(Cgmes3Catalog.smallGrid().dataSource(), importParams);

        MemDataSource exported = new MemDataSource();
        cgmes3.write("CGMES", new Properties(), exported);
        new CgmesGLExporter(cgmes3).exportData(exported);

        String gl = new String(exported.getData("_" + CgmesSubset.GEOGRAPHICAL_LOCATION.getIdentifier() + ".xml"), StandardCharsets.UTF_8);
        String eqModelId = cgmes3.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.EQUIPMENT)
                .map(CgmesMetadataModel::getId).orElseThrow();
        assertTrue(gl.contains("<md:Model.DependentOn rdf:resource=\"" + eqModelId + "\"/>"));
        assertTrue(gl.contains("<md:Model.profile>" + CgmesGLUtils.CIM_100_GL_PROFILE + "</md:Model.profile>"));
        assertEquals(count(gl, "rdf:ID="), count(gl, "<cim:IdentifiedObject.mRID>"));

        // The CGMES export does not include the boundary, required for the re-import
        ReadOnlyDataSource original = Cgmes3Catalog.smallGrid().dataSource();
        for (String name : original.listNames(".*EQ_BD.*")) {
            try (InputStream is = original.newInputStream(name); OutputStream os = exported.newOutputStream(name, false)) {
                is.transferTo(os);
            }
        }
        Network reimported = Network.read(exported, importParams);
        assertTrue(cgmes3.getLineCount() > 0);
        cgmes3.getLines().forEach(line -> {
            LinePosition<Line> expected = line.getExtension(LinePosition.class);
            LinePosition<Line> actual = reimported.getLine(line.getId()).getExtension(LinePosition.class);
            assertNotNull(actual, line.getId());
            assertEquals(expected.getCoordinates(), actual.getCoordinates(), line.getId());
        });
    }

    private static String exportGL(Network network) {
        MemDataSource dataSource = new MemDataSource();
        new CgmesGLExporter(network).exportData(dataSource);
        return new String(dataSource.getData("_" + CgmesSubset.GEOGRAPHICAL_LOCATION.getIdentifier() + ".xml"), StandardCharsets.UTF_8);
    }

    private static void checkPositions(String gl) {
        assertTrue(gl.contains("<md:Model.DependentOn rdf:resource="));
        assertEquals(1, count(gl, "<cim:CoordinateSystem "));
        assertTrue(gl.contains("<cim:CoordinateSystem.crsUrn>" + CgmesGLUtils.COORDINATE_SYSTEM_URN + "</cim:CoordinateSystem.crsUrn>"));
        assertEquals(3, count(gl, "<cim:Location "));
        assertTrue(gl.contains("<cim:Location.PowerSystemResources rdf:resource=\"#_Substation1\"/>"));
        assertTrue(gl.contains("<cim:Location.PowerSystemResources rdf:resource=\"#_Substation2\"/>"));
        assertTrue(gl.contains("<cim:Location.PowerSystemResources rdf:resource=\"#_Line\"/>"));
        assertEquals(6, count(gl, "<cim:PositionPoint "));
        // substation positions are single points, without sequence number
        assertEquals(4, count(gl, "<cim:PositionPoint.sequenceNumber>"));
        assertTrue(gl.contains("<cim:PositionPoint.xPosition>" + LINE_1.getLongitude() + "</cim:PositionPoint.xPosition>"));
        assertTrue(gl.contains("<cim:PositionPoint.yPosition>" + LINE_1.getLatitude() + "</cim:PositionPoint.yPosition>"));
    }

    private static int count(String text, String pattern) {
        return (int) Pattern.compile(Pattern.quote(pattern)).matcher(text).results().count();
    }

}
