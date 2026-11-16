/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix.serializer;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.math.matrix.MatrixFactory;
import com.powsybl.math.matrix.SparseMatrix;
import com.powsybl.math.matrix.SparseMatrixFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi at soft.it>}
 */
class SparseMatrixSerializeTest {

    private final MatrixFactory matrixFactory = new SparseMatrixFactory();

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    private SparseMatrix createRandomSparseMatrix(int numRows, int numCols, double density) {
        Random random = new Random();
        SparseMatrix matrix = (SparseMatrix) matrixFactory.create(numRows, numCols, 0);
        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                //Add a random value to the matrix with probability based on density
                if (random.nextDouble() < density) {
                    matrix.set(row, col, random.nextDouble());
                }
            }
        }
        return matrix;
    }

    private SparseMatrix createSimpleSparseMatrix() {
        SparseMatrix matrix = (SparseMatrix) matrixFactory.create(2, 5, 4);
        matrix.set(0, 0, 1d);
        matrix.set(1, 0, 2d);
        matrix.set(0, 2, 3d);
        matrix.set(1, 4, 4d);
        return matrix;
    }

    @Test
    void testSerializeToFile() {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("/work/sparse-matrix-test.bin");
        matrix.write(file);
        SparseMatrix m1 = SparseMatrix.read(file);
        assertEquals(matrix, m1);
    }

    @Test
    void testSerializeToFileExceptions() throws IOException {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("");
        assertThrows(UncheckedIOException.class, () -> matrix.write(file));
        assertThrows(UncheckedIOException.class, () -> SparseMatrix.read(file));
        InputStream testInputStream = new ByteArrayInputStream(new byte[0]);
        assertThrows(UncheckedIOException.class, () -> SparseMatrix.read(testInputStream));
        OutputStream mockOutputStream = Mockito.mock(OutputStream.class);
        Mockito.doThrow(new IOException()).when(mockOutputStream).close();
        assertThrows(UncheckedIOException.class, () -> matrix.write(mockOutputStream));
    }

    @Test
    void testSerializeLargerSparseMatrixToFile() {
        //create a 100x100 sparse matrix with 20% (circa) non zero values
        SparseMatrix matrix = createRandomSparseMatrix(100, 100, 0.2);
        Path file = fileSystem.getPath("/work/sparse-large-matrix-test.bin");
        matrix.write(file);
        SparseMatrix m1 = SparseMatrix.read(file);
        assertEquals(matrix, m1);
    }

    @Test
    void testSerializeSparseMatrixToMatlabFormat() {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("/work/sparse-matrix-test.mat");
        SparseMatrixMatSerializer.exportMat(matrix, file);
        SparseMatrix m1 = SparseMatrixMatSerializer.importMat(file);
        assertEquals(matrix, m1);
    }

    @Test
    void testSerializeToMatlabFormatExceptions() throws IOException {
        SparseMatrix matrix = createSimpleSparseMatrix();
        Path file = fileSystem.getPath("");
        assertThrows(UncheckedIOException.class, () -> SparseMatrixMatSerializer.exportMat(matrix, file));
        assertThrows(UncheckedIOException.class, () -> SparseMatrixMatSerializer.importMat(file));
    }

    @Test
    void testSerializeLargerSparseMatrixToMatlabFormat() {
        //create a 100x100 sparse matrix with 30% (circa) non zero values
        SparseMatrix matrix = createRandomSparseMatrix(100, 100, 0.3);
        Path file = fileSystem.getPath("/work/sparse-large-matrix-test.mat");
        SparseMatrixMatSerializer.exportMat(matrix, file);
        SparseMatrix m1 = SparseMatrixMatSerializer.importMat(file);
        assertEquals(matrix, m1);
    }
}
