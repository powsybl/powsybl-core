/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class SteadyStateHypothesisSubsetExportTest {

    private static final Pattern SSH_OBJECT = Pattern.compile("<cim:(\\w+) rdf:about=\"#_?([^\"]+)\">(.*?)</cim:\\1>", Pattern.DOTALL);

    @Test
    void subsetWithAllIdentifiablesExportsTheSameObjects() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        Set<Identifiable<?>> all = new LinkedHashSet<>();
        network.getConnectables().forEach(all::add);
        network.getSwitches().forEach(all::add);
        network.getHvdcLines().forEach(all::add);
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

        List<SshStatement> statements = exportSubset(network, List.of(generator));

        // The generator, its terminal, its regulating control and its generating unit
        assertEquals(List.of("GeneratingUnitSsh", "RegulatingControlSsh", "SynchronousMachineSsh", "TerminalSsh"), recordNames(statements));
        assertTrue(statements.stream().anyMatch(s -> s.rdfId().equals(generator.getId())));
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

        List<SshStatement> statements = exportSubset(network, List.of(bus));

        // The fictitious load and its terminal
        assertEquals(List.of("EnergyConsumerSsh", "TerminalSsh"), recordNames(statements));
    }

    private static String exportFull(Network network) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisExport.write(network, writer, new CgmesExportContext(network));
        return stringWriter.toString();
    }

    private static List<SshStatement> exportSubset(Network network, Collection<? extends Identifiable<?>> identifiables) throws XMLStreamException {
        List<SshStatement> statements = new ArrayList<>();
        SteadyStateHypothesisExport.exportIdentifiables(identifiables, statements::add, new CgmesExportContext(network));
        return statements;
    }

    private static String write(Network network, List<SshStatement> statements) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        CgmesExportContext context = new CgmesExportContext(network);
        CgmesExportUtil.writeRdfRoot(context, writer);
        for (SshStatement statement : statements) {
            statement.writeSsh(writer, context);
        }
        writer.writeEndDocument();
        return stringWriter.toString();
    }

    private static List<String> recordNames(List<SshStatement> statements) {
        return statements.stream().map(s -> s.getClass().getSimpleName()).sorted().toList();
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
