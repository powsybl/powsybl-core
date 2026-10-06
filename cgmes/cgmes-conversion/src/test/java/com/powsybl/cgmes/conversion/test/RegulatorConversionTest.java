/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static com.powsybl.iidm.network.PhaseTapChanger.RegulationMode.CURRENT_LIMITER;
import static com.powsybl.iidm.network.regulation.RegulationMode.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clement Philipot {@literal  <clement.philipot at rte-france.com>}
 */
class RegulatorConversionTest {

    private Properties importParams;

    @BeforeEach
    void setUp() {
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @Test
    void invalidRegulatingControls() {
        Network network = readCgmesResources(importParams, "/issues/regulator",
                "invalid_control_EQ.xml", "invalid_control_SSH.xml", "invalid_control_TP.xml");

        Generator generator1 = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        assertNull(generator1.getVoltageRegulation());
        assertTrue(Double.isNaN(generator1.getLocalTargetV()));
        assertSame(generator1.getTerminal(), generator1.getRegulatingTerminal());

        RatioTapChanger rtc = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957").getRatioTapChanger();
        assertNotNull(rtc);
        assertTrue(rtc.hasLoadTapChangingCapabilities());
        assertTrue(Double.isNaN(rtc.getRegulatingTargetV()));
        assertFalse(rtc.isRegulating());
        assertNull(rtc.getRegulatingTerminal());

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertNotNull(ptc);
        assertEquals(CURRENT_LIMITER, ptc.getRegulationMode());
        assertTrue(Double.isNaN(ptc.getRegulationValue()));
        assertFalse(ptc.isRegulating());
        assertNull(ptc.getRegulationTerminal());

        Generator generator2 = network.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa");
        assertEquals(generator2.getTerminal().getVoltageLevel().getNominalV(), generator2.getLocalTargetV(), 0.0);
        assertEquals(VOLTAGE, generator2.getVoltageRegulation().getMode());
    }

    @Test
    void missingRegulatingControls() {
        Network network = readCgmesResources(importParams, "/issues/regulator",
                "missing_control_EQ.xml", "missing_control_SSH.xml", "missing_control_TP.xml");

        Generator generator = network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
        assertNull(generator.getVoltageRegulation());
        assertTrue(Double.isNaN(generator.getLocalTargetV()));

        RatioTapChanger rtc = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543").getRatioTapChanger();
        assertNotNull(rtc);
        assertTrue(rtc.hasLoadTapChangingCapabilities());
        assertTrue(Double.isNaN(rtc.getRegulatingTargetV()));
        assertFalse(rtc.isRegulating());
        assertNull(rtc.getRegulatingTerminal());

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getPhaseTapChanger();
        assertNotNull(ptc);
        assertEquals(CURRENT_LIMITER, ptc.getRegulationMode());
        assertTrue(Double.isNaN(ptc.getRegulationValue()));
        assertFalse(ptc.isRegulating());
        assertNull(ptc.getRegulationTerminal());
    }

    @Test
    void shuntWithoutRegulatingControl() {
        Network network = readCgmesResources(importParams, "/issues/regulator",
                "missing_shunt_control_EQ.xml", "missing_shunt_control_SSH.xml", "missing_shunt_control_TP.xml");
        ShuntCompensator shunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertNull(shunt.getVoltageRegulation());
    }

    @Test
    void svcInvalidControlModeDefaultsToVoltage() {
        Network network = readCgmesResources(importParams, "/issues/regulator",
                "svc_invalid_cim16_EQ.xml", "svc_invalid_cim16_SSH.xml", "svc_invalid_cim16_TP.xml");
        StaticVarCompensator svc = network.getStaticVarCompensator("3c69652c-ff14-4550-9a87-b6fdaccbb5f4");
        assertNotNull(svc);
        assertTrue(svc.getVoltageRegulation().isRegulating());
        assertEquals(VOLTAGE, svc.getVoltageRegulation().getMode());
    }

    @Test
    void svcVoltageAndReactivePowerRegulation() {
        Network network = readCgmesResources(importParams, "/issues/regulator", "svc_EQ.xml", "svc_SSH.xml");

        StaticVarCompensator voltageSvc = network.getStaticVarCompensator("SVC_BASE");
        assertNotNull(voltageSvc);
        assertEquals(VOLTAGE, voltageSvc.getVoltageRegulation().getMode());
        assertEquals(229.5, voltageSvc.getRegulatingTargetV(), 0.0);
        assertEquals(0.0, voltageSvc.getLocalTargetQ());

        StaticVarCompensator reactivePowerSvc = network.getStaticVarCompensator("SVC_REACTIVE");
        assertNotNull(reactivePowerSvc);
        assertEquals(REACTIVE_POWER, reactivePowerSvc.getVoltageRegulation().getMode());
        assertEquals(229.5, reactivePowerSvc.getRegulatingTargetQ(), 0.0);
        assertTrue(Double.isNaN(reactivePowerSvc.getRegulatingTargetV()));
    }

    @Test
    void svcControlEnabledAndSetpoints() {
        Network network = readCgmesResources(importParams, "/issues/regulator", "svc_EQ.xml", "svc_SSH.xml");

        StaticVarCompensator voltageSvc = network.getStaticVarCompensator("SVC_BASE");
        assertNotNull(voltageSvc);
        assertEquals(VOLTAGE, voltageSvc.getVoltageRegulation().getMode());

        StaticVarCompensator offSvc = network.getStaticVarCompensator("SVC_OFF");
        assertNotNull(offSvc);
        assertFalse(offSvc.getVoltageRegulation().isRegulating());
        assertEquals(VOLTAGE, offSvc.getVoltageRegulation().getMode());

        StaticVarCompensator offSvc229 = network.getStaticVarCompensator("SVC_OFF_229");
        assertNotNull(offSvc229);
        assertEquals(VOLTAGE, offSvc229.getVoltageRegulation().getMode());
        assertEquals(0.0, offSvc229.getLocalTargetQ());
        assertEquals(229.5, offSvc229.getRegulatingTargetV(), 0.0);
        assertFalse(offSvc229.getVoltageRegulation().isRegulating());

        StaticVarCompensator offSvc231 = network.getStaticVarCompensator("SVC_OFF_231");
        assertNotNull(offSvc231);
        assertFalse(offSvc231.getVoltageRegulation().isRegulating());
        assertEquals(VOLTAGE, offSvc231.getVoltageRegulation().getMode());
        assertEquals(231.123, offSvc231.getRegulatingTargetV(), 0.0);
    }

    @Test
    void svcWithoutRegulatingControlUsesVoltageSetpoint() {
        Network network = readCgmesResources(importParams, "/issues/regulator", "svc_EQ.xml", "svc_SSH.xml");

        StaticVarCompensator svc = network.getStaticVarCompensator("SVC_BASE");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getVoltageRegulation().getMode());
        assertEquals(229.5, svc.getRegulatingTargetV(), 0.0);

        StaticVarCompensator svcWithoutControl = network.getStaticVarCompensator("SVC_NO_CONTROL");
        assertNotNull(svcWithoutControl);
        assertEquals(VOLTAGE, svcWithoutControl.getVoltageRegulation().getMode());
        assertEquals(159.5, svcWithoutControl.getRegulatingTargetV(), 0.0);
    }

