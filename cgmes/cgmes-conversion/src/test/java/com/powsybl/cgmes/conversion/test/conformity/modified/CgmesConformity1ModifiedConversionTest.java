/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.conformity.modified;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.cgmes.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import static com.powsybl.iidm.network.PhaseTapChanger.RegulationMode.CURRENT_LIMITER;
import static com.powsybl.iidm.network.StaticVarCompensator.RegulationMode.*;
import static org.junit.Assert.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1ModifiedConversionTest {

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void microBERatioPhaseTabularTest() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERatioPhaseTapChangerTabular().dataSource(), NetworkFactory.findDefault(), null);
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
    public void microBERatioPhaseFaultyTabularTest() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERatioPhaseTapChangerFaultyTabular().dataSource(), NetworkFactory.findDefault(), null);
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
    public void microBEPhaseTapChangerLinearTest() throws IOException {
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

    private static Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {

        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

        return n;
    }

    @Test
    public void microBEPtcSide2() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEPtcSide2().dataSource(), NetworkFactory.findDefault(), null);
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        assertNotNull(ptc);
        assertSame(twt.getTerminal2(), ptc.getRegulationTerminal());
    }

    @Test
    public void microBEUsingSshForRtcPtcDisabled() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERtcPtcDisabled().dataSource(), NetworkFactory.findDefault(), null);

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
    public void microBEReactiveCapabilityCurve() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEReactiveCapabilityCurve().dataSource(), NetworkFactory.findDefault(), null);
        ReactiveLimits rl = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getReactiveLimits();
        assertEquals(ReactiveLimitsKind.CURVE, rl.getKind());
        ReactiveCapabilityCurve rcc = (ReactiveCapabilityCurve) rl;
        assertEquals(4, rcc.getPointCount());
        assertEquals(-20, rl.getMinQ(-200), 0.001);
        assertEquals(-20, rl.getMinQ(-201), 0.001);
        assertEquals(-20 - (180.0 / 100.0), rl.getMinQ(-199), 0.001);
    }

    @Test
    public void microBEReactiveCapabilityCurveOnePoint() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEReactiveCapabilityCurveOnePoint().dataSource(), NetworkFactory.findDefault(), null);
        ReactiveLimits rl = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getReactiveLimits();
        assertEquals(ReactiveLimitsKind.MIN_MAX, rl.getKind());
        MinMaxReactiveLimits mm = (MinMaxReactiveLimits) rl;
        assertEquals(-200, mm.getMinQ(), 0);
        assertEquals(200, mm.getMaxQ(), 0);
    }

    @Test
    public void microBEPtcCurrentLimiter() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEPtcCurrentLimiter().dataSource(), NetworkFactory.findDefault(), null);

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertNotNull(ptc);
        assertEquals(CURRENT_LIMITER, ptc.getRegulationMode());
    }

    @Test
    public void microBEInvalidRegulatingControl() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEInvalidRegulatingControl().dataSource(), NetworkFactory.findDefault(), null);

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
    public void microBEMissingRegulatingControl() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEMissingRegulatingControl().dataSource(), NetworkFactory.findDefault(), null);

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
    public void microBESvInjection() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithSvInjection().dataSource(),
                        NetworkFactory.findDefault(), null);

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
    public void microBETieFlow() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlow().dataSource(),
            NetworkFactory.findDefault(), null);

        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        CgmesControlArea cgmesControlArea = cgmesControlAreas.getCgmesControlArea("BECONTROLAREA");
        assertEquals("BE", cgmesControlArea.getName());
        assertEquals("10BE------1", cgmesControlArea.getEnergyIdentificationCodeEIC());
        assertEquals(-205.90011555672567, cgmesControlArea.getNetInterchange(), 0.0);
        assertEquals(5, cgmesControlArea.getTerminals().size());
    }

    @Test
    public void microBEInvalidSvInjection() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEInvalidSvInjection().dataSource(),
                        NetworkFactory.findDefault(), null);

        Load load = network.getLoad("SvInjection1");
        assertNull(load);
    }

    @Test
    public void microBEEquivalentShunt() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource(),
                NetworkFactory.findDefault(), null);

        ShuntCompensator shunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertNotNull(shunt);
        assertEquals(1, shunt.getMaximumSectionCount());
        assertEquals(0.0012, shunt.getModel(ShuntCompensatorLinearModel.class).getBPerSection(), 0.0);
        assertEquals(1, shunt.getSectionCount());
    }

    @Test
    public void microBEMissingShuntRegulatingControlId() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog
                        .microGridBaseCaseBEMissingShuntRegulatingControlId().dataSource(), NetworkFactory.findDefault(), null);
        ShuntCompensator shunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertTrue(shunt.isVoltageRegulatorOn());
        assertEquals(shunt.getTerminal().getBusView().getBus().getV(), shunt.getTargetV(), 0.0d);
        assertEquals(0.0d, shunt.getTargetDeadband(), 0.0d);
        assertEquals(shunt.getTerminal(), shunt.getRegulatingTerminal());
    }

    @Test
    public void microBEUndefinedPatl() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEUndefinedPatl().dataSource(),
                NetworkFactory.findDefault(), null);
        Line line = network.getLine("ffbabc27-1ccd-4fdc-b037-e341706c8d29");
        CurrentLimits limits = line.getCurrentLimits1().orElse(null);
        assertNotNull(limits);
        assertEquals(2, limits.getTemporaryLimits().size());
        assertEquals(1312.0, limits.getPermanentLimit(), 0.0);
    }

    @Test
    public void microBEEquivalentInjectionRegulatingVoltage() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentInjectionRegulatingVoltage().dataSource(),
                NetworkFactory.findDefault(), null);

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
    public void microBEConformNonConformLoads() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEConformNonConformLoads().dataSource(),
                NetworkFactory.findDefault(), null);
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
    public void microBESwitchAtBoundary() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBESwitchAtBoundary().dataSource(),
                NetworkFactory.findDefault(), null);
        DanglingLine dl = network.getDanglingLine("78736387-5f60-4832-b3fe-d50daf81b0a6");
        assertEquals(0.0, dl.getR(), 0.0);
        assertEquals(0.0, dl.getX(), 0.0);
        assertEquals(0.0, dl.getG(), 0.0);
        assertEquals(0.0, dl.getB(), 0.0);
    }

    @Test
    public void microBETransformerAtBoundary() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBETransformerAtBoundary().dataSource(),
                NetworkFactory.findDefault(), null);
        DanglingLine dl = network.getDanglingLine("17086487-56ba-4979-b8de-064025a6b4da");

        assertEquals(2.588687265185185, dl.getR(), 0.0);
        assertEquals(13.880789206913578, dl.getX(), 0.0);
        assertEquals(0.0, dl.getG(), 0.0);
        assertEquals(0.0, dl.getB(), 0.0);
    }

    @Test
    public void microBEEquivalentBranchAtBoundary() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentBranchAtBoundary().dataSource(),
            NetworkFactory.findDefault(), null);
        DanglingLine dl = network.getDanglingLine("78736387-5f60-4832-b3fe-d50daf81b0a6");
        assertEquals(1.0, dl.getR(), 0.0);
        assertEquals(10.0, dl.getX(), 0.0);
    }

    @Test
    public void microBEEquivalentBranch() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentBranch().dataSource(),
            NetworkFactory.findDefault(), null);
        Line l = network.getLine("b58bf21a-096a-4dae-9a01-3f03b60c24c7");
        assertEquals(1.935000, l.getR(), 0.0);
        assertEquals(34.200000, l.getX(), 0.0);
        assertEquals(0.0, l.getG1(), 0.0);
        assertEquals(0.0, l.getB1(), 0.0);
        assertEquals(0.0, l.getG2(), 0.0);
        assertEquals(0.0, l.getB2(), 0.0);
    }

    @Test
    public void microBELimits() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBELimits().dataSource(),
                NetworkFactory.findDefault(), null);
        VoltageLevel vl = network.getVoltageLevel("469df5f7-058f-4451-a998-57a48e8a56fe");
        assertEquals(401.2, vl.getHighVoltageLimit(), 0.0);
        assertEquals(350.7, vl.getLowVoltageLimit(), 0.0);
        ThreeWindingsTransformer twt3 = network.getThreeWindingsTransformer("84ed55f4-61f5-4d9d-8755-bba7b877a246");
        assertTrue(twt3.getLeg1().getApparentPowerLimits().isEmpty());
        assertTrue(twt3.getLeg2().getApparentPowerLimits().isEmpty());
        assertTrue(twt3.getLeg3().getApparentPowerLimits().isEmpty());
        TwoWindingsTransformer twt2 = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543");
        ApparentPowerLimits apparentPowerLimits = twt2.getApparentPowerLimits1().orElse(null);
        assertNotNull(apparentPowerLimits);
        assertEquals(22863.1, apparentPowerLimits.getPermanentLimit(), 0.0);
        assertTrue(apparentPowerLimits.getTemporaryLimits().isEmpty());
    }

    @Test
    public void microBEFixedMinPMaxP() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseBEFixedMinPMaxP().dataSource(),
                NetworkFactory.findDefault(), null);
        Generator generator = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        assertEquals(50.0, generator.getMinP(), 0.0);
        assertEquals(200.0, generator.getMaxP(), 0.0);
    }

    @Test
    public void microBEIncorrectDate() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEIncorrectDate().dataSource(),
                NetworkFactory.findDefault(), null);
        assertEquals(0, network.getForecastDistance());
        assertTrue(new Duration(DateTime.now(), network.getCaseDate()).getStandardMinutes() < 10);
        CgmesSvMetadata cgmesSvMetadata = network.getExtension(CgmesSvMetadata.class);
        assertNotNull(cgmesSvMetadata);
        assertEquals(1, cgmesSvMetadata.getSvVersion());
    }

    @Test
    public void microBEMissingLimitValue() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEMissingLimitValue().dataSource(),
                NetworkFactory.findDefault(), null);
        DanglingLine line = network.getDanglingLine("17086487-56ba-4979-b8de-064025a6b4da");
        CurrentLimits limits = line.getCurrentLimits().orElse(null);
        assertNotNull(limits);
        assertNull(limits.getTemporaryLimit(10));
    }

    @Test
    public void microBEReactivePowerGen() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEReactivePowerGen().dataSource(), NetworkFactory.findDefault(), null);
        Generator g = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        RemoteReactivePowerControl ext = g.getExtension(RemoteReactivePowerControl.class);
        assertNotNull(ext);
        assertEquals(115.5, ext.getTargetQ(), 0.0);
        assertTrue(ext.isEnabled());
        assertSame(network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getTerminal2(), ext.getRegulatingTerminal());
    }

    @Test
    public void microAssembledSwitchAtBoundary() {
        final double tolerance = 1e-10;

        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSwitchAtBoundary().dataSource(),
                NetworkFactory.findDefault(), null);
        Line m = network.getLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");

        assertEquals(1.02, m.getR(), tolerance);
        assertEquals(12.0, m.getX(), tolerance);
        assertEquals(0.000015, m.getG1(), tolerance);
        assertEquals(0.000015, m.getG2(), tolerance);
        assertEquals(0.00007068585, m.getB1(), tolerance);
        assertEquals(0.00007068585, m.getB2(), tolerance);
    }

    @Test
    public void microAssembledTransformerAtBoundary() {
        final double tolerance = 1e-10;

        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledTransformerAtBoundary().dataSource(),
                NetworkFactory.findDefault(), null);
        Line m = network.getLine("17086487-56ba-4979-b8de-064025a6b4da + 8fdc7abd-3746-481a-a65e-3df56acd8b13");

        assertEquals(4.878525165580545, m.getR(), tolerance);
        assertEquals(81.682436981089380, m.getX(), tolerance);
        assertEquals(-0.000007804953422, m.getG1(), tolerance);
        assertEquals(0.000310492400731, m.getB1(), tolerance);
        assertEquals(0.000031997811967, m.getG2(), tolerance);
        assertEquals(-0.000228620410553, m.getB2(), tolerance);
    }

    @Test
    public void microAssembledThreeLinesAtBoundary() {
        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledThreeLinesAtBoundary().dataSource(),
                NetworkFactory.findDefault(), null);
        Line line = network.getLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");
        assertNotNull(line);
    }

    @Test
    public void microAssembledEquivalentBranchAtBoundary() {
        final double tolerance = 1e-10;

        InMemoryPlatformConfig platformConfigTieLines = new InMemoryPlatformConfig(fileSystem);
        platformConfigTieLines.createModuleConfig("import-export-parameters-default-value");

        Network network = new CgmesImport(platformConfigTieLines).importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledEquivalentBranchAtBoundary().dataSource(),
                NetworkFactory.findDefault(), null);
        Line m = network.getLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");
        assertEquals(2.01664607413, m.getR(), tolerance);
        assertEquals(21.991922797566996, m.getX(), tolerance);
        assertEquals(0.000007923595325, m.getG1(), tolerance);
        assertEquals(0.000022090405367, m.getG2(), tolerance);
        assertEquals(0.000038600957962, m.getB1(), tolerance);
        assertEquals(0.000102795699814, m.getB2(), tolerance);
    }

    @Test
    public void microT4InvalidSvcMode() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbInvalidSvcMode().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator offSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(offSvc);
        assertEquals(OFF, offSvc.getRegulationMode());
    }

    @Test
    public void microT4ReactivePowerSvc() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());
        assertEquals(229.5, svc.getVoltageSetpoint(), 0.0);
        assertTrue(Double.isNaN(svc.getReactivePowerSetpoint()));

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbReactivePowerSvc().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator reactivePowerSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(reactivePowerSvc);
        assertEquals(REACTIVE_POWER, reactivePowerSvc.getRegulationMode());
        assertEquals(229.5, reactivePowerSvc.getReactivePowerSetpoint(), 0.0);
        assertTrue(Double.isNaN(reactivePowerSvc.getVoltageSetpoint()));
    }

    @Test
    public void microT4OffSvc() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());

        Network modified1 = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbOffSvc().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator off1 = modified1.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(off1);
        assertEquals(OFF, off1.getRegulationMode());

        Network modified2 = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbOffSvcControl().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator off2 = modified2.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(off2);
        assertEquals(REACTIVE_POWER, off2.getRegulationMode());
        assertEquals(0.0d, off2.getReactivePowerSetpoint(), 0.0d);
    }

    @Test
    public void microT4SvcWithoutRegulatingControl() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());
        assertEquals(229.5, svc.getVoltageSetpoint(), 0.0);

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbSvcNoRegulatingControl().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator modifiedSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(modifiedSvc);
        assertEquals(VOLTAGE, modifiedSvc.getRegulationMode());
        assertEquals(159.5, modifiedSvc.getVoltageSetpoint(), 0.0);
    }

    @Test
    public void microT4ReactivePowerSvcWithMissingRegulatingControl() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getRegulationMode());
        assertEquals(229.5, svc.getVoltageSetpoint(), 0.0);

        Network modified = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microT4BeBbMissingRegControlReactivePowerSvc().dataSource(), NetworkFactory.findDefault(), null);
        StaticVarCompensator modifiedSvc = modified.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(modifiedSvc);
        assertEquals(REACTIVE_POWER, modifiedSvc.getRegulationMode());
        assertEquals(0.0, modifiedSvc.getReactivePowerSetpoint(), 0.0);
    }

    @Test
    public void miniBusBranchRtcRemoteRegulation() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniBusBranchRtcRemoteRegulation().dataSource(), NetworkFactory.findDefault(), null);

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
    public void miniBusBranchT3xTwoRegulatingControlsEnabled() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniBusBranchT3xTwoRegulatingControlsEnabled().dataSource(), NetworkFactory.findDefault(), null);

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
    public void miniBusBranchExternalInjectionControl() throws IOException {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.miniBusBranchExternalInjectionControl().dataSource(), NetworkFactory.findDefault(), null);
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
    public void miniNodeBreakerTestLimits() {
        // Original test case
        Network network0 = new CgmesImport().importData(CgmesConformity1Catalog.miniNodeBreaker().dataSource(), NetworkFactory.findDefault(), null);
        // The case has been manually modified to have OperationalLimits
        // defined for Equipment
        Network network1 = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerLimitsforEquipment().dataSource(), NetworkFactory.findDefault(), null);

        double tol = 0;

        // 1 - PATL Current defined for an Equipment ACTransmissionLine
        // Previous limit for one terminal has been modified to refer to the Equipment
        // In the modified case both ends have to see the same value
        Line l0 = network0.getLine("1e7f52a9-21d0-4ebe-9a8a-b29281d5bfc9");
        Line l1 = network1.getLine("1e7f52a9-21d0-4ebe-9a8a-b29281d5bfc9");
        assertEquals(525, l0.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);
        assertTrue(l0.getCurrentLimits2().isEmpty());
        assertEquals(525, l1.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);
        assertEquals(525, l1.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);

        // 2 - PATL Current defined for an ACTransmissionLine
        // that will be mapped to a DanglingLine in IIDM
        DanglingLine dl0 = network0.getDanglingLine("f32baf36-7ea3-4b6a-9452-71e7f18779f8");
        DanglingLine dl1 = network1.getDanglingLine("f32baf36-7ea3-4b6a-9452-71e7f18779f8");
        // In network0 limit is defined for the Terminal
        // In network1 limit is defined for the Equipment
        // In both cases the limit should be mapped to IIDM
        assertEquals(1000, dl0.getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);
        assertEquals(1000, dl1.getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);

        // 3 - PATL Current defined for a PowerTransformer, should be rejected
        TwoWindingsTransformer tx0 = network0.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        TwoWindingsTransformer tx1 = network1.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        assertEquals(158, tx0.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);
        assertEquals(1732, tx0.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);
        assertTrue(tx1.getCurrentLimits1().isEmpty());
        assertEquals(1732, tx1.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);

        // 4 - PATL Current defined for Switch, will be ignored
        // The transformer that had the original limit will lose it
        // Switches in IIDM do not have limits, so simply check that switch that receives the limit exists in both Networks
        TwoWindingsTransformer tx0s = network0.getTwoWindingsTransformer("6c89588b-3df5-4120-88e5-26164afb43e9");
        TwoWindingsTransformer tx1s = network1.getTwoWindingsTransformer("6c89588b-3df5-4120-88e5-26164afb43e9");
        Switch sw0 = network0.getSwitch("d0119330-220f-4ed3-ad3c-f893ad0534fb");
        Switch sw1 = network0.getSwitch("d0119330-220f-4ed3-ad3c-f893ad0534fb");
        assertEquals(1732, tx0s.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), tol);
        assertTrue(tx1s.getCurrentLimits2().isEmpty());
        assertNotNull(sw0);
        assertNotNull(sw1);
    }

    @Test
    public void miniNodeBreakerInvalidT2w() {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        platformConfig.createModuleConfig("import-export-parameters-default-value")
                .setStringProperty("iidm.import.cgmes.convert-boundary", "true");

        Network network = new CgmesImport(platformConfig).importData(CgmesConformity1Catalog.miniNodeBreaker().dataSource(),
                NetworkFactory.findDefault(), null);
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        assertNotNull(transformer);

        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.miniNodeBreakerInvalidT2w().dataSource();
        PowsyblException e = assertThrows(PowsyblException.class, () -> new CgmesImport(platformConfig).importData(ds, NetworkFactory.findDefault(), null));
        assertEquals("2 windings transformer 'ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51': the 2 windings of the transformer shall belong to the substation '183d126d-2522-4ff2-a8cd-c5016cf09c1b_S' ('183d126d-2522-4ff2-a8cd-c5016cf09c1b_S', 'd6056127-34f1-43a9-b029-23fddb913bd5')",
            e.getMessage());
    }

    @Test
    public void miniNodeBreakerSvInjection() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerSvInjection().dataSource(),
                        NetworkFactory.findDefault(), null);

        Load load = network.getLoad("SvInjection");
        assertNotNull(load);
        assertEquals(-0.2, load.getP0(), 0.0);
        assertEquals(-13.8, load.getQ0(), 0.0);
    }

    @Test
    public void miniNodeBreakerLoadBreakSwitch() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerLoadBreakSwitch().dataSource(),
                        NetworkFactory.findDefault(), null);

        Switch sw = network.getSwitch("fbdcf00d-8a07-4c62-9e39-86f459bea2be");
        assertNotNull(sw);
        assertEquals(SwitchKind.LOAD_BREAK_SWITCH, sw.getKind());
    }

    @Test
    public void miniNodeBreakerCimLine() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerCimLine().dataSource(),
                        NetworkFactory.findDefault(), null);

        VoltageLevel vl = network.getVoltageLevel("d3de846d-5271-465e-8558-3e736fa120c4_2_VL");
        assertNotNull(vl);
        assertNull(vl.getNullableSubstation());

        vl = network.getVoltageLevel("e2f8de8c-3191-4676-9ee7-f920e46f9085_2_VL");
        assertNotNull(vl);
        assertNull(vl.getNullableSubstation());
    }

    @Test
    public void miniNodeBreakerProtectedSwitch() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerProtectedSwitch().dataSource(),
                        NetworkFactory.findDefault(), null);

        Switch sw = network.getSwitch("fbdcf00d-8a07-4c62-9e39-86f459bea2be");
        assertNotNull(sw);
        // By default, a switch not specifically assigned to a given kid should be considered BREAKER
        assertEquals(SwitchKind.BREAKER, sw.getKind());
    }

    @Test
    public void smallBusBranchTieFlowWithoutControlArea() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallBusBranchTieFlowsWithoutControlArea().dataSource(), NetworkFactory.findDefault(), null);
        assertNull(network.getExtension(CgmesControlAreas.class));
    }

    @Test
    public void smallNodeBreakerHvdcDcLine2Inverter1Rectifier2() {
        // Small Grid Node Breaker HVDC modified so in the dcLine2
        // SVC1 (that is at side 2 of the DC line) is interpreted as a rectifier and
        // SVC2 (that is at side 1 of the line) is interpreted as an inverter
        assertNotNull(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2Inverter1Rectifier2().dataSource(), NetworkFactory.findDefault(), null));
    }

    @Test
    public void smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier() {
        // Small Grid Node Breaker HVDC modified so in the dcLine
        // both converters have targetPpcc consistent with side 1 inverter side 2 rectifier
        assertNotNull(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier().dataSource(), NetworkFactory.findDefault(), null));
    }

    @Test
    public void smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter() {
        // Small Grid Node Breaker HVDC modified so in the dcLine
        // both converters have targetPpcc consistent with side 1 rectifier side 2 inverter
        assertNotNull(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter().dataSource(), NetworkFactory.findDefault(), null));
    }

    @Test
    public void smallNodeBreakerHvdcVscReactiveQPcc() {
        // Small Grid Node Breaker HVDC modified so VSC converter are regulating in reactive power and not in voltage
        assertNotNull(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcVscReactiveQPcc().dataSource(), NetworkFactory.findDefault(), null));
    }

    @Test
    public void smallNodeBreakerHvdcNanTargetPpcc() {
        // Small Grid Node Breaker HVDC modified so targetPpcc are NaN
        assertNotNull(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcNanTargetPpcc().dataSource(), NetworkFactory.findDefault(), null));
    }

    @Test
    public void smallNodeBreakerHvdcMissingDCLineSegment() {
        // Small Grid Node Breaker HVDC modified so there is not DC Line Segment
        assertNotNull(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcMissingDCLineSegment().dataSource(), NetworkFactory.findDefault(), null));
    }

    @Test
    public void smallNodeBreakerVscControllerRemotePccTerminal() {
        assertNotNull(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerVscConverterRemotePccTerminal().dataSource(), NetworkFactory.findDefault(), null));
    }

    @Test
    public void miniNodeBreakerInternalLineZ0() {
        Network network = new CgmesImport()
                .importData(CgmesConformity1ModifiedCatalog.miniNodeBreakerInternalLineZ0().dataSource(), NetworkFactory.findDefault(), null);
        // The internal z0 line named "INTERCONNECTOR22" has been converted to a switch
        Switch sw = network.getSwitch("fdf5cfbe-9bf5-406a-8d04-fafe47afe31d");
        assertNotNull(sw);
        assertEquals("INTERCONNECTOR22", sw.getNameOrId());
    }

    @Test
    public void microGridBaseCaseAssembledEntsoeCategory() {
        Properties params = new Properties();
        params.put(CgmesImport.POST_PROCESSORS, "EntsoeCategory");
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledEntsoeCategory().dataSource(), params);
        assertEquals(31, network.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa").getExtension(GeneratorEntsoeCategory.class).getCode());
        assertEquals(42, network.getGenerator("9c3b8f97-7972-477d-9dc8-87365cc0ad0e").getExtension(GeneratorEntsoeCategory.class).getCode());
        assertNull(network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getExtension(GeneratorEntsoeCategory.class));
        assertNull(network.getGenerator("1dc9afba-23b5-41a0-8540-b479ed8baf4b").getExtension(GeneratorEntsoeCategory.class));
        assertNull(network.getGenerator("2844585c-0d35-488d-a449-685bcd57afbf").getExtension(GeneratorEntsoeCategory.class));
    }

    @Test
    public void microGridBaseCaseNLMultipleSlacks() {
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseCaseNLMultipleSlacks().dataSource(), null);
        Generator g = network.getGenerator("9c3b8f97-7972-477d-9dc8-87365cc0ad0e-bis");
        SlackTerminal st = g.getTerminal().getVoltageLevel().getExtension(SlackTerminal.class);
        assertNotNull(st);
        assertEquals(g.getTerminal().getConnectable().getId(), st.getTerminal().getConnectable().getId());
    }

    private FileSystem fileSystem;
}
