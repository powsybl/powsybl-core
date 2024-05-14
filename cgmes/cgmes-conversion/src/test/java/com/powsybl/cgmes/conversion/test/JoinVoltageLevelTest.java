/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class JoinVoltageLevelTest {

    @Test
    void smallNodeBreakerJoinVoltageLevelSwitch() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniNodeBreakerJoinVoltageLevelSwitch(), config);

        Switch sw = n.getSwitch("5e9f0079-647e-46da-b0ee-f5f24e127602");
        VoltageLevel voltageLevel = sw.getVoltageLevel();
        Substation substation = voltageLevel.getSubstation().orElse(null);

        boolean ok = compareVoltageLevelSubstation("d6056127-34f1-43a9-b029-23fddb913bd5", "a43d15db-44a6-4fda-a525-2402ff43226f", substation.getId(), voltageLevel.getId());
        assertTrue(ok);

        VoltageLevel voltageLevelIidm = n.getVoltageLevel("a43d15db-44a6-4fda-a525-2402ff43226f");
        assertEquals("a43d15eb-44a6-4fda-a525-2402ff43226f", voltageLevelIidm.getAliasFromType("MergedVoltageLevel1").get());
    }

    @Test
    void miniNodeBreakerSwitchBetweenVoltageLevelsOpen() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniNodeBreakerSwitchBetweenVoltageLevelsOpen(), config);

        Switch sw = n.getSwitch("5e9f0079-647e-46da-b0ee-f5f24e127602");
        assertNotNull(sw);

        boolean isOpen = sw.isOpen();
        assertTrue(isOpen);

        VoltageLevel voltageLevel = sw.getVoltageLevel();
        Substation substation = voltageLevel.getSubstation().orElse(null);

        boolean ok = compareVoltageLevelSubstation("d6056127-34f1-43a9-b029-23fddb913bd5", "a43d15db-44a6-4fda-a525-2402ff43226f", substation.getId(), voltageLevel.getId());
        assertTrue(ok);
    }

    @Test
    void smallNodeBreakerJoinVoltageLevelTx() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniNodeBreakerJoinVoltageLevelTx(), config);

        TwoWindingsTransformer t2x = n.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        Terminal t1 = t2x.getTerminal(TwoSides.ONE);
        VoltageLevel voltageLevel1 = t1.getVoltageLevel();
        Terminal t2 = t2x.getTerminal(TwoSides.TWO);
        VoltageLevel voltageLevel2 = t2.getVoltageLevel();

        Substation substation = t2x.getSubstation().orElse(null);

        boolean ok = compareVoltageLevelSubstation("d6056127-34f1-43a9-b029-23fddb913bd5",
            "a43d15db-44a6-4fda-a525-2402ff43226f", "0d68ac81-124d-4d21-afa8-6c503feef5b8", substation.getId(),
            voltageLevel1.getId(), voltageLevel2.getId());
        assertTrue(ok);

        Substation substationIidm = n.getSubstation("d6056127-34f1-43a9-b029-23fddb913bd5");
        assertEquals("d6056137-34f1-43a9-b029-23fddb913bd5", substationIidm.getAliasFromType("MergedSubstation1").get());
    }

    private static Network networkModel(GridModelReference testGridModel, Conversion.Config config) throws IOException {
        config.setConvertSvInjections(true);
        return ConversionUtil.networkModel(testGridModel, config);
    }

    private static boolean compareVoltageLevelSubstation(String expectedSubstationId, String expectedVoltageLevelId,
        String actualSubstationId, String actualVoltageLevelId) {
        boolean ok = true;
        if (!expectedSubstationId.equals(actualSubstationId)) {
            ok = false;
        }
        if (!expectedVoltageLevelId.equals(actualVoltageLevelId)) {
            ok = false;
        }
        if (!ok) {
            LOG.info("Substation expected: {} actual {}", expectedSubstationId, actualSubstationId);
            LOG.info("VoltageLevel expected: {} actual {}", expectedVoltageLevelId, actualVoltageLevelId);
        }
        return ok;
    }

    private static boolean compareVoltageLevelSubstation(String expectedSubstationId, String expectedVoltageLevelId1,
        String expectedVoltageLevelId2, String actualSubstationId, String actualVoltageLevelId1,
        String actualVoltageLevelId2) {
        boolean ok = true;
        if (!expectedSubstationId.equals(actualSubstationId)) {
            ok = false;
        }
        if (!expectedVoltageLevelId1.equals(actualVoltageLevelId1)) {
            ok = false;
        }
        if (!expectedVoltageLevelId2.equals(actualVoltageLevelId2)) {
            ok = false;
        }
        if (!ok) {
            LOG.info("Substation expected: {} actual {}", expectedSubstationId, actualSubstationId);
            LOG.info("VoltageLevel1 expected: {} actual {}", expectedVoltageLevelId1, actualVoltageLevelId1);
            LOG.info("VoltageLevel2 expected: {} actual {}", expectedVoltageLevelId2, actualVoltageLevelId2);
        }
        return ok;
    }

    private static final Logger LOG = LoggerFactory.getLogger(JoinVoltageLevelTest.class);
}
