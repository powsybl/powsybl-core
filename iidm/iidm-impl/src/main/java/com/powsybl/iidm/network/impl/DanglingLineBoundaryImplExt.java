/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.util.DanglingLineBoundaryImpl;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DanglingLineBoundaryImplExt extends DanglingLineBoundaryImpl {

    protected final DependentContainer<Boundary> dependentContainer = new DependentContainer<>(this);

    public DanglingLineBoundaryImplExt(DanglingLine parent) {
        super(parent);
    }

    public DependentContainer<Boundary> getDependentContainer() {
        return dependentContainer;
    }

    public void registerDependent(Dependent<Boundary> dependent) {
        dependentContainer.addDependent(dependent);
    }

    public void unregisterDependent(Dependent<Boundary> dependent) {
        dependentContainer.removeDependent(dependent);
    }
}
