/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Abdelsalem Hedhili {@literal <abdelsalem.hedhili at rte-france.com>}
 */

class UcteExporterTest extends AbstractSerDeTest {

    /**
     * Utility method to load a network file from resource directory without calling
     * @param filePath path of the file relative to resources directory
     * @return imported network
     */
    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    private static Network loadNetworkFromResourceFile(String filePath, Properties parameters) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), parameters);
    }

    private static void testExporter(Network network, String reference) throws IOException {
        testExporter(network, reference, new Properties());
    }

    private static void testExporter(Network network, String reference, Properties parameters) throws IOException {
        MemDataSource dataSource = new MemDataSource();

        UcteExporter exporter = new UcteExporter();
        exporter.export(network, parameters, dataSource);

        try (InputStream actual = dataSource.newInputStream(null, "uct");
             InputStream expected = UcteExporterTest.class.getResourceAsStream(reference)) {
            assertTxtEquals(expected, actual, Arrays.asList(1, 2));
        }
    }

    private static String exportToString(Network network) throws IOException {
        MemDataSource dataSource = new MemDataSource();
        new UcteExporter().export(network, new Properties(), dataSource);
        try (InputStream is = dataSource.newInputStream(null, "uct")) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String findNodeLine(String exportedText, String nodeCode) {
        return exportedText.lines()
                .filter(line -> line.length() >= 8 && line.substring(0, 8).equals(nodeCode))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No node line found for code " + nodeCode));
    }

    private static final String BUS_21 = "FFFFFF71";
    private static final String BUS_380 = "FFFFFF11";

    /**
     * Builds a bus-breaker network with a 380 kV voltage level (bus {@code FFFFFF11}, holding load {@code L380}) and a
     * 21 kV voltage level (bus {@code FFFFFF71}, initially empty) in the same substation.
     */
    private static Network createMultiInjectionNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("multi-injection", "test");
        Substation substation = network.newSubstation().setId("S").setCountry(Country.FR).add();
        VoltageLevel vl380 = substation.newVoltageLevel().setId("VL380").setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl380.getBusBreakerView().newBus().setId(BUS_380).add();
        addLoad(vl380, "L380", BUS_380, 1, 1);
        VoltageLevel vl21 = substation.newVoltageLevel().setId("VL21").setNominalV(21).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl21.getBusBreakerView().newBus().setId(BUS_21).add();
        return network;
    }

    private static VoltageLevel vl21(Network network) {
        return network.getVoltageLevel("VL21");
    }

    private static void addLoad(VoltageLevel voltageLevel, String id, String busId, double p0, double q0) {
        voltageLevel.newLoad().setId(id).setBus(busId).setConnectableBus(busId).setP0(p0).setQ0(q0).add();
    }

    private static GeneratorAdder generatorAdder(VoltageLevel voltageLevel, String id, double targetP, double minP, double maxP) {
        return voltageLevel.newGenerator()
                .setId(id)
                .setBus(BUS_21)
                .setConnectableBus(BUS_21)
                .setEnergySource(EnergySource.OTHER)
                .setTargetP(targetP)
                .setMinP(minP)
                .setMaxP(maxP)
                .setLocalTargetQ(0);
    }

    /** Adds a generator without voltage regulation (PQ). */
    private static Generator addGenerator(VoltageLevel voltageLevel, String id, double targetP, double minP, double maxP) {
        return generatorAdder(voltageLevel, id, targetP, minP, maxP).add();
    }

    /** Adds a voltage-regulating generator with a local target. */
    private static Generator addRegulatingGenerator(VoltageLevel voltageLevel, String id, double targetP, double targetV) {
        GeneratorAdder adder = generatorAdder(voltageLevel, id, targetP, 0, 1000).setLocalTargetV(targetV);
        adder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true).add();
        return adder.add();
    }

    /** Adds a voltage-regulating generator with no local target, regulating {@code terminal} at {@code targetValue}. */
    private static Generator addRemoteRegulatingGenerator(VoltageLevel voltageLevel, String id, double targetP,
                                                          Terminal terminal, double targetValue) {
        GeneratorAdder adder = generatorAdder(voltageLevel, id, targetP, 0, 1000);
        adder.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(true)
                .withTerminal(terminal).withTargetValue(targetValue).add();
        return adder.add();
    }

    @Test
    void testMerge() throws IOException {
        Network networkFR = loadNetworkFromResourceFile("/frTestGridForMerging.uct");
        testExporter(networkFR, "/frTestGridForMerging.uct");

        Network networkBE = loadNetworkFromResourceFile("/beTestGridForMerging.uct");
        testExporter(networkBE, "/beTestGridForMerging.uct");

        Network merge = Network.merge(networkBE, networkFR);
        testExporter(merge, "/uxTestGridForMerging.uct");
    }

    @Test
    void testMergeProperties() throws IOException {
        Network networkFR = loadNetworkFromResourceFile("/frForMergeProperties.uct");
        testExporter(networkFR, "/frForMergeProperties.uct");

        Network networkBE = loadNetworkFromResourceFile("/beForMergeProperties.uct");
        testExporter(networkBE, "/beForMergeProperties.uct");

        Network mergedNetwork = Network.merge(networkBE, networkFR);
        testExporter(mergedNetwork, "/uxForMergeProperties.uct");
    }

    @Test
    void testExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        testExporter(network, "/expectedExport.uct");
    }

    @Test
    void testExporter() {
        var exporter = new UcteExporter();
        assertEquals("UCTE", exporter.getFormat());
        assertNotEquals("IIDM", exporter.getFormat());
        assertEquals("IIDM to UCTE converter", exporter.getComment());
        assertNotEquals("UCTE to IIDM converter", exporter.getComment());
        assertEquals(2, exporter.getParameters().size());
    }

    @Test
    void testCouplerToXnodeImport() throws IOException {
        Network network = loadNetworkFromResourceFile("/couplerToXnodeExample.uct");
        testExporter(network, "/couplerToXnodeExample.uct");
    }

    @Test
    void shouldNotUseScientificalNotationForExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/testGridNoScientificNotation.uct");
        testExporter(network, "/testGridNoScientificNotation.uct");
    }

    @Test
    void testDefaultOneNamingStrategy() {
        NamingStrategy defaultNamingStrategy = UcteExporter.findNamingStrategy(null, ImmutableList.of(new DefaultNamingStrategy()));
        assertEquals("Default", defaultNamingStrategy.getName());
    }

    @Test
    void testDefaultTwoNamingStrategies() {
        try {
            UcteExporter.findNamingStrategy(null, ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    void testDefaultNoNamingStrategy() {
        try {
            UcteExporter.findNamingStrategy(null, ImmutableList.of());
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    void testChosenTwoNamingStrategies() {
        NamingStrategy namingStrategy = UcteExporter.findNamingStrategy("Default", ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
        assertEquals("Default", namingStrategy.getName());
        namingStrategy = UcteExporter.findNamingStrategy("OtherNamingStrategy", ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
        assertEquals("OtherNamingStrategy", namingStrategy.getName());
    }

    @Test
    void testWithIdDuplicationBetweenLineAndTransformer() throws IOException {
        Network network = loadNetworkFromResourceFile("/id_duplication_test.uct");
        testExporter(network, "/id_duplication_test.uct");
    }

    @Test
    void testElementStatusHandling() throws IOException {
        Network network = loadNetworkFromResourceFile("/multipleStatusTests.uct");
        testExporter(network, "/multipleStatusTests.uct");
    }

    @Test
    void testVoltageRegulatingXnode() throws IOException {
        Network network = loadNetworkFromResourceFile("/frVoltageRegulatingXnode.uct");
        testExporter(network, "/frVoltageRegulatingXnode.uct");
    }

    @Test
    void testExportGeneratorWithSentinelLimitsIsBlank() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        Generator generator = network.getGenerator("B_SU1_21_generator");
        generator.setMinP(-9999);
        generator.setMaxP(9999);
        generator.newMinMaxReactiveLimits()
                .setMinQ(-9999)
                .setMaxQ(9999)
                .add();

        String nodeLine = findNodeLine(exportToString(network), "B_SU1_21");
        assertEquals("       ", nodeLine.substring(65, 72));
        assertEquals("       ", nodeLine.substring(73, 80));
        assertEquals("       ", nodeLine.substring(81, 88));
        assertEquals("       ", nodeLine.substring(89, 96));
    }

    @Test
    void testExportGeneratorWithMaxValueActivePowerLimitsIsBlank() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        Generator generator = network.getGenerator("B_SU1_21_generator");
        generator.setMinP(-Double.MAX_VALUE);
        generator.setMaxP(Double.MAX_VALUE);

        String nodeLine = findNodeLine(exportToString(network), "B_SU1_21");
        assertEquals("       ", nodeLine.substring(65, 72));
        assertEquals("       ", nodeLine.substring(73, 80));
    }

    @Test
    void testExportGeneratorWithMaxValueReactivePowerLimitsIsBlank() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        Generator generator = network.getGenerator("B_SU1_21_generator");
        generator.newMinMaxReactiveLimits()
                .setMinQ(-Double.MAX_VALUE)
                .setMaxQ(Double.MAX_VALUE)
                .add();

        String nodeLine = findNodeLine(exportToString(network), "B_SU1_21");
        assertEquals("       ", nodeLine.substring(81, 88));
        assertEquals("       ", nodeLine.substring(89, 96));
    }

    @Test
    void testExportBoundaryLineWithMaxValueActivePowerLimitsIsBlank() throws IOException {
        Network network = loadNetworkFromResourceFile("/frVoltageRegulatingXnode.uct");
        BoundaryLine boundaryLine = network.getBoundaryLine("FFFFFF13 XXXXXX14 1");
        boundaryLine.getGeneration().setMinP(-Double.MAX_VALUE);
        boundaryLine.getGeneration().setMaxP(Double.MAX_VALUE);

        String nodeLine = findNodeLine(exportToString(network), "XXXXXX14");
        assertEquals("       ", nodeLine.substring(65, 72));
        assertEquals("       ", nodeLine.substring(73, 80));
        // Reactive limits were not touched: they keep exporting their original, real fixture values.
        assertEquals("1.00000", nodeLine.substring(81, 88));
        assertEquals("-1.0000", nodeLine.substring(89, 96));
    }

    @Test
    void testExportGeneratorWithOutOfRangeLimitIsBlank() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        Generator generator = network.getGenerator("B_SU1_21_generator");
        generator.setMaxP(50_000_000);
        generator.setMinP(-50_000_000);

        String nodeLine = findNodeLine(exportToString(network), "B_SU1_21");
        assertEquals("       ", nodeLine.substring(65, 72));
        assertEquals("       ", nodeLine.substring(73, 80));
    }

    @Test
    void testMissingPermanentLimit() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport_withoutPermanentLimit.uct");
        testExporter(network, "/expectedExport_withoutPermanentLimit.uct");
    }

    @Test
    void testXnodeTransformer() throws IOException {
        Network network = loadNetworkFromResourceFile("/xNodeTransformer.uct");
        testExporter(network, "/xNodeTransformer.uct");
    }

    @Test
    void testValidationUtil() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        for (Bus bus : network.getBusView().getBuses()) {
            bus.setV(bus.getVoltageLevel().getNominalV() * 1.4);
        }
        for (Generator gen : network.getGenerators()) {
            if (gen.isRegulatingWithMode(RegulationMode.VOLTAGE)) {
                double targetV = gen.getRegulatingTerminal().getVoltageLevel().getNominalV() * 1.4;
                if (gen.hasRegulatingTerminal()) {
                    gen.getVoltageRegulation().setTargetValue(targetV);
                } else {
                    gen.setLocalTargetV(targetV);
                }
            }
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null && rtc.isRegulating()) {
                double targetV = rtc.getRegulatingTerminal().getVoltageLevel().getNominalV() * 1.4;
                // Always with a Terminal
                rtc.getVoltageRegulation().setTargetValue(targetV);
            }
        }
        testExporter(network, "/invalidVoltageReference.uct");
    }

    @Test
    void roundTripOfNetworkWithXnodesConnectedToOneClosedLineMustSucceed() throws IOException {
        Network network = loadNetworkFromResourceFile("/xnodeOneClosedLine.uct");
        testExporter(network, "/xnodeOneClosedLine.uct");
    }

    @Test
    void roundTripOfNetworkWithXnodesConnectedToTwoClosedLineMustSucceed() throws IOException {
        Network network = loadNetworkFromResourceFile("/xnodeTwoClosedLine.uct");
        testExporter(network, "/xnodeTwoClosedLine.uct");
    }

    @Test
    void roundTripOfNetworkWithPstAngleRegulationMustSucceed() throws IOException {
        Network network = loadNetworkFromResourceFile("/phaseShifterActivePowerOn.uct");
        testExporter(network, "/phaseShifterActivePowerOn.uct");
    }

    @Test
    void roundTripOfNetworkWithTapChangers() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport2.uct");
        testExporter(network, "/expectedExport3.uct"); // because of asymmetrical phase shifter
    }

    @Test
    void roundTripOfNetworkWithTapChangers2() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport4.uct");
        testExporter(network, "/expectedExport4.uct");
    }

    @Test
    void roundTripOfNetworkWithTapChangers3() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport5.uct");
        testExporter(network, "/expectedExport5.uct");
    }

    @Test
    void testTapChangers() {
        Network network = loadNetworkFromResourceFile("/expectedExport2.uct");
        Network exportedNetwork = loadNetworkFromResourceFile("/expectedExport3.uct"); // because of asymmetrical phase shifter
        String rtcId = "0BBBBB5  0AAAAA2  1";
        assertEquals(network.getTwoWindingsTransformer(rtcId).getRatioTapChanger().getCurrentStep().getRho(),
                exportedNetwork.getTwoWindingsTransformer(rtcId).getRatioTapChanger().getCurrentStep().getRho());
        String ptcId = "HDDDDD2  HCCCCC1  1";
        assertEquals(network.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getRho(),
                exportedNetwork.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getRho(), 0.0001);
        assertEquals(network.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getAlpha(),
                exportedNetwork.getTwoWindingsTransformer(ptcId).getPhaseTapChanger().getCurrentStep().getAlpha(), 0.0001);
        String ptcId2 = "ZABCD221 ZEFGH221 1";
        assertEquals(network.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getRho(),
                exportedNetwork.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getRho());
        assertEquals(network.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getAlpha(),
                exportedNetwork.getTwoWindingsTransformer(ptcId2).getPhaseTapChanger().getCurrentStep().getAlpha(), 0.0001);
    }

    @Test
    void roundTripOfCombineRtcAndPtc() throws IOException {
        Properties parameters = new Properties();
        parameters.put("ucte.import.combine-phase-angle-regulation", "true");
        parameters.put("ucte.export.combine-phase-angle-regulation", "true");
        Network network = loadNetworkFromResourceFile("/expectedExport5.uct", parameters);
        testExporter(network, "/expectedExport5.uct", parameters);
    }

    /**
     * Two loads on the same bus: no exception, and the exported active and reactive loads are the sums.
     * <pre>
     *   FFFFFF71 --- LA (10 MW, 2 MVar)
     *            \-- LB (5 MW, 1 MVar)
     * </pre>
     */
    @Test
    void testExportTwoLoadsOnOneBus() throws IOException {
        Network network = createMultiInjectionNetwork();
        addLoad(vl21(network), "LA", BUS_21, 10, 2);
        addLoad(vl21(network), "LB", BUS_21, 5, 1);

        testExporter(network, "/multiGenLoadTwoLoads.uct");
    }

    /**
     * Two generators with finite limits: generation and the four limits are exported as the sums.
     * <pre>
     *   FFFFFF71 --- GA (P=100, P in [10, 200], Q in [-50, 50], Q=5)
     *            \-- GB (P=50,  P in [5, 100],  Q in [-20, 20], Q=3)
     * </pre>
     */
    @Test
    void testExportTwoGeneratorsWithFiniteLimitsAreSummed() throws IOException {
        Network network = createMultiInjectionNetwork();
        Generator genA = addGenerator(vl21(network), "GA", 100, 10, 200);
        genA.setLocalTargetQ(5);
        genA.newMinMaxReactiveLimits().setMinQ(-50).setMaxQ(50).add();
        Generator genB = addGenerator(vl21(network), "GB", 50, 5, 100);
        genB.setLocalTargetQ(3);
        genB.newMinMaxReactiveLimits().setMinQ(-20).setMaxQ(20).add();

        testExporter(network, "/multiGenLoadFiniteLimitsSummed.uct");
    }

    /**
     * Reactive capability curves are evaluated at each generator's own targetP and not at the summed one.
     * Both generators have the curve {@code minQ = -10 - P, maxQ = 10 + P} on [0, 200].
     * <pre>
     *   FFFFFF71 --- GA (P=50  =&gt; Q in [-60, 60])
     *            \-- GB (P=150 =&gt; Q in [-160, 160])
     * </pre>
     * Expected: minQ = -220, maxQ = 220 (at the summed P=200 it would have been -210 / 210 for both).
     */
    @Test
    void testExportReactiveLimitsEvaluatedAtOwnTargetP() throws IOException {
        Network network = createMultiInjectionNetwork();
        Generator genA = addGenerator(vl21(network), "GA", 50, 0, 200);
        Generator genB = addGenerator(vl21(network), "GB", 150, 0, 200);
        for (Generator generator : new Generator[] {genA, genB}) {
            generator.newReactiveCapabilityCurve()
                    .beginPoint().setP(0).setMinQ(-10).setMaxQ(10).endPoint()
                    .beginPoint().setP(200).setMinQ(-210).setMaxQ(210).endPoint()
                    .add();
        }

        testExporter(network, "/multiGenLoadReactiveLimitsAtOwnTargetP.uct");
    }

    /**
     * If any generator has no limit ({@code 9999}), the aggregated maxP is blank; the other limits (finite on both
     * generators) are still summed.
     */
    @Test
    void testExportOneUnboundedGeneratorMakesAggregatedLimitBlank() throws IOException {
        Network network = createMultiInjectionNetwork();
        addGenerator(vl21(network), "GA", 10, 1, 9999);
        addGenerator(vl21(network), "GB", 10, 2, 100);

        testExporter(network, "/multiGenLoadUnboundedLimit.uct");
    }

    /** Same as above with {@code Double.MAX_VALUE} as the "no limit" marker. */
    @Test
    void testExportOneMaxValueGeneratorMakesAggregatedLimitBlank() throws IOException {
        Network network = createMultiInjectionNetwork();
        addGenerator(vl21(network), "GA", 10, 1, Double.MAX_VALUE);
        addGenerator(vl21(network), "GB", 10, 2, 100);

        testExporter(network, "/multiGenLoadUnboundedLimit.uct");
    }

    /** Two generators with finite {@code maxP = 6000}: the sum (12000) is out of bounds and is exported blank. */
    @Test
    void testExportFiniteLimitsSummingOutOfBoundsAreBlank() throws IOException {
        Network network = createMultiInjectionNetwork();
        addGenerator(vl21(network), "GA", 10, 0, 6000);
        addGenerator(vl21(network), "GB", 10, 0, 6000);

        testExporter(network, "/multiGenLoadLimitSumOutOfBounds.uct");
    }

    /**
     * Two voltage-regulating generators with different local targets: the node is PU and its voltage reference is the
     * one of the generator with the largest targetP.
     */
    @Test
    void testExportConflictingLocalTargetsUsesLargestTargetP() throws IOException {
        Network network = createMultiInjectionNetwork();
        addRegulatingGenerator(vl21(network), "GA", 100, 22.0);
        addRegulatingGenerator(vl21(network), "GB", 200, 23.0);

        testExporter(network, "/multiGenLoadConflictingLocalTargets.uct");
    }

    /** Same targetP: the generator with the smallest id wins (GB is added first to rule out an iteration-order effect). */
    @Test
    void testExportConflictingLocalTargetsTieUsesSmallestId() throws IOException {
        Network network = createMultiInjectionNetwork();
        addRegulatingGenerator(vl21(network), "GB", 100, 23.0);
        addRegulatingGenerator(vl21(network), "GA", 100, 22.0);

        testExporter(network, "/multiGenLoadTargetTie.uct");
    }

    /**
     * Regulating generator with no local target and an explicit regulating terminal on the same bus.
     * <pre>
     *   FFFFFF71 --- L21 (regulating terminal)
     *            \-- GA (localTargetV = NaN, targetValue = 22.5)
     * </pre>
     */
    @Test
    void testExportSameBusRegulatingTerminalUsesTargetValue() throws IOException {
        Network network = createMultiInjectionNetwork();
        addLoad(vl21(network), "L21", BUS_21, 1, 1);
        addRemoteRegulatingGenerator(vl21(network), "GA", 100, network.getLoad("L21").getTerminal(), 22.5);

        testExporter(network, "/multiGenLoadSameBusRegulatingTerminal.uct");
    }

    /**
     * Generator on the 21 kV bus regulating the 380 kV bus at 405 kV: the target is rescaled to
     * {@code 405 * 21 / 380}.
     */
    @Test
    void testExportRemoteRegulationIsRescaled() throws IOException {
        Network network = createMultiInjectionNetwork();
        addRemoteRegulatingGenerator(vl21(network), "GA", 100, network.getLoad("L380").getTerminal(), 405);

        testExporter(network, "/multiGenLoadRemoteRegulation.uct");
    }

    /**
     * Local targets are preferred over rescaled remote ones, even if the remote generator has the larger targetP.
     * <pre>
     *   FFFFFF71 --- GA (P=10, localTargetV = 22)
     *            \-- GB (P=500, regulates FFFFFF11 at 405 kV)
     * </pre>
     */
    @Test
    void testExportLocalTargetPreferredOverRescaledRemote() throws IOException {
        Network network = createMultiInjectionNetwork();
        addRegulatingGenerator(vl21(network), "GA", 10, 22.0);
        addRemoteRegulatingGenerator(vl21(network), "GB", 500, network.getLoad("L380").getTerminal(), 405);

        testExporter(network, "/multiGenLoadLocalPreferredOverRemote.uct");
    }

    /** Generators with different power plant types are exported as type F. */
    @Test
    void testExportMixedPowerPlantTypesIsF() throws IOException {
        Network network = createMultiInjectionNetwork();
        addGenerator(vl21(network), "GA", 10, 0, 100).setEnergySource(EnergySource.HYDRO);
        addGenerator(vl21(network), "GB", 10, 0, 100).setEnergySource(EnergySource.THERMAL);

        testExporter(network, "/multiGenLoadMixedPowerPlantTypes.uct");
    }

    /** Generators with identical power plant types keep that type (HYDRO is H). */
    @Test
    void testExportIdenticalPowerPlantTypesIsKept() throws IOException {
        Network network = createMultiInjectionNetwork();
        addGenerator(vl21(network), "GA", 10, 0, 100).setEnergySource(EnergySource.HYDRO);
        addGenerator(vl21(network), "GB", 10, 0, 100).setEnergySource(EnergySource.HYDRO);

        testExporter(network, "/multiGenLoadIdenticalPowerPlantTypes.uct");
    }

    /**
     * Non-regulating generators only (PQ node): the voltage reference still comes from the local target, taken from
     * the generator with the largest targetP.
     */
    @Test
    void testExportNonRegulatingGeneratorsUseLocalTargetOfLargestTargetP() throws IOException {
        Network network = createMultiInjectionNetwork();
        addGenerator(vl21(network), "GA", 10, 0, 100).setLocalTargetV(22.0);
        addGenerator(vl21(network), "GB", 20, 0, 100).setLocalTargetV(23.0);

        testExporter(network, "/multiGenLoadNonRegulatingGenerators.uct");
    }
}
