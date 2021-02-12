/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface Boundary {

    interface BoundaryTerminal extends Terminal {

        @Override
        default Terminal.NodeBreakerView getNodeBreakerView() {
            throw new PowsyblException("Not supported for boundary terminals");
        }

        @Override
        default Terminal.BusBreakerView getBusBreakerView() {
            throw new PowsyblException("Not supported for boundary terminals");
        }

        @Override
        default Terminal.BusView getBusView() {
            throw new PowsyblException("Not supported for boundary terminals");
        }

        @Override
        default BoundaryTerminal setP(double p) {
            throw new PowsyblException("Not supported for boundary terminals");
        }

        @Override
        default BoundaryTerminal setQ(double q) {
            throw new PowsyblException("Not supported for boundary terminals");
        }

        @Override
        default boolean connect() {
            throw new PowsyblException("Not supported for boundary terminals");
        }

        @Override
        default boolean disconnect() {
            throw new PowsyblException("Not supported for boundary terminals");
        }

        @Override
        default void traverse(VoltageLevel.TopologyTraverser traverser) {
            throw new PowsyblException("Not supported for boundary terminals");
        }
    }

    double getV();

    double getAngle();

    double getP();

    double getQ();

    BoundaryTerminal getTerminal();
}
