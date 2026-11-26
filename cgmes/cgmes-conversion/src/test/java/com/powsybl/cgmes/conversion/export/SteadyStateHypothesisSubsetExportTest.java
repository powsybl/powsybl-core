/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.extensions.CgmesMetadataModelsAdder;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.statement.CgmesObject;
import com.powsybl.cgmes.model.statement.CgmesProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static com.powsybl.cgmes.conversion.export.CgmesTestModels.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class SteadyStateHypothesisSubsetExportTest {

    private static final Pattern SSH_OBJECT = Pattern.compile("<cim:(\\w+) rdf:about=\"#_?([^\"]+)\">(.*?)</cim:\\1>", Pattern.DOTALL);

    @Test
    void partialExportOfAllIdentifiablesExportsTheSameObjects() throws XMLStreamException {
        assertPartialExportOfAllIdentifiablesExportsTheSameObjects(cgmesRoundTrip(FourSubstationsNodeBreakerFactory.create()));
    }

    @Test
    void partialExportOfAllIdentifiablesOfABusBreakerNetworkExportsTheSameObjects() throws XMLStreamException {
        assertPartialExportOfAllIdentifiablesExportsTheSameObjects(cgmesRoundTrip(EurostagTutorialExample1Factory.create()));
    }

    static Stream<Arguments> networksWithRegulatingControls() {
        return Stream.of(
            Arguments.of("three windings transformer", (Supplier<Network>) ThreeWindingsTransformerNetworkFactory::create),
            Arguments.of("phase tap changer", (Supplier<Network>) PhaseShifterTestCaseFactory::createLocalActivePowerWithTargetDeadband),
            Arguments.of("static var compensator", (Supplier<Network>) SvcTestCaseFactory::createLocalVoltageControl));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("networksWithRegulatingControls")
    void partialExportOfAllIdentifiablesWithRegulatingControlsExportsTheSameObjects(String description, Supplier<Network> networkSupplier) throws XMLStreamException {
        assertPartialExportOfAllIdentifiablesExportsTheSameObjects(networkSupplier.get());
    }

    private static void assertPartialExportOfAllIdentifiablesExportsTheSameObjects(Network network) throws XMLStreamException {
        Set<Identifiable<?>> all = new LinkedHashSet<>();
        network.getConnectables().forEach(all::add);
        network.getSwitches().forEach(all::add);
        network.getHvdcLines().forEach(all::add);
        network.getAreas().forEach(all::add);
        // The buses of the bus-breaker voltage levels have SSH data: their fictitious injections
        network.getVoltageLevelStream()
            .filter(vl -> vl.getTopologyKind() == TopologyKind.BUS_BREAKER)
            .forEach(vl -> vl.getBusBreakerView().getBuses().forEach(all::add));

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
        // A synchronous machine and two shunt compensators share a regulating control
        Network network = withSharedControl();
        String sharedControl = sharedControl(network);

        SshObject control = byId(sshObjects(exportPartial(network, List.of(network.getShuntCompensator(SHARED_CONTROL_SHUNT))))).get(sharedControl);

        assertNotNull(control);
        assertEquals(byId(sshObjects(exportFull(network))).get(sharedControl), control);
    }

    @Test
    void controlSharedWithATapChangerIsWrittenWithTheViewsOfAllItsHolders(@TempDir Path tmpDir) throws IOException, XMLStreamException {
        // The CGMES import accepts a generator that refers to the TapChangerControl of a ratio tap changer
        Path dir = Files.createDirectories(tmpDir.resolve("shared"));
        EurostagTutorialExample1Factory.create().write("CGMES", null, dir.resolve("eurostag"));
        try (Stream<Path> files = Files.list(dir)) {
            for (Path eqFile : files.filter(p -> p.getFileName().toString().contains("_EQ")).toList()) {
                String eq = Files.readString(eqFile);
                Matcher tapChangerControl = Pattern.compile("<cim:TapChangerControl rdf:ID=\"_?([^\"]+)\"").matcher(eq);
                assertTrue(tapChangerControl.find());
                Matcher generatorControl = Pattern.compile("(<cim:SynchronousMachine rdf:ID=\"_?[^\"]+\">.*?<cim:RegulatingCondEq.RegulatingControl rdf:resource=\"#_?)[^\"]+(\")",
                    Pattern.DOTALL).matcher(eq);
                assertTrue(generatorControl.find());
                Files.writeString(eqFile, generatorControl.replaceFirst(Matcher.quoteReplacement(
                    generatorControl.group(1) + tapChangerControl.group(1) + generatorControl.group(2))));
            }
        }
        Network network = Network.read(dir);
        Generator generator = network.getGenerator("GEN");
        String sharedControl = generator.getProperty(PROPERTY_REGULATING_CONTROL);

        // Only the generator is exported, but its control is the TapChangerControl of the full export
        SshObject full = byId(sshObjects(exportFull(network))).get(sharedControl);
        SshObject partial = byId(sshObjects(exportPartial(network, List.of(generator)))).get(sharedControl);
        assertEquals("TapChangerControl", full.className());
        assertEquals(full, partial);
    }

    @Test
    void sharedControlWithDifferentTargetValuesIsNotExported() {
        Network network = withSharedControl();

        // The user changes the target value of only one of the holders
        Generator generator = network.getGenerator(SHARED_CONTROL_GENERATOR);
        double target = generator.getRegulatingTargetV() + 1.0;
        if (generator.getVoltageRegulation().getTerminal() != null) {
            generator.getVoltageRegulation().setTargetValue(target);
        } else {
            generator.setLocalTargetV(target);
        }
        List<Identifiable<?>> shunt = List.of(network.getShuntCompensator(SHARED_CONTROL_SHUNT));

        PowsyblException e = assertThrows(PowsyblException.class, () -> exportPartial(network, shunt));
        assertTrue(e.getMessage().contains(sharedControl(network)));
        assertTrue(e.getMessage().contains("See the section \"Shared regulating controls\""));
    }

    @Test
    void failedPartialExportWritesNothing() throws XMLStreamException {
        Network network = withSharedControl();
        Generator generator = network.getGenerator(SHARED_CONTROL_GENERATOR);
        double target = generator.getRegulatingTargetV() + 1.0;
        if (generator.getVoltageRegulation().getTerminal() != null) {
            generator.getVoltageRegulation().setTargetValue(target);
        } else {
            generator.setLocalTargetV(target);
        }
        CgmesExportContext context = new CgmesExportContext(network)
            .setSshExportedIdentifiables(List.of(network.getShuntCompensator(SHARED_CONTROL_SHUNT)));
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);

        // The check of the shared regulating control fails before the header is written
        assertThrows(PowsyblException.class, () -> SteadyStateHypothesisExport.write(network, writer, context));
        writer.flush();
        assertFalse(stringWriter.toString().contains("RDF"));
        assertFalse(stringWriter.toString().contains("FullModel"));
    }

    @Test
    void sharedControlWithDifferentDeadbandsIsNotExported() throws XMLStreamException {
        Network network = withSharedControl();
        ShuntCompensator shunt2 = network.getShuntCompensator(SHARED_CONTROL_SHUNT_2);
        assertNotNull(byId(sshObjects(exportPartial(network, List.of(shunt2)))).get(sharedControl(network)));

        // The user changes the deadband of only one of the shunts
        shunt2.getVoltageRegulation().setTargetDeadband(shunt2.getVoltageRegulation().getTargetDeadband() + 1.0);

        List<Identifiable<?>> shunt = List.of(shunt2);
        PowsyblException e = assertThrows(PowsyblException.class, () -> exportPartial(network, shunt));
        assertTrue(e.getMessage().contains(sharedControl(network)));
        assertTrue(e.getMessage().contains("See the section \"Shared regulating controls\""));
    }

    @Test
    void partialExportOfOneGeneratorExportsItsObjects() throws XMLStreamException {
        Network network = cgmesRoundTrip(EurostagTutorialExample1Factory.create());
        Generator generator = network.getGeneratorStream()
            .filter(g -> g.hasProperty(PROPERTY_REGULATING_CONTROL))
            .findFirst()
            .orElseThrow();
        // The generating unit has SSH data only with a participation factor
        generator.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4.0).withParticipationFactor(1.0).add();

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
        Network network = cgmesRoundTrip(EurostagTutorialExample1Factory.create());
        Generator generator = network.getGenerators().iterator().next();

        List<SshObject> once = sshObjects(exportPartial(network, List.of(generator)));
        List<SshObject> twice = sshObjects(exportPartial(network, List.of(generator, generator)));

        assertEquals(once, twice);
    }

    @Test
    void partialExportWithoutIdentifiablesExportsNoObject() throws XMLStreamException {
        Network network = cgmesRoundTrip(EurostagTutorialExample1Factory.create());

        String ssh = exportPartial(network, List.of());

        assertTrue(sshObjects(ssh).isEmpty());
        assertTrue(ssh.contains("FullModel"));
    }

    @Test
    void identifiablesWithoutSshDataAreIgnored() throws XMLStreamException {
        Network network = cgmesRoundTrip(EurostagTutorialExample1Factory.create());
        List<Identifiable<?>> withoutSshData = new ArrayList<>();
        withoutSshData.add(network);
        network.getSubstations().forEach(withoutSshData::add);
        network.getVoltageLevels().forEach(withoutSshData::add);

        assertTrue(sshObjects(exportPartial(network, withoutSshData)).isEmpty());
    }

    @Test
    void partialExportOfABusBreakerGenerator() throws XMLStreamException {
        Network network = EurostagTutorialExample1Factory.create();
        Generator generator = network.getGenerator("GEN");

        List<SshObject> partial = sshObjects(exportPartial(network, List.of(generator)));

        // The generator and its terminal
        assertEquals(List.of("RegulatingControl", "SynchronousMachine", "Terminal"), partial.stream().map(SshObject::className).sorted().toList());
        assertEquals("SynchronousMachine", byId(partial).get(generator.getId()).className());
    }

    @Test
    void busExportsItsFictitiousInjection() throws XMLStreamException {
        Network network = EurostagTutorialExample1Factory.create();
        Bus bus = network.getBusBreakerView().getBus("NGEN");
        bus.setFictitiousP0(10.0);

        List<SshObject> partial = sshObjects(exportPartial(network, List.of(bus)));

        // The fictitious load and its terminal
        assertEquals(List.of("NonConformLoad", "Terminal"), partial.stream().map(SshObject::className).sorted().toList());
    }

    @Test
    void exportSendsTheWrittenObjectsToTheSink() throws XMLStreamException {
        Network network = cgmesRoundTrip(EurostagTutorialExample1Factory.create());
        List<Identifiable<?>> identifiables = List.of(network.getGenerators().iterator().next(), network.getLoads().iterator().next());

        assertExportSendsTheWrittenObjects(network, new CgmesExportContext(network)
            .setSshExportedIdentifiables(identifiables));
        assertExportSendsTheWrittenObjects(network, new CgmesExportContext(network));
    }

    private static void assertExportSendsTheWrittenObjects(Network network, CgmesExportContext context) throws XMLStreamException {
        List<CgmesObject> sent = new ArrayList<>();
        SteadyStateHypothesisExport.export(network, sent::add, context);
        List<SshObject> written = sshObjects(export(network, context));

        assertFalse(sent.isEmpty());
        assertEquals(written, sent.stream().map(SteadyStateHypothesisSubsetExportTest::sshObject).toList());
    }

    // The object as the XML writer writes it, in the form of sshObjects
    private static SshObject sshObject(CgmesObject cgmesObject) {
        StringBuilder content = new StringBuilder();
        for (CgmesProperty property : cgmesObject.properties()) {
            if (property.resource()) {
                content.append("<cim:").append(property.predicate()).append(" rdf:resource=\"").append(property.object()).append("\"/>");
            } else {
                String text = property.object().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                content.append("<cim:").append(property.predicate()).append('>').append(text)
                    .append("</cim:").append(property.predicate()).append('>');
            }
            content.append(' ');
        }
        return new SshObject(cgmesObject.type(), cgmesObject.subject().replaceFirst("^_", ""),
            content.toString().replaceAll("\\s+", " ").trim());
    }

    @Test
    void modelCreatedIsWrittenInTheHeader() throws XMLStreamException {
        Network network = EurostagTutorialExample1Factory.create();
        CgmesExportContext context = new CgmesExportContext(network);
        assertNull(context.getModelCreated());

        context.setModelCreated(ZonedDateTime.parse("2026-11-19T10:00:00Z"));
        String created = "<md:Model.created>2026-11-19T10:00:00Z</md:Model.created>";

        assertTrue(export(network, context).contains(created));
        StringWriter sv = new StringWriter();
        StateVariablesExport.write(network, XmlUtil.initializeWriter(true, "    ", sv), context);
        assertTrue(sv.toString().contains(created));

        context.setSshExportedIdentifiables(List.of(network.getGenerator("GEN")));
        assertTrue(export(network, context).contains(created));
    }

    @Test
    void headerExampleOfTheDocumentationHasANewIdAndSupersedesThePreviousModel() throws XMLStreamException {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CgmesMetadataModelsAdder.class)
            .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setId("previous-ssh")
                .setVersion(1)
                .setModelingAuthoritySet("http://example.com/modeling-authority")
                .addSupersedes("older-ssh")
                .addProfile("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1")
            .add()
            .add();
        CgmesExportContext context = new CgmesExportContext(network)
            .setSshExportedIdentifiables(List.of(network.getGenerator("GEN")));

        // Without a model update, the model keeps the id of the SSH model of the network
        assertEquals("previous-ssh",
            CgmesExport.initializeModelForExport(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, context, true, false).getId());

        // The example of the documentation
        context.setModelVersion(String.valueOf(1 + 1));
        CgmesMetadataModel model = CgmesExport.initializeModelForExport(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, context, true, true);
        model.clearSupersedes();
        model.addSupersedes("previous-ssh");
        StringWriter stringWriter = new StringWriter();
        SteadyStateHypothesisExport.write(network, XmlUtil.initializeWriter(true, "    ", stringWriter), context, model);
        String ssh = stringWriter.toString();

        List<String> ids = Pattern.compile("FullModel rdf:about=\"(.*?)\"").matcher(ssh).results().map(r -> r.group(1)).toList();
        assertEquals(1, ids.size());
        assertNotEquals("previous-ssh", ids.get(0));
        assertEquals(List.of("previous-ssh"),
            Pattern.compile("Model.Supersedes rdf:resource=\"(.*?)\"").matcher(ssh).results().map(r -> r.group(1)).toList());
    }

    @Test
    void exportedIdentifiablesMakeTheExportPartial() {
        Network network = cgmesRoundTrip(EurostagTutorialExample1Factory.create());
        CgmesExportContext context = new CgmesExportContext(network);
        assertFalse(context.isPartialSshExport());
        assertEquals(List.of(), context.getSshExportedIdentifiables());

        Generator generator = network.getGenerators().iterator().next();
        context.setSshExportedIdentifiables(List.of(generator));
        assertTrue(context.isPartialSshExport());
        assertEquals(List.of(generator), context.getSshExportedIdentifiables());
    }

    private static String exportFull(Network network) throws XMLStreamException {
        return export(network, new CgmesExportContext(network));
    }

    private static String exportPartial(Network network, Collection<? extends Identifiable<?>> identifiables) throws XMLStreamException {
        CgmesExportContext context = new CgmesExportContext(network)
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
