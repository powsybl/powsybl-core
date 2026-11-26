/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.statement.CgmesObject;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class SteadyStateHypothesisSubsetExportTest {

    private static final Pattern SSH_OBJECT = Pattern.compile("<cim:(\\w+) rdf:about=\"#_?([^\"]+)\">(.*?)</cim:\\1>", Pattern.DOTALL);

    /**
     * Networks with all the kinds of identifiables that have SSH data.
     */
    static Stream<Arguments> networks() {
        return Stream.of(
            Arguments.of("CGMES MicroGrid BE", (Supplier<Network>) () -> Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource())),
            Arguments.of("CGMES SmallGrid node-breaker", (Supplier<Network>) () -> Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource())),
            Arguments.of("CGMES SmallGrid HVDC", (Supplier<Network>) () -> Network.read(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource())),
            Arguments.of("four substations, with SVC, VSC and LCC", (Supplier<Network>) FourSubstationsNodeBreakerFactory::create),
            Arguments.of("battery", (Supplier<Network>) BatteryNetworkFactory::create),
            Arguments.of("tie lines and areas", (Supplier<Network>) EurostagTutorialExample1Factory::createWithTieLinesAndAreas),
            Arguments.of("detailed DC model, VSC", (Supplier<Network>) DcDetailedNetworkFactory::createVscSymmetricalMonopole),
            Arguments.of("detailed DC model, LCC", (Supplier<Network>) DcDetailedNetworkFactory::createLccMonopoleGroundReturn),
            Arguments.of("detailed DC model, DC switch", (Supplier<Network>) DcDetailedNetworkFactory::createSimple2NodesDcSwitch)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("networks")
    void subsetWithAllIdentifiablesExportsTheSameObjects(String description, Supplier<Network> networkSupplier) throws XMLStreamException {
        Network network = networkSupplier.get();
        Set<Identifiable<?>> all = new LinkedHashSet<>();
        network.getConnectables().forEach(all::add);
        network.getSwitches().forEach(all::add);
        network.getHvdcLines().forEach(all::add);
        network.getTieLines().forEach(all::add);
        network.getDcConnectables().forEach(all::add);
        network.getDcSwitches().forEach(all::add);
        network.getAreas().forEach(all::add);
        network.getBusBreakerView().getBuses().forEach(all::add);

        Map<String, SshObject> full = sshObjects(exportFull(network));
        Map<String, SshObject> subset = sshObjects(write(network, exportSubset(network, all)));

        assertEquals(full.keySet(), subset.keySet());
        full.forEach((id, object) -> {
            // The combined view of a regulating control depends on the order of its users
            if (!object.className().equals("RegulatingControl") && !object.className().equals("TapChangerControl")) {
                assertEquals(object, subset.get(id));
            }
        });
    }

    @Test
    void subsetWithOneGeneratorExportsItsObjects() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        Generator generator = network.getGeneratorStream()
            .filter(g -> g.hasProperty(PROPERTY_REGULATING_CONTROL))
            .findFirst()
            .orElseThrow();

        List<CgmesObject> objects = exportSubset(network, List.of(generator));

        // The generator, its terminal, its regulating control and its generating unit
        assertEquals(List.of("GeneratingUnit", "RegulatingControl", "SynchronousMachine", "Terminal"), types(objects));
        assertTrue(objects.stream().anyMatch(o -> o.subject().equals(generator.getId())));
    }

    @Test
    void identifiablesWithoutSshDataAreIgnored() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        List<Identifiable<?>> withoutSshData = new ArrayList<>();
        withoutSshData.add(network);
        network.getSubstations().forEach(withoutSshData::add);
        network.getVoltageLevels().forEach(withoutSshData::add);

        assertTrue(exportSubset(network, withoutSshData).isEmpty());
    }

    @Test
    void busExportsItsFictitiousInjection() throws XMLStreamException {
        Network network = EurostagTutorialExample1Factory.create();
        Bus bus = network.getBusBreakerView().getBus("NGEN");
        bus.setFictitiousP0(10.0);

        List<CgmesObject> objects = exportSubset(network, List.of(bus));

        // The fictitious load and its terminal
        assertEquals(List.of("NonConformLoad", "Terminal"), types(objects));
    }

    private static String exportFull(Network network) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisExport.write(network, writer, new CgmesExportContext(network));
        return stringWriter.toString();
    }

    private static List<CgmesObject> exportSubset(Network network, Collection<? extends Identifiable<?>> identifiables) throws XMLStreamException {
        List<CgmesObject> objects = new ArrayList<>();
        SteadyStateHypothesisExport.exportIdentifiables(identifiables, objects::add, new CgmesExportContext(network));
        return objects;
    }

    private static String write(Network network, List<CgmesObject> objects) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisWriter sshWriter = new SteadyStateHypothesisWriter(writer, new CgmesExportContext(network));
        sshWriter.writeRdfRoot();
        for (CgmesObject object : objects) {
            sshWriter.write(object);
        }
        sshWriter.writeEndDocument();
        return stringWriter.toString();
    }

    private static List<String> types(List<CgmesObject> objects) {
        return objects.stream().map(CgmesObject::type).sorted().toList();
    }

    private static Map<String, SshObject> sshObjects(String ssh) {
        Map<String, SshObject> objects = new TreeMap<>();
        Matcher m = SSH_OBJECT.matcher(ssh);
        while (m.find()) {
            objects.put(m.group(2), new SshObject(m.group(1), m.group(3).replaceAll("\\s+", " ").trim()));
        }
        return objects;
    }

    private record SshObject(String className, String content) {
    }
}
