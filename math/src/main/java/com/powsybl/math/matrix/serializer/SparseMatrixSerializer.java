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
import java.nio.file.Path;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
public interface SparseMatrixSerializer {

    /**
     * Get the format of this serializer
     *
     * @return the format name of this serializer
     */
    String getFormat();

    /**
     * Get a brief description of this serializer
     *
     * @return a brief description of this serializer
     */
    String getComment();

    /**
     * Save a SparseMatrix.
     *
     * @param matrix The sparse matrix
     * @param outputStream The output stream used by the serializer
     */
    void save(SparseMatrix matrix, OutputStream outputStream);

    /**
     * Save a SparseMatrix.
     *
     * @param matrix The sparse matrix
     * @param file The output file used by the serializer
     */
    void save(SparseMatrix matrix, Path file);

    /**
     * Load a SparseMatrix.
     *
     * @param inputStream The input stream used by the serializer
     * @return The sparse matrix read from the input stream
     */
    SparseMatrix load(InputStream inputStream);

    /**
     * Load a SparseMatrix.
     *
     * @param file The input file used by the serializer
     * @return The sparse matrix read from the file
     */
    SparseMatrix load(Path file);
}
