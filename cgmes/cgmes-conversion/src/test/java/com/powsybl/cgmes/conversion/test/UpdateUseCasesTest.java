/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class UpdateUseCasesTest {

    private static final String PARTIAL_DIR = "/update/use_cases/partial_update_using_one_variant/";
    private static final String COMPLETE_DIR = "/update/use_cases/complete_update_using_different_variants/";

    @Test
    void partialUpdateUsingOneVariantTest() {
        Network network = readCgmesResources(PARTIAL_DIR, "partial_update_EQ.xml", "partial_update_00_SSH.xml");
        assertEquals(3, network.getLoadCount());
        assertPartialEq(network);
        assertPartialSsh(network, 10.1, 5.1, 10.2, 5.2, 10.3, 5.3);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, PARTIAL_DIR, "partial_update_08_SSH.xml");
        assertPartialSsh(network, 20.1, 10.1, 10.2, 5.2, 10.3, 5.3);

        readCgmesResources(network, properties, PARTIAL_DIR, "partial_update_16_SSH.xml");
        assertPartialSsh(network, 20.1, 10.1, 20.2, 10.2, 10.3, 5.3);

        readCgmesResources(network, properties, PARTIAL_DIR, "partial_update_24_SSH.xml");
        assertPartialSsh(network, 20.1, 10.1, 20.2, 10.2, 20.3, 10.3);
    }

    private static void assertPartialEq(Network network) {
        assertNotNull(network.getLoad("EnergyConsumer_1"));
        assertNotNull(network.getLoad("EnergyConsumer_2"));
        assertNotNull(network.getLoad("EnergyConsumer_3"));
    }

    void assertPartialSsh(Network network, double p1, double q1, double p2, double q2, double p3, double q3) {
        assertSsh(network.getLoad("EnergyConsumer_1"), p1, q1);
        assertSsh(network.getLoad("EnergyConsumer_2"), p2, q2);
        assertSsh(network.getLoad("EnergyConsumer_3"), p3, q3);
    }

    private static void assertSsh(Load load, double p0, double q0) {
        assertNotNull(load);
        assertEquals(p0, load.getP0());
        assertEquals(q0, load.getQ0());
    }

    @Test
    void completeUpdateUsingDifferentVariantsTest() {
        Network network = readCgmesResources(COMPLETE_DIR, "complete_update_EQ.xml", "complete_update_00_SSH.xml");
        assertEquals(3, network.getLoadCount());
        assertCompleteEq(network);

        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "update-08");
        network.getVariantManager().setWorkingVariant("update-08");
        readCgmesResources(network, COMPLETE_DIR, "complete_update_08_SSH.xml");

        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "update-16");
        network.getVariantManager().setWorkingVariant("update-16");
        readCgmesResources(network, COMPLETE_DIR, "complete_update_16_SSH.xml");

        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "update-24");
        network.getVariantManager().setWorkingVariant("update-24");
        readCgmesResources(network, COMPLETE_DIR, "complete_update_24_SSH.xml");

        network.getVariantManager().setWorkingVariant("InitialState");
        assertCompleteSsh(network, 20.1, 10.1, 20.2, 10.2, 20.3, 10.3);

        network.getVariantManager().setWorkingVariant("update-08");
        assertCompleteSsh(network, 8.1, 4.1, 8.2, 4.2, 8.3, 4.3);

        network.getVariantManager().setWorkingVariant("update-16");
        assertCompleteSsh(network, 16.1, 8.1, 16.2, 8.2, 16.3, 8.3);

        network.getVariantManager().setWorkingVariant("update-24");
        assertCompleteSsh(network, 24.1, 12.1, 24.2, 12.2, 24.3, 12.3);
    }

    private static void assertCompleteEq(Network network) {
        assertNotNull(network.getLoad("EnergySource_1"));
        assertNotNull(network.getLoad("EnergySource_2"));
        assertNotNull(network.getLoad("EnergySource_3"));
    }

    void assertCompleteSsh(Network network, double p1, double q1, double p2, double q2, double p3, double q3) {
        assertSsh(network.getLoad("EnergySource_1"), p1, q1);
        assertSsh(network.getLoad("EnergySource_2"), p2, q2);
        assertSsh(network.getLoad("EnergySource_3"), p3, q3);
    }
}
