/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.events.ExtensionUpdateNetworkEvent;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
        network = EurostagTutorialExample1Factory.create();
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

    /**
     * The supported changes without a test case, and the reason.
     */
    private static final Map<String, String> NO_TEST_CASE = Map.of(
        "activePowerControl.participationFactor", "the IIDM of this branch does not record this change",
        "referencePriorities", "IIDM does not record this change (issue 4244)",
        "referencePriorities.referencePriority", "IIDM does not record this change (issue 4244)");

    /**
     * The supported changes of {@link SshExportedIdentifiables#capabilities()}, as type.change, or as type for the creation of an extension.
     */
    private static Stream<String> capabilities() {
        return SshExportedIdentifiables.capabilities().entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(change -> change.isEmpty() ? e.getKey() : e.getKey() + "." + change));
    }

    /**
     * The test cases of the supported changes. The name of a case is its supported change,
     * with a detail in parentheses if the supported change has more than one case.
     */
    private static List<Arguments> testCases() {
        return Stream.of(supportedInjectionChanges(), supportedOtherChanges(), supportedConnectionAndLegChanges())
            .flatMap(Function.identity())
            .toList();
    }

    private static String capabilityOf(Arguments testCase) {
        String name = (String) testCase.get()[0];
        int detail = name.indexOf(" (");
        return detail < 0 ? name : name.substring(0, detail);
    }

    @Test
    void eachSupportedChangeHasATestCase() {
        Set<String> supported = capabilities().filter(c -> !NO_TEST_CASE.containsKey(c)).collect(Collectors.toCollection(TreeSet::new));
        Set<String> tested = testCases().stream().map(SshExportedIdentifiablesTest::capabilityOf).collect(Collectors.toCollection(TreeSet::new));

        assertEquals(supported, tested);
    }

    /**
     * The test cases of the supported changes, in the order of {@link SshExportedIdentifiables#capabilities()}.
     */
    static Stream<Arguments> supportedChanges() {
        Map<String, List<Arguments>> testCases = testCases().stream()
            .collect(Collectors.groupingBy(SshExportedIdentifiablesTest::capabilityOf, LinkedHashMap::new, Collectors.toList()));
        return capabilities().flatMap(c -> testCases.getOrDefault(c, List.of()).stream());
    }

    private static Stream<Arguments> supportedInjectionChanges() {
        Supplier<Network> fourSubstations = FourSubstationsNodeBreakerFactory::create;
        Supplier<Network> regulatingShunt = ShuntTestCaseFactory::create;
        return Stream.of(
            supported("Switch.open", fourSubstations, n -> {
                Switch sw = n.getSwitches().iterator().next();
                return new Change(sw, () -> sw.setOpen(!sw.isOpen()));
            }),
            supported("DcSwitch.open", DcDetailedNetworkFactory::createSimple2NodesDcSwitch, n -> {
                DcSwitch sw = n.getDcSwitches().iterator().next();
                return new Change(sw, () -> sw.setOpen(!sw.isOpen()));
            }),
            supported("Load.p0", fourSubstations, n -> {
                Load load = n.getLoads().iterator().next();
                return new Change(load, () -> load.setP0(load.getP0() + 1.0));
            }),
            supported("Load.q0", fourSubstations, n -> {
                Load load = n.getLoads().iterator().next();
                return new Change(load, () -> load.setQ0(load.getQ0() + 1.0));
            }),
            supported("Generator.targetP", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setTargetP(g.getTargetP() + 1.0));
            }),
            supported("Generator.localTargetQ", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setLocalTargetQ(g.getLocalTargetQ() + 1.0));
            }),
            supported("Generator.localTargetV", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.setLocalTargetV(g.getLocalTargetV() + 1.0));
            }),
            supported("Generator.VoltageRegulation.isRegulating", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.getVoltageRegulation().setRegulating(!g.getVoltageRegulation().isRegulating()));
            }),
            supported("Generator.VoltageRegulation.TargetValue", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                regulateRemoteVoltage(g, 400.0);
                return new Change(g, () -> g.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("Generator.connected (node-breaker)", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.getTerminal().disconnect());
            }),
            supported("Battery.targetP", BatteryNetworkFactory::create, n -> {
                Battery b = n.getBatteries().iterator().next();
                return new Change(b, () -> b.setTargetP(b.getTargetP() + 1.0));
            }),
            supported("Battery.localTargetQ", BatteryNetworkFactory::create, n -> {
                Battery b = n.getBatteries().iterator().next();
                return new Change(b, () -> b.setLocalTargetQ(b.getLocalTargetQ() + 1.0));
            }),
            supported("Battery.VoltageRegulation.isRegulating", BatteryNetworkFactory::create, n -> {
                Battery b = n.getBatteries().iterator().next();
                regulateLocalVoltage(b, 400.0);
                return new Change(b, () -> b.getVoltageRegulation().setRegulating(false));
            }),
            supported("ShuntCompensator.sectionCount", fourSubstations, n -> {
                ShuntCompensator s = n.getShuntCompensators().iterator().next();
                return new Change(s, () -> s.setSectionCount(s.getSectionCount() == 0 ? 1 : 0));
            }),
            supported("ShuntCompensator.localTargetV", fourSubstations, n -> {
                ShuntCompensator s = n.getShuntCompensators().iterator().next();
                regulateLocalVoltage(s, 400.0);
                return new Change(s, () -> s.setLocalTargetV(399.0));
            }),
            supported("ShuntCompensator.VoltageRegulation.isRegulating", regulatingShunt, n -> {
                ShuntCompensator s = regulatingHolder(n.getShuntCompensatorStream());
                return new Change(s, () -> s.getVoltageRegulation().setRegulating(!s.getVoltageRegulation().isRegulating()));
            }),
            supported("ShuntCompensator.VoltageRegulation.TargetDeadband", regulatingShunt, n -> {
                ShuntCompensator s = regulatingHolder(n.getShuntCompensatorStream());
                return new Change(s, () -> s.getVoltageRegulation().setTargetDeadband(s.getVoltageRegulation().getTargetDeadband() + 1.0));
            }),
            supported("ShuntCompensator.VoltageRegulation.TargetValue", regulatingShunt, n -> {
                ShuntCompensator s = regulatingHolder(n.getShuntCompensatorStream());
                // A target value needs a terminal
                s.getVoltageRegulation().setTerminal(s.getTerminal(), 400.0);
                return new Change(s, () -> s.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("StaticVarCompensator.localTargetQ", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.setLocalTargetQ(10.0));
            }),
            supported("StaticVarCompensator.localTargetV", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.setLocalTargetV(svc.getLocalTargetV() + 1.0));
            }),
            supported("StaticVarCompensator.VoltageRegulation.isRegulating", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.getVoltageRegulation().setRegulating(!svc.getVoltageRegulation().isRegulating()));
            }),
            supported("StaticVarCompensator.VoltageRegulation.TargetValue", fourSubstations, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                regulateRemoteVoltage(svc, 400.0);
                return new Change(svc, () -> svc.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("VscConverterStation.localTargetQ", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.setLocalTargetQ(v.getLocalTargetQ() + 1.0));
            }),
            supported("VscConverterStation.localTargetV", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.setLocalTargetV(v.getLocalTargetV() + 1.0));
            }),
            supported("VscConverterStation.VoltageRegulation.TargetValue", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                regulateRemoteVoltage(v, 400.0);
                return new Change(v, () -> v.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("VscConverterStation.VoltageRegulation.RegulationMode", fourSubstations, n -> {
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
            supported("LccConverterStation.powerFactor", fourSubstations, n -> {
                LccConverterStation l = n.getLccConverterStations().iterator().next();
                return new Change(l, () -> l.setPowerFactor(0.5f));
            }),
            supported("HvdcLine.activePowerSetpoint", fourSubstations, n -> {
                HvdcLine h = n.getHvdcLines().iterator().next();
                return new Change(h, () -> h.setActivePowerSetpoint(h.getActivePowerSetpoint() + 1.0));
            }),
            supported("HvdcLine.convertersMode", fourSubstations, n -> {
                HvdcLine h = n.getHvdcLines().iterator().next();
                return new Change(h, () -> h.setConvertersMode(h.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER
                    ? HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER : HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER));
            }),
            supported("VoltageSourceConverter.targetP", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setTargetP(c.getTargetP() + 1.0));
            }),
            supported("VoltageSourceConverter.targetVdc", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setTargetVdc(c.getTargetVdc() + 1.0));
            }),
            supported("VoltageSourceConverter.controlMode", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setControlMode(c.getControlMode() == AcDcConverter.ControlMode.P_PCC ? AcDcConverter.ControlMode.V_DC : AcDcConverter.ControlMode.P_PCC));
            }),
            supported("VoltageSourceConverter.localTargetQ", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.setLocalTargetQ(c.getLocalTargetQ() + 1.0));
            }),
            supported("VoltageSourceConverter.localTargetV", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                regulateLocalVoltage(c, 400.0);
                return new Change(c, () -> c.setLocalTargetV(401.0));
            }),
            supported("VoltageSourceConverter.VoltageRegulation.TargetValue", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                regulateRemoteVoltage(c, 400.0);
                return new Change(c, () -> c.getVoltageRegulation().setTargetValue(401.0));
            }),
            supported("VoltageSourceConverter.VoltageRegulation.RegulationMode", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                regulateRemoteVoltage(c, 400.0);
                return new Change(c, () -> c.getVoltageRegulation().setMode(RegulationMode.REACTIVE_POWER));
            }),
            supported("VoltageSourceConverter.connected (DC terminal)", vsc, n -> {
                VoltageSourceConverter c = n.getVoltageSourceConverters().iterator().next();
                return new Change(c, () -> c.getDcTerminals().get(0).disconnect());
            }),
            supported("LineCommutatedConverter.targetP", lcc, n -> {
                LineCommutatedConverter c = n.getLineCommutatedConverters().iterator().next();
                return new Change(c, () -> c.setTargetP(c.getTargetP() + 1.0));
            }),
            supported("LineCommutatedConverter.targetVdc", lcc, n -> {
                LineCommutatedConverter c = n.getLineCommutatedConverters().iterator().next();
                return new Change(c, () -> c.setTargetVdc(c.getTargetVdc() + 1.0));
            }),
            supported("LineCommutatedConverter.controlMode", lcc, n -> {
                LineCommutatedConverter c = n.getLineCommutatedConverters().iterator().next();
                return new Change(c, () -> c.setControlMode(c.getControlMode() == AcDcConverter.ControlMode.P_PCC ? AcDcConverter.ControlMode.V_DC : AcDcConverter.ControlMode.P_PCC));
            }),
            supported("TwoWindingsTransformer.ratioTapChanger.tapPosition", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().setTapPosition(t.getRatioTapChanger().getLowTapPosition()));
            }),
            supported("TwoWindingsTransformer.ratioTapChanger.VoltageRegulation.isRegulating", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().getVoltageRegulation().setRegulating(!t.getRatioTapChanger().getVoltageRegulation().isRegulating()));
            }),
            supported("TwoWindingsTransformer.ratioTapChanger.VoltageRegulation.TargetValue", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().getVoltageRegulation().setTargetValue(t.getRatioTapChanger().getVoltageRegulation().getTargetValue() + 1.0));
            }),
            supported("TwoWindingsTransformer.ratioTapChanger.VoltageRegulation.TargetDeadband", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getRatioTapChanger().getVoltageRegulation().setTargetDeadband(1.0));
            }),
            supported("TwoWindingsTransformer.phaseTapChanger.tapPosition", fourSubstations, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                return new Change(t, () -> t.getPhaseTapChanger().setTapPosition(t.getPhaseTapChanger().getLowTapPosition()));
            }),
            supported("TwoWindingsTransformer.phaseTapChanger.regulating", PhaseShifterTestCaseFactory::create, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                // A regulating phase tap changer needs a deadband
                t.getPhaseTapChanger().setTargetDeadband(1.0);
                return new Change(t, () -> t.getPhaseTapChanger().setRegulating(!t.getPhaseTapChanger().isRegulating()));
            }),
            supported("TwoWindingsTransformer.phaseTapChanger.regulationValue", fourSubstations, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                return new Change(t, () -> t.getPhaseTapChanger().setRegulationValue(12.0));
            }),
            supported("TwoWindingsTransformer.phaseTapChanger.targetDeadband", fourSubstations, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformerStream().filter(TwoWindingsTransformer::hasPhaseTapChanger).findFirst().orElseThrow();
                return new Change(t, () -> t.getPhaseTapChanger().setTargetDeadband(1.0));
            }),
            supported("ThreeWindingsTransformer.ratioTapChanger.tapPosition", ThreeWindingsTransformerNetworkFactory::create, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                return new Change(t, () -> t.getLeg2().getRatioTapChanger().setTapPosition(t.getLeg2().getRatioTapChanger().getLowTapPosition()));
            }),
            supported("BoundaryLine.p0", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.setP0(bl.getP0() + 1.0));
            }),
            supported("BoundaryLine.q0", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.setQ0(bl.getQ0() + 1.0));
            }),
            supported("BoundaryLine.targetV", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.getGeneration().setTargetV(bl.getGeneration().getTargetV() + 1.0));
            }),
            supported("BoundaryLine.voltageRegulationOn", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                bl.getGeneration().setTargetQ(0.0);
                return new Change(bl, () -> bl.getGeneration().setVoltageRegulationOn(false));
            }),
            supported("Generator.connected (bus-breaker)", eurostag, n -> {
                Generator g = n.getGenerator("GEN");
                return new Change(g, () -> g.getTerminal().disconnect());
            }),
            supported("Area.interchangeTarget", EurostagTutorialExample1Factory::createWithTieLinesAndAreas, n -> {
                Area area = n.getAreas().iterator().next();
                return new Change(area, () -> area.setInterchangeTarget(10.0));
            }),
            supported("activePowerControl", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                return new Change(g, () -> g.newExtension(ActivePowerControlAdder.class).withParticipate(true).withDroop(4.0).withParticipationFactor(1.0).add());
            })
        );
    }

    private static Stream<Arguments> supportedConnectionAndLegChanges() {
        Supplier<Network> eurostag = EurostagTutorialExample1Factory::create;
        Supplier<Network> threeWindings = ThreeWindingsTransformerNetworkFactory::create;
        return Stream.of(
            supported("BusbarSection.connected", FourSubstationsNodeBreakerFactory::create, n -> {
                BusbarSection b = n.getBusbarSections().iterator().next();
                return new Change(b, () -> b.getTerminal().disconnect());
            }),
            supported("Ground.connected", TwoVoltageLevelNetworkFactory::createWithGrounds, n -> {
                Ground g = n.getGround("GroundBB");
                return new Change(g, () -> g.getTerminal().disconnect());
            }),
            supported("Line.connected", eurostag, n -> {
                Line l = n.getLine("NHV1_NHV2_1");
                return new Change(l, () -> l.getTerminal1().disconnect());
            }),
            supported("DcLine.connected", DcDetailedNetworkFactory::createVscSymmetricalMonopole, n -> {
                DcLine l = n.getDcLines().iterator().next();
                return new Change(l, () -> l.getDcTerminal1().disconnect());
            }),
            supported("DcGround.connected", DcDetailedNetworkFactory::createLccMonopoleGroundReturn, n -> {
                DcGround g = n.getDcGrounds().iterator().next();
                return new Change(g, () -> g.getDcTerminal().disconnect());
            }),
            supported("Load.connected", eurostag, n -> {
                Load l = n.getLoad("LOAD");
                return new Change(l, () -> l.getTerminal().disconnect());
            }),
            supported("Battery.connected", BatteryNetworkFactory::create, n -> {
                Battery b = n.getBatteries().iterator().next();
                return new Change(b, () -> b.getTerminal().disconnect());
            }),
            supported("ShuntCompensator.connected", ShuntTestCaseFactory::create, n -> {
                ShuntCompensator s = n.getShuntCompensators().iterator().next();
                return new Change(s, () -> s.getTerminal().disconnect());
            }),
            supported("StaticVarCompensator.connected", SvcTestCaseFactory::create, n -> {
                StaticVarCompensator svc = n.getStaticVarCompensators().iterator().next();
                return new Change(svc, () -> svc.getTerminal().disconnect());
            }),
            supported("VscConverterStation.connected", HvdcTestNetwork::createVsc, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.getTerminal().disconnect());
            }),
            supported("LccConverterStation.connected", HvdcTestNetwork::createLcc, n -> {
                LccConverterStation l = n.getLccConverterStations().iterator().next();
                return new Change(l, () -> l.getTerminal().disconnect());
            }),
            supported("LineCommutatedConverter.connected", DcDetailedNetworkFactory::createLccMonopoleGroundReturn, n -> {
                LineCommutatedConverter c = n.getLineCommutatedConverters().iterator().next();
                return new Change(c, () -> c.getTerminal1().disconnect());
            }),
            supported("TwoWindingsTransformer.connected", eurostag, n -> {
                TwoWindingsTransformer t = n.getTwoWindingsTransformer("NHV2_NLOAD");
                return new Change(t, () -> t.getTerminal1().disconnect());
            }),
            supported("ThreeWindingsTransformer.connected", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                return new Change(t, () -> t.getLeg3().getTerminal().disconnect());
            }),
            supported("BoundaryLine.connected", BoundaryLineNetworkFactory::createWithGeneration, n -> {
                BoundaryLine bl = n.getBoundaryLines().iterator().next();
                return new Change(bl, () -> bl.getTerminal().disconnect());
            }),
            supported("ThreeWindingsTransformer.ratioTapChanger.VoltageRegulation.isRegulating", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                VoltageRegulation regulation = t.getLeg2().getRatioTapChanger().getVoltageRegulation();
                return new Change(t, () -> regulation.setRegulating(!regulation.isRegulating()));
            }),
            supported("ThreeWindingsTransformer.ratioTapChanger.VoltageRegulation.TargetValue", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                VoltageRegulation regulation = t.getLeg2().getRatioTapChanger().getVoltageRegulation();
                return new Change(t, () -> regulation.setTargetValue(regulation.getTargetValue() + 1.0));
            }),
            supported("ThreeWindingsTransformer.ratioTapChanger.VoltageRegulation.TargetDeadband", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                VoltageRegulation regulation = t.getLeg2().getRatioTapChanger().getVoltageRegulation();
                return new Change(t, () -> regulation.setTargetDeadband(1.0));
            }),
            supported("ThreeWindingsTransformer.phaseTapChanger.tapPosition", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                PhaseTapChanger ptc = phaseTapChangerOfLeg1(t);
                return new Change(t, () -> ptc.setTapPosition(1));
            }),
            supported("ThreeWindingsTransformer.phaseTapChanger.regulating", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                PhaseTapChanger ptc = phaseTapChangerOfLeg1(t);
                // A three windings transformer can have only one regulating tap changer
                t.getLeg2().getRatioTapChanger().getVoltageRegulation().setRegulating(false);
                return new Change(t, () -> ptc.setRegulating(true));
            }),
            supported("ThreeWindingsTransformer.phaseTapChanger.regulationValue", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                PhaseTapChanger ptc = phaseTapChangerOfLeg1(t);
                return new Change(t, () -> ptc.setRegulationValue(20.0));
            }),
            supported("ThreeWindingsTransformer.phaseTapChanger.targetDeadband", threeWindings, n -> {
                ThreeWindingsTransformer t = n.getThreeWindingsTransformers().iterator().next();
                PhaseTapChanger ptc = phaseTapChangerOfLeg1(t);
                return new Change(t, () -> ptc.setTargetDeadband(2.0));
            })
        );
    }

    /**
     * The phase tap changer of the first leg of a three windings transformer. The test factory has none, so it is added once.
     */
    private static PhaseTapChanger phaseTapChangerOfLeg1(ThreeWindingsTransformer t) {
        ThreeWindingsTransformer.Leg leg = t.getLeg1();
        if (!leg.hasPhaseTapChanger()) {
            leg.newPhaseTapChanger()
                .setTapPosition(0)
                .setRegulationTerminal(leg.getTerminal())
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10.0)
                .setTargetDeadband(1.0)
                .setRegulating(false)
                .beginStep().setAlpha(-10.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep()
                .beginStep().setAlpha(10.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep()
                .add();
        }
        return leg.getPhaseTapChanger();
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

    /**
     * The supported changes without a round trip test, and the reason.
     */
    private static final Map<String, String> NO_ROUND_TRIP;

    static {
        String notKept = "the CGMES export and import of the network do not keep this equipment";
        String importFails = "the CGMES import of the network fails";
        String exportFails = "the CGMES export of the network fails";
        Map<String, String> noRoundTrip = new HashMap<>();
        for (String description : List.of("DcSwitch.open", "Battery.targetP", "Battery.localTargetQ", "Battery.connected", "Battery.VoltageRegulation.isRegulating", "Area.interchangeTarget",
                "VoltageSourceConverter.targetP", "VoltageSourceConverter.targetVdc", "VoltageSourceConverter.controlMode", "VoltageSourceConverter.localTargetQ",
                "VoltageSourceConverter.localTargetV", "VoltageSourceConverter.VoltageRegulation.TargetValue", "VoltageSourceConverter.VoltageRegulation.RegulationMode",
                "VoltageSourceConverter.connected (DC terminal)",
                "DcLine.connected", "DcGround.connected")) {
            noRoundTrip.put(description, notKept);
        }
        for (String description : List.of("LineCommutatedConverter.targetP", "LineCommutatedConverter.targetVdc", "LineCommutatedConverter.controlMode",
                "LineCommutatedConverter.connected")) {
            noRoundTrip.put(description, importFails);
        }
        for (String description : List.of("BoundaryLine.p0", "BoundaryLine.q0", "BoundaryLine.targetV", "BoundaryLine.voltageRegulationOn", "BoundaryLine.connected",
                "Ground.connected")) {
            noRoundTrip.put(description, exportFails);
        }
        noRoundTrip.put("Generator.connected (node-breaker)", "the CGMES import adds a fictitious switch to a disconnected node-breaker terminal");
        noRoundTrip.put("LccConverterStation.connected", "the update of this branch reconnects a bus-breaker terminal that is missing from the SSH (issue 4245)");
        noRoundTrip.put("VscConverterStation.VoltageRegulation.TargetValue", "a CGMES model has no remote voltage regulation for a VSC converter station");
        NO_ROUND_TRIP = Map.copyOf(noRoundTrip);
    }

    static Stream<Arguments> supportedChangesWithRoundTrip() {
        return supportedChanges().filter(arguments -> !NO_ROUND_TRIP.containsKey((String) arguments.get()[0]));
    }

    /**
     * The sender and the receiver load the same CGMES model. The sender records the change and exports it as a partial SSH,
     * the receiver applies the partial SSH with the network update, and both networks must have the same IIDM data.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedChangesWithRoundTrip")
    void supportedChangeIsAppliedByTheUpdate(String description, Supplier<Network> networkSupplier, Function<Network, Change> changeBuilder)
            throws IOException, XMLStreamException {
        // The preparation of the change, for example a voltage regulation, is part of the CGMES model
        Network base = networkSupplier.get();
        changeBuilder.apply(base);
        MemDataSource model = CgmesTestModels.cgmesFiles(base);
        Network sender = Network.read(model);
        Network receiver = Network.read(model);
        Change change = changeBuilder.apply(sender);
        // The preparation does not change the imported networks again; the receiver does not get the change
        changeBuilder.apply(receiver);
        Set<Identifiable<?>> changed = SshExportedIdentifiables.fromEvents(sender, recordEvents(sender, change), FAIL);
        assertNotEquals(CgmesTestModels.iidmData(sender), CgmesTestModels.iidmData(receiver), "The change gives no difference");

        MemDataSource partialSsh = new MemDataSource();
        try (OutputStream os = partialSsh.newOutputStream("partial_SSH.xml", false)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            SteadyStateHypothesisExport.write(sender, writer, new CgmesExportContext(sender).setSshExportedIdentifiables(changed));
        }
        Properties properties = new Properties();
        properties.put(CgmesImport.USE_PREVIOUS_VALUES_DURING_UPDATE, "true");
        receiver.update(partialSsh, properties);

        assertEquals(CgmesTestModels.iidmData(sender), CgmesTestModels.iidmData(receiver));
    }

    static Stream<Arguments> unsupportedChanges() {
        Supplier<Network> fourSubstations = FourSubstationsNodeBreakerFactory::create;
        Supplier<Network> eurostag = EurostagTutorialExample1Factory::create;
        return Stream.of(
            // The SSH writes the local target voltage only as the target value of a local voltage regulation
            unsupported("shunt localTargetV without voltage regulation", fourSubstations, n -> {
                ShuntCompensator s = n.getShuntCompensators().iterator().next();
                return new Change(s, () -> s.setLocalTargetV(399.0));
            }),
            unsupported("generator localTargetV with a remote voltage regulation", fourSubstations, n -> {
                Generator g = n.getGenerators().iterator().next();
                regulateRemoteVoltage(g, 400.0);
                return new Change(g, () -> g.setLocalTargetV(399.0));
            }),
            // The SSH has no regulation status for a VSC converter station
            unsupported("VSC station regulating", fourSubstations, n -> {
                VscConverterStation v = n.getVscConverterStations().iterator().next();
                return new Change(v, () -> v.getVoltageRegulation().setRegulating(!v.getVoltageRegulation().isRegulating()));
            }),
            // The fictitious injection of a bus is a new equipment in a network imported from CGMES
            unsupported("bus fictitiousP0", eurostag, n -> {
                Bus bus = n.getBusBreakerView().getBus("NGEN");
                return new Change(bus, () -> bus.setFictitiousP0(10.0));
            }),
            unsupported("bus fictitiousQ0", eurostag, n -> {
                Bus bus = n.getBusBreakerView().getBus("NGEN");
                return new Change(bus, () -> bus.setFictitiousQ0(10.0));
            }),
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
     * Give the holder a local voltage regulation, so that its local target voltage is in the SSH.
     */
    private static void regulateLocalVoltage(VoltageRegulationHolder<?> holder, double localTargetV) {
        holder.setLocalTargetV(localTargetV);
        holder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetDeadband(0.0)
            .withRegulating(true)
            .build();
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
