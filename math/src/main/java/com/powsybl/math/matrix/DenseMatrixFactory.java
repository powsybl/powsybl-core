/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

/**
 * Dense matrix factory.
 *
 * @see DenseMatrix
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DenseMatrixFactory implements MatrixFactory {

    @Override
    public DenseMatrix create(int rowCount, int columnCount, int estimatedValueCount) {
        return new DenseMatrix(rowCount, columnCount);
    }
}
