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

import com.powsybl.iidm.network.Area;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TieFlowConversionTest {

    @Test
    void smallBaseCaseTieFlowMappedToSwitch() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.smallGridBaseCaseTieFlowMappedToSwitch(), config);

        assertEquals(1, n.getAreaStream().count());

        Area controlArea = n.getAreas().iterator().next();
        assertEquals(1, controlArea.getAreaBoundaryStream().filter(b -> b.getTerminal().isPresent()).count());
        assertEquals(2, controlArea.getAreaBoundaryStream().filter(b -> b.getBoundary().isPresent()).count());
        assertTrue(containsTerminal(controlArea, "044ef2e7-c766-11e1-8775-005056c00008", IdentifiableType.DANGLING_LINE));
    }

    @Test
    void smallBaseCaseTieFlowMappedToEquivalentInjection() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.smallGridBaseCaseTieFlowMappedToEquivalentInjection(), config);

        assertEquals(1, n.getAreaStream().count());

        Area controlArea = n.getAreas().iterator().next();
        assertEquals(0, controlArea.getAreaBoundaryStream().filter(b -> b.getTerminal().isPresent()).count());
        assertEquals(3, controlArea.getAreaBoundaryStream().filter(b -> b.getBoundary().isPresent()).count());
        assertTrue(containsBoundary(controlArea, "044ef2e7-c766-11e1-8775-005056c00008", IdentifiableType.DANGLING_LINE));
    }

    @Test
    void microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection(), config);

        assertEquals(1, n.getAreaStream().count());

        Area controlArea = n.getAreas().iterator().next();
        assertEquals(4, controlArea.getAreaBoundaryStream().filter(b -> b.getTerminal().isPresent()).count());
        assertEquals(1, controlArea.getAreaBoundaryStream().filter(b -> b.getBoundary().isPresent()).count());
        assertTrue(containsBoundary(controlArea, "17086487-56ba-4979-b8de-064025a6b4da", IdentifiableType.DANGLING_LINE));
    }

    @Test
    void microGridBaseCaseBEWithTieFlowMappedToSwitch() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlowMappedToSwitch(), config);

        assertEquals(1, n.getAreaStream().count());

        Area controlArea = n.getAreas().iterator().next();
        assertEquals(5, controlArea.getAreaBoundaryStream().filter(b -> b.getTerminal().isPresent()).count());
        assertEquals(0, controlArea.getAreaBoundaryStream().filter(b -> b.getBoundary().isPresent()).count());
        assertTrue(containsTerminal(controlArea, "17086487-56ba-4979-b8de-064025a6b4da", IdentifiableType.DANGLING_LINE));
    }

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config) {
        config.setConvertSvInjections(true);
        return ConversionUtil.networkModel(testGridModel, config);
    }

    private static boolean containsTerminal(Area controlArea, String connectableId, IdentifiableType identifiableType) {
        boolean ok = controlArea.getAreaBoundaryStream().filter(b -> b.getTerminal().isPresent()).anyMatch(t -> isConnectableOk(connectableId, identifiableType,
            t.getTerminal().get().getConnectable().getId(), t.getTerminal().get().getConnectable().getType()));
        if (!ok) {
            LOG.info("Terminal to find connectableId {} identifiableType {}", connectableId, identifiableType);
            controlArea.getAreaBoundaryStream().filter(b -> b.getTerminal().isPresent()).forEach(b -> LOG.info("Terminal inside cgmesControlArea connectableId {} identifiableType {}",
                    b.getTerminal().get().getConnectable().getId(), b.getTerminal().get().getConnectable().getType()));
        }
        return ok;
    }

    private static boolean containsBoundary(Area controlArea, String connectableId, IdentifiableType identifiableType) {
        boolean ok = controlArea.getAreaBoundaryStream()
                .filter(b -> b.getBoundary().isPresent())
                .anyMatch(b -> isConnectableOk(connectableId, identifiableType,
                    b.getBoundary().get().getDanglingLine().getId(), b.getBoundary().get().getDanglingLine().getType()));
        if (!ok) {
            LOG.info("Boundary to find connectableId {} identifiableType {}", connectableId, identifiableType);
            controlArea.getAreaBoundaryStream()
                    .filter(b -> b.getBoundary().isPresent())
                    .forEach(b -> LOG.info("Boundary inside cgmesControlArea danglingLineId {}}",
                        b.getBoundary().get().getDanglingLine().getId()));
        }
        return ok;
    }

    private static boolean isConnectableOk(String refConnectableId, IdentifiableType refIdentifiableType, String connectableId, IdentifiableType identifiableType) {
        return refConnectableId.equals(connectableId) && refIdentifiableType.equals(identifiableType);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TieFlowConversionTest.class);
}
