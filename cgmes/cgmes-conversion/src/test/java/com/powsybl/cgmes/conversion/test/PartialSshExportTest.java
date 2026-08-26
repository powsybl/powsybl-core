/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.PartialSshExport;
import com.powsybl.cgmes.conversion.export.PartialSshExport.UnsupportedChangeBehavior;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class PartialSshExportTest extends AbstractSerDeTest {

    private static final double TOLERANCE = 1e-7;

    private static final String SWITCH_DIR = "/update/switch/";
    private static final String LOAD_DIR = "/update/load/";
    private static final String GENERATOR_DIR = "/update/generator/";
    private static final String TRANSFORMER_DIR = "/update/transformer/";
    private static final String SHUNT_DIR = "/update/shunt-compensator/";
    private static final String STATIC_VAR_COMPENSATOR_DIR = "/update/static-var-compensator/";
    private static final String HVDC_DIR = "/update/hvdc/";
    private static final String DC_DIR = "/issues/hvdc/";

    /**
     * The transformer of the BE micro grid whose phase tap changer is a PhaseTapChangerAsymmetrical. Its
     * counterpart in the same model is a PhaseTapChangerSymmetrical, a class the CGMES update query does not
     * select, so a position written for it is silently dropped on import whatever the exporter does.
     * (https://github.com/powsybl/powsybl-core/issues/4034)
     */
    private static final String ASYMMETRICAL_PHASE_TAP_CHANGER = "b94318f6-6d24-4f56-96b9-df2531ad6543";

    private static final Pattern FULL_MODEL_ID_PATTERN = Pattern.compile("FullModel rdf:about=\"(.*?)\"");
    private static final Pattern MODEL_VERSION_PATTERN = Pattern.compile("Model.version>(.*?)<");

    private static final String USE_PREVIOUS_VALUES = "iidm.import.cgmes.use-previous-values-during-update";

    // Helpers. A round trip loads the same base model twice, applies the changes to one of the two copies,
    // exports them as a partial SSH file and applies that file to the other copy. This is the exchange the
    // partial SSH export is meant to support, so every supported change is verified this way.

    record RoundTripResult(Network sender, Network receiver, List<NetworkEvent> events, String sshXml) {
    }

    private RoundTripResult roundTrip(String dir, Consumer<Network> senderChanges, String... baselineFiles) throws IOException {
        return roundTrip(new Properties(), dir, senderChanges, baselineFiles);
    }

    private RoundTripResult roundTrip(Properties importParameters, String dir, Consumer<Network> senderChanges,
                                      String... baselineFiles) throws IOException {
        return roundTrip(importParameters, () -> readCgmesResources(importParameters, dir, baselineFiles), senderChanges);
    }

    /** Round trip on a whole grid model, for instance one of the CGMES conformity models. */
    private RoundTripResult roundTrip(ReadOnlyDataSource dataSource, Consumer<Network> senderChanges) throws IOException {
        return roundTrip(new Properties(), () -> Network.read(dataSource, new Properties()), senderChanges);
    }

    private RoundTripResult roundTrip(Properties importParameters, Supplier<Network> load,
                                      Consumer<Network> senderChanges) throws IOException {
        Network sender = load.get();
        Network receiver = load.get();

        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);
        senderChanges.accept(sender);

        String baseName = "partial-ssh-roundtrip";
        Path exportDir = tmpDir.toAbsolutePath();
        Path sshFile = exportDir.resolve(baseName + "_SSH.xml");
        List<NetworkEvent> exportedEvents =
                PartialSshExport.write(sender, recorder.getEvents(), sshFile, UnsupportedChangeBehavior.FAIL);

        // Nothing can be dropped when unsupported changes are rejected, so every compacted change has to be
        // reported as exported. This catches a mapping that silently writes nothing without rejecting.
        assertEquals(PartialSshExport.compactEvents(recorder.getEvents()), exportedEvents);

        Properties updateParameters = new Properties();
        updateParameters.putAll(importParameters);
        updateParameters.put(USE_PREVIOUS_VALUES, "true");
        receiver.update(new GenericReadOnlyDataSource(exportDir, baseName), updateParameters);

        return new RoundTripResult(sender, receiver, List.copyOf(recorder.getEvents()), Files.readString(sshFile));
    }

    // Round trips, one per supported change

    @Test
    void switchStateRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(SWITCH_DIR, sender -> {
            sender.getSwitch("SeriesCompensator").setOpen(false);
            sender.getSwitch("Breaker").setOpen(true);
        }, "switch_EQ.xml", "switch_SSH.xml");

        assertTrue(result.sshXml().contains("<cim:Breaker rdf:about=\"#_Breaker\">"));
        assertSwitchState(result, "SeriesCompensator");
        assertSwitchState(result, "Breaker");
    }

    /** Closing again is the other half of the switch state: the baseline of this variant has the breaker open. */
    @Test
    void switchClosingRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(SWITCH_DIR,
                sender -> sender.getSwitch("Breaker").setOpen(false), "switch_EQ.xml", "switch_SSH_1.xml");

        assertTrue(result.sshXml().contains("<cim:Switch.open>false</cim:Switch.open>"));
        assertFalse(result.sender().getSwitch("Breaker").isOpen());
        assertSwitchState(result, "Breaker");
    }

    /**
     * An ACLineSegment, EquivalentBranch or SeriesCompensator that the import turned into an IIDM switch is not a
     * Switch in CGMES: it has no open state of its own, and the import derives the state of the IIDM switch from
     * the connection status of its terminals. Opening one is the case that a Switch.open encoding would lose.
     */
    @Test
    void branchModelledAsSwitchOpeningRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(SWITCH_DIR,
                sender -> sender.getSwitch("SeriesCompensator").setOpen(true), "switch_EQ.xml", "switch_SSH_1.xml");

        assertFalse(result.sshXml().contains("Switch.open"));
        assertTrue(result.sshXml().contains("<cim:Terminal rdf:about=\"#_SeriesCompensator-T1\">"));
        assertTrue(result.sender().getSwitch("SeriesCompensator").isOpen());
        assertSwitchState(result, "SeriesCompensator");
    }

    @Test
    void dcSwitchStateRoundTrip() throws IOException {
        Properties importParameters = new Properties();
        importParameters.put(CgmesImport.USE_DETAILED_DC_MODEL, "true");

        RoundTripResult result = roundTrip(importParameters, DC_DIR,
                sender -> sender.getDcSwitch("DCSW_1_1").setOpen(true), "mixed_bipole_EQ.xml");

        assertTrue(result.sshXml().contains("<cim:DCTerminal rdf:about=\"#_T_DCSW_1_1_1\">"));
        assertTrue(result.sender().getDcSwitch("DCSW_1_1").isOpen());
        assertEquals(result.sender().getDcSwitch("DCSW_1_1").isOpen(), result.receiver().getDcSwitch("DCSW_1_1").isOpen());
    }

    @Test
    void loadSetpointsRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(LOAD_DIR, sender -> {
            sender.getLoad("EnergyConsumer").setP0(10.5).setQ0(5.5);
            sender.getLoad("EnergySource").setP0(-200.5).setQ0(-90.5);
        }, "load_EQ.xml", "load_SSH.xml");

        assertTrue(result.sshXml().contains("<cim:EnergyConsumer rdf:about=\"#_EnergyConsumer\">"));
        assertTrue(result.sshXml().contains("<cim:EnergySource rdf:about=\"#_EnergySource\">"));
        assertLoadSetpoints(result, "EnergyConsumer");
        assertLoadSetpoints(result, "EnergySource");
    }

    /**
     * A load exported as an AsynchronousMachine carries its setpoints like any other load. The exporter rejects
     * the change for now, because the CGMES steady state hypothesis of an AsynchronousMachine is incomplete and
     * the import cannot read the setpoints back, so exporting them would lose the change silently.
     */
    @Disabled("Requires https://github.com/powsybl/powsybl-core/issues/4029")
    @Test
    void asynchronousMachineSetpointsRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(LOAD_DIR,
                sender -> sender.getLoad("AsynchronousMachine").setP0(200.5).setQ0(50.5),
                "load_EQ.xml", "load_SSH.xml");

        assertLoadSetpoints(result, "AsynchronousMachine");
    }

    /**
     * The CGMES import only accepts an injection power when both components are present, so a change of a single
     * setpoint has to export both. Exporting only the changed one silently loses the change.
     */
    @Test
    void activePowerOnlyLoadChangeRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(LOAD_DIR,
                sender -> sender.getLoad("EnergyConsumer").setP0(77.5), "load_EQ.xml", "load_SSH.xml");

        assertEquals(1, result.events().size());
        assertTrue(result.sshXml().contains("<cim:EnergyConsumer.p>77.5</cim:EnergyConsumer.p>"));
        assertTrue(result.sshXml().contains("<cim:EnergyConsumer.q>"));
        assertLoadSetpoints(result, "EnergyConsumer");
    }

    @Test
    void generatorSetpointsRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(GENERATOR_DIR,
                sender -> sender.getGenerator("SynchronousMachine").setTargetP(165.0).setTargetQ(-5.0).setTargetV(410.0),
                "generator_EQ.xml", "generator_SSH.xml");

        assertGeneratorTargets(result, "SynchronousMachine");
    }

    @Test
    void generatorVoltageRegulationRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(GENERATOR_DIR,
                sender -> sender.getGenerator("SynchronousMachine").setVoltageRegulatorOn(false),
                "generator_EQ.xml", "generator_SSH.xml");

        assertFalse(result.sender().getGenerator("SynchronousMachine").isVoltageRegulatorOn());
        assertEquals(result.sender().getGenerator("SynchronousMachine").isVoltageRegulatorOn(),
                result.receiver().getGenerator("SynchronousMachine").isVoltageRegulatorOn());
    }

    /** A voltage setpoint lives on the RegulatingControl, it must not restate the machine active power. */
    @Test
    void voltageSetpointOnlyDoesNotExportActivePower() throws IOException {
        RoundTripResult result = roundTrip(GENERATOR_DIR,
                sender -> sender.getGenerator("SynchronousMachine").setTargetV(410.0),
                "generator_EQ.xml", "generator_SSH.xml");

        assertFalse(result.sshXml().contains("<cim:RotatingMachine.p>"));
        assertEquals(result.sender().getGenerator("SynchronousMachine").getTargetV(),
                result.receiver().getGenerator("SynchronousMachine").getTargetV(), TOLERANCE);
    }

    @Test
    void tapPositionsRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(TRANSFORMER_DIR, sender -> {
            sender.getTwoWindingsTransformer("T2W").getPhaseTapChanger().setTapPosition(-1);
            sender.getThreeWindingsTransformer("T3W").getLeg2().getRatioTapChanger().setTapPosition(7);
        }, "transformer_EQ.xml", "transformer_SSH.xml");

        TwoWindingsTransformer expected2w = result.sender().getTwoWindingsTransformer("T2W");
        TwoWindingsTransformer actual2w = result.receiver().getTwoWindingsTransformer("T2W");
        assertEquals(expected2w.getPhaseTapChanger().getTapPosition(), actual2w.getPhaseTapChanger().getTapPosition());

        ThreeWindingsTransformer expected3w = result.sender().getThreeWindingsTransformer("T3W");
        ThreeWindingsTransformer actual3w = result.receiver().getThreeWindingsTransformer("T3W");
        assertEquals(expected3w.getLeg2().getRatioTapChanger().getTapPosition(),
                actual3w.getLeg2().getRatioTapChanger().getTapPosition());
    }

    /**
     * The phase tap changer left out of {@link #conformityModelMultiEquipmentRoundTrip}, whose position the CGMES
     * update query does not select, so the receiving side keeps the position it had.
     */
    @Disabled("Requires https://github.com/powsybl/powsybl-core/issues/4034")
    @Test
    void symmetricalPhaseTapChangerPositionRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource(),
                sender -> moveUp(symmetricalPhaseTapChanger(sender).getPhaseTapChanger()));

        assertEquals(symmetricalPhaseTapChanger(result.sender()).getPhaseTapChanger().getTapPosition(),
                symmetricalPhaseTapChanger(result.receiver()).getPhaseTapChanger().getTapPosition());
    }

    /** The one phase tap changer of the BE micro grid that is not the asymmetrical one. */
    private static TwoWindingsTransformer symmetricalPhaseTapChanger(Network network) {
        return network.getTwoWindingsTransformerStream()
                .filter(t -> t.hasPhaseTapChanger() && !t.getId().equals(ASYMMETRICAL_PHASE_TAP_CHANGER))
                .findFirst().orElseThrow();
    }

    @Test
    void shuntOperatingValuesRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(SHUNT_DIR, sender -> {
            sender.getShuntCompensator("LinearShuntCompensator").setTargetV(407.0);
            sender.getShuntCompensator("NonLinearShuntCompensator").setSectionCount(2).setTargetV(406.0);
        }, "shuntCompensator_EQ.xml", "shuntCompensator_SSH.xml");

        assertShuntOperatingValues(result, "LinearShuntCompensator");
        assertShuntOperatingValues(result, "NonLinearShuntCompensator");
    }

    @Test
    void staticVarCompensatorSetpointsRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(STATIC_VAR_COMPENSATOR_DIR, sender -> {
            sender.getStaticVarCompensator("StaticVarCompensator-V").setVoltageSetpoint(400.0);
            sender.getStaticVarCompensator("StaticVarCompensator-Q").setReactivePowerSetpoint(215.0);
        }, "staticVarCompensator_EQ.xml", "staticVarCompensator_SSH.xml");

        assertStaticVarCompensatorSetpoints(result, "StaticVarCompensator-V");
        assertStaticVarCompensatorSetpoints(result, "StaticVarCompensator-Q");
    }

    @Test
    void hvdcAndVscVoltageSetpointsRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(HVDC_DIR, sender -> {
            sender.getHvdcLine("DCLineSegment-Lcc").setActivePowerSetpoint(350.0);
            HvdcLine vscLine = sender.getHvdcLine("DCLineSegment-Vsc");
            vscLine.setActivePowerSetpoint(497.0);
            ((VscConverterStation) vscLine.getConverterStation1()).setVoltageSetpoint(396.54);
            ((VscConverterStation) vscLine.getConverterStation2()).setVoltageSetpoint(391.0);
        }, "hvdc_EQ.xml", "hvdc_SSH.xml");

        assertHvdcSetpoint(result, "DCLineSegment-Lcc");
        assertHvdcSetpoint(result, "DCLineSegment-Vsc");
        assertVscVoltageSetpoint(result, 1);
        assertVscVoltageSetpoint(result, 2);
    }

    /**
     * Zero is a setpoint like any other, and a line brought down to no power is exactly the change an operator
     * expects to survive. The CGMES update currently reads a targetPpcc of zero as no value at all, so the
     * receiving side keeps the power it had.
     */
    @Disabled("Requires https://github.com/powsybl/powsybl-core/issues/4028")
    @Test
    void hvdcActivePowerSetpointOfZeroRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(HVDC_DIR,
                sender -> sender.getHvdcLine("DCLineSegment-Lcc").setActivePowerSetpoint(0.0),
                "hvdc_EQ.xml", "hvdc_SSH.xml");

        assertEquals(0.0, result.receiver().getHvdcLine("DCLineSegment-Lcc").getActivePowerSetpoint(), TOLERANCE);
        assertHvdcSetpoint(result, "DCLineSegment-Lcc");
    }

    /**
     * The reactive power setpoint of a voltage source converter, which the exporter rejects for now because the
     * value does not survive a steady state hypothesis round trip with its sign intact.
     */
    @Disabled("Requires https://github.com/powsybl/powsybl-core/issues/4027")
    @Test
    void vscReactivePowerSetpointRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(HVDC_DIR,
                sender -> converter(sender, 2).setReactivePowerSetpoint(30.0), "hvdc_EQ.xml", "hvdc_SSH.xml");

        assertEquals(converter(result.sender(), 2).getReactivePowerSetpoint(),
                converter(result.receiver(), 2).getReactivePowerSetpoint(), TOLERANCE);
    }

    /**
     * The tests above use small purpose-built models, one per equipment type. This one runs a single export over a
     * real CGMES conformity model, changing every element of every supported type at once, so that the exporter is
     * also exercised against the naming, the class variety and the regulating control layout of an actual grid
     * model rather than of a fixture.
     */
    @Test
    void conformityModelMultiEquipmentRoundTrip() throws IOException {
        RoundTripResult result = roundTrip(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource(), sender -> {
            sender.getLoadStream().forEach(l -> l.setP0(l.getP0() + 1.0).setQ0(l.getQ0() + 1.0));
            sender.getGeneratorStream().forEach(g -> g.setTargetP(g.getTargetP() + 1.0).setTargetQ(g.getTargetQ() + 1.0));
            sender.getShuntCompensatorStream()
                    .filter(s -> s.getSectionCount() < s.getMaximumSectionCount())
                    .forEach(s -> s.setSectionCount(s.getSectionCount() + 1));
            sender.getStaticVarCompensatorStream()
                    .filter(s -> s.getRegulationMode() == StaticVarCompensator.RegulationMode.VOLTAGE)
                    .forEach(s -> s.setVoltageSetpoint(s.getVoltageSetpoint() + 1.0));
            sender.getTwoWindingsTransformerStream()
                    .filter(t -> t.hasRatioTapChanger() && canMoveUp(t.getRatioTapChanger()))
                    .forEach(t -> moveUp(t.getRatioTapChanger()));
            sender.getThreeWindingsTransformerStream()
                    .filter(t -> t.getLeg2().hasRatioTapChanger() && canMoveUp(t.getLeg2().getRatioTapChanger()))
                    .forEach(t -> moveUp(t.getLeg2().getRatioTapChanger()));
            // Only the asymmetrical phase tap changer of this model is moved. The symmetrical one is left alone
            // because the CGMES import cannot read its position back, see the note on ASYMMETRICAL_PHASE_TAP_CHANGER.
            moveUp(sender.getTwoWindingsTransformer(ASYMMETRICAL_PHASE_TAP_CHANGER).getPhaseTapChanger());
        });

        // Guard the coverage of this test: if the model or a filter above changes so that a category stops being
        // exercised, the loops below would silently assert nothing.
        Set<String> changedAttributes = result.events().stream()
                .filter(UpdateNetworkEvent.class::isInstance)
                .map(UpdateNetworkEvent.class::cast)
                .map(UpdateNetworkEvent::attribute)
                .collect(Collectors.toSet());
        assertTrue(changedAttributes.containsAll(Set.of("p0", "q0", "targetP", "targetQ", "sectionCount",
                        "voltageSetpoint", "ratioTapChanger.tapPosition", "ratioTapChanger2.tapPosition",
                        "phaseTapChanger.tapPosition")),
                () -> "some equipment type was not exercised, changed attributes were " + changedAttributes);

        for (Load expected : result.sender().getLoads()) {
            assertLoadSetpoints(result, expected.getId());
        }
        for (Generator expected : result.sender().getGenerators()) {
            Generator actual = result.receiver().getGenerator(expected.getId());
            assertEquals(expected.getTargetP(), actual.getTargetP(), TOLERANCE, expected.getId());
            assertEquals(expected.getTargetQ(), actual.getTargetQ(), TOLERANCE, expected.getId());
        }
        for (ShuntCompensator expected : result.sender().getShuntCompensators()) {
            assertEquals(expected.getSectionCount(), result.receiver().getShuntCompensator(expected.getId()).getSectionCount(),
                    expected.getId());
        }
        for (StaticVarCompensator expected : result.sender().getStaticVarCompensators()) {
            assertStaticVarCompensatorSetpoints(result, expected.getId());
        }
        for (TwoWindingsTransformer expected : result.sender().getTwoWindingsTransformers()) {
            TwoWindingsTransformer actual = result.receiver().getTwoWindingsTransformer(expected.getId());
            if (expected.hasRatioTapChanger()) {
                assertEquals(expected.getRatioTapChanger().getTapPosition(), actual.getRatioTapChanger().getTapPosition(),
                        expected.getId());
            }
        }
        for (ThreeWindingsTransformer expected : result.sender().getThreeWindingsTransformers()) {
            ThreeWindingsTransformer actual = result.receiver().getThreeWindingsTransformer(expected.getId());
            if (expected.getLeg2().hasRatioTapChanger()) {
                assertEquals(expected.getLeg2().getRatioTapChanger().getTapPosition(),
                        actual.getLeg2().getRatioTapChanger().getTapPosition(), expected.getId());
            }
        }
        assertEquals(result.sender().getTwoWindingsTransformer(ASYMMETRICAL_PHASE_TAP_CHANGER).getPhaseTapChanger().getTapPosition(),
                result.receiver().getTwoWindingsTransformer(ASYMMETRICAL_PHASE_TAP_CHANGER).getPhaseTapChanger().getTapPosition());
    }

    private static boolean canMoveUp(TapChanger<?, ?, ?, ?> tapChanger) {
        return tapChanger.getTapPosition() < tapChanger.getHighTapPosition();
    }

    private static void moveUp(TapChanger<?, ?, ?, ?> tapChanger) {
        tapChanger.setTapPosition(tapChanger.getTapPosition() + 1);
    }

    // Every object is described exactly once, whatever the number of changes affecting it

    @Test
    void changesOnTheSameObjectAreMergedIntoOneDescription() {
        Network sender = readCgmesResources(GENERATOR_DIR, "generator_EQ.xml", "generator_SSH.xml");
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getGenerator("SynchronousMachine").setTargetP(165.0).setTargetQ(-5.0).setTargetV(410.0);

        String sshXml = PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL);
        assertEquals(1, countOccurrences(sshXml, "<cim:SynchronousMachine rdf:about="));
        assertEquals(1, countOccurrences(sshXml, "<cim:RegulatingControl rdf:about="));
    }

    /**
     * A RegulatingControl carries a single target whose meaning depends on the regulation mode. Writing one
     * description per changed setpoint would produce two contradictory targets for the same object.
     */
    @Test
    void bothStaticVarCompensatorSetpointsProduceOneConsistentRegulatingControl() {
        Network sender = readCgmesResources(STATIC_VAR_COMPENSATOR_DIR, "staticVarCompensator_EQ.xml", "staticVarCompensator_SSH.xml");
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        StaticVarCompensator svc = sender.getStaticVarCompensator("StaticVarCompensator-V");
        svc.setVoltageSetpoint(400.0);
        svc.setReactivePowerSetpoint(50.0);

        String sshXml = PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL);
        assertEquals(1, countOccurrences(sshXml, "<cim:RegulatingControl rdf:about="));
        // The compensator regulates voltage, so the exported target is the voltage setpoint, in kV
        assertTrue(sshXml.contains("<cim:RegulatingControl.targetValue>400</cim:RegulatingControl.targetValue>"));
        assertTrue(sshXml.contains("UnitMultiplier.k\"/>"));
        assertFalse(sshXml.contains("UnitMultiplier.M\"/>"));
    }

    // Changes that cannot be expressed in a Steady State Hypothesis file

    /**
     * The CGMES import creates fictitious switches to represent disconnected terminals. They have no master
     * resource identifier in the equipment model, so a receiver could not resolve a description of them.
     */
    @Test
    void changeOnAFictitiousSwitchIsRejected() {
        Network sender = readCgmesResources(SWITCH_DIR, "switch_EQ.xml", "switch_SSH.xml");
        Switch fictitiousSwitch = sender.getSwitchStream()
                .filter(sw -> sw.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL) != null)
                .findFirst().orElse(null);
        assertNotNull(fictitiousSwitch, "the test model is expected to contain a fictitious switch");

        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);
        fictitiousSwitch.setOpen(!fictitiousSwitch.isOpen());

        PowsyblException exception = assertThrows(PowsyblException.class,
                () -> PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL));
        assertTrue(exception.getMessage().contains("no counterpart in the CGMES equipment model"));
    }

    /**
     * A partial file references a RegulatingControl the receiver already has, so equipment that has none in the
     * source model cannot carry a regulation change: a generated identifier would resolve to nothing there.
     */
    @Test
    void changeOnEquipmentWithoutARegulatingControlIsRejected() {
        Network sender = readCgmesResources(GENERATOR_DIR, "generator_EQ.xml", "generator_SSH.xml");
        Generator generator = sender.getGenerator("SynchronousMachine");
        assertTrue(generator.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL),
                "the test model is expected to give the generator a regulating control");

        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);
        generator.setTargetV(410.0);

        PowsyblException exception = assertThrows(PowsyblException.class,
                () -> PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL));
        assertTrue(exception.getMessage().contains("has no CGMES regulating control"));
    }

    /**
     * Whether a change can be expressed at all is sometimes only known once part of it has been translated:
     * switching voltage regulation off writes the control enabled flag of the machine before it discovers that the
     * machine has no RegulatingControl to carry the target. A receiver must never see the one without the other.
     */
    @Test
    void anUnexportableChangeLeavesNothingBehind() {
        Network sender = readCgmesResources(GENERATOR_DIR, "generator_EQ.xml", "generator_SSH.xml");
        Generator generator = sender.getGenerator("SynchronousMachine");
        generator.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);

        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);
        generator.setVoltageRegulatorOn(false);

        String sshXml = PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.IGNORE);
        assertFalse(sshXml.contains("RegulatingCondEq.controlEnabled"));
        assertFalse(sshXml.contains("<cim:SynchronousMachine rdf:about="));
        assertFalse(sshXml.contains("<cim:RegulatingControl rdf:about="));
    }

    /**
     * Only the change that cannot be exported is dropped. A property it shares with a change that was exported
     * stays in the file, carrying the value the network currently has, as it would without the dropped change.
     */
    @Test
    void anUnexportableChangeDoesNotDropTheChangesAroundIt() {
        Network sender = readCgmesResources(GENERATOR_DIR, "generator_EQ.xml", "generator_SSH.xml");
        Generator generator = sender.getGenerator("SynchronousMachine");
        generator.removeProperty(Conversion.PROPERTY_REGULATING_CONTROL);

        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);
        generator.setTargetP(120.0);
        generator.setVoltageRegulatorOn(false);

        String sshXml = PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.IGNORE);
        assertTrue(sshXml.contains("<cim:RotatingMachine.p>-120</cim:RotatingMachine.p>"));
        assertTrue(sshXml.contains("RegulatingCondEq.controlEnabled"));
        assertFalse(sshXml.contains("<cim:RegulatingControl rdf:about="));
    }

    @Test
    void structuralChangeIsRejected() {
        Network sender = readCgmesResources(LOAD_DIR, "load_EQ.xml", "load_SSH.xml");
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getLoad("EnergyConsumer").remove();

        PowsyblException exception = assertThrows(PowsyblException.class,
                () -> PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL));
        assertFalse(exception.getMessage().isEmpty());
    }

    @Test
    void unsupportedChangesAreSkippedWhenIgnoring() {
        Network sender = readCgmesResources(LOAD_DIR, "load_EQ.xml", "load_SSH.xml");
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getLoad("EnergyConsumer").setP0(10.5).setQ0(5.5);
        sender.getLoad("EnergySource").remove();

        String sshXml = PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.IGNORE);
        assertTrue(sshXml.contains("<cim:EnergyConsumer rdf:about=\"#_EnergyConsumer\">"));
        assertFalse(sshXml.contains("EnergySource"));
    }

    /**
     * The export reports which changes reached the file. Under {@link UnsupportedChangeBehavior#IGNORE} that is a
     * strict subset of the recorded changes, holding the very objects that were passed in.
     */
    @Test
    void ignoredChangesAreLeftOutOfTheExportedChanges() {
        Network sender = readCgmesResources(LOAD_DIR, "load_EQ.xml", "load_SSH.xml");
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getLoad("EnergyConsumer").setP0(10.5).setQ0(5.5);
        sender.getLoad("EnergySource").remove();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<NetworkEvent> exportedEvents = PartialSshExport.write(sender, recorder.getEvents(), outputStream,
                new PartialSshExport.ExportOptions()
                        .setUnsupportedChangeBehavior(UnsupportedChangeBehavior.IGNORE));

        List<NetworkEvent> compactedEvents = PartialSshExport.compactEvents(recorder.getEvents());
        assertTrue(compactedEvents.containsAll(exportedEvents));
        assertNotEquals(compactedEvents.size(), exportedEvents.size());
        assertTrue(exportedEvents.stream()
                .allMatch(event -> event instanceof UpdateNetworkEvent update && "EnergyConsumer".equals(update.id())));
    }

    /** Under {@link UnsupportedChangeBehavior#FAIL} nothing can be dropped, so the report is the compacted log. */
    @Test
    void everyCompactedChangeIsReportedAsExportedWhenFailing() throws IOException {
        Network sender = readCgmesResources(GENERATOR_DIR, "generator_EQ.xml", "generator_SSH.xml");
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        Generator generator = sender.getGenerator("SynchronousMachine");
        generator.setTargetP(100.0);
        generator.setTargetP(120.0);
        generator.setTargetV(410.0);

        List<NetworkEvent> exportedEvents = PartialSshExport.write(sender, recorder.getEvents(),
                tmpDir.resolve("manifest_SSH.xml"), UnsupportedChangeBehavior.FAIL);

        assertEquals(PartialSshExport.compactEvents(recorder.getEvents()), exportedEvents);
        assertNotEquals(recorder.getEvents().size(), exportedEvents.size());
    }

    @Test
    void emptyChangeListProducesAHeaderOnly() {
        Network sender = readCgmesResources(LOAD_DIR, "load_EQ.xml", "load_SSH.xml");

        String sshXml = PartialSshExport.toString(sender, List.of(), UnsupportedChangeBehavior.FAIL);
        assertTrue(sshXml.contains("</md:FullModel>"));
        assertFalse(sshXml.contains("rdf:about=\"#_"));
    }

    /**
     * A partial SSH references the SSH it replaces and the equipment model it applies to, and a merged network has
     * one of each per subnetwork. Exporting from the merged network would silently produce a header without them.
     */
    @Test
    void mergedNetworkIsRejected() {
        Network merged = Network.merge(Network.create("igm1", "test"), Network.create("igm2", "test"));

        PowsyblException exception = assertThrows(PowsyblException.class,
                () -> PartialSshExport.toString(merged, List.of(), UnsupportedChangeBehavior.FAIL));
        assertTrue(exception.getMessage().contains("subnetworks"));
    }

    // Compaction

    @Test
    void repeatedUpdatesAreCompactedWithoutChangingExportedXml() {
        Network sender = readCgmesResources(LOAD_DIR, "load_EQ.xml", "load_SSH.xml");
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getLoad("EnergyConsumer").setP0(10.5).setQ0(5.5).setP0(12.5).setQ0(6.5);

        List<NetworkEvent> compactedEvents = PartialSshExport.compactEvents(recorder.getEvents());

        assertEquals(2, compactedEvents.size());
        assertEquals(Set.of("p0", "q0"), compactedEvents.stream()
                .map(UpdateNetworkEvent.class::cast)
                .map(UpdateNetworkEvent::attribute)
                .collect(Collectors.toSet()));
        assertEquals(PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL),
                PartialSshExport.toString(sender, compactedEvents, UnsupportedChangeBehavior.FAIL));
    }

    // Header

    @Test
    void metadataDefaultsToNewModelVersionAndSupersedesSourceSsh() {
        Network sender = readCgmesResources(SWITCH_DIR, "switch_EQ.xml", "switch_SSH.xml");
        CgmesMetadataModel sourceSshModel = sourceSshModel(sender);
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getSwitch("Breaker").setOpen(true);

        String sshXml = PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL);

        assertNotEquals(sourceSshModel.getId(), extractFirstMatch(sshXml, FULL_MODEL_ID_PATTERN));
        assertEquals(Integer.toString(sourceSshModel.getVersion() + 1), extractFirstMatch(sshXml, MODEL_VERSION_PATTERN));
        assertTrue(sshXml.contains("Model.Supersedes rdf:resource=\"" + sourceSshModel.getId() + "\""));
        assertTrue(sshXml.contains("<md:Model.scenarioTime>"));
        assertTrue(sshXml.contains("<md:Model.created>"));
    }

    /**
     * A partial SSH is only applicable next to the equipment model it was derived from, so the dependencies of the
     * source SSH have to be carried over.
     */
    @Test
    void metadataKeepsDependenciesOfSourceSsh() {
        Network sender = readCgmesResources(GENERATOR_DIR, "generator_EQ.xml", "generator_SSH.xml");
        CgmesMetadataModel sourceSshModel = sourceSshModel(sender);
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getGenerator("SynchronousMachine").setTargetP(165.0);

        String sshXml = PartialSshExport.toString(sender, recorder.getEvents(), UnsupportedChangeBehavior.FAIL);
        for (String dependentOn : sourceSshModel.getDependentOn()) {
            assertTrue(sshXml.contains("Model.DependentOn rdf:resource=\"" + dependentOn + "\""),
                    "expected a dependency on " + dependentOn);
        }
    }

    @Test
    void metadataOptionsOverrideDefaultHeaderValues() {
        Network sender = readCgmesResources(SWITCH_DIR, "switch_EQ.xml", "switch_SSH.xml");
        CgmesMetadataModel sourceSshModel = sourceSshModel(sender);
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);

        sender.getSwitch("Breaker").setOpen(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PartialSshExport.write(sender, recorder.getEvents(), outputStream,
                new PartialSshExport.ExportOptions()
                        .setModelId("urn:uuid:partial-ssh-test")
                        .setDescription("partial ssh update")
                        .setVersion(99)
                        .setModelingAuthoritySet("urn:mas:test")
                        .clearDependencies()
                        .addDependentOn("urn:dependency:eq")
                        .setSupersedePreviousSshModel(false)
                        .addSupersedes("urn:supersedes:ssh"));
        String sshXml = outputStream.toString(StandardCharsets.UTF_8);

        assertEquals("urn:uuid:partial-ssh-test", extractFirstMatch(sshXml, FULL_MODEL_ID_PATTERN));
        assertEquals("99", extractFirstMatch(sshXml, MODEL_VERSION_PATTERN));
        assertTrue(sshXml.contains("<md:Model.description>partial ssh update</md:Model.description>"));
        assertTrue(sshXml.contains("<md:Model.modelingAuthoritySet>urn:mas:test</md:Model.modelingAuthoritySet>"));
        assertTrue(sshXml.contains("Model.DependentOn rdf:resource=\"urn:dependency:eq\""));
        assertTrue(sshXml.contains("Model.Supersedes rdf:resource=\"urn:supersedes:ssh\""));
        assertFalse(sshXml.contains("Model.Supersedes rdf:resource=\"" + sourceSshModel.getId() + "\""));
        for (String dependentOn : sourceSshModel.getDependentOn()) {
            assertFalse(sshXml.contains("Model.DependentOn rdf:resource=\"" + dependentOn + "\""));
        }
    }

    // Helpers

    private static CgmesMetadataModel sourceSshModel(Network network) {
        return network.getExtension(CgmesMetadataModels.class)
                .getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .orElseThrow();
    }

    private static int countOccurrences(String text, String searched) {
        int count = 0;
        for (int index = text.indexOf(searched); index >= 0; index = text.indexOf(searched, index + 1)) {
            count++;
        }
        return count;
    }

    private static String extractFirstMatch(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private static void assertSwitchState(RoundTripResult result, String id) {
        assertEquals(result.sender().getSwitch(id).isOpen(), result.receiver().getSwitch(id).isOpen(), id);
    }

    private static void assertLoadSetpoints(RoundTripResult result, String id) {
        Load expected = result.sender().getLoad(id);
        Load actual = result.receiver().getLoad(id);
        assertEquals(expected.getP0(), actual.getP0(), TOLERANCE, id);
        assertEquals(expected.getQ0(), actual.getQ0(), TOLERANCE, id);
    }

    private static void assertGeneratorTargets(RoundTripResult result, String id) {
        Generator expected = result.sender().getGenerator(id);
        Generator actual = result.receiver().getGenerator(id);
        assertEquals(expected.getTargetP(), actual.getTargetP(), TOLERANCE, id);
        assertEquals(expected.getTargetQ(), actual.getTargetQ(), TOLERANCE, id);
        assertEquals(expected.getTargetV(), actual.getTargetV(), TOLERANCE, id);
    }

    private static void assertShuntOperatingValues(RoundTripResult result, String id) {
        ShuntCompensator expected = result.sender().getShuntCompensator(id);
        ShuntCompensator actual = result.receiver().getShuntCompensator(id);
        assertEquals(expected.getSectionCount(), actual.getSectionCount(), id);
        assertEquals(expected.getTargetV(), actual.getTargetV(), TOLERANCE, id);
    }

    private static void assertStaticVarCompensatorSetpoints(RoundTripResult result, String id) {
        StaticVarCompensator expected = result.sender().getStaticVarCompensator(id);
        StaticVarCompensator actual = result.receiver().getStaticVarCompensator(id);
        assertEquals(expected.getVoltageSetpoint(), actual.getVoltageSetpoint(), TOLERANCE, id);
        assertEquals(expected.getReactivePowerSetpoint(), actual.getReactivePowerSetpoint(), TOLERANCE, id);
    }

    private static void assertHvdcSetpoint(RoundTripResult result, String id) {
        assertEquals(result.sender().getHvdcLine(id).getActivePowerSetpoint(),
                result.receiver().getHvdcLine(id).getActivePowerSetpoint(), TOLERANCE, id);
    }

    private static void assertVscVoltageSetpoint(RoundTripResult result, int side) {
        VscConverterStation expected = converter(result.sender(), side);
        VscConverterStation actual = converter(result.receiver(), side);
        assertEquals(expected.getVoltageSetpoint(), actual.getVoltageSetpoint(), TOLERANCE, "converter " + side);
    }

    private static VscConverterStation converter(Network network, int side) {
        HvdcLine line = network.getHvdcLine("DCLineSegment-Vsc");
        return (VscConverterStation) (side == 1 ? line.getConverterStation1() : line.getConverterStation2());
    }
}
