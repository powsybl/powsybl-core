/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.CgmesTestModels;
import com.powsybl.cgmes.conversion.export.SshExportedIdentifiables;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Round trip of a partial SSH export:
 * the changes recorded on a network are exported as a partial SSH, the partial SSH is applied with Network.update
 * to a copy of the original network, and the SSH data of both networks must be the same.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class PartialSshRoundTripTest {

    private static final Pattern SSH_OBJECT = Pattern.compile("<cim:(\\w+) rdf:about=\"#_?([^\"]+)\">(.*?)</cim:\\1>", Pattern.DOTALL);

    @TempDir
    Path tmpDir;

    static Stream<Arguments> changes() {
        // CGMES models built in code: node-breaker, bus-breaker, with a regulating shunt, with a shared control
        Supplier<ReadOnlyDataSource> nodeBreaker = () -> CgmesTestModels.cgmesFiles(FourSubstationsNodeBreakerFactory.create());
        Supplier<ReadOnlyDataSource> busBreaker = () -> CgmesTestModels.cgmesFiles(EurostagTutorialExample1Factory.create());
        Supplier<ReadOnlyDataSource> busBreakerShunt = () -> CgmesTestModels.cgmesFiles(ShuntTestCaseFactory.create());
        Supplier<ReadOnlyDataSource> sharedControl = CgmesTestModels::sharedControlFiles;
        return Stream.of(
            Arguments.of("load p0 and q0", nodeBreaker, (Consumer<Network>) n -> {
                Load load = n.getLoads().iterator().next();
                load.setP0(load.getP0() + 10.0).setQ0(load.getQ0() + 5.0);
            }),
            Arguments.of("generator targetP", nodeBreaker, (Consumer<Network>) n -> {
                Generator generator = n.getGenerators().iterator().next();
                generator.setTargetP(generator.getTargetP() + 10.0);
            }),
            Arguments.of("generator stops regulating", nodeBreaker, (Consumer<Network>) n -> n.getGeneratorStream()
                .filter(g -> g.getVoltageRegulation() != null && g.getVoltageRegulation().isRegulating())
                .findFirst().orElseThrow().getVoltageRegulation().setRegulating(false)),
            Arguments.of("generator target voltage", nodeBreaker, (Consumer<Network>) n -> {
                Generator generator = n.getGeneratorStream().filter(g -> g.hasProperty(PROPERTY_REGULATING_CONTROL)).findFirst().orElseThrow();
                generator.setLocalTargetV(generator.getLocalTargetV() + 1.0);
            }),
            Arguments.of("ratio tap position", nodeBreaker, (Consumer<Network>) n -> {
                RatioTapChanger rtc = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasRatioTapChanger)
                    .findFirst().orElseThrow().getRatioTapChanger();
                rtc.setTapPosition(rtc.getTapPosition() == rtc.getHighTapPosition() ? rtc.getTapPosition() - 1 : rtc.getTapPosition() + 1);
            }),
            Arguments.of("shunt sections", nodeBreaker, (Consumer<Network>) n -> {
                ShuntCompensator shunt = n.getShuntCompensators().iterator().next();
                shunt.setSectionCount(shunt.getSectionCount() == shunt.getMaximumSectionCount() ? shunt.getSectionCount() - 1 : shunt.getSectionCount() + 1);
            }),
            Arguments.of("injection disconnection", nodeBreaker, (Consumer<Network>) n -> assertTrue(n.getConnectableStream()
                .filter(Injection.class::isInstance).map(c -> (Injection<?>) c).anyMatch(i -> i.getTerminal().disconnect()))),
            Arguments.of("bus-breaker load p0 and q0", busBreaker, (Consumer<Network>) n -> {
                Load load = n.getLoads().iterator().next();
                load.setP0(load.getP0() + 10.0).setQ0(load.getQ0() + 5.0);
            }),
            Arguments.of("bus-breaker generator targetP", busBreaker, (Consumer<Network>) n -> {
                Generator generator = n.getGenerators().iterator().next();
                generator.setTargetP(generator.getTargetP() + 10.0);
            }),
            Arguments.of("bus-breaker ratio tap position", busBreaker, (Consumer<Network>) n -> {
                RatioTapChanger rtc = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasRatioTapChanger)
                    .findFirst().orElseThrow().getRatioTapChanger();
                rtc.setTapPosition(rtc.getTapPosition() == rtc.getHighTapPosition() ? rtc.getTapPosition() - 1 : rtc.getTapPosition() + 1);
            }),
            Arguments.of("bus-breaker shunt sections", busBreakerShunt, (Consumer<Network>) n -> {
                ShuntCompensator shunt = n.getShuntCompensators().iterator().next();
                shunt.setSectionCount(shunt.getSectionCount() == shunt.getMaximumSectionCount() ? shunt.getSectionCount() - 1 : shunt.getSectionCount() + 1);
            }),
            Arguments.of("bus-breaker injection disconnection", busBreaker, (Consumer<Network>) n -> assertTrue(n.getConnectableStream()
                .filter(Injection.class::isInstance).map(c -> (Injection<?>) c).anyMatch(i -> i.getTerminal().disconnect()))),
            Arguments.of("breaker opening", nodeBreaker, (Consumer<Network>) n -> n.getSwitchStream()
                .filter(sw -> sw.getKind() == SwitchKind.BREAKER && !sw.isOpen()).findFirst().orElseThrow().setOpen(true)),
            Arguments.of("transformer disconnection", nodeBreaker, (Consumer<Network>) n -> assertTrue(n.getTwoWindingsTransformers().iterator().next().disconnect())),
            Arguments.of("HVDC active power setpoint", nodeBreaker, (Consumer<Network>) n -> {
                HvdcLine hvdcLine = n.getHvdcLines().iterator().next();
                hvdcLine.setActivePowerSetpoint(hvdcLine.getActivePowerSetpoint() + 10.0);
            }),
            Arguments.of("shared regulating control, all holders changed", sharedControl, (Consumer<Network>) n -> {
                // A synchronous machine and two shunt compensators share this regulating control
                Generator generator = n.getGenerator(CgmesTestModels.SHARED_CONTROL_GENERATOR);
                double target = generator.getRegulatingTargetV() + 1.0;
                setTargetV(generator, target);
                setTargetV(n.getShuntCompensator(CgmesTestModels.SHARED_CONTROL_SHUNT), target);
                setTargetV(n.getShuntCompensator(CgmesTestModels.SHARED_CONTROL_SHUNT_2), target);
            })
        );
    }

    private static void setTargetV(VoltageRegulationHolder<?> holder, double targetV) {
        VoltageRegulation regulation = holder.getVoltageRegulation();
        if (regulation.getTerminal() != null) {
            regulation.setTargetValue(targetV);
        } else {
            holder.setLocalTargetV(targetV);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("changes")
    void roundTrip(String description, Supplier<ReadOnlyDataSource> model, Consumer<Network> change) throws IOException, XMLStreamException {
        ReadOnlyDataSource source = model.get();

        // The network of the user: record the change and export it as a partial SSH
        Network network = Network.read(source);
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        network.addListener(recorder);
        change.accept(network);
        network.removeListener(recorder);
        Set<Identifiable<?>> changed = SshExportedIdentifiables.fromEvents(network, recorder.getEvents(), SshExportedIdentifiables.UnsupportedChangeBehavior.FAIL);
        assertFalse(changed.isEmpty(), "The change gives no event");
        Path dir = Files.createDirectories(tmpDir.resolve("partial"));
        exportPartialSsh(network, changed, dir.resolve("partial_SSH.xml"));

        // The copy of the original network: the change must be visible in the SSH, then apply the partial SSH
        Network copy = Network.read(source);
        assertFalse(differences(network, copy).isEmpty(), "The change gives no difference in the SSH");
        Properties properties = new Properties();
        properties.put(CgmesImport.USE_PREVIOUS_VALUES_DURING_UPDATE, "true");
        copy.update(new GenericReadOnlyDataSource(dir, "partial"), properties);

        // Both networks must have the same SSH data
        assertEquals(List.of(), differences(network, copy));
    }

    private static void exportPartialSsh(Network network, Set<Identifiable<?>> changed, Path file) throws IOException, XMLStreamException {
        CgmesExportContext context = new CgmesExportContext(network)
            .setSshExportedIdentifiables(changed);
        try (OutputStream os = Files.newOutputStream(file)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            SteadyStateHypothesisExport.write(network, writer, context);
        }
        Map<String, SshObject> partial = sshObjects(Files.readString(file));
        assertFalse(partial.isEmpty());
        assertTrue(partial.size() < sshObjects(fullSsh(network)).size());
    }

    private static List<String> differences(Network expected, Network actual) throws XMLStreamException {
        Map<String, SshObject> expectedObjects = sshObjects(fullSsh(expected));
        Map<String, SshObject> actualObjects = sshObjects(fullSsh(actual));
        List<String> differences = new ArrayList<>();
        expectedObjects.forEach((id, object) -> {
            if (!object.equals(actualObjects.get(id))) {
                differences.add(object + " -> " + actualObjects.get(id));
            }
        });
        actualObjects.keySet().stream().filter(id -> !expectedObjects.containsKey(id)).forEach(id -> differences.add("unexpected " + actualObjects.get(id)));
        return differences;
    }

    private static String fullSsh(Network network) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisExport.write(network, writer, new CgmesExportContext(network));
        return stringWriter.toString();
    }

    private static Map<String, SshObject> sshObjects(String ssh) {
        Map<String, SshObject> objects = new TreeMap<>();
        Matcher m = SSH_OBJECT.matcher(ssh);
        while (m.find()) {
            objects.put(m.group(2), new SshObject(m.group(1), m.group(2), m.group(3).replaceAll("\\s+", " ").trim()));
        }
        return objects;
    }

    private record SshObject(String className, String id, String content) {
    }
}
