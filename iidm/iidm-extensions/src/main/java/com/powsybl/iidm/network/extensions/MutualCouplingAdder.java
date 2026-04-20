/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.Line;

/**
 * This interface is a builder to add a mutual coupling between two lines.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface MutualCouplingAdder {

    /**
     * Sets the first line.
     * @param line1 the first line
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withLine1(Line line1);

    /**
     * Sets the second line.
     * @param line2 the second line
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withLine2(Line line2);

    /**
     * Sets the mutual coupling resistance.
     * @param r the resistance in ohms
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withR(double r);

    /**
     * Sets the mutual coupling reactance.
     * @param x the reactance in ohms
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withX(double x);

    /**
     * Sets the starting position of the mutual coupling on the first line.
     * The position is a proportion of the line length and is between 0 and 1.
     * @param start the starting position
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withLine1Start(double start);

    /**
     * Sets the ending position of the mutual coupling on the first line.
     * The position is a proportion of the line length and is between 0 and 1.
     * @param end the ending position
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withLine1End(double end);

    /**
     * Sets the starting position of the mutual coupling on the second line.
     * The position is a proportion of the line length and is between 0 and 1.
     * @param start the starting position
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withLine2Start(double start);

    /**
     * Sets the ending position of the mutual coupling on the second line.
     * The position is a proportion of the line length and is between 0 and 1.
     * @param end the ending position
     * @return the current mutual coupling adder
     */
    MutualCouplingAdder withLine2End(double end);

    /**
     * Adds the mutual coupling.
     * @return the added mutual coupling
     */
    MutualCoupling add();
}
