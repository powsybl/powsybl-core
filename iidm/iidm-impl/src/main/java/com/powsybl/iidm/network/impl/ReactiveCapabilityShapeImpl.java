/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityShape;
import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;
import com.powsybl.iidm.network.ReactiveLimitsKind;

import java.util.Collection;

/**
 * Implementation of ReactiveCapabilityShape interface
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public final class ReactiveCapabilityShapeImpl extends AbstractPropertiesHolder implements ReactiveCapabilityShape {

    /**
     * The convex polyhedron
     */
    private final ReactiveCapabilityShapePolyhedron polyhedron;

    /**
     * @return the kind of reactive limit (SHAPE)
     */
    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.SHAPE;
    }

    /**
     * Return the minimum reactive power q for a specific active power p and a specific voltage u
     * @param p the active power in MW
     * @param u the voltage in kV
     * @return the minimum reactive power q for a specific active power p and a specific voltage u
     */
    @Override
    public double getMinQ(final double p, final double u) {
        return polyhedron.getMinQ(p, u);
    }

    /**
     * Return the maximum reactive power q for a specific active power p and a specific voltage u
     * @param p the active power in MW
     * @param u the voltage in kV
     * @return the maximum reactive power q for a specific active power p and a specific voltage u
     */
    @Override
    public double getMaxQ(final double p, final double u) {
        return polyhedron.getMaxQ(p, u);
    }

    /**
     * Return the minimum reactive power q for a specific active power p
     * @param p the active power
     * @return the minimum reactive power q for a specific active power p
     */
    @Override
    public double getMinQ(final double p) {
        return polyhedron.getMinQ(p);
    }

    /**
     * Return the maximum reactive power q for a specific active power p
     * @param p the active power
     * @return the maximum reactive power q for a specific active power p
     */
    @Override
    public double getMaxQ(final double p) {
        return polyhedron.getMaxQ(p);
    }

    /**
     * Constructor
     * @param polyhedron the reactive capability shape polyhedron
     */
    private ReactiveCapabilityShapeImpl(final ReactiveCapabilityShapePolyhedron polyhedron) {
        this.polyhedron = polyhedron;
    }

    /**
     * Builder
     * @param polyhedron the reactive capability shape polyhedron
     */
    public static ReactiveCapabilityShapeImpl build(final ReactiveCapabilityShapePolyhedron polyhedron) {
        return new ReactiveCapabilityShapeImpl(polyhedron);
    }

    @Override
    public boolean isInside(double p, double q, double u) {
        return this.polyhedron.isInside(p, q, u);
    }

    @Override
    public Collection<ReactiveCapabilityShapePlane> getPlanes() {
        return polyhedron.getPlanes();
    }

}
