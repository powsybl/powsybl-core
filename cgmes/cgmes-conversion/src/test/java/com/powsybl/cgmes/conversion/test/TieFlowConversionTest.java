/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.test.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TieFlowConversionTest {

    @Test
    public void smallBaseCaseTieFlowMappedToSwitch() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.smallGridBaseCaseTieFlowMappedToSwitch(), config);

        CgmesControlAreas cgmesControlAreas = n.getExtension(CgmesControlAreas.class);
        assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());

        cgmesControlAreas.getCgmesControlAreas().forEach(cgmesControlArea -> {
            assertEquals(1, cgmesControlArea.getTerminals().size());
            assertEquals(2, cgmesControlArea.getBoundaries().size());
            assertTrue(containsTerminal(cgmesControlArea, "_044ef2e7-c766-11e1-8775-005056c00008", IdentifiableType.DANGLING_LINE));
        });
    }

    @Test
    public void smallBaseCaseTieFlowMappedToEquivalentInjection() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.smallGridBaseCaseTieFlowMappedToEquivalentInjection(), config);

        CgmesControlAreas cgmesControlAreas = n.getExtension(CgmesControlAreas.class);
        assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());

        cgmesControlAreas.getCgmesControlAreas().forEach(cgmesControlArea -> {
            assertEquals(0, cgmesControlArea.getTerminals().size());
            assertEquals(3, cgmesControlArea.getBoundaries().size());
            assertTrue(containsBoundary(cgmesControlArea, "_044ef2e7-c766-11e1-8775-005056c00008", IdentifiableType.DANGLING_LINE));
        });
    }

    private Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {

        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        config.setProfileUsedForInitialStateValues(Conversion.Config.StateProfile.SSH.name());
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

        return n;
    }

    private static boolean containsTerminal(CgmesControlArea cgmesControlArea, String connectableId, IdentifiableType connectableType) {
        boolean ok = cgmesControlArea.getTerminals().stream().anyMatch(t -> isConnectableOk(connectableId, connectableType,
            t.getConnectable().getId(), t.getConnectable().getType()));
        if (!ok) {
            LOG.info("Terminal to find connectableId {} connectableType {}", connectableId, connectableType);
            cgmesControlArea.getBoundaries().forEach(t -> LOG.info("Terminal inside cgmesControlArea connectableId {} connectableType {}",
                    t.getConnectable().getId(), t.getConnectable().getType()));
        }
        return ok;
    }

    private static boolean containsBoundary(CgmesControlArea cgmesControlArea, String connectableId, IdentifiableType connectableType) {
        boolean ok = cgmesControlArea.getBoundaries().stream().anyMatch(b -> isConnectableOk(connectableId, connectableType,
            b.getConnectable().getId(), b.getConnectable().getType()));
        if (!ok) {
            LOG.info("Boundary to find connectableId {} connectableType {}", connectableId, connectableType);
            cgmesControlArea.getBoundaries().forEach(b -> LOG.info("Boundary inside cgmesControlArea connectableId {} connectableType {}",
                    b.getConnectable().getId(), b.getConnectable().getType()));
        }
        return ok;
    }

    private static boolean isConnectableOk(String refConnectableId, IdentifiableType refConnectableType, String connectableId, IdentifiableType connectableType) {
        return refConnectableId.equals(connectableId) && refConnectableType.equals(connectableType);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TieFlowConversionTest.class);
}
