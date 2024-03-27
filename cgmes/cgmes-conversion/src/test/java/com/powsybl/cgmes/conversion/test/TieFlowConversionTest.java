/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TieFlowConversionTest {

    @Test
    void smallBaseCaseTieFlowMappedToSwitch() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.smallGridBaseCaseTieFlowMappedToSwitch(), config);

        CgmesControlAreas cgmesControlAreas = n.getExtension(CgmesControlAreas.class);
        assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());

        cgmesControlAreas.getCgmesControlAreas().forEach(cgmesControlArea -> {
            assertEquals(1, cgmesControlArea.getTerminals().size());
            assertEquals(2, cgmesControlArea.getBoundaries().size());
            assertTrue(containsTerminal(cgmesControlArea, "044ef2e7-c766-11e1-8775-005056c00008", IdentifiableType.DANGLING_LINE));
        });
    }

    @Test
    void smallBaseCaseTieFlowMappedToEquivalentInjection() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.smallGridBaseCaseTieFlowMappedToEquivalentInjection(), config);

        CgmesControlAreas cgmesControlAreas = n.getExtension(CgmesControlAreas.class);
        assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());

        cgmesControlAreas.getCgmesControlAreas().forEach(cgmesControlArea -> {
            assertEquals(0, cgmesControlArea.getTerminals().size());
            assertEquals(3, cgmesControlArea.getBoundaries().size());
            assertTrue(containsBoundary(cgmesControlArea, "044ef2e7-c766-11e1-8775-005056c00008", IdentifiableType.DANGLING_LINE));
        });
    }

    @Test
    void microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection(), config);

        CgmesControlAreas cgmesControlAreas = n.getExtension(CgmesControlAreas.class);
        assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());

        cgmesControlAreas.getCgmesControlAreas().forEach(cgmesControlArea -> {
            assertEquals(4, cgmesControlArea.getTerminals().size());
            assertEquals(1, cgmesControlArea.getBoundaries().size());
            assertTrue(containsBoundary(cgmesControlArea, "17086487-56ba-4979-b8de-064025a6b4da", IdentifiableType.DANGLING_LINE));
        });
    }

    @Test
    void microGridBaseCaseBEWithTieFlowMappedToSwitch() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlowMappedToSwitch(), config);

        CgmesControlAreas cgmesControlAreas = n.getExtension(CgmesControlAreas.class);
        assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());

        cgmesControlAreas.getCgmesControlAreas().forEach(cgmesControlArea -> {
            assertEquals(5, cgmesControlArea.getTerminals().size());
            assertEquals(0, cgmesControlArea.getBoundaries().size());
            assertTrue(containsTerminal(cgmesControlArea, "17086487-56ba-4979-b8de-064025a6b4da", IdentifiableType.DANGLING_LINE));
        });
    }

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config) {
        config.setConvertSvInjections(true);
        return ConversionUtil.networkModel(testGridModel, config);
    }

    private static boolean containsTerminal(CgmesControlArea cgmesControlArea, String connectableId, IdentifiableType identifiableType) {
        boolean ok = cgmesControlArea.getTerminals().stream().anyMatch(t -> isConnectableOk(connectableId, identifiableType,
            t.getConnectable().getId(), t.getConnectable().getType()));
        if (!ok) {
            LOG.info("Terminal to find connectableId {} identifiableType {}", connectableId, identifiableType);
            cgmesControlArea.getTerminals().forEach(t -> LOG.info("Terminal inside cgmesControlArea connectableId {} identifiableType {}",
                    t.getConnectable().getId(), t.getConnectable().getType()));
        }
        return ok;
    }

    private static boolean containsBoundary(CgmesControlArea cgmesControlArea, String connectableId, IdentifiableType identifiableType) {
        boolean ok = cgmesControlArea.getBoundaries().stream().anyMatch(b -> isConnectableOk(connectableId, identifiableType,
            b.getDanglingLine().getId(), b.getDanglingLine().getType()));
        if (!ok) {
            LOG.info("Boundary to find connectableId {} identifiableType {}", connectableId, identifiableType);
            cgmesControlArea.getBoundaries().forEach(b -> LOG.info("Boundary inside cgmesControlArea danglingLineId {}}",
                    b.getDanglingLine().getId()));
        }
        return ok;
    }

    private static boolean isConnectableOk(String refConnectableId, IdentifiableType refIdentifiableType, String connectableId, IdentifiableType identifiableType) {
        return refConnectableId.equals(connectableId) && refIdentifiableType.equals(identifiableType);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TieFlowConversionTest.class);
}
