/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.commons.PowsyblException;
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
    void partialExportOfAllIdentifiablesExportsTheSameObjects() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        Set<Identifiable<?>> all = new LinkedHashSet<>();
        network.getConnectables().forEach(all::add);
        network.getSwitches().forEach(all::add);
        network.getHvdcLines().forEach(all::add);
        network.getAreas().forEach(all::add);

        List<SshObject> full = sshObjects(exportFull(network));
        List<SshObject> partial = sshObjects(exportPartial(network, all));

        // The same objects, each one written only once
        assertEquals(ids(full), ids(partial));
        assertEquals(partial.size(), new HashSet<>(ids(partial)).size());
        Map<String, SshObject> partialById = byId(partial);
        full.forEach(object -> {
            // The combined view of a regulating control depends on the order of its users
            if (!object.className().equals("RegulatingControl") && !object.className().equals("TapChangerControl")) {
                assertEquals(object, partialById.get(object.id()));
            }
        });
    }

    @Test
    void partialExportOfOneGeneratorExportsItsObjects() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        Generator generator = network.getGeneratorStream()
            .filter(g -> g.hasProperty(PROPERTY_REGULATING_CONTROL))
            .findFirst()
            .orElseThrow();

        List<SshObject> partial = sshObjects(exportPartial(network, List.of(generator)));

        // The generator, its terminal, its regulating control and its generating unit
        assertEquals(4, partial.size());
        assertEquals("SynchronousMachine", byId(partial).get(generator.getId()).className());
        List<String> classes = partial.stream().map(SshObject::className).toList();
        assertTrue(classes.containsAll(List.of("RegulatingControl", "SynchronousMachine", "Terminal")));
        assertTrue(classes.stream().anyMatch(c -> c.endsWith("GeneratingUnit")));
    }

    @Test
    void partialExportWritesEachObjectOnce() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        Generator generator = network.getGenerators().iterator().next();

        List<SshObject> once = sshObjects(exportPartial(network, List.of(generator)));
        List<SshObject> twice = sshObjects(exportPartial(network, List.of(generator, generator)));

        assertEquals(once, twice);
    }

    @Test
    void partialExportWithoutIdentifiablesExportsNoObject() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());

        String ssh = exportPartial(network, List.of());

        assertTrue(sshObjects(ssh).isEmpty());
        assertTrue(ssh.contains("FullModel"));
    }

    @Test
    void identifiablesWithoutSshDataAreIgnored() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        List<Identifiable<?>> withoutSshData = new ArrayList<>();
        withoutSshData.add(network);
        network.getSubstations().forEach(withoutSshData::add);
        network.getVoltageLevels().forEach(withoutSshData::add);

        assertTrue(sshObjects(exportPartial(network, withoutSshData)).isEmpty());
    }

    @Test
    void partialExportOfABusBreakerNetworkIsNotSupported() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Identifiable<?>> generator = List.of(network.getGenerator("GEN"));

        PowsyblException e = assertThrows(PowsyblException.class, () -> exportPartial(network, generator));
        assertTrue(e.getMessage().contains("node-breaker"));
    }

    @Test
    void fullExportIgnoresTheIdentifiables() throws XMLStreamException {
        Network network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        CgmesExportContext context = new CgmesExportContext(network)
            .setSshExportedIdentifiables(List.of(network.getGenerators().iterator().next()));

        assertEquals(sshObjects(exportFull(network)), sshObjects(export(network, context)));
    }

    private static String exportFull(Network network) throws XMLStreamException {
        return export(network, new CgmesExportContext(network));
    }

    private static String exportPartial(Network network, Collection<? extends Identifiable<?>> identifiables) throws XMLStreamException {
        CgmesExportContext context = new CgmesExportContext(network)
            .setPartialSshExport(true)
            .setSshExportedIdentifiables(identifiables);
        return export(network, context);
    }

    private static String export(Network network, CgmesExportContext context) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisExport.write(network, writer, context);
        return stringWriter.toString();
    }

    private static List<SshObject> sshObjects(String ssh) {
        List<SshObject> objects = new ArrayList<>();
        Matcher m = SSH_OBJECT.matcher(ssh);
        while (m.find()) {
            objects.add(new SshObject(m.group(1), m.group(2), m.group(3).replaceAll("\\s+", " ").trim()));
        }
        return objects;
    }

    private static Set<String> ids(List<SshObject> objects) {
        Set<String> ids = new TreeSet<>();
        objects.forEach(o -> ids.add(o.id()));
        return ids;
    }

    private static Map<String, SshObject> byId(List<SshObject> objects) {
        Map<String, SshObject> byId = new HashMap<>();
        objects.forEach(o -> byId.put(o.id(), o));
        return byId;
    }

    private record SshObject(String className, String id, String content) {
    }
}
