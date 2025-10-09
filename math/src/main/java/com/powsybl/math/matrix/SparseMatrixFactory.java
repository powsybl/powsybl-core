/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

/**
 * Sparse matrix factory.
 *
 * @see SparseMatrix
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SparseMatrixFactory implements MatrixFactory {

    private final double rgrowthThreshold;

    public SparseMatrixFactory() {
        this(SparseLUDecomposition.DEFAULT_RGROWTH_THRESHOLD);
    }

    public SparseMatrixFactory(double rgrowthThreshold) {
        this.rgrowthThreshold = rgrowthThreshold;
    }

    @Override
    public SparseMatrix create(int rowCount, int columnCount, int estimatedValueCount) {
        SparseMatrix m = new SparseMatrix(rowCount, columnCount, estimatedValueCount);
        m.setRgrowthThreshold(rgrowthThreshold);
        return m;
    }
}
