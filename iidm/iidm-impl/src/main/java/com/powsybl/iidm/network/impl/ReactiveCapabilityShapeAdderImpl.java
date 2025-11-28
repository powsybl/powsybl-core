/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;

/**
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at rte-france.com>}

 */
class ReactiveCapabilityShapeAdderImpl<O extends ReactiveLimitsOwner & Validable> implements ReactiveCapabilityShapeAdder {

    private final O owner;

    ReactiveCapabilityShapePolyhedron polyhedron;

    /**
     * Constructor
     * @param owner the ReactiveLimitsOwner
     */
    ReactiveCapabilityShapeAdderImpl(O owner) {
        this.owner = owner;
        polyhedron = ReactiveCapabilityShapePolyhedron.build(new ArrayList<>());
    }

    @Override
    public ReactiveCapabilityShape add() {
        if (polyhedron.isEmpty()) {
            throw new ValidationException(owner, "a reactive capability shape should not have an empty polyhedron");
        }
        ReactiveCapabilityShapeImpl shape = ReactiveCapabilityShapeImpl.build(polyhedron);
        owner.setReactiveLimits(shape);
        return shape;
    }

    @Override
    public ReactiveCapabilityShapeAdder addPlane(double alpha, double beta, double gamma, boolean isGreaterOrEqual) {
        if (isGreaterOrEqual) {
            polyhedron.addReactiveCapabilityShapePlane(ReactiveCapabilityShapePlane.build(alpha, beta).greaterOrEqual(gamma));
        } else {
            polyhedron.addReactiveCapabilityShapePlane(ReactiveCapabilityShapePlane.build(alpha, beta).lessOrEqual(gamma));
        }
        return this;
    }

}
