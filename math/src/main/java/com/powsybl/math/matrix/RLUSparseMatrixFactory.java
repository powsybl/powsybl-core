/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sparse matrix factory that routes LU decomposition to the RTE RLU native implementation
 * (powsybl-rte-math-native) instead of the default KLU implementation.
 *
 * <p>Drop-in replacement for {@link SparseMatrixFactory}: any code that calls
 * {@link Matrix#decomposeLU()} on the produced matrices will transparently use
 * {@link SparseRLUDecomposition} backed by the {@code rtemath} native library.</p>
 *
 * @see SparseRLUDecomposition
 * @see SparseMatrixFactory
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class RLUSparseMatrixFactory implements MatrixFactory {

    private final double rgrowthThreshold;

    public RLUSparseMatrixFactory() {
        this(SparseRLUDecomposition.DEFAULT_RGROWTH_THRESHOLD);
    }

    public RLUSparseMatrixFactory(double rgrowthThreshold) {
        this.rgrowthThreshold = rgrowthThreshold;
    }

    private static final class RLUSparseMatrix extends SparseMatrix {

        private static final Logger LOGGER = LoggerFactory.getLogger(RLUSparseMatrixFactory.class);

        RLUSparseMatrix(int rowCount, int columnCount, int estimatedValueCount) {
            super(rowCount, columnCount, estimatedValueCount);
        }

        @Override
        public LUDecomposition decomposeLU() {
            LOGGER.debug("RLU decomposition");
            return decomposeRLU();
        }
    }

    @Override
    public SparseMatrix create(int rowCount, int columnCount, int estimatedValueCount) {
        RLUSparseMatrix m = new RLUSparseMatrix(rowCount, columnCount, estimatedValueCount);
        m.setRgrowthThreshold(rgrowthThreshold);
        return m;
    }
}
