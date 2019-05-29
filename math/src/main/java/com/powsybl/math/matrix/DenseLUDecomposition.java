/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import java.util.Objects;

/**
 * Dense matrix LU decomposition based on Jama library.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DenseLUDecomposition implements LUDecomposition {

    private final DenseMatrix matrix;

    private Jama.LUDecomposition decomposition;

    DenseLUDecomposition(DenseMatrix matrix) {
        this.matrix = Objects.requireNonNull(matrix);
        decomposition = matrix.toJamaMatrix().lu();
    }

    @Override
    public void redecompose() {
        decomposition = matrix.toJamaMatrix().lu();
    }

    @Override
    public void solve(double[] b) {
        Jama.Matrix x = decomposition.solve(new Jama.Matrix(b, b.length));
        System.arraycopy(x.getColumnPackedCopy(), 0, b, 0, b.length);
    }

    @Override
    public void solve(DenseMatrix b) {
        Jama.Matrix x = decomposition.solve(b.toJamaMatrix());
        b.setValues(x.getColumnPackedCopy());
    }

    @Override
    public void close() {
        // nothing to close
    }
}
