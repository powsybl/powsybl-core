/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.identifiers.IdBasedNetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifierContingencyList;
import com.powsybl.iidm.network.identifiers.VoltageLevelAndOrderNetworkElementIdentifier;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class IdentifierActionListTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Map<ActionBuilder, NetworkElementIdentifier> elementIdentifierMap = new HashMap<>();
        elementIdentifierMap.put(new TerminalsConnectionActionBuilder().withId("lineConnectionAction").withOpen(true)
                .withSide(ThreeSides.ONE),
            new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '1'));
        IdentifierActionList identifierActionList = new IdentifierActionList(Collections.emptyList(), elementIdentifierMap);
        List<Action> actionsCreated = identifierActionList.getActions(network);
        assertEquals(1, actionsCreated.size());
        assertEquals("lineConnectionAction", actionsCreated.get(0).getId());
        assertEquals("NHV1_NHV2_1", ((TerminalsConnectionAction) actionsCreated.get(0)).getElementId());
    }

    @Test
    void testSeveralIdentifiablesFound() {
        Network network = EurostagTutorialExample1Factory.create();
        Map<ActionBuilder, NetworkElementIdentifier> elementIdentifierMap = new HashMap<>();
        List<NetworkElementIdentifier> networkElementIdentifiers = new ArrayList<>();
        networkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_1"));
        networkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_2"));
        elementIdentifierMap.put(new TerminalsConnectionActionBuilder().withId("lineConnectionAction").withOpen(true)
                .withSide(ThreeSides.ONE),
            new NetworkElementIdentifierContingencyList(networkElementIdentifiers));
        IdentifierActionList identifierActionList = new IdentifierActionList(Collections.emptyList(), elementIdentifierMap);
        String message = Assertions.assertThrows(PowsyblException.class, () -> identifierActionList.getActions(network)).getMessage();
        Assertions.assertEquals("for identifier in action builder more than one or none network element was found", message);
    }
}
