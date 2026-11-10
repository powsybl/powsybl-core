/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.ucte.network.UcteElement;
import com.powsybl.ucte.network.UcteElementStatus;
import com.powsybl.ucte.network.UcteNetwork;
import com.powsybl.ucte.network.UcteNode;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.io.UcteReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static com.powsybl.ucte.converter.util.UcteConverterConstants.ELEMENT_NAME_PROPERTY_KEY;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end export to UCTE-DEF of a CGMES 3 network with out-of-service equipment: the out-of-service equipment is
 * exported out of operation, and the fictitious switches created at the CGMES import are not exported.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class CgmesInServiceUcteExportTest {

    @Test
    @DisplayName("CGMES 3 out-of-service equipment is exported to UCTE-DEF out of operation, without fictitious switch")
    void outOfServiceEquipmentTest() throws IOException {
        UcteNetwork ucte = exportToUcte(importDataset());
        assertEquals(UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION, statusOf(ucte, "LINE OUT"));
        assertEquals(UcteElementStatus.REAL_ELEMENT_IN_OPERATION, statusOf(ucte, "LINE IN"));
        assertEquals(UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION, statusOf(ucte, "TRAFO OUT"));
        assertEquals(UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION, statusOf(ucte, "BLINE OUT"));
        assertEquals(UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION, statusOf(ucte, "TIE 1"));
        assertEquals(UcteElementStatus.REAL_ELEMENT_IN_OPERATION, statusOf(ucte, "TIE 2"));
        assertEquals(UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION, statusOf(ucte, "COUPLER"));

        // No fictitious switch is exported: the only coupler is the CGMES one
        assertEquals(1, ucte.getLines().stream().filter(l -> l.getStatus() == UcteElementStatus.BUSBAR_COUPLER_IN_OPERATION
                || l.getStatus() == UcteElementStatus.BUSBAR_COUPLER_OUT_OF_OPERATION).count());
    }

    @Test
    @DisplayName("Node-breaker: the end of an out-of-service line is exported on a UCTE node of its own")
    void outOfServiceLineEndTest() throws IOException {
        // Current output in node-breaker: an end of an out-of-service line is detached by a fictitious switch, and its
        // bus-breaker view bus is exported as a UCTE node referenced by no other element
        UcteNetwork ucte = exportToUcte(importDataset());
        UcteElement lineOut = elementOf(ucte, "LINE OUT");
        assertTrue(isReferencedOnlyBy(ucte, lineOut.getId().getNodeCode1(), lineOut)
                || isReferencedOnlyBy(ucte, lineOut.getId().getNodeCode2(), lineOut));
    }

    @Test
    @DisplayName("Node-breaker: an out-of-service generator or load is exported on a UCTE node of its own, with its power")
    void outOfServiceInjectionsTest() throws IOException {
        // Current output in node-breaker: an out-of-service generator or load is detached by a fictitious switch, and its
        // bus-breaker view bus is exported as an extra UCTE node, referenced by no element, that holds its power
        UcteNetwork ucte = exportToUcte(importDataset());

        // The node of the in-service generator and load does not count the out-of-service ones
        UcteNode node2 = nodeWith(ucte, UcteNode::getActivePowerGeneration, -100);
        assertEquals(10, node2.getActiveLoad(), 1e-6);

        UcteNode loadOutNode = nodeWith(ucte, UcteNode::getActiveLoad, 20);
        assertTrue(isReferencedByNoElement(ucte, loadOutNode.getCode()));
        UcteNode generatorOutNode = nodeWith(ucte, UcteNode::getActivePowerGeneration, -50);
        assertTrue(isReferencedByNoElement(ucte, generatorOutNode.getCode()));
    }

    /**
     * Import the CGMES 3 test dataset and name the elements checked in the UCTE-DEF export.
     *
     * @return the imported network
     */
    private static Network importDataset() {
        Network network = Network.read(new ResourceDataSource("inService",
                new ResourceSet("/cgmes-in-service/", "inService_EQ.xml", "inService_EQ_BD.xml", "inService_SSH.xml")));
        network.getLine("LineOut").setProperty(ELEMENT_NAME_PROPERTY_KEY, "LINE OUT");
        network.getLine("LineIn").setProperty(ELEMENT_NAME_PROPERTY_KEY, "LINE IN");
        network.getTwoWindingsTransformer("TransformerOut").setProperty(ELEMENT_NAME_PROPERTY_KEY, "TRAFO OUT");
        network.getSwitch("Coupler").setProperty(ELEMENT_NAME_PROPERTY_KEY, "COUPLER");
        network.getBoundaryLine("BoundaryLineOut").setProperty(ELEMENT_NAME_PROPERTY_KEY, "BLINE OUT");
        TieLine tieLine = network.getBoundaryLine("TieHalf1").getTieLine().orElseThrow();
        tieLine.getBoundaryLine1().setProperty(ELEMENT_NAME_PROPERTY_KEY, "TIE 1");
        tieLine.getBoundaryLine2().setProperty(ELEMENT_NAME_PROPERTY_KEY, "TIE 2");
        return network;
    }

    /**
     * Export a network to UCTE-DEF and read the result back.
     *
     * @param network the network to export
     * @return the exported UCTE network
     * @throws IOException if the export cannot be read
     */
    private static UcteNetwork exportToUcte(Network network) throws IOException {
        Properties properties = new Properties();
        properties.put(UcteExporter.NAMING_STRATEGY, "Counter");
        MemDataSource dataSource = new MemDataSource();
        new UcteExporter().export(network, properties, dataSource, ReportNode.NO_OP);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(dataSource.getData(null, "uct")), StandardCharsets.UTF_8))) {
            return new UcteReader().read(reader, ReportNode.NO_OP);
        }
    }

    /**
     * Get the lines (couplers included) and the transformers of a UCTE network.
     *
     * @param ucte the UCTE network
     * @return the elements
     */
    private static Stream<UcteElement> elements(UcteNetwork ucte) {
        return Stream.concat(ucte.getLines().stream(), ucte.getTransformers().stream());
    }

    /**
     * Get the element of a UCTE network that has the given element name.
     *
     * @param ucte the UCTE network
     * @param elementName the element name
     * @return the element
     */
    private static UcteElement elementOf(UcteNetwork ucte, String elementName) {
        return elements(ucte).filter(e -> elementName.equals(e.getElementName())).findFirst().orElseThrow();
    }

    /**
     * Get the status of the element of a UCTE network that has the given element name.
     *
     * @param ucte the UCTE network
     * @param elementName the element name
     * @return the status of the element
     */
    private static UcteElementStatus statusOf(UcteNetwork ucte, String elementName) {
        return elementOf(ucte, elementName).getStatus();
    }

    /**
     * Get the single node of a UCTE network that has the given value.
     *
     * @param ucte the UCTE network
     * @param value the value of the node to look at
     * @param expected the expected value
     * @return the node
     */
    private static UcteNode nodeWith(UcteNetwork ucte, ToDoubleFunction<UcteNode> value, double expected) {
        return ucte.getNodes().stream().filter(n -> Math.abs(value.applyAsDouble(n) - expected) < 1e-6).reduce((n1, n2) -> {
            throw new AssertionError("Several nodes with the value " + expected);
        }).orElseThrow();
    }

    /**
     * Tell whether a UCTE node is referenced by no element.
     *
     * @param ucte the UCTE network
     * @param node the code of the node
     * @return {@code true} if no element references the node
     */
    private static boolean isReferencedByNoElement(UcteNetwork ucte, UcteNodeCode node) {
        return elements(ucte).noneMatch(e -> node.equals(e.getId().getNodeCode1()) || node.equals(e.getId().getNodeCode2()));
    }

    /**
     * Tell whether a UCTE node is referenced by no element other than the given one.
     *
     * @param ucte the UCTE network
     * @param node the code of the node
     * @param element the element
     * @return {@code true} if no other element references the node
     */
    private static boolean isReferencedOnlyBy(UcteNetwork ucte, UcteNodeCode node, UcteElement element) {
        return elements(ucte).filter(e -> e != element)
                .noneMatch(e -> node.equals(e.getId().getNodeCode1()) || node.equals(e.getId().getNodeCode2()));
    }
}
