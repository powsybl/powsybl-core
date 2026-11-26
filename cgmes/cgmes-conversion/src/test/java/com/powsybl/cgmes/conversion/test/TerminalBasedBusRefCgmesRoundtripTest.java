/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TerminalBasedBusRefCgmesRoundtripTest {

    @Test
    void referencesSurviveNodeBreakerCgmesRoundtripWhenTerminalMembershipIsPreserved() throws IOException {
        ConversionTester originalTester = newConversionTester();
        originalTester.testConversion(null, CgmesConformity1Catalog.miniNodeBreaker());
        ConversionTester reorderedTester = newConversionTester();
        ReorderedGridModelReference reorderedReference = new ReorderedGridModelReference(
                CgmesConformity1Catalog.miniNodeBreaker(), new AtomicInteger());
        reorderedTester.testConversion(null, reorderedReference);
        assertTrue(reorderedReference.reorderedProfiles().get() > 0, "The equipment profile must actually be reordered");

        Network original = originalTester.lastConvertedNetwork();
        Network roundTrip = originalTester.lastExportImportCgmesNetwork();
        Network reorderedRoundTrip = reorderedTester.lastExportImportCgmesNetwork();
        assertNotNull(roundTrip);
        assertNotNull(reorderedRoundTrip);
        Map<TerminalBasedBusRef, Bus> originalBuses = getBusesByReference(original);
        Map<TerminalBasedBusRef, Bus> roundTripBuses = getBusesByReference(roundTrip);
        Map<TerminalBasedBusRef, Bus> reorderedBuses = getBusesByReference(reorderedRoundTrip);
        assertFalse(originalBuses.isEmpty());
        assertReferencesAndTerminalMembershipMatch(originalBuses, roundTripBuses);
        assertReferencesAndTerminalMembershipMatch(originalBuses, reorderedBuses);

        boolean busIdChanged = originalBuses.entrySet().stream()
                .anyMatch(entry -> !entry.getValue().getId().equals(reorderedBuses.get(entry.getKey()).getId()));
        assertTrue(busIdChanged, "Different CGMES node allocation must be allowed to change computed bus IDs");
    }

    private static ConversionTester newConversionTester() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "EQ,TP,SSH,SV");
        exportParams.put(CgmesExport.MODELING_AUTHORITY_SET, "http://A1.de/Planning/ENTSOE/2");
        ComparisonConfig comparisonConfig = new ComparisonConfig()
                .tolerance(1e-5)
                .checkNetworkId(false)
                .exportedSubset(Set.of(CgmesSubset.EQUIPMENT, CgmesSubset.TOPOLOGY,
                        CgmesSubset.STEADY_STATE_HYPOTHESIS, CgmesSubset.STATE_VARIABLES))
                .onlyReportDifferences();
        ConversionTester tester = new ConversionTester(importParams, exportParams,
                TripleStoreFactory.onlyDefaultImplementation(), comparisonConfig);
        tester.setTestExportImportCgmes(true);
        return tester;
    }

    private static void assertReferencesAndTerminalMembershipMatch(Map<TerminalBasedBusRef, Bus> first,
                                                                    Map<TerminalBasedBusRef, Bus> second) {
        assertEquals(first.keySet(), second.keySet());
        first.forEach((reference, bus) ->
                assertEquals(getEligibleTerminalIdentities(bus), getEligibleTerminalIdentities(second.get(reference))));
    }

    private static Map<TerminalBasedBusRef, Bus> getBusesByReference(Network network) {
        Map<TerminalBasedBusRef, Bus> buses = new HashMap<>();
        network.getBusView().getBusStream().forEach(bus -> {
            assertTrue(TerminalBasedBusRef.of(bus, connectable -> false).isEmpty());
            TerminalBasedBusRef.of(bus).ifPresentOrElse(reference ->
                    assertNull(buses.put(reference, bus)),
                    () -> assertTrue(getEligibleTerminalIdentities(bus).isEmpty()));
        });
        return buses;
    }

    private static Set<TerminalIdentity> getEligibleTerminalIdentities(Bus bus) {
        return bus.getConnectedTerminalStream()
                .filter(terminal -> !terminal.getConnectable().isFictitious()
                        && !(terminal.getConnectable() instanceof BusbarSection))
                .map(terminal -> new TerminalIdentity(terminal.getConnectable().getId(),
                        Terminal.getConnectableSide(terminal).orElse(null),
                        Terminal.getConnectableTerminalNumber(terminal).orElse(null)))
                .collect(Collectors.toSet());
    }

    private record TerminalIdentity(String connectableId, ThreeSides side, TerminalNumber terminalNumber) {
    }

    private record ReorderedGridModelReference(GridModelReference delegate, AtomicInteger reorderedProfiles) implements GridModelReference {

        @Override
        public CgmesModel expected() {
            return delegate.expected();
        }

        @Override
        public ReadOnlyDataSource dataSource() {
            return new ReorderedDataSource(delegate.dataSource(), reorderedProfiles);
        }

        @Override
        public String name() {
            return delegate.name() + "-reordered";
        }
    }

    private static final class ReorderedDataSource implements ReadOnlyDataSource {

        private final ReadOnlyDataSource delegate;
        private final AtomicInteger reorderedProfiles;

        private ReorderedDataSource(ReadOnlyDataSource delegate, AtomicInteger reorderedProfiles) {
            this.delegate = delegate;
            this.reorderedProfiles = reorderedProfiles;
        }

        @Override
        public String getBaseName() {
            return delegate.getBaseName();
        }

        @Override
        public String getDataExtension() {
            return delegate.getDataExtension();
        }

        @Override
        public boolean exists(String suffix, String ext) throws IOException {
            return delegate.exists(suffix, ext);
        }

        @Override
        public boolean exists(String fileName) throws IOException {
            return delegate.exists(fileName);
        }

        @Override
        public boolean isDataExtension(String ext) {
            return delegate.isDataExtension(ext);
        }

        @Override
        public InputStream newInputStream(String suffix, String ext) throws IOException {
            InputStream input = delegate.newInputStream(suffix, ext);
            return isEquipmentProfile(suffix) ? reorder(input) : input;
        }

        @Override
        public InputStream newInputStream(String fileName) throws IOException {
            InputStream input = delegate.newInputStream(fileName);
            return isEquipmentProfile(fileName) ? reorder(input) : input;
        }

        @Override
        public Set<String> listNames(String regex) throws IOException {
            return delegate.listNames(regex);
        }

        private static boolean isEquipmentProfile(String name) {
            String upperCaseName = name.toUpperCase(Locale.ROOT);
            return upperCaseName.contains("_EQ_") || upperCaseName.endsWith("_EQ.XML");
        }

        private InputStream reorder(InputStream input) throws IOException {
            try (input) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                var document = documentBuilderFactory.newDocumentBuilder().parse(input);
                Node root = document.getDocumentElement();
                NodeList childNodes = root.getChildNodes();
                List<Node> children = new ArrayList<>(childNodes.getLength());
                for (int i = 0; i < childNodes.getLength(); i++) {
                    children.add(childNodes.item(i));
                }
                Collections.reverse(children);
                children.forEach(root::removeChild);
                children.forEach(root::appendChild);

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                var transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                var transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.transform(new DOMSource(document), new StreamResult(output));
                reorderedProfiles.incrementAndGet();
                return new ByteArrayInputStream(output.toByteArray());
            } catch (ParserConfigurationException | SAXException | TransformerException | IllegalArgumentException e) {
                throw new IOException("Unable to reorder CGMES equipment elements", e);
            }
        }
    }
}
