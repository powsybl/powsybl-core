/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import java.util.Objects;

/**
 * Abstract class for matrix that provides an implementation for common methods.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractMatrix implements Matrix {

    /**
     * Get an estimation of non zero value count.
     *
     * @return an estimation of non zero value count
     */
    protected abstract int getEstimatedNonZeroValueCount();

    /**
     * Check that row {@code i} and column {@code j} are in matrix bounds.
     *
     * @param i row index
     * @param j column index
     */
    protected void checkBounds(int i, int j) {
        if (i < 0 || i >= getRowCount()) {
            throw new IllegalArgumentException("Row index out of bound [0, " + (getRowCount() - 1) + "]");
        }
        if (j < 0 || j >= getColumnCount()) {
            throw new IllegalArgumentException("Column index out of bound [0, " + (getColumnCount() - 1) + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix copy(MatrixFactory factory) {
        Objects.requireNonNull(factory);
        Matrix matrix = factory.create(getRowCount(), getColumnCount(), getEstimatedNonZeroValueCount());
        iterateNonZeroValue(matrix::set);
        return matrix;
    }
}
