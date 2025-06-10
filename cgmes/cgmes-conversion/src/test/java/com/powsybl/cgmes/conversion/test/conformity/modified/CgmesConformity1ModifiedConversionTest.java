/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.conformity.modified;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.PhaseTapChanger.RegulationMode.CURRENT_LIMITER;
import static com.powsybl.iidm.network.StaticVarCompensator.RegulationMode.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesConformity1ModifiedConversionTest {

    private Properties importParams;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void microBEUnmergedXnode() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEUnmergedXnode().dataSource(),
                        NetworkFactory.findDefault(), importParams);
        DanglingLine dl = network.getDanglingLine("a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4");
        assertNotNull(dl);
        DanglingLine test = network.getDanglingLine("test");
        assertNotNull(test);
    }

    @Test
    void microBEExplicitBase() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEExplicitBase().dataSource(), NetworkFactory.findDefault(), importParams);
        assertNotNull(network);
    }

    @Test
    void microBEDuplicateRegion() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseDuplicateRegion().dataSource(), NetworkFactory.findDefault(), importParams);
        assertNotNull(network);
    }

    @Test
    void microBERatioPhaseTabularTest() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERatioPhaseTapChangerTabular().dataSource(), NetworkFactory.findDefault(), importParams);
        RatioTapChanger rtc = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543")
                .getRatioTapChanger();
        assertEquals(6, rtc.getStepCount());
        // ratio is missing for step 3
        // ratio is defined explicitly as 1.0 for step 4
        // r not defined in step 5
        // x not defined in step 6
        assertEquals(1.0, rtc.getStep(3).getRho(), 0);
        assertEquals(1.0, rtc.getStep(4).getRho(), 0);
        assertEquals(0.0, rtc.getStep(5).getR(), 0);
        assertEquals(0.0, rtc.getStep(6).getX(), 0);

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                .getPhaseTapChanger();
        // r,x not defined for step 1
        // ratio not defined for any step
        assertEquals(4, ptc.getStepCount());
        assertEquals(0.0, ptc.getStep(1).getR(), 0);
        assertEquals(0.0, ptc.getStep(1).getX(), 0);
        for (int k = 1; k <= 4; k++) {
            assertEquals(1.0, ptc.getStep(k).getRho(), 0);
        }
    }

    @Test
    void microBERatioPhaseFaultyTabularTest() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERatioPhaseTapChangerFaultyTabular().dataSource(), NetworkFactory.findDefault(), importParams);
        RatioTapChanger rtc = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543")
                .getRatioTapChanger();
        int neutralStep = 4;
        double stepVoltageIncrement = 1.250000;
        // table with steps 1, 2, 3, 8 ignored, rtc considered linear with 6 steps
        assertEquals(6, rtc.getStepCount());
        for (int k = 1; k <= 6; k++) {
            assertEquals(0.0, rtc.getStep(k).getR(), 0.0);
            assertEquals(0.0, rtc.getStep(k).getX(), 0.0);
            assertEquals(0.0, rtc.getStep(k).getG(), 0.0);
            assertEquals(0.0, rtc.getStep(k).getB(), 0.0);
            assertEquals(1 / (1.0 + (k - neutralStep) * (stepVoltageIncrement / 100.0)), rtc.getStep(k).getRho(), 0.0);
        }
        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                .getPhaseTapChanger();
        // table with step 1 and 4 ignored, ptc considered linear (with no step increment) with 5 steps
        assertEquals(5, ptc.getStepCount());
        for (int k = 1; k <= 5; k++) {
            assertEquals(0.0, ptc.getStep(k).getR(), 0.0);
            assertEquals(0.0, ptc.getStep(k).getX(), 0.0);
            assertEquals(0.0, ptc.getStep(k).getG(), 0.0);
            assertEquals(0.0, ptc.getStep(k).getB(), 0.0);
            assertEquals(1.0, ptc.getStep(k).getRho(), 0.0);
            assertEquals(0.0, ptc.getStep(k).getAlpha(), 0.0);
        }
    }

    @Test
    void microBEPhaseTapChangerLinearTest() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microT4BePhaseTapChangerLinear(),
            config);

        PhaseTapChanger ptc = n.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertEquals(25, ptc.getStepCount());

        for (int step = 1; step <= ptc.getStepCount(); step++) {
            assertEquals(1.0, ptc.getStep(step).getRho(), 0);
            assertEquals(0.0, ptc.getStep(step).getR(), 0);
            assertEquals(-87.517240, ptc.getStep(1).getX(), 0.000001);
            assertEquals(0.0, ptc.getStep(step).getG(), 0);
            assertEquals(0.0, ptc.getStep(step).getB(), 0);
        }

        // Check alpha in some steps
        assertEquals(14.4, ptc.getStep(1).getAlpha(), 0.001);
        assertEquals(8.4, ptc.getStep(6).getAlpha(), 0.001);
        assertEquals(2.4, ptc.getStep(11).getAlpha(), 0.001);
        assertEquals(0.0, ptc.getStep(13).getAlpha(), 0.0);
        assertEquals(-1.2, ptc.getStep(14).getAlpha(), 0.001);
        assertEquals(-7.2, ptc.getStep(19).getAlpha(), 0.001);
        assertEquals(-14.4, ptc.getStep(25).getAlpha(), 0.001);
    }

    private static Network networkModel(GridModelReference testGridModel, Conversion.Config config) {
        config.setConvertSvInjections(true);
        return ConversionUtil.networkModel(testGridModel, config);
    }

    @Test
    void microBEPtcSide2() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEPtcSide2().dataSource(), NetworkFactory.findDefault(), importParams);
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        assertNotNull(ptc);
        assertSame(twt.getTerminal2(), ptc.getRegulationTerminal());
    }

    @Test
    void microBEUsingSshForRtcPtcDisabled() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERtcPtcDisabled().dataSource(), NetworkFactory.findDefault(), importParams);

        // Even if the tap changers keep their controlEnabled flag == true,
        // Their associated regulating control (tap changer control) is disabled
        // So in IIDM the tap changers should not be regulating

        RatioTapChanger rtc = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957").getRatioTapChanger();
        assertNotNull(rtc);
        assertFalse(rtc.isRegulating());

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertNotNull(ptc);
        assertFalse(ptc.isRegulating());
    }

    @Test
    void microBEReactiveCapabilityCurve() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEReactiveCapabilityCurve().dataSource(), NetworkFactory.findDefault(), importParams);
        ReactiveLimits rl = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getReactiveLimits();
        assertEquals(ReactiveLimitsKind.CURVE, rl.getKind());
        ReactiveCapabilityCurve rcc = (ReactiveCapabilityCurve) rl;
        assertEquals(4, rcc.getPointCount());
        assertEquals(-20, rl.getMinQ(-200), 0.001);
        assertEquals(-20, rl.getMinQ(-201), 0.001);
        assertEquals(-20 - (180.0 / 100.0), rl.getMinQ(-199), 0.001);
    }

    @Test
    void microBEReactiveCapabilityCurveOnePoint() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEReactiveCapabilityCurveOnePoint().dataSource(), NetworkFactory.findDefault(), importParams);
        ReactiveLimits rl = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getReactiveLimits();
        assertEquals(ReactiveLimitsKind.MIN_MAX, rl.getKind());
        MinMaxReactiveLimits mm = (MinMaxReactiveLimits) rl;
        assertEquals(-200, mm.getMinQ(), 0);
        assertEquals(200, mm.getMaxQ(), 0);
    }

    @Test
    void microBEPtcCurrentLimiter() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEPtcCurrentLimiter().dataSource(), NetworkFactory.findDefault(), importParams);

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertNotNull(ptc);
        assertEquals(CURRENT_LIMITER, ptc.getRegulationMode());
    }

    @Test
    void microBEInvalidRegulatingControl() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEInvalidRegulatingControl().dataSource(), NetworkFactory.findDefault(), importParams);

        Generator generator1 = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        assertFalse(generator1.isVoltageRegulatorOn());
        assertTrue(Double.isNaN(generator1.getTargetV()));
        assertSame(generator1.getTerminal(), generator1.getRegulatingTerminal());

        RatioTapChanger rtc = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957").getRatioTapChanger();
        assertNotNull(rtc);
        assertTrue(rtc.hasLoadTapChangingCapabilities());
        assertTrue(Double.isNaN(rtc.getTargetV()));
        assertFalse(rtc.isRegulating());
        assertNull(rtc.getRegulationTerminal());

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertNotNull(ptc);
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, ptc.getRegulationMode());
        assertTrue(Double.isNaN(ptc.getRegulationValue()));
        assertFalse(ptc.isRegulating());
        assertNull(ptc.getRegulationTerminal());

        Generator generator2 = network.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa");
        assertEquals(generator2.getTerminal().getVoltageLevel().getNominalV(), generator2.getTargetV(), 0.0);
    }

    @Test
    void microBEMissingRegulatingControl() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEMissingRegulatingControl().dataSource(), NetworkFactory.findDefault(), importParams);

        Generator generator = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        assertFalse(generator.isVoltageRegulatorOn());
        assertTrue(Double.isNaN(generator.getTargetV()));

        RatioTapChanger rtc = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543").getRatioTapChanger();
        assertNotNull(rtc);
        assertTrue(rtc.hasLoadTapChangingCapabilities());
        assertTrue(Double.isNaN(rtc.getTargetV()));
        assertFalse(rtc.isRegulating());
        assertNull(rtc.getRegulationTerminal());

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertNotNull(ptc);
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, ptc.getRegulationMode());
        assertTrue(Double.isNaN(ptc.getRegulationValue()));
        assertFalse(ptc.isRegulating());
        assertNull(ptc.getRegulationTerminal());
    }

    @Test
    void microBESvInjection() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithSvInjection().dataSource(),
                        NetworkFactory.findDefault(), importParams);

        Load load = network.getLoad("SvInjection1");
        assertNotNull(load);
        assertEquals(-0.2, load.getP0(), 0.0);
        assertEquals(-13.8, load.getQ0(), 0.0);

        Load load2 = network.getLoad("SvInjection2");
        assertNotNull(load2);
        assertEquals(-0.2, load2.getP0(), 0.0);
        assertEquals(0.0, load2.getQ0(), 0.0);

        Load load3 = network.getLoad("SvInjection3");
        assertNotNull(load3);
        assertEquals(-0.2, load3.getP0(), 0.0);
        assertEquals(-13.8, load3.getQ0(), 0.0);
    }

    @Test
    void microBETieFlow() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlow().dataSource(),
            NetworkFactory.findDefault(), importParams);

        // Check that the query discarded the areas that aren't of type Interchange
        assertEquals(1, network.getAreaCount());

        Area area = network.getArea("BECONTROLAREA");
        assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area.getAreaType());
        assertEquals("BE", area.getNameOrId());
        assertEquals("10BE------1", area.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).get());
        assertEquals(-205.90011555672567, area.getInterchangeTarget().getAsDouble(), 0.0);
        assertEquals(5, area.getAreaBoundaryStream().count());
    }

    @Test
    void microBEInvalidSvInjection() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEInvalidSvInjection().dataSource(),
                        NetworkFactory.findDefault(), importParams);

        Load load = network.getLoad("SvInjection1");
        assertNull(load);
    }

    @Test
    void microBEEquivalentShunt() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource(),
                NetworkFactory.findDefault(), importParams);

        ShuntCompensator shunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertNotNull(shunt);
        assertEquals(1, shunt.getMaximumSectionCount());
        assertEquals(0.0012, shunt.getModel(ShuntCompensatorLinearModel.class).getBPerSection(), 0.0);
        assertEquals(1, shunt.getSectionCount());
    }

    @Test
    void microBEMissingShuntRegulatingControlId() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog
                        .microGridBaseCaseBEMissingShuntRegulatingControlId().dataSource(), NetworkFactory.findDefault(), importParams);
        ShuntCompensator shunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertTrue(shunt.isVoltageRegulatorOn());
        assertEquals(shunt.getTerminal().getBusView().getBus().getV(), shunt.getTargetV(), 0.0d);
        assertEquals(0.0d, shunt.getTargetDeadband(), 0.0d);
        assertEquals(shunt.getTerminal(), shunt.getRegulatingTerminal());
    }

    @Test
    void microBEEquivalentInjectionRegulatingVoltage() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentInjectionRegulatingVoltage().dataSource(),
                NetworkFactory.findDefault(), importParams);

        DanglingLine danglingLineRegulating = network.getDanglingLine("a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4");
        assertNotNull(danglingLineRegulating);
        assertTrue(danglingLineRegulating.getGeneration().isVoltageRegulationOn());
        assertEquals(220.1234, danglingLineRegulating.getGeneration().getTargetV(), 0.0);

        DanglingLine danglingLineNotRegulating = network.getDanglingLine("17086487-56ba-4979-b8de-064025a6b4da");
        assertNotNull(danglingLineNotRegulating);
        assertEquals(-27.365225, danglingLineNotRegulating.getP0(), 0.0);
        assertEquals(0.425626, danglingLineNotRegulating.getQ0(), 0.0);
    }

    @Test
    void microBEConformNonConformLoads() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEConformNonConformLoads().dataSource(),
                NetworkFactory.findDefault(), importParams);
        Load conformLoad = network.getLoad("cb459405-cc14-4215-a45c-416789205904");
        Load nonConformLoad = network.getLoad("1c6beed6-1acf-42e7-ba55-0cc9f04bddd8");
        LoadDetail conformDetails = conformLoad.getExtension(LoadDetail.class);
        assertNotNull(conformDetails);
        assertEquals(0.0, conformDetails.getFixedActivePower(), 0.0);
        assertEquals(0.0, conformDetails.getFixedReactivePower(), 0.0);
        assertEquals(200.0, conformDetails.getVariableActivePower(), 0.0);
        assertEquals(90.0, conformDetails.getVariableReactivePower(), 0.0);
        LoadDetail nonConformDetails = nonConformLoad.getExtension(LoadDetail.class);
        assertNotNull(nonConformDetails);
        assertEquals(200.0, nonConformDetails.getFixedActivePower(), 0.0);
        assertEquals(50.0, nonConformDetails.getFixedReactivePower(), 0.0);
        assertEquals(0.0, nonConformDetails.getVariableActivePower(), 0.0);
        assertEquals(0.0, nonConformDetails.getVariableReactivePower(), 0.0);
    }

    @Test
    void microBESwitchAtBoundary() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBESwitchAtBoundary().dataSource(),
                NetworkFactory.findDefault(), importParams);
        DanglingLine dl = network.getDanglingLine("78736387-5f60-4832-b3fe-d50daf81b0a6");
        assertEquals(0.0, dl.getR(), 0.0);
        assertEquals(0.0, dl.getX(), 0.0);
        assertEquals(0.0, dl.getG(), 0.0);
        assertEquals(0.0, dl.getB(), 0.0);
    }

    @Test
    void microBETransformerAtBoundary() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBETransformerAtBoundary().dataSource(),
                NetworkFactory.findDefault(), importParams);
        DanglingLine dl = network.getDanglingLine("17086487-56ba-4979-b8de-064025a6b4da");

        assertEquals(2.588687265185185, dl.getR(), 0.0);
        assertEquals(13.880789206913578, dl.getX(), 0.0);
        assertEquals(0.0, dl.getG(), 0.0);
        assertEquals(0.0, dl.getB(), 0.0);
    }

    @Test
    void microBEEquivalentBranchAtBoundary() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentBranchAtBoundary().dataSource(),
            NetworkFactory.findDefault(), importParams);
        DanglingLine dl = network.getDanglingLine("78736387-5f60-4832-b3fe-d50daf81b0a6");
        assertEquals(1.0, dl.getR(), 0.0);
        assertEquals(10.0, dl.getX(), 0.0);
    }

    @Test
    void microBEEquivalentBranch() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentBranch().dataSource(),
            NetworkFactory.findDefault(), importParams);
        Line l = network.getLine("b58bf21a-096a-4dae-9a01-3f03b60c24c7");
        assertEquals(1.935000, l.getR(), 0.0);
        assertEquals(34.200000, l.getX(), 0.0);
        assertEquals(0.0, l.getG1(), 0.0);
        assertEquals(0.0, l.getB1(), 0.0);
        assertEquals(0.0, l.getG2(), 0.0);
        assertEquals(0.0, l.getB2(), 0.0);
    }

    @Test
    void microBEFixedMinPMaxP() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseBEFixedMinPMaxP().dataSource(),
                NetworkFactory.findDefault(), importParams);
        Generator generator = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        assertEquals(50.0, generator.getMinP(), 0.0);
        assertEquals(200.0, generator.getMaxP(), 0.0);
        assertFalse(generator.isCondenser());
    }

    @Test
    void microBEIncorrectDate() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEIncorrectDate().dataSource(),
                NetworkFactory.findDefault(), importParams);
        assertEquals(0, network.getForecastDistance());
        assertTrue(Duration.between(ZonedDateTime.now(), network.getCaseDate()).toMinutes() < 10);
        CgmesMetadataModels cgmesMetadata = network.getExtension(CgmesMetadataModels.class);
        assertNotNull(cgmesMetadata);
        Optional<CgmesMetadataModel> svModel = cgmesMetadata.getModelForSubset(CgmesSubset.STATE_VARIABLES);
        assertTrue(svModel.isPresent());
        assertEquals(1, svModel.get().getVersion());
    }

    @Test
    void microBEReactivePowerGen() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEReactivePowerGen().dataSource(), NetworkFactory.findDefault(), importParams);
        Generator g = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        RemoteReactivePowerControl ext = g.getExtension(RemoteReactivePowerControl.class);
        assertNotNull(ext);
        assertEquals(115.5, ext.getTargetQ(), 0.0);
        assertTrue(ext.isEnabled());
        assertSame(network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getTerminal2(), ext.getRegulatingTerminal());
    }

    @Test
    void microAssembledSwitchAtBoundary() {
        final double tolerance = 1e-10;

        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSwitchAtBoundary().dataSource(),
                NetworkFactory.findDefault(), importParams);
        TieLine m = network.getTieLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");

        assertEquals(1.02, m.getR(), tolerance);
        assertEquals(12.0, m.getX(), tolerance);
        assertEquals(0.0, m.getG1(), tolerance);
        assertEquals(0.0000299999, m.getG2(), tolerance);
        assertEquals(0.0, m.getB1(), tolerance);
        assertEquals(0.0001413717, m.getB2(), tolerance);
    }

    @Test
    void microAssembledTransformerAtBoundary() {
        final double tolerance = 1e-10;

        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledTransformerAtBoundary().dataSource(),
                NetworkFactory.findDefault(), importParams);
        TieLine m = network.getTieLine("17086487-56ba-4979-b8de-064025a6b4da + 8fdc7abd-3746-481a-a65e-3df56acd8b13");

        assertEquals(4.788687265185187, m.getR(), tolerance);
        assertEquals(79.88078920691358, m.getX(), tolerance);
        assertEquals(0.0, m.getG1(), tolerance);
        assertEquals(0.0, m.getB1(), tolerance);
        assertEquals(0.0000242, m.getG2(), tolerance);
        assertEquals(0.0000898495, m.getB2(), tolerance);
    }

    @Test
    void microAssembledThreeLinesAtBoundary() {
        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledThreeLinesAtBoundary().dataSource(),
                NetworkFactory.findDefault(), importParams);
        TieLine line = network.getTieLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");
        assertNotNull(line);
    }

    @Test
    void microAssembledEquivalentBranchAtBoundary() {
        final double tolerance = 1e-10;

        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledEquivalentBranchAtBoundary().dataSource(),
                NetworkFactory.findDefault(), importParams);
        TieLine m = network.getTieLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");
        assertEquals(2.02, m.getR(), tolerance);
        assertEquals(22.0, m.getX(), tolerance);
        assertEquals(0.0, m.getG1(), tolerance);
        assertEquals(0.000029999999999998778, m.getG2(), tolerance);
        assertEquals(0.0, m.getB1(), tolerance);
        assertEquals(0.00014137169999998977, m.getB2(), tolerance);
    }

    @Test
    void microT4InvalidSvcMode() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbInvalidSvcMode().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator offSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(offSvc);
        assertEquals(OFF, offSvc.getRegulationMode());
    }

    @Test
    void microT4ReactivePowerSvc() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());
        assertEquals(229.5, svc.getVoltageSetpoint(), 0.0);
        assertTrue(Double.isNaN(svc.getReactivePowerSetpoint()));

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbReactivePowerSvc().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator reactivePowerSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(reactivePowerSvc);
        assertEquals(REACTIVE_POWER, reactivePowerSvc.getRegulationMode());
        assertEquals(229.5, reactivePowerSvc.getReactivePowerSetpoint(), 0.0);
        assertTrue(Double.isNaN(reactivePowerSvc.getVoltageSetpoint()));
    }

    @Test
    void microT4OffSvc() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());

        Network modified1 = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbOffSvc().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator off1 = modified1.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(off1);
        assertEquals(OFF, off1.getRegulationMode());

        Network modified2 = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbOffSvcControl().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator off2 = modified2.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(off2);
        assertEquals(REACTIVE_POWER, off2.getRegulationMode());
        assertEquals(0.0d, off2.getReactivePowerSetpoint(), 0.0d);

        Network modified3 = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbOffSvcControlV().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator off3 = modified3.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(off3);
        assertEquals(OFF, off3.getRegulationMode());
        assertEquals(231.123, off3.getVoltageSetpoint(), 0.0);
    }

    @Test
    void microT4SvcWithoutRegulatingControl() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());
        assertEquals(229.5, svc.getVoltageSetpoint(), 0.0);

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbSvcNoRegulatingControl().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator modifiedSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(modifiedSvc);
        assertEquals(VOLTAGE, modifiedSvc.getRegulationMode());
        assertEquals(159.5, modifiedSvc.getVoltageSetpoint(), 0.0);
    }

    @Test
    void microT4ReactivePowerSvcWithMissingRegulatingControl() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());
        assertEquals(229.5, svc.getVoltageSetpoint(), 0.0);

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbMissingRegControlReactivePowerSvc().dataSource(), NetworkFactory.findDefault(), importParams);
        StaticVarCompensator modifiedSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(modifiedSvc);
        assertEquals(REACTIVE_POWER, modifiedSvc.getRegulationMode());
        assertEquals(0.0, modifiedSvc.getReactivePowerSetpoint(), 0.0);
    }

    @Test
    void miniBusBranchRtcRemoteRegulation() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniBusBranchRtcRemoteRegulation().dataSource(), NetworkFactory.findDefault(), importParams);

        TwoWindingsTransformer twt2 = network.getTwoWindingsTransformer("813365c3-5be7-4ef0-a0a7-abd1ae6dc174");
        RatioTapChanger rtc = twt2.getRatioTapChanger();
        assertNotNull(rtc);
        Terminal regulatingTerminal = rtc.getRegulationTerminal();
        assertNotNull(regulatingTerminal);
        assertSame(twt2.getTerminal1().getBusBreakerView().getBus(), regulatingTerminal.getBusBreakerView().getBus());

        ThreeWindingsTransformer twt3 = network.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773");
        RatioTapChanger rtc2 = twt3.getLeg3().getRatioTapChanger();
        assertNotNull(rtc2);
        Terminal regulatingTerminal2 = rtc2.getRegulationTerminal();
        assertNotNull(regulatingTerminal2);
        assertSame(network.getVoltageLevel("93778e52-3fd5-456d-8b10-987c3e6bc47e").getBusBreakerView().getBus("03163ede-7eec-457f-8641-365982227d7c"),
                regulatingTerminal2.getBusBreakerView().getBus());
    }

    @Test
    void miniBusBranchT3xTwoRegulatingControlsEnabled() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniBusBranchT3xTwoRegulatingControlsEnabled().dataSource(), NetworkFactory.findDefault(), importParams);

        ThreeWindingsTransformer twt3 = network.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773");
        RatioTapChanger rtc2 = twt3.getLeg2().getRatioTapChanger();
        assertNotNull(rtc2);
        Terminal regulatingTerminal2 = rtc2.getRegulationTerminal();
        assertNotNull(regulatingTerminal2);
        assertTrue(rtc2.isRegulating());

        RatioTapChanger rtc3 = twt3.getLeg3().getRatioTapChanger();
        assertNotNull(rtc3);
        Terminal regulatingTerminal3 = rtc3.getRegulationTerminal();
        assertNotNull(regulatingTerminal3);
        assertFalse(rtc3.isRegulating());
    }

    @Test
    void miniBusBranchExternalInjectionControl() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniBusBranchExternalInjectionControl().dataSource(), NetworkFactory.findDefault(), importParams);
        // External network injections with shared control enabled
        // One external network injection has control enabled
        // The other one has it disabled
        assertFalse(network.getGenerator("089c1945-4101-487f-a557-66c013b748f6").isVoltageRegulatorOn());
        assertTrue(network.getGenerator("3de9e1ad-4562-44df-b268-70ed0517e9e7").isVoltageRegulatorOn());
        assertEquals(10.0, network.getGenerator("089c1945-4101-487f-a557-66c013b748f6").getTargetV(), 1e-10);
        // Even if the control is disabled, the target voltage must be set
        assertEquals(10.0, network.getGenerator("3de9e1ad-4562-44df-b268-70ed0517e9e7").getTargetV(), 1e-10);
    }

    @Test
    void miniNodeBreakerInvalidT2w() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        platformConfig.createModuleConfig("import-export-parameters-default-value")
                .setStringProperty("iidm.import.cgmes.convert-boundary", "true");

        Network network = new CgmesImport(platformConfig).importData(CgmesConformity1Catalog.miniNodeBreaker().dataSource(),
                NetworkFactory.findDefault(), importParams);
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        assertNotNull(transformer);

        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.miniNodeBreakerInvalidT2w().dataSource();
        CgmesImport importer = new CgmesImport(platformConfig);
        NetworkFactory networkFactory = NetworkFactory.findDefault();
        PowsyblException e = assertThrows(PowsyblException.class, () -> importer.importData(ds, networkFactory, importParams));
        assertEquals("2 windings transformer 'ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51': the 2 windings of the transformer shall belong to the substation '183d126d-2522-4ff2-a8cd-c5016cf09c1b_S' ('183d126d-2522-4ff2-a8cd-c5016cf09c1b_S', 'd6056127-34f1-43a9-b029-23fddb913bd5')",
            e.getMessage());
    }

    @Test
    void miniNodeBreakerSvInjection() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerSvInjection().dataSource(),
                        NetworkFactory.findDefault(), importParams);

        Load load = network.getLoad("SvInjection");
        assertNotNull(load);
        assertEquals(-0.2, load.getP0(), 0.0);
        assertEquals(-13.8, load.getQ0(), 0.0);
    }

    @Test
    void miniNodeBreakerMissingSubstationRegion() {
        // Check that we fail with a powsybl exception instead of a NPE
        CgmesImport importer = new CgmesImport();
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.miniNodeBreakerMissingSubstationRegion().dataSource();
        NetworkFactory networkFactory = NetworkFactory.findDefault();
        CgmesModelException exception = assertThrows(CgmesModelException.class, () -> importer.importData(dataSource, networkFactory, importParams));
        assertEquals("BusbarSection 78f2ae0e-da25-483a-8a71-76f709389894 voltage level 347fb7af-642f-4c60-97d9-c03d440b6a82 has not been created in IIDM",
                exception.getMessage());
    }

    @Test
    void smallBusBranchTieFlowWithoutControlArea() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallBusBranchTieFlowsWithoutControlArea().dataSource(), NetworkFactory.findDefault(), importParams);
        assertEquals(0, network.getAreaCount());
    }

    @Test
    void microGridBaseCaseAssembledEntsoeCategory() {
        importParams.put(CgmesImport.POST_PROCESSORS, "EntsoeCategory");
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledEntsoeCategory().dataSource(), importParams);
        assertEquals(31, network.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa").getExtension(GeneratorEntsoeCategory.class).getCode());
        assertEquals(42, network.getGenerator("9c3b8f97-7972-477d-9dc8-87365cc0ad0e").getExtension(GeneratorEntsoeCategory.class).getCode());
        assertNull(network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getExtension(GeneratorEntsoeCategory.class));
        assertNull(network.getGenerator("1dc9afba-23b5-41a0-8540-b479ed8baf4b").getExtension(GeneratorEntsoeCategory.class));
        assertNull(network.getGenerator("2844585c-0d35-488d-a449-685bcd57afbf").getExtension(GeneratorEntsoeCategory.class));
    }

    @Test
    void microGridBaseCaseNLMultipleReferencePriorities() {
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseCaseNLMultipleReferencePriorities().dataSource(), importParams);
        List<ReferencePriority> referencePriorities = ReferencePriorities.get(network);
        assertEquals(2, referencePriorities.size());
        assertEquals("9c3b8f97-7972-477d-9dc8-87365cc0ad0e", referencePriorities.get(0).getTerminal().getConnectable().getId());
        assertEquals("9c3b8f97-7972-477d-9dc8-87365cc0ad0e-bis", referencePriorities.get(1).getTerminal().getConnectable().getId());
    }

    @Test
    void microGridBaseCaseNLShuntCompensatorGP() {
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseCaseNLShuntCompensatorGP().dataSource(), importParams);
        assertEquals(0.0000123, network.getShuntCompensator("fbfed7e3-3dec-4829-a286-029e73535685").getG(), 0.0);
        assertEquals(0.123, network.getShuntCompensator("fbfed7e3-3dec-4829-a286-029e73535685").getTerminal().getP(), 0.0);
    }

    @Test
    void microGridBaseCaseBESingleFile() {
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseCaseBESingleFile().dataSource(), importParams);
        assertEquals(6, network.getExtension(CgmesModelExtension.class).getCgmesModel().boundaryNodes().size());
        assertEquals(5, network.getDanglingLineCount());
    }

    @Test
    void smallNodeBreakerHvdcNoSequenceNumbers() {
        Network networkSeq = Importers.importData("CGMES", CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), importParams);
        Network networkNoSeq = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcNoSequenceNumbers().dataSource(), importParams);
        // Make sure we have not lost any line, switch
        assertEquals(networkSeq.getLineCount(), networkNoSeq.getLineCount());
        assertEquals(networkSeq.getSwitchCount(), networkNoSeq.getSwitchCount());
        assertEquals(networkSeq.getHvdcLineCount(), networkNoSeq.getHvdcLineCount());

        // Check terminal ids have been sorted properly
        CgmesModel cgmesSeq = networkSeq.getExtension(CgmesModelExtension.class).getCgmesModel();
        CgmesModel cgmesNoSeq = networkNoSeq.getExtension(CgmesModelExtension.class).getCgmesModel();
        checkTerminals(cgmesSeq.acLineSegments(), cgmesNoSeq.acLineSegments(), "ACLineSegment", "Terminal1", "Terminal2");
        checkTerminals(cgmesSeq.switches(), cgmesNoSeq.switches(), "Switch", "Terminal1", "Terminal2");
        checkTerminals(cgmesSeq.seriesCompensators(), cgmesNoSeq.seriesCompensators(), "SeriesCompensator", "Terminal1", "Terminal2");
        checkTerminals(cgmesSeq.dcLineSegments(), cgmesNoSeq.dcLineSegments(), "DCLineSegment", "DCTerminal1", "DCTerminal2");
    }

    @Test
    void microGridBaseBEStationSupply() {
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseBEStationSupply().dataSource(), importParams);
        Load l = network.getLoad("b1480a00-b427-4001-a26c-51954d2bb7e9_station_supply");
        assertNotNull(l);
        assertEquals(6.5, l.getP0(), 1e-3);
        assertEquals(0.001, l.getQ0(), 1e-3);
        assertEquals(LoadType.AUXILIARY, l.getLoadType());
    }

    @Test
    void microGridBaseBETargetDeadbandNegative() {
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseBETargetDeadbandNegative().dataSource(), importParams);
        String transformerId;

        transformerId = "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0";
        PhaseTapChanger ptc = network.getTwoWindingsTransformer(transformerId).getPhaseTapChanger();
        assertTrue(Double.isNaN(ptc.getTargetDeadband()));
        assertFalse(ptc.isRegulating());

        transformerId = "b94318f6-6d24-4f56-96b9-df2531ad6543";
        RatioTapChanger rtc = network.getTwoWindingsTransformer(transformerId).getRatioTapChanger();
        assertTrue(Double.isNaN(rtc.getTargetDeadband()));
        assertFalse(rtc.isRegulating());
    }

    @Test
    void microGridBELineDisconnectedAtBoundaryNode() {
        String dlId = "17086487-56ba-4979-b8de-064025a6b4da";

        importParams.setProperty(CgmesImport.DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED, "true");
        Network be0 = Network.read(CgmesConformity1ModifiedCatalog.microGridBaseCaseBELineDisconnectedAtBoundaryNode().dataSource(), importParams);
        Bus bus0 = be0.getDanglingLine(dlId).getTerminal().getBusView().getBus();
        assertNull(bus0);

        importParams.setProperty(CgmesImport.DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED, "false");
        Network be1 = Network.read(CgmesConformity1ModifiedCatalog.microGridBaseCaseBELineDisconnectedAtBoundaryNode().dataSource(), importParams);
        Bus bus1 = be1.getDanglingLine(dlId).getTerminal().getBusView().getBus();
        assertNotNull(bus1);
    }

    private static void checkTerminals(PropertyBags eqSeq, PropertyBags eqNoSeq, String idPropertyName, String terminal1PropertyName, String terminal2PropertyName) {
        Map<String, String> eqsSeqTerminal1 = eqSeq.stream().collect(Collectors.toMap(acls -> acls.getId(idPropertyName), acls -> acls.getId(terminal1PropertyName)));
        Map<String, String> eqsSeqTerminal2 = eqSeq.stream().collect(Collectors.toMap(acls -> acls.getId(idPropertyName), acls -> acls.getId(terminal2PropertyName)));
        eqSeq.forEach(eq -> {
            assertEquals("1", eq.getLocal("seq1"));
            assertEquals("2", eq.getLocal("seq2"));
        });
        eqNoSeq.forEach(eq -> {
            assertNull(eq.getLocal("seq1"));
            assertNull(eq.getLocal("seq2"));
            String eqNoSeqTerminal1 = eq.getId(terminal1PropertyName);
            String eqNoSeqTerminal2 = eq.getId(terminal2PropertyName);

            String id = eq.getId(idPropertyName);
            String eqSeqTerminal1 = eqsSeqTerminal1.get(id);
            String eqSeqTerminal2 = eqsSeqTerminal2.get(id);

            if (eqSeqTerminal1.equals(eqNoSeqTerminal1)) {
                assertEquals(eqSeqTerminal2, eqNoSeqTerminal2);
                assertTrue(eqNoSeqTerminal1.compareTo(eqNoSeqTerminal2) < 0);
            } else {
                assertEquals(eqSeqTerminal1, eqNoSeqTerminal2);
                assertEquals(eqSeqTerminal2, eqNoSeqTerminal1);
                assertTrue(eqSeqTerminal1.compareTo(eqSeqTerminal2) > 0);
            }
        });
    }

    private FileSystem fileSystem;
}
