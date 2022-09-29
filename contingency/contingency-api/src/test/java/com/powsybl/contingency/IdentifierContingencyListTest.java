/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.contingency.list.identifiant.Identifier;
import com.powsybl.contingency.contingency.list.identifiant.IdentifierList;
import com.powsybl.contingency.contingency.list.identifiant.SimpleIdentifier;
import com.powsybl.contingency.contingency.list.identifiant.UcteIdentifier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierContingencyListTest {

    @Test
    public void testSimpleIdentifier() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        List<Identifier> identifierList = new ArrayList<>();
        identifierList.add(new SimpleIdentifier("LINE_S2S3"));
        identifierList.add(new SimpleIdentifier("LINE_S3S4"));
        identifierList.add(new SimpleIdentifier("LINE_S4S1"));
        identifierList.add(new SimpleIdentifier("test"));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", "LINE", identifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));
    }

    @Test
    public void testUcteIdentifier() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Identifier> identifierList = new ArrayList<>();
        identifierList.add(new UcteIdentifier("VLHV1", "VLHV2", 1));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", "LINE", identifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("NHV1_NHV2_1", new LineContingency("NHV1_NHV2_1")), contingencies.get(0));
    }

    @Test
    public void testIdentifierList() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Identifier> identifierList = new ArrayList<>();
        List<Identifier> identifierListElements = new ArrayList<>();
        identifierListElements.add(new SimpleIdentifier("test"));
        identifierListElements.add(new SimpleIdentifier("NHV1_NHV2"));
        identifierListElements.add(new UcteIdentifier("VLHV1", "VLHV2", 2));
        identifierListElements.add(new UcteIdentifier("VLHV1", "VLHV2", 1));
        identifierList.add(new IdentifierList(identifierListElements));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", "LINE", identifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("NHV1_NHV2_2", new LineContingency("NHV1_NHV2_2")), contingencies.get(0));
    }
}
