/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.simulator.loadflow;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.contingency.Contingency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RunningContext {

    private int round = 0;

    private final Network network;

    private final Contingency contingency;

    private final TimeLine timeLine;

    private final Map<String, AtomicInteger> rulesMatchCount = new HashMap<>();

    public RunningContext(Network network, Contingency contingency) {
        this.network = network;
        this.contingency = contingency;
        timeLine = new TimeLine(contingency);
    }

    public RunningContext(Network network) {
        this(network, null);
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Network getNetwork() {
        return network;
    }

    public Contingency getContingency() {
        return contingency;
    }

    public TimeLine getTimeLine() {
        return timeLine;
    }

    private AtomicInteger getRuleMatchCountInternal(String ruleId) {
        AtomicInteger count = rulesMatchCount.get(ruleId);
        if (count == null) {
            count = new AtomicInteger();
            rulesMatchCount.put(ruleId, count);
        }
        return count;
    }

    public int getRuleMatchCount(String ruleId) {
        return getRuleMatchCountInternal(ruleId).get();
    }

    public void incrementRuleMatchCount(String ruleId) {
        getRuleMatchCountInternal(ruleId).incrementAndGet();
    }
}
