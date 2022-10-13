/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.security.action.*;
import com.powsybl.security.condition.TrueCondition;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class JsonActionAndOperatorStrategyTest extends AbstractConverterTest {
    @Test
    public void actionRoundTrip() throws IOException {
        List<Action> actions = new ArrayList<>();
        actions.add(new SwitchAction("id1", "switchId1", true));
        actions.add(new MultipleActionsAction("id2", Collections.singletonList(new SwitchAction("id3", "switchId2", true))));
        actions.add(new LineConnectionAction("id3", "lineId3", true, true));
        actions.add(new LineConnectionAction("id4", "lineId4", false));
        actions.add(new PhaseTapChangerTapPositionAction("id5", "transformerId1", true, 5, ThreeWindingsTransformer.Side.TWO));
        actions.add(new PhaseTapChangerTapPositionAction("id6", "transformerId2", false, 12));
        actions.add(new PhaseTapChangerTapPositionAction("id7", "transformerId3", true, -5, ThreeWindingsTransformer.Side.ONE));
        actions.add(new PhaseTapChangerTapPositionAction("id8", "transformerId3", false, 2, ThreeWindingsTransformer.Side.THREE));
        actions.add(new GeneratorActionBuilder().withId("id9").withGeneratorId("generatorId1").withActivePowerRelativeValue(true).withActivePowerValue(100.0).build());
        actions.add(new GeneratorActionBuilder().withId("id10").withGeneratorId("generatorId2").withVoltageRegulatorOn(true).withTargetV(225.0).build());
        actions.add(new GeneratorActionBuilder().withId("id11").withGeneratorId("generatorId2").withVoltageRegulatorOn(false).withTargetQ(400.0).build());
        ActionList actionList = new ActionList(actions);
        roundTripTest(actionList, ActionList::writeJsonFile, ActionList::readJsonFile, "/ActionFileTest.json");
    }

    @Test
    public void operatorStrategyRoundTrip() throws IOException {
        List<OperatorStrategy> operatorStrategies = new ArrayList<>();
        operatorStrategies.add(new OperatorStrategy("id1", "contingencyId1", new TrueCondition(), Arrays.asList("actionId1", "actionId2", "actionId3")));
        operatorStrategies.add(new OperatorStrategy("id1", "contingencyId1", new TrueCondition(), Arrays.asList("actionId1", "actionId2", "actionId3")));
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(operatorStrategies);
        roundTripTest(operatorStrategyList, OperatorStrategyList::writeFile, OperatorStrategyList::readFile, "/OperatorStrategyFileTest.json");
    }

    @Test
    public void wrongActions() {
        final InputStream inputStream = getClass().getResourceAsStream("/WrongActionFileTest.json");
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: for phase tap changer tap position action relative value field can't be null\n" +
                " at [Source: (BufferedInputStream); line: 8, column: 3] (through reference chain: java.util.ArrayList[0])", assertThrows(UncheckedIOException.class, () ->
                ActionList.readJsonInputStream(inputStream)).getMessage());

        final InputStream inputStream2 = getClass().getResourceAsStream("/WrongActionFileTest2.json");
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: for phase tap changer tap position action value field can't equal zero\n" +
                " at [Source: (BufferedInputStream); line: 8, column: 3] (through reference chain: java.util.ArrayList[0])", assertThrows(UncheckedIOException.class, () ->
                ActionList.readJsonInputStream(inputStream2)).getMessage());
    }
}
