/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class SteadyStateHypothesisSubsetExportTest {

    private static final String SHARED_CONTROL = "5f711f63-f631-45d3-b26c-33821ab2ee56";
    private static final String SHARED_CONTROL_GENERATOR = "04633e31-c766-11e1-8775-005056c00008";
    private static final String SHARED_CONTROL_SHUNT = "045b00d9-c766-11e1-8775-005056c00008";
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
        full.forEach(object -> assertEquals(object, partialById.get(object.id())));
    }

    @Test
    void sharedControlIsWrittenWithTheViewsOfAllItsHolders() throws XMLStreamException {
        // In CGMES 3 SmallGrid, a synchronous machine and a shunt compensator share a regulating control
        Network network = Network.read(Cgmes3Catalog.smallGrid().dataSource());
        assertEquals(SHARED_CONTROL, network.getGenerator(SHARED_CONTROL_GENERATOR).getProperty(PROPERTY_REGULATING_CONTROL));
        assertEquals(SHARED_CONTROL, network.getShuntCompensator(SHARED_CONTROL_SHUNT).getProperty(PROPERTY_REGULATING_CONTROL));

        SshObject control = byId(sshObjects(exportPartial(network, List.of(network.getShuntCompensator(SHARED_CONTROL_SHUNT))))).get(SHARED_CONTROL);

        assertNotNull(control);
        assertEquals(byId(sshObjects(exportFull(network))).get(SHARED_CONTROL), control);
    }

    @Test
    void sharedControlWithDifferentTargetValuesIsNotExported() {
        Network network = Network.read(Cgmes3Catalog.smallGrid().dataSource());

        // The user changes the target value of only one of the holders
        Generator generator = network.getGenerator(SHARED_CONTROL_GENERATOR);
        double target = generator.getRegulatingTargetV() + 1.0;
        if (generator.getVoltageRegulation().getTerminal() != null) {
            generator.getVoltageRegulation().setTargetValue(target);
        } else {
            generator.setTargetV(target);
        }
        List<Identifiable<?>> shunt = List.of(network.getShuntCompensator(SHARED_CONTROL_SHUNT));

        PowsyblException e = assertThrows(PowsyblException.class, () -> exportPartial(network, shunt));
        assertTrue(e.getMessage().contains(SHARED_CONTROL));
    }

    @Test
    void sharedControlWithDifferentDeadbandsIsNotExported(@TempDir Path tmpDir) throws IOException, XMLStreamException {
        // The EQ is changed so that a second shunt also refers to the regulating control of a first shunt
        Network base = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        List<ShuntCompensator> shunts = regulatingHolders(base.getShuntCompensatorStream());
        String sharedControl = shunts.get(0).getProperty(PROPERTY_REGULATING_CONTROL);
        Network network = withSharedControl(tmpDir, shunts.get(1).getId(), sharedControl);
        ShuntCompensator shunt2 = network.getShuntCompensator(shunts.get(1).getId());
        assertNotNull(byId(sshObjects(exportPartial(network, List.of(shunt2)))).get(sharedControl));

        // The user changes the deadband of only one of the shunts
        shunt2.getVoltageRegulation().setTargetDeadband(shunt2.getVoltageRegulation().getTargetDeadband() + 1.0);

        PowsyblException e = assertThrows(PowsyblException.class, () -> exportPartial(network, List.of(shunt2)));
        assertTrue(e.getMessage().contains(sharedControl));
    }

    private static <T extends Identifiable<T> & VoltageRegulationHolder<T>> List<T> regulatingHolders(Stream<T> holders) {
        return holders.filter(h -> h.hasProperty(PROPERTY_REGULATING_CONTROL) && h.getVoltageRegulation() != null).toList();
    }

    /**
     * A copy of SmallGrid NodeBreaker where the given holder refers to the given regulating control in the EQ.
     */
    private static Network withSharedControl(Path tmpDir, String holderId, String regulatingControlId) throws IOException {
        ReadOnlyDataSource source = CgmesConformity1Catalog.smallNodeBreaker().dataSource();
        Pattern holder = Pattern.compile("(<cim:(\\w+) rdf:ID=\"_?" + Pattern.quote(holderId)
            + "\">.*?<cim:RegulatingCondEq.RegulatingControl rdf:resource=\"#_?)[^\"]+(\".*?</cim:\\2>)", Pattern.DOTALL);
        boolean changed = false;
        for (String name : source.listNames(".*")) {
            String content;
            try (InputStream is = source.newInputStream(name)) {
                content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (name.contains("_EQ") && !name.contains("_BD")) {
                Matcher m = holder.matcher(content);
                if (m.find()) {
                    content = m.replaceFirst(Matcher.quoteReplacement(m.group(1) + regulatingControlId + m.group(3)));
                    changed = true;
                }
            }
            Files.writeString(tmpDir.resolve(name.replaceAll("^.*/", "")), content);
        }
        assertTrue(changed);
        Network network = Network.read(tmpDir);
        assertEquals(regulatingControlId, network.getIdentifiable(holderId).getProperty(PROPERTY_REGULATING_CONTROL));
        return network;
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
