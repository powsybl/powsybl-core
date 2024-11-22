/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DependentContainer<T> {

    private final T referenced;

    private final List<Dependent<T>> dependents = new ArrayList<>();

    public DependentContainer(T referenced) {
        this.referenced = Objects.requireNonNull(referenced);
    }

    public void registerDependent(Dependent<T> dependent) {
        dependents.add(Objects.requireNonNull(dependent));
    }

    public void unregisterDependent(Dependent<T> dependent) {
        dependents.remove(Objects.requireNonNull(dependent));
    }

    public void notifyDependentOfRemoval() {
        for (Dependent<T> dependent : dependents) {
            dependent.onReferencedRemoval(referenced);
        }
    }
}