    @Test
    void reactivePowerSvcWithoutRegulatingControlIsNotRegulating() {
        Network network = readCgmesResources(importParams, "/issues/regulator", "svc_EQ.xml", "svc_SSH.xml");

        StaticVarCompensator svc = network.getStaticVarCompensator("SVC_BASE");
        assertNotNull(svc);
        assertEquals(VOLTAGE, svc.getVoltageRegulation().getMode());
        assertEquals(229.5, svc.getRegulatingTargetV(), 0.0);

        StaticVarCompensator svcWithoutControl = network.getStaticVarCompensator("SVC_NO_REACTIVE_CONTROL");
        assertNotNull(svcWithoutControl);
        assertFalse(svcWithoutControl.isRegulating());
        assertEquals(0.0, svcWithoutControl.getLocalTargetQ(), 0.0);
    }

    @Test
    void remoteRegulationTerminalsResolveToTransformerBuses() {
        Network network = readCgmesResources(importParams, "/issues/regulator",
                "remote_regulation_EQ.xml", "remote_regulation_SSH.xml", "remote_regulation_TP.xml");

        TwoWindingsTransformer twt2 = network.getTwoWindingsTransformer("813365c3-5be7-4ef0-a0a7-abd1ae6dc174");
        RatioTapChanger rtc = twt2.getRatioTapChanger();
        assertNotNull(rtc);
        Terminal regulatingTerminal = rtc.getRegulatingTerminal();
        assertNotNull(regulatingTerminal);
        assertSame(twt2.getTerminal1().getBusBreakerView().getBus(), regulatingTerminal.getBusBreakerView().getBus());

        ThreeWindingsTransformer twt3 = network.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773");
        RatioTapChanger rtc2 = twt3.getLeg3().getRatioTapChanger();
        assertNotNull(rtc2);
        Terminal regulatingTerminal2 = rtc2.getRegulatingTerminal();
        assertNotNull(regulatingTerminal2);
        assertSame(network.getVoltageLevel("93778e52-3fd5-456d-8b10-987c3e6bc47e").getBusBreakerView().getBus("03163ede-7eec-457f-8641-365982227d7c"),
                regulatingTerminal2.getBusBreakerView().getBus());
    }

    @Test
    void threeWindingTransformerKeepsOnlyFirstRegulatingControlEnabled() {
        Network network = readCgmesResources(importParams, "/issues/regulator",
                "two_regulating_controls_EQ.xml", "two_regulating_controls_SSH.xml", "two_regulating_controls_TP.xml");

        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773");
        RatioTapChanger leg2TapChanger = transformer.getLeg2().getRatioTapChanger();
        assertNotNull(leg2TapChanger);
        assertNotNull(leg2TapChanger.getRegulatingTerminal());
        assertTrue(leg2TapChanger.isRegulating());

        RatioTapChanger leg3TapChanger = transformer.getLeg3().getRatioTapChanger();
        assertNotNull(leg3TapChanger);
        assertNotNull(leg3TapChanger.getRegulatingTerminal());
        assertFalse(leg3TapChanger.isRegulating());
    }
}
