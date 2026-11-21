/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LoadDetail;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clement Philipot {@literal <clement.philipot at rte-france.com>}
 */

class LoadAndSvInjectionConversionTest {

    private static final String RESOURCE_DIR = "/issues/loads/";

    @Test
    void svInjectionsBusBreaker() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_NODE_BREAKER_AS_BUS_BREAKER, "true");
        Network network = readCgmesResources(importParams, RESOURCE_DIR, "sv_injection_EQ.xml", "sv_injection_SV.xml");

        assertLoadPower(network.getLoad("SVI1"), -0.2, -13.8);
        assertLoadPower(network.getLoad("SVI2"), -0.2, 0.0);
        assertLoadPower(network.getLoad("SVI3"), -0.2, -13.8);
    }

    @Test
    void invalidSvInjectionTopologicalNode() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_NODE_BREAKER_AS_BUS_BREAKER, "true");
        Network network = readCgmesResources(importParams, RESOURCE_DIR, "sv_injection_EQ.xml", "sv_injection_SV.xml");

        assertNull(network.getLoad("SVI_INVALID"));
    }

    @Test
    void svInjectionNodeBreaker() {
        Network network = readCgmesResources(RESOURCE_DIR, "sv_injection_EQ.xml", "sv_injection_SV.xml");

        assertLoadPower(network.getLoad("SVI_NB"), -0.2, -13.8);
    }

    @Test
    void conformAndNonConformLoadDetails() {
        Network network = readCgmesResources(RESOURCE_DIR, "load_types_EQ.xml", "load_types_SSH.xml");

        LoadDetail conformDetails = network.getLoad("CL1").getExtension(LoadDetail.class);
        assertNotNull(conformDetails);
        assertEquals(0.0, conformDetails.getFixedActivePower(), 0.0);
        assertEquals(0.0, conformDetails.getFixedReactivePower(), 0.0);
        assertEquals(200.0, conformDetails.getVariableActivePower(), 0.0);
        assertEquals(90.0, conformDetails.getVariableReactivePower(), 0.0);

        LoadDetail nonConformDetails = network.getLoad("NCL1").getExtension(LoadDetail.class);
        assertNotNull(nonConformDetails);
        assertEquals(200.0, nonConformDetails.getFixedActivePower(), 0.0);
        assertEquals(50.0, nonConformDetails.getFixedReactivePower(), 0.0);
        assertEquals(0.0, nonConformDetails.getVariableActivePower(), 0.0);
        assertEquals(0.0, nonConformDetails.getVariableReactivePower(), 0.0);
    }

    @Test
    void stationSupplyLoadTypeAndPower() {
        Network network = readCgmesResources(RESOURCE_DIR, "load_types_EQ.xml", "load_types_SSH.xml");
        Load load = network.getLoad("SS1");

        assertNotNull(load);
        assertEquals(6.5, load.getP0(), 1e-3);
        assertEquals(0.001, load.getQ0(), 1e-3);
        assertEquals(LoadType.AUXILIARY, load.getLoadType());
    }

    private static void assertLoadPower(Load load, double p0, double q0) {
        assertNotNull(load);
        assertEquals(p0, load.getP0(), 0.0);
        assertEquals(q0, load.getQ0(), 0.0);
    }
}
