/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

/**
 * LU decomposition utility class. As some decomposition implementation may allocate resources that need to be released
 * a try-with-resource block has to be used to ensure correct resource management.
 * <pre>
 * try (LUDecomposition decomposition = m.decomposeLU()) {
 *     decomposition.solve(...)
 * }
 * </pre>
 *
 * @see <a href="https://en.wikipedia.org/wiki/LU_decomposition">https://en.wikipedia.org/wiki/LU_decomposition</a>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LUDecomposition extends AutoCloseable {

    /**
     * Solve A * x = b where b is a column vector.
     *
     * @param b a column vector
     */
    void solve(double[] b);

    /**
     * Solve A * x = b where b is a dense matrix.
     *
     * @param b a matrix
     */
    void solve(DenseMatrix b);

    /**
     * {@inheritDoc}
     */
    @Override
    void close();
}
