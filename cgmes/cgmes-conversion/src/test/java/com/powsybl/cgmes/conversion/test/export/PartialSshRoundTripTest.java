/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.SshExportedIdentifiables;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
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
        GridModelReference smallGrid = CgmesConformity1Catalog.smallNodeBreaker();
        GridModelReference miniGrid = CgmesConformity1Catalog.miniNodeBreaker();
        GridModelReference smallGridHvdc = CgmesConformity1Catalog.smallNodeBreakerHvdc();
        GridModelReference cgmes3SmallGrid = Cgmes3Catalog.smallGrid();
        return Stream.of(
            Arguments.of("load p0 and q0", smallGrid, (Consumer<Network>) n -> {
                Load load = n.getLoads().iterator().next();
                load.setP0(load.getP0() + 10.0).setQ0(load.getQ0() + 5.0);
            }),
            Arguments.of("generator targetP", smallGrid, (Consumer<Network>) n -> {
                Generator generator = n.getGenerators().iterator().next();
                generator.setTargetP(generator.getTargetP() + 10.0);
            }),
            Arguments.of("generator stops regulating", smallGrid, (Consumer<Network>) n -> n.getGeneratorStream()
                .filter(g -> g.getVoltageRegulation() != null && g.getVoltageRegulation().isRegulating())
                .findFirst().orElseThrow().getVoltageRegulation().setRegulating(false)),
            Arguments.of("generator target voltage", smallGrid, (Consumer<Network>) n -> {
                Generator generator = n.getGeneratorStream().filter(g -> g.hasProperty(PROPERTY_REGULATING_CONTROL)).findFirst().orElseThrow();
                generator.setTargetV(generator.getRegulatingTargetV() + 1.0);
            }),
            Arguments.of("ratio tap position", smallGrid, (Consumer<Network>) n -> {
                RatioTapChanger rtc = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasRatioTapChanger)
                    .findFirst().orElseThrow().getRatioTapChanger();
                rtc.setTapPosition(rtc.getTapPosition() == rtc.getHighTapPosition() ? rtc.getTapPosition() - 1 : rtc.getTapPosition() + 1);
            }),
            Arguments.of("shunt sections", smallGrid, (Consumer<Network>) n -> {
                ShuntCompensator shunt = n.getShuntCompensators().iterator().next();
                shunt.setSectionCount(shunt.getSectionCount() == shunt.getMaximumSectionCount() ? shunt.getSectionCount() - 1 : shunt.getSectionCount() + 1);
            }),
            Arguments.of("boundary line p0", smallGrid, (Consumer<Network>) n -> {
                BoundaryLine boundaryLine = n.getBoundaryLines(BoundaryLineFilter.ALL).iterator().next();
                boundaryLine.setP0(boundaryLine.getP0() + 10.0);
            }),
            Arguments.of("injection disconnection", smallGrid, (Consumer<Network>) n -> assertTrue(n.getConnectableStream()
                .filter(Injection.class::isInstance).map(c -> (Injection<?>) c).anyMatch(i -> i.getTerminal().disconnect()))),
            Arguments.of("breaker opening", miniGrid, (Consumer<Network>) n -> n.getSwitchStream()
                .filter(sw -> sw.getKind() == SwitchKind.BREAKER && !sw.isOpen()).findFirst().orElseThrow().setOpen(true)),
            Arguments.of("transformer disconnection", miniGrid, (Consumer<Network>) n -> assertTrue(n.getTwoWindingsTransformers().iterator().next().disconnect())),
            Arguments.of("HVDC active power setpoint", smallGridHvdc, (Consumer<Network>) n -> {
                HvdcLine hvdcLine = n.getHvdcLines().iterator().next();
                hvdcLine.setActivePowerSetpoint(hvdcLine.getActivePowerSetpoint() + 10.0);
            }),
            Arguments.of("shared regulating control, all holders changed", cgmes3SmallGrid, (Consumer<Network>) n -> {
                // A synchronous machine and a shunt compensator share this regulating control
                Generator generator = n.getGenerator("04633e31-c766-11e1-8775-005056c00008");
                ShuntCompensator shunt = n.getShuntCompensator("045b00d9-c766-11e1-8775-005056c00008");
                double target = generator.getRegulatingTargetV() + 1.0;
                setTargetV(generator, target);
                setTargetV(shunt, target);
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
    void roundTrip(String description, GridModelReference model, Consumer<Network> change) throws IOException, XMLStreamException {
        ReadOnlyDataSource source = model.dataSource();

        // The network of the user: record the change and export it as a partial SSH
        Network network = Network.read(source);
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        network.addListener(recorder);
        change.accept(network);
        network.removeListener(recorder);
        Set<Identifiable<?>> changed = SshExportedIdentifiables.fromEvents(network, recorder.getEvents());
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
            .setPartialSshExport(true)
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
