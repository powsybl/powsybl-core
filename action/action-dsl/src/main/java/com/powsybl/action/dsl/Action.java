/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Action {

    private final String id;

    private String description;

    private final List<ModificationTask> tasks;

    public Action(String id) {
        this(id, new ArrayList<>());
    }

    public Action(String id, List<ModificationTask> tasks) {
        this.id = Objects.requireNonNull(id);
        this.tasks = Objects.requireNonNull(tasks);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ModificationTask> getTasks() {
        return tasks;
    }

    public void run(Network network, ComputationManager computationManager) {
        for (ModificationTask task : tasks) {
            task.modify(network, computationManager);
        }
    }
}
