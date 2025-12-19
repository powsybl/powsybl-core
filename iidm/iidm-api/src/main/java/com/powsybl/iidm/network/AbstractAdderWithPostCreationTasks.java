/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>Abstract class for adders that should handle post-creation tasks.</p>
 * <p>Post-creation tasks are automatically performed on the object that the adder creates, after its creation.</p>
 *
 * @param <T> the type of the object that is created by the adder
 * @param <R> the type of the object that will be returned by the <code>add()</code> method
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractAdderWithPostCreationTasks<T, R> implements AdderWithPostCreationTasks<T> {
    List<Consumer<T>> postCreationTasks = new ArrayList<>();

    protected abstract T prepareObjectToAdd();

    protected abstract R addObject(T obj);

    public void addPostCreationTask(Consumer<T> postCreationTask) {
        postCreationTasks.add(postCreationTask);
    }

    public final R add() {
        T objToAdd = prepareObjectToAdd();
        postCreationTasks.forEach(task -> task.accept(objToAdd));
        postCreationTasks.clear();
        return addObject(objToAdd);
    }
}
