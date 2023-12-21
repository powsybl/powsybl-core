/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix.serializer;

import com.powsybl.math.matrix.SparseMatrix;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Sparse matrix serializer implementation that uses the Matlab format.
 * .
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public class SparseMatrixMatSerializer extends AbstractSparseMatrixSerializer {

    private static final String FORMAT_ID = "MAT";

    @Override
    public String getFormat() {
        return FORMAT_ID;
    }

    @Override
    public String getComment() {
        return "MATLAB binary format";
    }

    @Override
    public void save(SparseMatrix matrix, OutputStream outputStream) {
        SparseMatrixMatUtils.save(matrix, outputStream);
    }

    @Override
    public SparseMatrix load(InputStream inputStream) {
        return SparseMatrixMatUtils.load(inputStream);
    }
}
