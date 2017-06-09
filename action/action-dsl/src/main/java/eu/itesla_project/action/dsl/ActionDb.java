/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.dsl;

import eu.itesla_project.contingency.Contingency;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ActionDb {

    private final Map<String, Contingency> contingencies = new HashMap<>();

    private final Map<String, Rule> rules = new HashMap<>();

    private final Map<String, Action> actions = new HashMap<>();

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
            throw new RuntimeException("Contingency '" + id + "' not found");
        }
        return contingency;
    }

    public void addRule(Rule rule) {
        Objects.requireNonNull(rule);
        rules.put(rule.getId(), rule);
    }

    public Collection<Rule> getRules() {
        return rules.values();
    }

    public void addAction(Action action) {
        Objects.requireNonNull(action);
        actions.put(action.getId(), action);
    }

    public Action getAction(String id) {
        Objects.requireNonNull(id);
        Action action = actions.get(id);
        if (action == null) {
            throw new RuntimeException("Action '" + id + "' not found");
        }
        return action;
    }
}
