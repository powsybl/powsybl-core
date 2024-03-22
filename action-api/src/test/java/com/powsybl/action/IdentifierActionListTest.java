/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.VoltageLevelAndOrderNetworkElementIdentifier;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class IdentifierActionListTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Map<ActionBuilder<?>, NetworkElementIdentifier> elementIdentifierMap = new HashMap<>();
        elementIdentifierMap.put(new TerminalsConnectionActionBuilder().withId("lineConnectionAction").withOpen(true)
                .withSide(ThreeSides.ONE),
            new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '1'));
        IdentifierActionList identifierActionList = new IdentifierActionList(Collections.emptyList(), elementIdentifierMap);
        List<Action> actionsCreated = identifierActionList.getActions(network);
        assertEquals(1, actionsCreated.size());
        assertEquals("lineConnectionAction", actionsCreated.get(0).getId());
        assertEquals("NHV1_NHV2_1", ((TerminalsConnectionAction) actionsCreated.get(0)).getElementId());
    }
}
