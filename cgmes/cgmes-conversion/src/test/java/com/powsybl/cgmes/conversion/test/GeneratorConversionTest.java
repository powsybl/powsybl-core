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
}
