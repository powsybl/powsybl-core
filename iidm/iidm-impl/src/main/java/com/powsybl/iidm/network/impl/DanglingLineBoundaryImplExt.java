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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DanglingLineBoundaryImplExt extends DanglingLineBoundaryImpl {

    protected final List<Dependent<Boundary>> dependents = new ArrayList<>();

    public DanglingLineBoundaryImplExt(DanglingLine parent) {
        super(parent);
    }

    public List<Dependent<Boundary>> getDependents() {
        return dependents;
    }

    public void notifyDependentOfRemoval() {
        for (Dependent<Boundary> dependent : dependents) {
            dependent.onReferencedRemoval(this);
        }
    }
}
