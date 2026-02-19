/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.google.common.collect.ImmutableList;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.impl.extensions.LinePositionImpl;
import com.powsybl.iidm.network.impl.extensions.SubstationPositionImpl;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        network.newLine()
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
    }

    @Test
    void test() {
        Substation substation1 = network.getSubstation("Substation1");
        SubstationPosition substationPosition1 = new SubstationPositionImpl(substation1, SUBSTATION_1);
        substation1.addExtension(SubstationPosition.class, substationPosition1);
        Substation substation2 = network.getSubstation("Substation2");
        SubstationPosition substationPosition2 = new SubstationPositionImpl(substation2, SUBSTATION_2);
        substation2.addExtension(SubstationPosition.class, substationPosition2);
        Line line = network.getLine("Line");
        line.addExtension(LinePosition.class, new LinePositionImpl<>(line, ImmutableList.of(SUBSTATION_1, LINE_1, LINE_2, SUBSTATION_2)));

        TripleStore tripleStore = Mockito.mock(TripleStore.class);
        Mockito.when(tripleStore.add(ArgumentMatchers.anyString(), ArgumentMatchers.eq(CgmesNamespace.CIM_16_NAMESPACE),
                        ArgumentMatchers.eq("CoordinateSystem"), ArgumentMatchers.any(PropertyBag.class)))
                                .thenReturn("CoordinateSystemId");

        DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getBaseName()).thenReturn(network.getId().toLowerCase());

        ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> namespaceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PropertyBag> propertiesCaptor = ArgumentCaptor.forClass(PropertyBag.class);

        new CgmesGLExporter(network, tripleStore).exportData(dataSource);

        // check add namespace
        Mockito.verify(tripleStore, Mockito.times(3)).addNamespace(prefixCaptor.capture(), namespaceCaptor.capture());
        checkNamespace(prefixCaptor.getAllValues().get(0), namespaceCaptor.getAllValues().get(0), "data", "http://" + network.getId().toLowerCase() + "/#");
        checkNamespace(prefixCaptor.getAllValues().get(1), namespaceCaptor.getAllValues().get(1), "cim", CgmesNamespace.CIM_16_NAMESPACE);
        checkNamespace(prefixCaptor.getAllValues().get(2), namespaceCaptor.getAllValues().get(2), "md", CgmesGLExporter.MD_NAMESPACE);

        // check add statements
        Mockito.verify(tripleStore, Mockito.times(11)).add(contextCaptor.capture(), nsCaptor.capture(),
                       typeCaptor.capture(), propertiesCaptor.capture());
        checkCoordinateSystem(contextCaptor.getAllValues().get(1), nsCaptor.getAllValues().get(1), typeCaptor.getAllValues().get(1),
                              propertiesCaptor.getAllValues().get(1), network.getId().toLowerCase());
        checkLocation(contextCaptor.getAllValues().get(2), nsCaptor.getAllValues().get(2), typeCaptor.getAllValues().get(2),
                      propertiesCaptor.getAllValues().get(2), network.getId().toLowerCase(), "Substation1", "Substation1");
        checkPositionPoint(contextCaptor.getAllValues().get(3), nsCaptor.getAllValues().get(3), typeCaptor.getAllValues().get(3),
                           propertiesCaptor.getAllValues().get(3), network.getId().toLowerCase(), SUBSTATION_1, -1);
        checkLocation(contextCaptor.getAllValues().get(4), nsCaptor.getAllValues().get(4), typeCaptor.getAllValues().get(4),
                      propertiesCaptor.getAllValues().get(4), network.getId().toLowerCase(), "Substation2", "Substation2");
        checkPositionPoint(contextCaptor.getAllValues().get(5), nsCaptor.getAllValues().get(5), typeCaptor.getAllValues().get(5),
                           propertiesCaptor.getAllValues().get(5), network.getId().toLowerCase(), SUBSTATION_2, -1);
        checkLocation(contextCaptor.getAllValues().get(6), nsCaptor.getAllValues().get(6), typeCaptor.getAllValues().get(6),
                      propertiesCaptor.getAllValues().get(6), network.getId().toLowerCase(), "Line", "Line");
        checkPositionPoint(contextCaptor.getAllValues().get(7), nsCaptor.getAllValues().get(7), typeCaptor.getAllValues().get(7),
                           propertiesCaptor.getAllValues().get(7), network.getId().toLowerCase(), SUBSTATION_1, 1);
        checkPositionPoint(contextCaptor.getAllValues().get(8), nsCaptor.getAllValues().get(8), typeCaptor.getAllValues().get(8),
                           propertiesCaptor.getAllValues().get(8), network.getId().toLowerCase(), LINE_1, 2);
        checkPositionPoint(contextCaptor.getAllValues().get(9), nsCaptor.getAllValues().get(9), typeCaptor.getAllValues().get(9),
                           propertiesCaptor.getAllValues().get(9), network.getId().toLowerCase(), LINE_2, 3);
        checkPositionPoint(contextCaptor.getAllValues().get(10), nsCaptor.getAllValues().get(10), typeCaptor.getAllValues().get(10),
                           propertiesCaptor.getAllValues().get(10), network.getId().toLowerCase(), SUBSTATION_2, 4);
    }

    private void checkNamespace(String prefix, String ns, String expetedPrefix, String expectedNs) {
        assertEquals(expetedPrefix, prefix);
        assertEquals(expectedNs, ns);
    }

    private void checkProperties(String context, String namespace, String type, PropertyBag properties, String basename,
                                 String expectedType, List<String> expectedProperties, List<String> expectedResources,
                                 List<String> expectedClassProperties) {
        assertTrue(CgmesSubset.GEOGRAPHICAL_LOCATION.isValidName(context));
        assertEquals(basename + "_" + CgmesSubset.GEOGRAPHICAL_LOCATION.getIdentifier() + ".xml", context);
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE, namespace);
        assertEquals(expectedType, type);
        assertEquals(expectedProperties.size(), properties.propertyNames().size());
        expectedProperties.forEach(property -> assertTrue(properties.propertyNames().contains(property)));
        expectedResources.forEach(resource -> assertTrue(properties.isResource(resource)));
        expectedClassProperties.forEach(classProperty -> assertTrue(properties.isClassProperty(classProperty)));
    }

    private void checkCoordinateSystem(String context, String namespace, String type, PropertyBag properties, String basename) {
        checkProperties(context, namespace, type, properties, basename, "CoordinateSystem",
                Arrays.asList("IdentifiedObject.name", "crsUrn"), Collections.emptyList(),
                List.of("IdentifiedObject.name"));
        assertEquals(CgmesGLUtils.COORDINATE_SYSTEM_URN, properties.get("crsUrn"));
    }

    private void checkLocation(String context, String namespace, String type, PropertyBag properties, String basename,
                               String expectedName, String expectedPowerSystemResource) {
        checkProperties(context, namespace, type, properties, basename, "Location",
                Arrays.asList("IdentifiedObject.name", "CoordinateSystem", "PowerSystemResources"),
                Arrays.asList("CoordinateSystem", "PowerSystemResources"),
                List.of("IdentifiedObject.name"));
        assertEquals(expectedName, properties.get("IdentifiedObject.name"));
        assertEquals("CoordinateSystemId", properties.get("CoordinateSystem"));
        assertEquals(expectedPowerSystemResource, properties.get("PowerSystemResources"));
    }

    private void checkPositionPoint(String context, String namespace, String type, PropertyBag properties, String basename,
                                    Coordinate expectedCoordinate, int expectedSeq) {
        checkProperties(context, namespace, type, properties, basename, "PositionPoint",
                expectedSeq == -1 ? Arrays.asList("xPosition", "yPosition", "Location") : Arrays.asList("xPosition", "yPosition", "sequenceNumber", "Location"),
                List.of("Location"), Collections.emptyList());
        assertEquals(expectedCoordinate.getLongitude(), properties.asDouble("xPosition"), 0);
        assertEquals(expectedCoordinate.getLatitude(), properties.asDouble("yPosition"), 0);
        if (expectedSeq != -1) {
            assertEquals(expectedSeq, properties.asInt("sequenceNumber"), 0);
        }
    }

}
