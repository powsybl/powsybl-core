/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class JoinVoltageLevelTest {

    @Test
    public void smallNodeBreakerJoinVoltageLevelSwitch() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniNodeBreakerJoinVoltageLevelSwitch(), config);

        Switch sw = n.getSwitch("_5e9f0079-647e-46da-b0ee-f5f24e127602");
        VoltageLevel voltageLevel = sw.getVoltageLevel();
        Substation substation = voltageLevel.getOptionalSubstation().orElse(null);

        boolean ok = compareVoltageLevelSubstation("_d6056127-34f1-43a9-b029-23fddb913bd5", "_a43d15db-44a6-4fda-a525-2402ff43226f", substation.getId(), voltageLevel.getId());
        assertTrue(ok);

        VoltageLevel voltageLevelIidm = n.getVoltageLevel("_a43d15db-44a6-4fda-a525-2402ff43226f");
        assertEquals("_a43d15eb-44a6-4fda-a525-2402ff43226f", voltageLevelIidm.getAliasFromType("MergedVoltageLevel1").get());
    }

    @Test
    public void miniNodeBreakerSwitchBetweenVoltageLevelsOpen() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniNodeBreakerSwitchBetweenVoltageLevelsOpen(), config);

        Switch sw = n.getSwitch("_5e9f0079-647e-46da-b0ee-f5f24e127602");
        assertNotNull(sw);

        boolean isOpen = sw.isOpen();
        assertTrue(isOpen);

        VoltageLevel voltageLevel = sw.getVoltageLevel();
        Substation substation = voltageLevel.getOptionalSubstation().orElse(null);

        boolean ok = compareVoltageLevelSubstation("_d6056127-34f1-43a9-b029-23fddb913bd5", "_a43d15db-44a6-4fda-a525-2402ff43226f", substation.getId(), voltageLevel.getId());
        assertTrue(ok);
    }

    @Test
    public void smallNodeBreakerJoinVoltageLevelTx() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniNodeBreakerJoinVoltageLevelTx(), config);

        TwoWindingsTransformer t2x = n.getTwoWindingsTransformer("_ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        Terminal t1 = t2x.getTerminal(Side.ONE);
        VoltageLevel voltageLevel1 = t1.getVoltageLevel();
        Terminal t2 = t2x.getTerminal(Side.TWO);
        VoltageLevel voltageLevel2 = t2.getVoltageLevel();

        Substation substation = t2x.getSubstation();

        boolean ok = compareVoltageLevelSubstation("_d6056127-34f1-43a9-b029-23fddb913bd5",
            "_a43d15db-44a6-4fda-a525-2402ff43226f", "_0d68ac81-124d-4d21-afa8-6c503feef5b8", substation.getId(),
            voltageLevel1.getId(), voltageLevel2.getId());
        assertTrue(ok);

        Substation substationIidm = n.getSubstation("_d6056127-34f1-43a9-b029-23fddb913bd5");
        assertEquals("_d6056137-34f1-43a9-b029-23fddb913bd5", substationIidm.getAliasFromType("MergedSubstation1").get());
    }

    private static Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {

        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        config.setProfileUsedForInitialStateValues(Conversion.Config.StateProfile.SSH.name());
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

        return n;
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
