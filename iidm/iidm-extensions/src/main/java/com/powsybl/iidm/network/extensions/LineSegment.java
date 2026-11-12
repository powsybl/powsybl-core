/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;

/**
 * Represents a line segment defined by its start and end positions.
 * The positions must satisfy the following conditions:
 * - The start and end positions must be valid double values.
 * - The start position must be greater than or equal to 0.
 * - The end position must be less than or equal to 1.
 * - The start position must be less than or equal to the end position.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public record LineSegment(double start, double end) {

    public static final LineSegment FULL_LINE = new LineSegment(0, 1);

    /**
     * Constructs a LineSegment using the specified start and end positions.
     * The line segment is valid only if the following conditions are met:
     * - The start and end positions are valid double values.
     * - The start position is greater than or equal to 0.
     * - The end position is less than or equal to 1.
     * - The start position is less than or equal to the end position.
     */
    public LineSegment {
        if (Double.isNaN(start) || Double.isNaN(end) || start < 0 || end > 1 || start > end) {
            throw new PowsyblException("Invalid line segment: start: " + start + ", end: " + end + ".");
        }
    }
}
