/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

/**
 * Interface for matrix factory.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface MatrixFactory {

    /**
     * Create a matrix with m rows and n column. An estimation of the number of non zero values
     * may be provided for internal capacity reservation.
     *
     * @param rowCount row count
     * @param columnCount column count
     * @param estimatedValueCount estimated number of values
     *
     * @return a matrix with {@code rowCount} rows and {@code columnCount} columns
     */
    Matrix create(int rowCount, int columnCount, int estimatedValueCount);
}
