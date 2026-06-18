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
     * Gets the second line.
     * @return the second line
     */
    Line getLine2();

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
     * Gets the starting and ending position of the mutual coupling on the first line.
     * The positions are a proportion of the line length and are between 0 and 1.
     * @return the starting position
     */
    LineSegment getLine1Segment();

    /**
     * Gets the starting and ending position of the mutual coupling on the second line.
     * The positions are a proportion of the line length and are between 0 and 1.
     * @return the starting position
     */
    LineSegment getLine2Segment();

    /**
     * Sets the starting and ending position of the mutual coupling on the second line.
     * The positions are a proportion of the line length and are between 0 and 1.
     * @param line1Segment the segment on line 1
     */
    void setLine1Segment(LineSegment line1Segment);

    /**
     * Sets the starting and ending position of the mutual coupling on the second line.
     * The positions are a proportion of the line length and are between 0 and 1.
     * @param line2Segment the segment on line 2
     */
    void setLine2Segment(LineSegment line2Segment);

}
