/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.powerfactory.model.PowerFactoryDataLoader;
import com.powsybl.powerfactory.model.StudyCase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.commons.test.ComparisonUtils.compareXml;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class PowerFactoryImporterTest extends AbstractSerDeTest {

    @Test
    void testBase() {
        PowerFactoryImporter importer = new PowerFactoryImporter();
        assertEquals("POWER-FACTORY", importer.getFormat());
        assertTrue(importer.getParameters().isEmpty());
        assertEquals("PowerFactory to IIDM converter", importer.getComment());
    }

    @Test
    void testExistsAndCopy() throws IOException {
        InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/ieee14.dgs"));
        Path dgsFile = fileSystem.getPath("/work/ieee14.dgs");
        Files.copy(is, dgsFile);

        Optional<StudyCase> studyCase = PowerFactoryDataLoader.load(dgsFile, StudyCase.class);
        assertTrue(studyCase.isPresent());

        PowerFactoryImporter importer = new PowerFactoryImporter();
        assertTrue(importer.exists(DataSourceUtil.createDataSource(fileSystem.getPath("/work"), "", "ieee14")));
        assertFalse(importer.exists(DataSourceUtil.createDataSource(fileSystem.getPath("/work"), "", "error")));

        importer.copy(DataSourceUtil.createDataSource(fileSystem.getPath("/work"), "", "ieee14"),
                DataSourceUtil.createDataSource(fileSystem.getPath("/work"), "", "ieee14-copy"));
        assertTrue(Files.exists(fileSystem.getPath("/work/ieee14-copy.dgs")));
    }

    private Network importAndCompareXml(String id) {
        return importAndCompareXml(id, ".dgs");
    }

    private Network importJsonAndCompareXml(String id) {
        return importAndCompareXml(id, ".json");
    }

    private Network importAndCompareXml(String id, String fileExtension) {
        Network network = new PowerFactoryImporter()
                .importData(new ResourceDataSource(id, new ResourceSet("/", id + fileExtension)),
                        NetworkFactory.findDefault(),
                        null);

        Path file = fileSystem.getPath("/work/" + id + ".xiidm");
        network.setCaseDate(ZonedDateTime.parse("2021-01-01T10:00:00.000+02:00"));
        NetworkSerDe.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareXml(getClass().getResourceAsStream("/" + id + ".xiidm"), is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return network;
    }

    @Test
    void ieee14Test() {
        assertTrue(importAndCompareXiidm("ieee14"));
    }

    @Test
    void twoBusesLineWithBTest() {
        assertTrue(importAndCompareXiidm("TwoBusesLineWithB"));
    }

    @Test
    void twoBusesLineWithGandBTest() {
        assertTrue(importAndCompareXiidm("TwoBusesLineWithGandB"));
    }

    @Test
    void twoBusesLineWithTandBTest() {
        assertTrue(importAndCompareXiidm("TwoBusesLineWithTandB"));
    }

    @Test
    void twoBusesLineWithCTest() {
        assertTrue(importAndCompareXiidm("TwoBusesLineWithC"));
    }

    @Test
    void twoBusesLineWithNumberOfParallelLines() {
        assertTrue(importAndCompareXiidm("TwoBusesLineWithNumberOfParallelLines"));
    }

    @Test
    void twoBusesGeneratorTest() {
        assertTrue(importAndCompareXiidm("TwoBusesGenerator"));
    }

    @Test
    void twoBusesGeneratorWithoutIvmodeTest() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorWithoutIvmode"));
    }

    @Test
    void twoBusesGeneratorAvmodeTest() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorAvmode"));
    }

    @Test
    void twoBusesGeneratorWithoutActiveLimitsTest() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorWithoutActiveLimits"));
    }

    @Test
    void twoBusesGeneratorIqtypeTest() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorIqtype"));
    }

    @Test
    void twoBusesGeneratorWithoutIqtypeTest() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorWithoutIqtype"));
    }

    @Test
    void twoBusesGeneratorElmReactiveLimits() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorElmReactiveLimits"));
    }

    @Test
    void twoBusesGeneratorTypReactiveLimits() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorTypReactiveLimits"));
    }

    @Test
    void twoBusesGeneratorTypMvarReactiveLimits() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorTypMvarReactiveLimits"));
    }

    @Test
    void switches() {
        assertTrue(importAndCompareXiidm("Switches"));
    }

    @Test
    void switchesNegativeVoltage() {
        assertTrue(importAndCompareXiidm("Switches-negative-voltage"));
    }

    @Test
    void switchesMissingVoltage() {
        assertTrue(importAndCompareXiidm("Switches-missing-voltage"));
    }

    @Test
    void switchesMissingAngle() {
        assertTrue(importAndCompareXiidm("Switches-missing-angle"));
    }

    @Test
    void switchesWithoutBus() {
        assertTrue(importAndCompareXiidm("Switches-without-bus"));
    }

    @Test
    void transformerPhaseGBComplete() {
        assertTrue(importJsonAndCompareXiidm("Transformer-Phase-GB-complete"));
    }

    @Test
    void threeMibPhaseWinding1Complete() {
        assertTrue(importJsonAndCompareXiidm("ThreeMIB_T3W_phase_winding1_complete"));
    }

    @Test
    void commonImpedance() {
        assertTrue(importAndCompareXiidm("CommonImpedance"));
    }

    @Test
    void commonImpedanceOnlyImpedance12() {
        assertTrue(importAndCompareXiidm("CommonImpedanceOnlyImpedance12"));
    }

    @Test
    void commonImpedanceWithDifferentNominal() {
        assertTrue(importAndCompareXiidm("CommonImpedanceWithDifferentNominal"));
    }

    @Test
    void twoBusesGeneratorAndShuntRL() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorAndShuntRL"));
    }

    @Test
    void twoBusesGeneratorAndShuntRLrxrea() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorAndShuntRLrxrea"));
    }

    @Test
    void twoBusesGeneratorAndShuntC() {
        assertTrue(importAndCompareXiidm("TwoBusesGeneratorAndShuntC"));
    }

    @Test
    void tower() {
        assertTrue(importAndCompareXiidm("Tower"));
    }

    @Test
    void voltageLevelsAndSubstations() {
        assertTrue(importAndCompareXiidm("VoltageLevelsAndSubstations"));
    }

    @Test
    void hvdc() {
        assertTrue(importAndCompareXiidm("Hvdc"));
    }

    @Test
    void capabilityCurve() {
        assertTrue(importAndCompareXiidm("CapabilityCurve"));
    }

    @Test
    void transformersWithPhaseAngleClock() {
        assertTrue(importAndCompareXiidm("TransformersWithPhaseAngleClock"));
    }

    @Test
    void threeWindingsTransformerWinding1Ratio() {
        assertTrue(importAndCompareXiidm("ThreeWindingsTransformerWinding1Ratio"));
    }

    @Test
    void slackBustp() {
        assertTrue(importAndCompareXiidm("Slack_bustp"));
    }

    @Test
    void slackIpctrl() {
        assertTrue(importAndCompareXiidm("Slack_ip_ctrl"));
    }

    private boolean importAndCompareXiidm(String powerfactoryCase) {
        importAndCompareXml(powerfactoryCase);
        return true;
    }

    private boolean importJsonAndCompareXiidm(String powerfactoryCase) {
        importJsonAndCompareXml(powerfactoryCase);
        return true;
    }

    @Test
    void transformerVhVl() {
        assertTrue(transformerBalance("Transformer-VhVl", 0.00009));
    }

    @Test
    void transformerVhVlNonNeutral() {
        assertTrue(transformerBalance("Transformer-VhVl-Non-Neutral", 0.0001));
    }

    @Test
    void transformerVhVlGB() {
        assertTrue(transformerBalance("Transformer-VhVl-GB", 0.09));
    }

    @Test
    void transformerVhVlGBNonNeutral() {
        assertTrue(transformerBalance("Transformer-VhVl-GB-Non-Neutral", 0.09));
    }

    @Test
    void transformerVhVlGBNonNeutralProportion() {
        assertTrue(transformerBalance("Transformer-VhVl-GB-Non-Neutral-proportion", 0.000025));
    }

    @Test
    void transformerVlVh() {
        assertTrue(transformerBalance("Transformer-VlVh", 0.0009));
    }

    @Test
    void transformerVlVhNonNeutral() {
        assertTrue(transformerBalance("Transformer-VlVh-Non-Neutral", 0.0003));
    }

    @Test
    void transformerVlVhGB() {
        assertTrue(transformerBalance("Transformer-VlVh-GB", 0.09));
    }

    @Test
    void transformerVlVhGBNonNeutral() {
        assertTrue(transformerBalance("Transformer-VlVh-GB-Non-Neutral", 0.09));
    }

    @Test
    void transformerVlVhGBNonNeutralProportion() {
        assertTrue(transformerBalance("Transformer-VlVh-GB-Non-Neutral-proportion", 0.0003));
    }

    private boolean transformerBalance(String powerfactoryCase, double tol) {
        Network network = importAndCompareXml(powerfactoryCase);
        transformerNetworkBalance(network, tol);
        return true;
    }

    /**
     * Three buses solved case:
     * Bus 1: SlackBus where a generator and the line "lne_1_2_1" are connected
     * Bus 2: TransportBus where the line "lne_1_2_1" and the transformer "trf_2_3_1" are connected
     * Bus 3: LoadBus where the transformer "trf_2_3_1" and the load "lod_3_1" are connected
     */
    private static void transformerNetworkBalance(Network network, double tol) {

        Load load = network.getLoad("lod_3_1");
        assertNotNull(load);
        Line line = network.getLine("lne_1_2_1");
        assertNotNull(line);
        TwoWindingsTransformer t2wt = network.getTwoWindingsTransformer("trf_2_3_1");
        assertNotNull(t2wt);

        BranchData lineData = new BranchData(line, 0.0, false);
        BranchData t2wtData = new BranchData(t2wt, 0.0, false, true);
        assertEquals(0.0, lineData.getComputedP2() + t2wtData.getComputedP1(), tol);
        assertEquals(0.0, lineData.getComputedQ2() + t2wtData.getComputedQ1(), tol);
        assertEquals(0.0, t2wtData.getComputedP2() + load.getP0(), tol);
        assertEquals(0.0, t2wtData.getComputedQ2() + load.getQ0(), tol);
    }

    @Test
    void transformerPhase() {
        assertTrue(phaseShifterBalance("Transformer-Phase", 0.001));
    }

    @Test
    void transformerPhaseWithmTaps() {
        assertTrue(phaseShifterBalance("Transformer-Phase-with-mTaps", 0.001));
    }

    @Test
    void transformerPhaseNeutral() {
        assertTrue(phaseShifterBalance("Transformer-Phase-Neutral", 0.0009));
    }

    @Test
    void transformerPhaseNeutralWithmTaps() {
        assertTrue(phaseShifterBalance("Transformer-Phase-Neutral-with-mTaps", 0.0009));
    }

    @Test
    void transformerPhaseGB() {
        assertTrue(phaseShifterBalance("Transformer-Phase-GB", 0.9));
    }

    @Test
    void transformerPhaseGBWithmTaps() {
        assertTrue(phaseShifterBalance("Transformer-Phase-GB-with-mTaps", 0.9));
    }

    @Test
    void transformerPhaseGBNeutral() {
        assertTrue(phaseShifterBalance("Transformer-Phase-GB-Neutral", 0.9));
    }

    @Test
    void transformerPhaseGBNeutralWithmTaps() {
        assertTrue(phaseShifterBalance("Transformer-Phase-GB-Neutral-with-mTaps", 0.9));
    }

    private boolean phaseShifterBalance(String powerfactoryCase, double tol) {
        Network network = importAndCompareXml(powerfactoryCase);
        phaseNetworkBalance(network, tol);
        return true;
    }

    /**
     * Three buses solved case:
     * Bus 1: SlackBus where a generator, the line "lne_1_2_1" and the line "lne_1_3_1" are connected
     * Bus 2: TransportBus where the line "lne_1_2_1" and the transformer "trf_2_3_1" are connected
     * Bus 3: LoadBus where the transformer "trf_2_3_1", the line "lne_1_3_1" and the load "lod_3_1" are connected
     */
    private static void phaseNetworkBalance(Network network, double tol) {

        Load load = network.getLoad("lod_3_1");
        assertNotNull(load);
        Line line12 = network.getLine("lne_1_2_1");
        assertNotNull(line12);
        Line line13 = network.getLine("lne_1_3_1");
        assertNotNull(line13);
        TwoWindingsTransformer t2wt = network.getTwoWindingsTransformer("trf_2_3_1");
        assertNotNull(t2wt);

        BranchData line12Data = new BranchData(line12, 0.0, false);
        BranchData line13Data = new BranchData(line13, 0.0, false);
        BranchData t2wtData = new BranchData(t2wt, 0.0, false, true);
        assertEquals(0.0, line12Data.getComputedP2() + t2wtData.getComputedP1(), tol);
        assertEquals(0.0, line12Data.getComputedQ2() + t2wtData.getComputedQ1(), tol);
        assertEquals(0.0, line13Data.getComputedP2() + t2wtData.getComputedP2() + load.getP0(), tol);
        assertEquals(0.0, line13Data.getComputedQ2() + t2wtData.getComputedQ2() + load.getQ0(), tol);
    }

    @Test
    void threeMibT3wPhaseTest() {
        Network network = importAndCompareXml("ThreeMIB_T3W_phase_solved");
        threeMibT3wPhaseTestNetworkBalance(network, 435.876560, 0.09);
        assertTrue(true);
    }

    /**
     * Only the balance at the three buses of the three windings transformer is done:
     * Bus 4 (500 kV): Load "lod_4_1", line "lne_4_1_1", twoWindingsTransformer "trf_4_1_1" and threeWindingsTransformer "trf_4_2_7_1" are connected
     * Bus 2 (18 kV) : Generator "sym_1_2_1" and  threeWindingsTransformer "trf_4_2_7_1" are connected
     * Bus 7 (16 kV) : Load "lod_7_1" and threeWindingsTransformer "trf_4_2_7_1" are connected
     */
    private static void threeMibT3wPhaseTestNetworkBalance(Network network, double targetQ, double tol) {

        Load load4 = network.getLoad("lod_4_1");
        assertNotNull(load4);
        Load load7 = network.getLoad("lod_7_1");
        assertNotNull(load7);
        Generator generator2 = network.getGenerator("sym_2_1");
        assertNotNull(generator2);

        Line line45 = network.getLine("lne_4_5_1");
        assertNotNull(line45);
        TwoWindingsTransformer t2wt41 = network.getTwoWindingsTransformer("trf_4_1_1");
        assertNotNull(t2wt41);
        ThreeWindingsTransformer t3wt427 = network.getThreeWindingsTransformer("tr3_4_2_7_1");
        assertNotNull(t2wt41);

        BranchData line45Data = new BranchData(line45, 0.0, false);
        BranchData t2wtData41 = new BranchData(t2wt41, 0.0, false, true);
        TwtData t3wtData427 = new TwtData(t3wt427, 0.0, false, true);

        // The case does not have the reactive of the generator. We set it manually
        generator2.setTargetQ(targetQ);

        assertEquals(0.0, t3wtData427.getComputedP(ThreeSides.ONE) + line45Data.getComputedP1() + t2wtData41.getComputedP1() + load4.getP0(), tol);
        assertEquals(0.0, t3wtData427.getComputedQ(ThreeSides.ONE) + line45Data.getComputedQ1() + t2wtData41.getComputedQ1() + load4.getQ0(), tol);
        assertEquals(0.0, t3wtData427.getComputedP(ThreeSides.TWO) - generator2.getTargetP(), tol);
        assertEquals(0.0, t3wtData427.getComputedQ(ThreeSides.TWO) - generator2.getTargetQ(), tol);
        assertEquals(0.0, t3wtData427.getComputedP(ThreeSides.THREE) + load7.getP0(), tol);
        assertEquals(0.0, t3wtData427.getComputedQ(ThreeSides.THREE) + load7.getQ0(), tol);
    }

    @Test
    void threeMibPhaseWinding1() {
        assertTrue(threeWindingPhaseImportCompareXmlAndNetworkBalance("ThreeMIB_T3W_phase_winding1", 514.75293551, 0.09));
    }

    @Test
    void threeMibPhaseWinding1Ratio() {
        assertTrue(threeWindingPhaseImportCompareXmlAndNetworkBalance("ThreeMIB_T3W_phase_winding1_ratio", 591.898015, 0.09));
    }

    @Test
    void threeMibPhaseWinding2() {
        assertTrue(threeWindingPhaseImportCompareXmlAndNetworkBalance("ThreeMIB_T3W_phase_winding2", 658.367984, 0.09));
    }

    @Test
    void threeMibPhaseWinding3() {
        assertTrue(threeWindingPhaseImportCompareXmlAndNetworkBalance("ThreeMIB_T3W_phase_winding3", 596.52371, 0.09));
    }

    @Test
    void threeMibPhaseWinding12() {
        assertTrue(threeWindingPhaseImportCompareXmlAndNetworkBalance("ThreeMIB_T3W_phase_winding12", 575.835158, 0.09));
    }

    private boolean threeWindingPhaseImportCompareXmlAndNetworkBalance(String caseFile, double targetQ, double tol) {
        Network network = importAndCompareXml(caseFile);
        threeMibPhaseWindingTestNetworkBalance(network, targetQ, tol);
        return true;
    }

    /**
     * Only the balance at the three buses of the three windings transformer is done:
     * Bus 4 (500 kV): Load "lod_4_1", line "lne_4_1_1", twoWindingsTransformer "trf_4_1_1" and threeWindingsTransformer "trf_4_2_7_1" are connected
     * Bus 2 (18 kV) : Generator "sym_1_2_1", twoWindingsTransformer "trf_6_2_1" and  threeWindingsTransformer "trf_4_2_7_1" are connected
     * Bus 7 (16 kV) : Load "lod_7_1", twoWindingsTransformer "trf_5_7_1" and threeWindingsTransformer "trf_4_2_7_1" are connected
     */
    private static void threeMibPhaseWindingTestNetworkBalance(Network network, double targetQ, double tol) {

        Load load4 = network.getLoad("lod_4_1");
        assertNotNull(load4);
        Load load7 = network.getLoad("lod_7_1");
        assertNotNull(load7);
        Generator generator2 = network.getGenerator("sym_2_1");
        assertNotNull(generator2);

        Line line45 = network.getLine("lne_4_5_1");
        assertNotNull(line45);
        TwoWindingsTransformer t2wt41 = network.getTwoWindingsTransformer("trf_4_1_1");
        assertNotNull(t2wt41);
        TwoWindingsTransformer t2wt62 = network.getTwoWindingsTransformer("trf_6_2_1");
        assertNotNull(t2wt62);
        TwoWindingsTransformer t2wt57 = network.getTwoWindingsTransformer("trf_5_7_1");
        assertNotNull(t2wt57);
        ThreeWindingsTransformer t3wt427 = network.getThreeWindingsTransformer("tr3_4_2_7_1");
        assertNotNull(t2wt41);

        BranchData line45Data = new BranchData(line45, 0.0, false);
        BranchData t2wtData41 = new BranchData(t2wt41, 0.0, false, true);
        BranchData t2wtData62 = new BranchData(t2wt62, 0.0, false, true);
        BranchData t2wtData57 = new BranchData(t2wt57, 0.0, false, true);
        TwtData t3wtData427 = new TwtData(t3wt427, 0.0, false, true);

        // The case does not have the reactive of the generator. We set it manually
        generator2.setTargetQ(targetQ);

        assertEquals(0.0, t3wtData427.getComputedP(ThreeSides.ONE) + line45Data.getComputedP1() + t2wtData41.getComputedP1() + load4.getP0(), tol);
        assertEquals(0.0, t3wtData427.getComputedQ(ThreeSides.ONE) + line45Data.getComputedQ1() + t2wtData41.getComputedQ1() + load4.getQ0(), tol);
        assertEquals(0.0, t3wtData427.getComputedP(ThreeSides.TWO) + t2wtData62.getComputedP2() - generator2.getTargetP(), tol);
        assertEquals(0.0, t3wtData427.getComputedQ(ThreeSides.TWO) + t2wtData62.getComputedQ2() - generator2.getTargetQ(), tol);
        assertEquals(0.0, t3wtData427.getComputedP(ThreeSides.THREE) + t2wtData57.getComputedP2() + load7.getP0(), tol);
        assertEquals(0.0, t3wtData427.getComputedQ(ThreeSides.THREE) + t2wtData57.getComputedQ2() + load7.getQ0(), tol);
    }

}
