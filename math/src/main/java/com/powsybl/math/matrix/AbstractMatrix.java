/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.matrix;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractMatrix implements Matrix {

    protected abstract int getEstimatedNonZeroValueCount();

    @Override
    public Matrix copy(MatrixFactory factory) {
        Objects.requireNonNull(factory);
        Matrix matrix = factory.create(getM(), getN(), getEstimatedNonZeroValueCount());
        iterateNonZeroValue(matrix::setValue);
        return matrix;
    }
}
