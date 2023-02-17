/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.contingency.list.IdentifierContingencyList;
import com.powsybl.contingency.contingency.list.identifier.*;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifierList;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
class NetworkElementIdentifierContingencyListTest {

    @Test
    void testSimpleIdentifier() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        List<NetworkElementIdentifier> networkElementIdentifierList = new ArrayList<>();
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("LINE_S2S3"));
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("LINE_S3S4"));
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("LINE_S4S1"));
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("test"));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", "LINE", networkElementIdentifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));
    }

    @Test
    void testUcteIdentifier() {
        Network network = EurostagTutorialExample1Factory.create();
        List<NetworkElementIdentifier> networkElementIdentifierList = new ArrayList<>();
        networkElementIdentifierList.add(new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '1'));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", "LINE", networkElementIdentifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("NHV1_NHV2_1", new LineContingency("NHV1_NHV2_1")), contingencies.get(0));
    }

    @Test
    void testIdentifierList() {
        Network network = EurostagTutorialExample1Factory.create();
        List<NetworkElementIdentifier> networkElementIdentifierList = new ArrayList<>();
        List<NetworkElementIdentifier> networkElementIdentifierListElements = new ArrayList<>();
        networkElementIdentifierListElements.add(new IdBasedNetworkElementIdentifier("test"));
        networkElementIdentifierListElements.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2"));
        networkElementIdentifierListElements.add(new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '2'));
        networkElementIdentifierListElements.add(new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '1'));
        networkElementIdentifierList.add(new NetworkElementIdentifierList(networkElementIdentifierListElements));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", "LINE", networkElementIdentifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("NHV1_NHV2_2", new LineContingency("NHV1_NHV2_2")), contingencies.get(0));
    }
}
