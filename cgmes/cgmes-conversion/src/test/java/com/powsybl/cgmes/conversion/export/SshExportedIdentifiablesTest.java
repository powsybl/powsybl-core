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
import com.powsybl.iidm.network.events.ExtensionUpdateNetworkEvent;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.export.SshExportedIdentifiables.UnsupportedChangeBehavior.FAIL;
import static com.powsybl.cgmes.conversion.export.SshExportedIdentifiables.UnsupportedChangeBehavior.IGNORE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class SshExportedIdentifiablesTest {

    private static final Pattern SSH_OBJECT_ID = Pattern.compile("<cim:\\w+ rdf:about=\"#_?([^\"]+)\">");

    private Network network;
    private NetworkEventRecorder recorder;

    /**
     * A change of the network: the changed identifiable, and the action to record.
     * The network is prepared when the change is built, before the recording.
     */
    private record Change(Identifiable<?> identifiable, Runnable action) {
    }

    @BeforeEach
    void setUp() {
        network = Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        recorder = new NetworkEventRecorder();
        network.addListener(recorder);
    }

    @Test
    void eventsGiveTheChangedIdentifiablesOnce() {
        Generator generator = network.getGenerators().iterator().next();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformerStream()
            .filter(TwoWindingsTransformer::hasRatioTapChanger)
            .findFirst()
            .orElseThrow();
        Load load = network.getLoads().iterator().next();

        generator.setTargetP(generator.getTargetP() + 1.0);
        RatioTapChanger rtc = transformer.getRatioTapChanger();
        rtc.setTapPosition(rtc.getTapPosition() == rtc.getHighTapPosition() ? rtc.getTapPosition() - 1 : rtc.getTapPosition() + 1);
        load.setP0(load.getP0() + 1.0);
        generator.setTargetP(generator.getTargetP() + 1.0);

        // A tap changer event has the id of its transformer
        assertEquals(List.of(generator, transformer, load), List.copyOf(SshExportedIdentifiables.fromEvents(network, recorder.getEvents(), FAIL)));
    }

    @Test
    void extensionCreationGivesItsIdentifiable() {
        Generator generator = network.getGenerators().iterator().next();
        generator.removeExtension(ActivePowerControl.class);
        recorder.reset();

        generator.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4.0).add();

        assertEquals(List.of(generator), List.copyOf(SshExportedIdentifiables.fromEvents(network, recorder.getEvents(), FAIL)));
    }

    @Test
    void variantEventsAreSkipped() {
        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "other");

        assertFalse(recorder.getEvents().isEmpty());
        assertTrue(SshExportedIdentifiables.fromEvents(network, recorder.getEvents(), FAIL).isEmpty());
    }

    @Test
    void changeOfAnotherVariantIsUnsupported() {
        String initialVariant = network.getVariantManager().getWorkingVariantId();
        network.getVariantManager().cloneVariant(initialVariant, "other");
        network.getVariantManager().setWorkingVariant("other");
        Load load = network.getLoads().iterator().next();
        load.setP0(load.getP0() + 1.0);
        network.getVariantManager().setWorkingVariant(initialVariant);
        List<NetworkEvent> events = recorder.getEvents();

        PowsyblException e = assertThrows(PowsyblException.class, () -> SshExportedIdentifiables.fromEvents(network, events, FAIL));
        assertTrue(e.getMessage().contains("variant other"));
        assertTrue(SshExportedIdentifiables.fromEvents(network, events, IGNORE).isEmpty());

        // On its own variant, the change is supported
        network.getVariantManager().setWorkingVariant("other");
        assertEquals(List.of(load), List.copyOf(SshExportedIdentifiables.fromEvents(network, events, FAIL)));
    }

    @Test
    void ignoreKeepsTheSupportedChanges() {
        Load load = network.getLoads().iterator().next();
        Line line = network.getLines().iterator().next();

        load.setP0(load.getP0() + 1.0);
        line.setR(line.getR() + 1.0);

        assertEquals(List.of(load), List.copyOf(SshExportedIdentifiables.fromEvents(network, recorder.getEvents(), IGNORE)));
    }

    @Test
    void extensionChangesWithoutIidmEventsAreSupported() {
        // IIDM does not notify these changes yet (issue 4244): the events are built here
        Generator generator = network.getGenerators().iterator().next();
        String variantId = network.getVariantManager().getWorkingVariantId();
        List<NetworkEvent> events = List.of(
            new ExtensionUpdateNetworkEvent(generator.getId(), ActivePowerControl.NAME, "participationFactor", variantId, 1.0, 2.0),
            new ExtensionUpdateNetworkEvent(generator.getId(), ReferencePriorities.NAME, "referencePriority", variantId, 0, 1));

        assertEquals(List.of(generator), List.copyOf(SshExportedIdentifiables.fromEvents(network, events, FAIL)));
    }

    static Stream<Arguments> supportedChanges() {
        return Stream.concat(supportedInjectionChanges(), supportedOtherChanges());
    }

    private static Stream<Arguments> supportedInjectionChanges() {
        Supplier<Network> fourSubstations = FourSubstationsNodeBreakerFactory::create;
        Supplier<Network> smallGrid = () -> Network.read(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
        return Stream.of(
            supported("switch open", fourSubstations, n -> {
                Switch sw = n.getSwitches().iterator().next();
                return new Change(sw, () -> sw.setOpen(!sw.isOpen()));
            }),
            supported("DC switch open", DcDetailedNetworkFactory::createSimple2NodesDcSwitch, n -> {
                DcSwitch sw = n.getDcSwitches().iterator().next();
                return new Change(sw, () -> sw.setOpen(!sw.isOpen()));
            }),
            supported("load p0", fourSubstations, n -> {
                Load load = n.getLoads().iterator().next();
                return new Change(load, () -> load.setP0(load.getP0() + 1.0));
            }),
            supported("load q0", fourSubstations, n -> {
                Load load = n.getLoads().iterator().next();
                return new Change(load, () -> load.setQ0(load.getQ0() + 1.0));
            }),
            supported("generator targetP", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setTargetP(g.getTargetP() + 1.0));
            }),
            supported("generator localTargetQ", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setLocalTargetQ(g.getLocalTargetQ() + 1.0));
            }),
            supported("generator localTargetV", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setLocalTargetV(g.getLocalTargetV() + 1.0));
            }),
            supported("generator regulating", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.getVoltageRegulation().setRegulating(!g.getVoltageRegulation().isRegulating()));
            }),
            supported("generator target value", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                regulateRemoteVoltage(g, 400.0);
                return new Change(g, () -> g.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("generator disconnection", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.getTerminal().disconnect());
            }),
            supported("battery targetP", BatteryNetworkFactory::create, n -> {
                Battery b = n.getBatteries().iterator().next();
                return new Change(b, () -> b.setTargetP(b.getTargetP() + 1.0));
            }),
            supported("battery localTargetQ", BatteryNetworkFactory::create, n -> {
                Battery b = n.getBatteries().iterator().next();
                return new Change(b, () -> b.setLocalTargetQ(b.getLocalTargetQ() + 1.0));
            }),
            supported("shunt sectionCount", fourSubstations, n -> {
                ShuntCompensator s = n.getShuntCompensators().iterator().next();
                return new Change(s, () -> s.setSectionCount(s.getSectionCount() == 0 ? 1 : 0));
            }),
            supported("shunt localTargetV", fourSubstations, n -> {
                ShuntCompensator s = n.getShuntCompensators().iterator().next();
                return new Change(s, () -> s.setLocalTargetV(399.0));
            }),
            supported("shunt regulating", smallGrid, n -> {
                ShuntCompensator s = regulatingHolder(n.getShuntCompensatorStream());
                return new Change(s, () -> s.getVoltageRegulation().setRegulating(!s.getVoltageRegulation().isRegulating()));
            }),
            supported("shunt target deadband", smallGrid, n -> {
                ShuntCompensator s = regulatingHolder(n.getShuntCompensatorStream());
                return new Change(s, () -> s.getVoltageRegulation().setTargetDeadband(s.getVoltageRegulation().getTargetDeadband() + 1.0));
            }),
            supported("shunt target value", smallGrid, n -> {
                ShuntCompensator s = regulatingHolder(n.getShuntCompensatorStream());
                // A target value needs a terminal
                s.getVoltageRegulation().setTerminal(s.getTerminal(), 400.0);
                return new Change(s, () -> s.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("SVC localTargetQ", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.setLocalTargetQ(10.0));
            }),
            supported("SVC localTargetV", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.setLocalTargetV(svc.getLocalTargetV() + 1.0));
            }),
            supported("SVC regulating", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.getVoltageRegulation().setRegulating(!svc.getVoltageRegulation().isRegulating()));
            }),
            supported("SVC target value", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                regulateRemoteVoltage(svc, 400.0);
                return new Change(svc, () -> svc.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("VSC station localTargetQ", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.setLocalTargetQ(v.getLocalTargetQ() + 1.0));
            }),
            supported("VSC station localTargetV", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.setLocalTargetV(v.getLocalTargetV() + 1.0));
            }),
            supported("VSC station regulating", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.getVoltageRegulation().setRegulating(!v.getVoltageRegulation().isRegulating()));
            }),
            supported("VSC station target value", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                regulateRemoteVoltage(v, 400.0);
                return new Change(v, () -> v.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("VSC station regulation mode", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                // A reactive power regulation needs a terminal
                regulateRemoteVoltage(v, 400.0);
                return new Change(v, () -> v.getVoltageRegulation().setMode(RegulationMode.REACTIVE_POWER));
            })
        );
    }

    private static Stream<Arguments> supportedOtherChanges() {
        Supplier<Network> fourSubstations = FourSubstationsNodeBreakerFactory::create;
        Supplier<Network> eurostag = EurostagTutorialExample1Factory::create;
        Supplier<Network> vsc = DcDetailedNetworkFactory::createVscSymmetricalMonopole;
        Supplier<Network> lcc = DcDetailedNetworkFactory::createLccMonopoleGroundReturn;
        return Stream.of(
            supported("LCC station powerFactor", fourSubstations, n -> {
                LccConverterStation l = n.getLccConverterStations().iterator().next();
                return new Change(l, () -> l.setPowerFactor(0.5f));
            }),
            supported("HVDC line activePowerSetpoint", fourSubstations, n -> {
                HvdcLine h = n.getHvdcLines().iterator().next();
                return new Change(h, () -> h.setActivePowerSetpoint(h.getActivePowerSetpoint() + 1.0));
            }),
            supported("HVDC line convertersMode", fourSubstations, n -> {
                HvdcLine h = n.getHvdcLines().iterator().next();
                return new Change(h, () -> h.setConvertersMode(h.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER
                    ? HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER : HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER));
            }),
            supported("VSC converter targetP", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setTargetP(c.getTargetP() + 1.0));
            }),
            supported("VSC converter targetVdc", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setTargetVdc(c.getTargetVdc() + 1.0));
            }),
            supported("VSC converter controlMode", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setControlMode(c.getControlMode() == AcDcConverter.ControlMode.P_PCC ? AcDcConverter.ControlMode.V_DC : AcDcConverter.ControlMode.P_PCC));
            }),
            supported("VSC converter localTargetQ", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setLocalTargetQ(c.getLocalTargetQ() + 1.0));
            }),
            supported("VSC converter localTargetV", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setLocalTargetV(Double.isNaN(c.getLocalTargetV()) ? 400.0 : c.getLocalTargetV() + 1.0));
            }),
            supported("VSC converter target value", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                regulateRemoteVoltage(c, 400.0);
                return new Change(c, () -> c.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("VSC converter regulation mode", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                regulateRemoteVoltage(c, 400.0);
                return new Change(c, () -> c.getVoltageRegulation().setMode(RegulationMode.REACTIVE_POWER));
            }),
            supported("VSC converter DC disconnection", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.getDcTerminals().get(0).disconnect());
            }),
            supported("LCC converter targetP", lcc, n -> {
                LineCommutatedConverter c = n.getLineCommutatedConverters().iterator().next();
                return new Change(c, () -> c.setTargetP(c.getTargetP() + 1.0));
            }),
            supported("LCC converter targetVdc", lcc, n -> {
                LineCommutatedConverter c = n.getLineCommutatedConverters().iterator().next();
                return new Change(c, () -> c.setTargetVdc(c.getTargetVdc() + 1.0));
            }),
            supported("LCC converter controlMode", lcc, n -> {
                LineCommutatedConverter c = n.getLineCommutatedConverters().iterator().next();
                return new Change(c, () -> c.setControlMode(c.getControlMode() == AcDcConverter.ControlMode.P_PCC ? AcDcConverter.ControlMode.V_DC : AcDcConverter.ControlMode.P_PCC));
            }),
            supported("ratio tap position", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().setTapPosition(t.getRatioTapChanger().getLowTapPosition()));
            }),
            supported("ratio tap changer regulating", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().getVoltageRegulation().setRegulating(!t.getRatioTapChanger().getVoltageRegulation().isRegulating()));
            }),
            supported("ratio tap changer target value", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().getVoltageRegulation().setTargetValue(t.getRatioTapChanger().getVoltageRegulation().getTargetValue() + 1.0));
            }),
            supported("ratio tap changer target deadband", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().getVoltageRegulation().setTargetDeadband(1.0));
            }),
            supported("phase tap position", fourSubstations, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                return new Change(t, () -> t.getPhaseTapChanger().setTapPosition(t.getPhaseTapChanger().getLowTapPosition()));
            }),
            supported("phase tap changer regulating", PhaseShifterTestCaseFactory::create, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                // A regulating phase tap changer needs a deadband
                t.getPhaseTapChanger().setTargetDeadband(1.0);
                return new Change(t, () -> t.getPhaseTapChanger().setRegulating(!t.getPhaseTapChanger().isRegulating()));
            }),
            supported("phase tap changer regulation value", fourSubstations, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                return new Change(t, () -> t.getPhaseTapChanger().setRegulationValue(12.0));
            }),
            supported("phase tap changer target deadband", fourSubstations, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                return new Change(t, () -> t.getPhaseTapChanger().setTargetDeadband(1.0));
            }),
            supported("three windings tap position", ThreeWindingsTransformerNetworkFactory::create, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                return new Change(t, () -> t.getLeg2().getRatioTapChanger().setTapPosition(t.getLeg2().getRatioTapChanger().getLowTapPosition()));
            }),
            supported("boundary line p0", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.setP0(bl.getP0() + 1.0));
            }),
            supported("boundary line q0", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.setQ0(bl.getQ0() + 1.0));
            }),
            supported("boundary line targetV", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.getGeneration().setTargetV(bl.getGeneration().getTargetV() + 1.0));
            }),
            supported("boundary line voltageRegulationOn", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                bl.getGeneration().setTargetQ(0.0);
                return new Change(bl, () -> bl.getGeneration().setVoltageRegulationOn(false));
            }),
            supported("bus fictitiousP0", eurostag, n -> {
                Bus bus = n.getBusBreakerView().getBus("NGEN");
                return new Change(bus, () -> bus.setFictitiousP0(10.0));
            }),
            supported("bus fictitiousQ0", eurostag, n -> {
                Bus bus = n.getBusBreakerView().getBus("NGEN");
                return new Change(bus, () -> bus.setFictitiousQ0(10.0));
            }),
            supported("bus-breaker generator disconnection", eurostag, n -> {
                Generator g = n.getGenerator("GEN");
                return new Change(g, () -> g.getTerminal().disconnect());
            }),
            supported("area interchangeTarget", EurostagTutorialExample1Factory::createWithTieLinesAndAreas, n -> {
                Area area = n.getAreas().iterator().next();
                return new Change(area, () -> area.setInterchangeTarget(10.0));
            }),
            supported("active power control creation", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4.0).withParticipationFactor(1.0).add());
            })
        );
    }

    private static Arguments supported(String description, Supplier<Network> network, Function<Network, Change> change) {
        return Arguments.of(description, network, change);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedChanges")
    void supportedChangeGivesItsIdentifiable(String description, Supplier<Network> networkSupplier, Function<Network, Change> changeBuilder) {
        Network n = networkSupplier.get();
        Change change = changeBuilder.apply(n);
        List<NetworkEvent> events = recordEvents(n, change);

        assertFalse(events.isEmpty(), "The change gives no event");
        // A change can also give events of other identifiables: a disconnection opens a breaker
        assertTrue(SshExportedIdentifiables.fromEvents(n, events, FAIL).contains(change.identifiable()));
    }

    static Stream<Arguments> unsupportedChanges() {
        Supplier<Network> fourSubstations = FourSubstationsNodeBreakerFactory::create;
        return Stream.of(
            unsupported("line resistance", fourSubstations, n -> {
                Line line = n.getLines().iterator().next();
                return new Change(line, () -> line.setR(line.getR() + 1.0));
            }),
            unsupported("generator maxP", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setMaxP(g.getMaxP() + 1.0));
            }),
            unsupported("generator name", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setName("other name"));
            }),
            unsupported("generator property", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setProperty("key", "value"));
            }),
            unsupported("generator target deadband", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.getVoltageRegulation().setTargetDeadband(1.0));
            }),
            unsupported("generator slope", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.getVoltageRegulation().setSlope(0.1));
            }),
            unsupported("SVC regulation mode", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.getVoltageRegulation().setMode(svc.isWithMode(RegulationMode.VOLTAGE) ? RegulationMode.REACTIVE_POWER : RegulationMode.VOLTAGE));
            }),
            unsupported("VSC station lossFactor", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.setLossFactor(v.getLossFactor() + 1.0f));
            }),
            unsupported("boundary line generation targetP", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.getGeneration().setTargetP(bl.getGeneration().getTargetP() + 1.0));
            }),
            unsupported("load creation", fourSubstations, n -> {
                VoltageLevel vl = n.getVoltageLevels().iterator().next();
                return new Change(vl, () -> vl.newLoad().setId("NEW_LOAD").setNode(999).setP0(1.0).setQ0(1.0).add());
            }),
            unsupported("load removal", fourSubstations, n -> {
                Load load = n.getLoads().iterator().next();
                return new Change(load, load::remove);
            }),
            unsupported("active power control droop", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                g.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4.0).withParticipationFactor(1.0).add();
                return new Change(g, () -> g.getExtension(ActivePowerControl.class).setDroop(5.0));
            }),
            unsupported("active power control removal", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                g.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4.0).withParticipationFactor(1.0).add();
                return new Change(g, () -> g.removeExtension(ActivePowerControl.class));
            })
        );
    }

    private static Arguments unsupported(String description, Supplier<Network> network, Function<Network, Change> change) {
        return Arguments.of(description, network, change);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedChanges")
    void unsupportedChangeFailsOrIsIgnored(String description, Supplier<Network> networkSupplier, Function<Network, Change> changeBuilder) {
        Network n = networkSupplier.get();
        Change change = changeBuilder.apply(n);
        List<NetworkEvent> events = recordEvents(n, change);
        String id = change.identifiable().getId();

        assertFalse(events.isEmpty(), "The change gives no event");
        PowsyblException e = assertThrows(PowsyblException.class, () -> SshExportedIdentifiables.fromEvents(n, events, FAIL));
        assertTrue(e.getMessage().startsWith("Change can not be exported to a partial SSH"), e.getMessage());
        assertTrue(e.getMessage().contains("See the section \"Supported changes\""), e.getMessage());
        assertTrue(SshExportedIdentifiables.fromEvents(n, events, IGNORE).stream().noneMatch(i -> i.getId().equals(id)));
    }

    @Test
    void partialSshExportOfTheRecordedChanges() throws XMLStreamException {
        Load load = network.getLoads().iterator().next();
        load.setP0(load.getP0() + 1.0);

        CgmesExportContext context = new CgmesExportContext(network)
            .setPartialSshExport(true)
            .setSshExportedIdentifiables(SshExportedIdentifiables.fromEvents(network, recorder.getEvents(), FAIL));
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisExport.write(network, writer, context);

        // The load and its terminal
        List<String> ids = new ArrayList<>();
        Matcher m = SSH_OBJECT_ID.matcher(stringWriter.toString());
        while (m.find()) {
            ids.add(m.group(1));
        }
        assertEquals(List.of(load.getId(), CgmesExportUtil.getTerminalId(load.getTerminal(), context)), ids);
    }

    private static List<NetworkEvent> recordEvents(Network network, Change change) {
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        change.action().run();
        network.removeListener(eventRecorder);
        return eventRecorder.getEvents();
    }

    /**
     * Give the holder a voltage regulation at its own terminal, so that its target value can be changed.
     */
    private static void regulateRemoteVoltage(VoltageRegulationHolder<?> holder, double targetValue) {
        holder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(holder.getTerminal())
            .withTargetValue(targetValue)
            .withRegulating(true)
            .build();
    }

    private static <T extends VoltageRegulationHolder<?>> T regulatingHolder(Stream<T> holders) {
        return holders.filter(h -> h.getVoltageRegulation() != null).findFirst().orElseThrow();
    }
}
