/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.security.action.Action;
import com.powsybl.security.action.ActionList;
import com.powsybl.security.action.MultipleActionsAction;
import com.powsybl.security.action.SwitchAction;
import com.powsybl.security.action.GenerationRedispatchAction;
import com.powsybl.security.condition.TrueCondition;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class JsonActionAndOperatorStrategyTest extends AbstractConverterTest {
    @Test
    public void actionRoundTrip() throws IOException {
        List<Action> actions = new ArrayList<>();
        actions.add(new SwitchAction("id1", "switchId1", true));
        actions.add(new GenerationRedispatchAction("id2", "generatorId1", true, 100.0));
        actions.add(new MultipleActionsAction("id3", Collections.singletonList(new SwitchAction("id4", "switchId2", true))));
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
}
