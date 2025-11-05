/**
 * Copyright (c) 2024, Artelys (http://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.ReactiveLimitsTestNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class GeneratorConversionTest extends AbstractSerDeTest {

    @Test
    void generatingUnitTypes() {
        Network network = readCgmesResources("/", "GeneratingUnitTypes.xml");
        assertEquals(EnergySource.OTHER, network.getGenerator("gu_sm").getEnergySource());
        assertEquals(EnergySource.THERMAL, network.getGenerator("tgu_sm").getEnergySource());
        assertEquals(EnergySource.HYDRO, network.getGenerator("hgu_sm").getEnergySource());
        assertEquals(EnergySource.NUCLEAR, network.getGenerator("ngu_sm").getEnergySource());
        assertEquals(EnergySource.WIND, network.getGenerator("offshore_wgu_sm").getEnergySource());
        assertEquals("offshore", network.getGenerator("offshore_wgu_sm").getProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE));
        assertEquals(EnergySource.WIND, network.getGenerator("onshore_wgu_sm").getEnergySource());
        assertEquals("onshore", network.getGenerator("onshore_wgu_sm").getProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE));
        assertEquals(EnergySource.SOLAR, network.getGenerator("sgu_sm").getEnergySource());
    }

    @Test
    void reactiveLimitsExportTest() throws IOException {
        // IIDM network has 2 Generators. G1 has reactive limits of kind curve, G2 has reactive limits of kind min/max.
        Network network = ReactiveLimitsTestNetworkFactory.create();
        assertEquals(ReactiveLimitsKind.CURVE, network.getGenerator("G1").getReactiveLimits().getKind());
        assertEquals(ReactiveLimitsKind.MIN_MAX, network.getGenerator("G2").getReactiveLimits().getKind());

        // Export CGMES EQ profile.
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        // CGMES SynchronousMachine G1 has a ReactiveCapabilityCurve but no minQ/maxQ.
        String gen1 = getElement(eqFile, "SynchronousMachine", "G1");
        assertTrue(gen1.contains("<cim:SynchronousMachine.InitialReactiveCapabilityCurve"));
        assertFalse(gen1.contains("<cim:SynchronousMachine.minQ>"));
        assertFalse(gen1.contains("<cim:SynchronousMachine.maxQ>"));

        // CGMES SynchronousMachine G2 has a minQ/maxQ but no ReactiveCapabilityCurve.
        String gen2 = getElement(eqFile, "SynchronousMachine", "G2");
        assertFalse(gen2.contains("<cim:SynchronousMachine.InitialReactiveCapabilityCurve"));
        assertTrue(gen2.contains("<cim:SynchronousMachine.minQ>"));
        assertTrue(gen2.contains("<cim:SynchronousMachine.maxQ>"));
    }

    @Test
    void synchronousMachineImportIsCondenser() {
        Network network = readCgmesResources("/", "SynchronousMachineTypes.xml");

        assertFalse(network.getGenerator("sm_generator").isCondenser());
        assertFalse(network.getGenerator("sm_motor").isCondenser());
        assertTrue(network.getGenerator("sm_generator_condenser").isCondenser());
        assertTrue(network.getGenerator("sm_condenser").isCondenser());
    }

    @Test
    void synchronousMachineOperatingModeAndKindConversion() throws IOException {
        Network network = createNetwork();
        Generator generator1 = network.getGenerator("GEN");
        Generator generator2 = network.getGenerator("GEN2");
        Generator generator3 = network.getGenerator("GEN3");

        String eqXml = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir, new Properties());
        String sshXml = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDir, new Properties());

        String generator1Eq = getElement(eqXml, "SynchronousMachine", generator1.getId());
        assertNotNull(generator1Eq);
        assertTrue(generator1Eq.contains("SynchronousMachineKind.generatorOrCondenser"));
        String generator1Ssh = getElement(sshXml, "SynchronousMachine", generator1.getId());
        assertNotNull(generator1Ssh);
        assertTrue(generator1Ssh.contains("SynchronousMachineOperatingMode.generator"));

        String generator2Eq = getElement(eqXml, "SynchronousMachine", generator2.getId());
        assertNotNull(generator2Eq);
        assertTrue(generator2Eq.contains("SynchronousMachineKind.generatorOrCondenser"));
        String generator2Ssh = getElement(sshXml, "SynchronousMachine", generator2.getId());
        assertNotNull(generator2Ssh);
        assertTrue(generator2Ssh.contains("SynchronousMachineOperatingMode.condenser"));

        String generator3Eq = getElement(eqXml, "SynchronousMachine", generator3.getId());
        assertNotNull(generator3Eq);
        assertTrue(generator3Eq.contains("SynchronousMachineKind.motor"));
        String generator3Ssh = getElement(sshXml, "SynchronousMachine", generator3.getId());
        assertNotNull(generator3Ssh);
        assertTrue(generator3Ssh.contains("SynchronousMachineOperatingMode.motor"));

    }

    private Network createNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        Substation substation1 = network.newSubstation()
            .setId("S")
            .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
            .setId("VL")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        voltageLevel1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS")
            .setNode(0)
            .add();

        // Will be exported as a generatorOrCondenser and operating as a generator
        Generator generator1 = voltageLevel1.newGenerator()
            .setId("GEN")
            .setNode(1)
            .setMinP(0.0)
            .setMaxP(100.0)
            .setTargetP(25.0)
            .setTargetQ(10.0)
            .setVoltageRegulatorOn(false)
            .setCondenser(true)
            .add();
        generator1.newMinMaxReactiveLimits().setMinQ(-50.0).setMaxQ(50.0).add();
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(1).add();

        // Will be exported as a generatorOrCondenser and operating as a condenser
        Generator generator2 = voltageLevel1.newGenerator()
            .setId("GEN2")
            .setNode(2)
            .setMinP(0.0)
            .setMaxP(100.0)
            .setTargetP(0.0)
            .setTargetQ(10.0)
            .setVoltageRegulatorOn(false)
            .setCondenser(true)
            .add();
        generator2.newMinMaxReactiveLimits().setMinQ(-50.0).setMaxQ(50.0).add();
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(2).add();

        // Will be exported as motor and operating as a motor
        Generator generator3 = voltageLevel1.newGenerator()
            .setId("GEN3")
            .setNode(3)
            .setMinP(0.0)
            .setMaxP(100.0)
            .setTargetP(-10.0)
            .setTargetQ(10.0)
            .setVoltageRegulatorOn(false)
            .add();
        ReactiveCapabilityCurveAdder rcca = generator3.newReactiveCapabilityCurve();
        rcca.beginPoint()
            .setP(-100.0)
            .setMinQ(-200.0)
            .setMaxQ(200.0)
            .endPoint();
        rcca.beginPoint()
            .setP(-10)
            .setMinQ(-200.0)
            .setMaxQ(200.0)
            .endPoint();
        rcca.add();
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(3).add();

        return network;
    }
}
