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
 * - The start position must be greater than or equal to 0.
 * - The end position must be less than or equal to 1.
 * - The start position must be less than or equal to the end position.
 * If these conditions are not met, an exception of type {@code PowsyblException} is thrown.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public record LineSegment(double start, double end) {

    /**
     * Constructs a {@code LineSegment} using the specified start and end positions.
     * The line segment is valid only if the following conditions are met:
     * - The start position is greater than or equal to 0.
     * - The end position is less than or equal to 1.
     * - The start position is less than or equal to the end position.
     * If any of these conditions are not satisfied, a {@code PowsyblException} is thrown.
     *
     * @param start The start position of the line segment.
     * @param end The end position of the line segment.
     * @throws PowsyblException If the start and end positions do not satisfy the required constraints.
     */
    public LineSegment {
        if (start < 0 || end > 1 || start > end) {
            throw new PowsyblException("Invalid line segment: start: " + start + ", end: " + end + ".");
        }
    }
}
