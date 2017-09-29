/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SparseMatrixFactory implements MatrixFactory {

    @Override
    public SparseMatrix create(int m, int n, int estimatedNonZeroValueCount) {
        return new SparseMatrix(m, n, estimatedNonZeroValueCount);
    }
}
