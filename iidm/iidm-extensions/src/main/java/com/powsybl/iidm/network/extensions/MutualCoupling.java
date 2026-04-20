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
 * This extension models electrical coupling between pairs of lines.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface MutualCoupling {

    /**
     * Gets the first line.
     * @return the first line
     */
    Line getLine1();

    /**
     * Sets the first line.
     * @param line1 the first line
     */
    void setLine1(Line line1);

    /**
     * Gets the second line.
     * @return the second line
     */
    Line getLine2();

    /**
     * Sets the second line.
     * @param line2 the second line
     */
    void setLine2(Line line2);

    /**
     * Gets the mutual coupling resistance.
     * @return the resistance in ohms
     */
    double getR();

    /**
     * Sets the mutual coupling resistance.
     * @param r the resistance in ohms
     */
    void setR(double r);

    /**
     * Gets the mutual coupling reactance.
     * @return the reactance in ohms
     */
    double getX();

    /**
     * Sets the mutual coupling reactance.
     * @param x the reactance in ohms
     */
    void setX(double x);

    /**
     * Gets the starting position of the mutual coupling on the first line.
     * @return the starting position
     */
    double getLine1Start();

    /**
     * Sets the starting position of the mutual coupling on the first line.
     * @param line1Start the starting position
     */
    void setLine1Start(double line1Start);

    /**
     * Gets the starting position of the mutual coupling on the second line.
     * @return the starting position
     */
    double getLine2Start();

    /**
     * Sets the starting position of the mutual coupling on the second line.
     * @param line2Start the starting position
     */
    void setLine2Start(double line2Start);

    /**
     * Gets the ending position of the mutual coupling on the first line.
     * @return the ending position
     */
    double getLine1End();

    /**
     * Sets the ending position of the mutual coupling on the first line.
     * @param line1End the ending position
     */
    void setLine1End(double line1End);

    /**
     * Gets the ending position of the mutual coupling on the second line.
     * @return the ending position
     */
    double getLine2End();

    /**
     * Sets the ending position of the mutual coupling on the second line.
     * @param line2End the ending position
     */
    void setLine2End(double line2End);

}
