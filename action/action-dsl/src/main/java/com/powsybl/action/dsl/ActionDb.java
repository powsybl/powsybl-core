/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.contingency.Contingency;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ActionDb {

    private final Map<String, Contingency> contingencies = new LinkedHashMap<>();

    private final Map<String, Rule> rules = new LinkedHashMap<>();

    private final Map<String, Action> actions = new LinkedHashMap<>();

    public void addContingency(Contingency contingency) {
        Objects.requireNonNull(contingency);
        contingencies.put(contingency.getId(), contingency);
    }

    public Collection<Contingency> getContingencies() {
        return contingencies.values();
    }

    public Contingency getContingency(String id) {
        Objects.requireNonNull(id);
        Contingency contingency = contingencies.get(id);
        if (contingency == null) {
            throw new ActionDslException("Contingency '" + id + "' not found");
        }
        return contingency;
    }

    public void addRule(Rule rule) {
        Objects.requireNonNull(rule);
        String id = rule.getId();
        if (rules.containsKey(id)) {
            throw new ActionDslException("Rule '" + id + "' is defined several times");
        }
        rules.put(id, rule);
    }

    public Collection<Rule> getRules() {
        return rules.values();
    }

    public void addAction(Action action) {
        Objects.requireNonNull(action);
        String id = action.getId();
        if (actions.containsKey(id)) {
            throw new ActionDslException("Action '" + id + "' is defined several times");
        }
        actions.put(id, action);
    }

    public Action getAction(String id) {
        Objects.requireNonNull(id);
        Action action = actions.get(id);
        if (action == null) {
            throw new ActionDslException("Action '" + id + "' not found");
        }
        return action;
    }

    void checkUndefinedActions(Set<String> actionIds) {
        Objects.requireNonNull(actionIds);

        String strActionIds = actionIds.stream()
                .filter(id -> !actions.containsKey(id))
                .collect(Collectors.joining(", "));
        if (!strActionIds.isEmpty()) {
            throw new ActionDslException("Actions [" + strActionIds + "] not found");
        }
    }
}
